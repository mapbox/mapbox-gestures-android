package com.mapbox.android.gestures

import GesturesUiTestUtils.DEFAULT_GESTURE_DURATION
import GesturesUiTestUtils.move
import GesturesUiTestUtils.pinch
import GesturesUiTestUtils.quickScale
import android.os.Build
import android.os.Handler
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.mapbox.android.gestures.testapp.R
import com.mapbox.android.gestures.testapp.TestActivity
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class ScaleGestureDetectorTest {

  @Rule
  @JvmField
  val activityTestRule = ActivityTestRule(TestActivity::class.java)

  private lateinit var gesturesManager: AndroidGesturesManager

  @Before
  fun setup() {
    gesturesManager = activityTestRule.activity.gesturesManager
  }

  @Test
  fun onScaleBegin_doNotStart_false() {
    gesturesManager.setStandardScaleGestureListener(object : StandardScaleGestureDetector.StandardOnScaleGestureListener {
      override fun onScaleBegin(detector: StandardScaleGestureDetector): Boolean {
        return false
      }

      override fun onScale(detector: StandardScaleGestureDetector): Boolean {
        Assert.fail("scale should not start")
        return false
      }

      override fun onScaleEnd(detector: StandardScaleGestureDetector, velocityX: Float, velocityY: Float) {
        Assert.fail("scale should not start")
      }
    })
    onView(withId(R.id.content)).perform(pinch(250f, 500f))
  }

  @Test
  fun onScaleBegin_doNotStart_thresholdNotMet() {
    gesturesManager.setStandardScaleGestureListener(object : StandardScaleGestureDetector.StandardOnScaleGestureListener {
      override fun onScaleBegin(detector: StandardScaleGestureDetector): Boolean {
        Assert.fail("scale should not start")
        return true
      }

      override fun onScale(detector: StandardScaleGestureDetector): Boolean {
        Assert.fail("scale should not start")
        return false
      }

      override fun onScaleEnd(detector: StandardScaleGestureDetector, velocityX: Float, velocityY: Float) {
        Assert.fail("scale should not start")
      }
    })

    val startSpan = 250f
    val threshold = activityTestRule.activity.resources.getDimension(R.dimen.mapbox_defaultScaleSpanSinceStartThreshold)
    onView(withId(R.id.content)).perform(pinch(startSpan, startSpan + threshold / 2f))
  }

  @Test
  fun onScaleBegin_doNotStart_minSpanNotMet() {
    gesturesManager.setStandardScaleGestureListener(object : StandardScaleGestureDetector.StandardOnScaleGestureListener {
      override fun onScaleBegin(detector: StandardScaleGestureDetector): Boolean {
        Assert.fail("scale should not start")
        return true
      }

      override fun onScale(detector: StandardScaleGestureDetector): Boolean {
        Assert.fail("scale should not start")
        return false
      }

      override fun onScaleEnd(detector: StandardScaleGestureDetector, velocityX: Float, velocityY: Float) {
        Assert.fail("scale should not start")
      }
    })

    gesturesManager.standardScaleGestureDetector.spanSinceStartThreshold = 0f

    val threshold: Float = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
      activityTestRule.activity.resources.getDimension(R.dimen.mapbox_internalMinSpan23)
    } else {
      activityTestRule.activity.resources.getDimension(R.dimen.mapbox_internalMinSpan24)
    }
    onView(withId(R.id.content)).perform(pinch(10f, threshold - 10))
  }

  @Test
  @Throws(InterruptedException::class)
  fun onScaleBegin_start_thresholdMet() {
    val latch = CountDownLatch(1)
    gesturesManager.setStandardScaleGestureListener(object : StandardScaleGestureDetector.StandardOnScaleGestureListener {
      override fun onScaleBegin(detector: StandardScaleGestureDetector): Boolean {
        return true
      }

      override fun onScale(detector: StandardScaleGestureDetector): Boolean {
        if (latch.count > 0) {
          latch.countDown()
        }
        return false
      }

      override fun onScaleEnd(detector: StandardScaleGestureDetector, velocityX: Float, velocityY: Float) {}
    })

    val startSpan = 250f
    val threshold = activityTestRule.activity.resources.getDimension(R.dimen.mapbox_defaultScaleSpanSinceStartThreshold)

    onView(withId(R.id.content)).perform(pinch(startSpan, startSpan + threshold * 2f))

    if (!latch.await(DEFAULT_GESTURE_DURATION, TimeUnit.MILLISECONDS)) {
      Assert.fail("scale was not called")
    }
  }

  @Test
  @Throws(InterruptedException::class)
  fun onScale_interrupt() {
    val startLatch = CountDownLatch(2)
    val endLatch = CountDownLatch(2)
    gesturesManager.setStandardScaleGestureListener(object : StandardScaleGestureDetector.StandardOnScaleGestureListener {

      private var interrupted: Boolean = false

      override fun onScaleBegin(detector: StandardScaleGestureDetector): Boolean {
        startLatch.countDown()
        detector.interrupt()
        return true
      }

      override fun onScale(detector: StandardScaleGestureDetector): Boolean {
        if (!interrupted) {
          interrupted = true
          Handler().postDelayed({ detector.interrupt() }, DEFAULT_GESTURE_DURATION / 2)
        }
        return true
      }

      override fun onScaleEnd(detector: StandardScaleGestureDetector, velocityX: Float, velocityY: Float) {
        endLatch.countDown()
      }
    })

    val startSpan = 250f
    onView(withId(R.id.content)).perform(pinch(startSpan, startSpan * 2f))

    if (!startLatch.await(DEFAULT_GESTURE_DURATION, TimeUnit.MILLISECONDS) || !endLatch.await(DEFAULT_GESTURE_DURATION, TimeUnit.MILLISECONDS)) {
      Assert.fail("scale was not interrupted")
    }
  }

  @Test
  fun onScale_interrupt_increasedThreshold() {
    val startSpan = 250f
    val beginInvocations = intArrayOf(0)
    val endInvocations = intArrayOf(0)
    gesturesManager.setStandardScaleGestureListener(object : StandardScaleGestureDetector.StandardOnScaleGestureListener {

      private var interrupted: Boolean = false

      override fun onScaleBegin(detector: StandardScaleGestureDetector): Boolean {
        beginInvocations[0]++
        return true
      }

      override fun onScale(detector: StandardScaleGestureDetector): Boolean {
        if (!interrupted) {
          interrupted = true
          detector.spanSinceStartThreshold = startSpan * 2f
          Handler().postDelayed({ detector.interrupt() }, DEFAULT_GESTURE_DURATION / 2)
        }
        return true
      }

      override fun onScaleEnd(detector: StandardScaleGestureDetector, velocityX: Float, velocityY: Float) {
        endInvocations[0]++
      }
    })

    onView(withId(R.id.content)).perform(pinch(startSpan, startSpan * 2f))

    Assert.assertEquals(1, beginInvocations[0])
    Assert.assertEquals(1, endInvocations[0])
  }

  @Test
  fun onScale_scalingOut() {
    gesturesManager.setStandardScaleGestureListener(object : StandardScaleGestureDetector.StandardOnScaleGestureListener {
      override fun onScaleBegin(detector: StandardScaleGestureDetector): Boolean {
        return true
      }

      override fun onScale(detector: StandardScaleGestureDetector): Boolean {
        Assert.assertTrue(detector.isScalingOut)
        return true
      }

      override fun onScaleEnd(detector: StandardScaleGestureDetector, velocityX: Float, velocityY: Float) {
        Assert.assertTrue(detector.isScalingOut)
      }
    })

    val startSpan = 250f
    onView(withId(R.id.content)).perform(pinch(startSpan, 0f))
  }

  @Test
  fun onScale_scalingIn() {
    gesturesManager.setStandardScaleGestureListener(object : StandardScaleGestureDetector.StandardOnScaleGestureListener {
      override fun onScaleBegin(detector: StandardScaleGestureDetector): Boolean {
        return true
      }

      override fun onScale(detector: StandardScaleGestureDetector): Boolean {
        Assert.assertFalse(detector.isScalingOut)
        return true
      }

      override fun onScaleEnd(detector: StandardScaleGestureDetector, velocityX: Float, velocityY: Float) {
        Assert.assertFalse(detector.isScalingOut)
      }
    })

    val startSpan = 250f
    onView(withId(R.id.content)).perform(pinch(0f, startSpan))
  }

  @Test
  fun onScale_scalingOut_scaleFactor() {
    gesturesManager.setStandardScaleGestureListener(object : StandardScaleGestureDetector.StandardOnScaleGestureListener {
      override fun onScaleBegin(detector: StandardScaleGestureDetector): Boolean {
        return true
      }

      override fun onScale(detector: StandardScaleGestureDetector): Boolean {
        Assert.assertTrue(detector.scaleFactor < 1f)
        return true
      }

      override fun onScaleEnd(detector: StandardScaleGestureDetector, velocityX: Float, velocityY: Float) {
        Assert.assertEquals(1f, detector.scaleFactor)
      }
    })

    val startSpan = 250f
    onView(withId(R.id.content)).perform(pinch(startSpan, 0f))
  }

  @Test
  fun onScale_scalingIn_scaleFactor() {
    gesturesManager.setStandardScaleGestureListener(object : StandardScaleGestureDetector.StandardOnScaleGestureListener {
      override fun onScaleBegin(detector: StandardScaleGestureDetector): Boolean {
        return true
      }

      override fun onScale(detector: StandardScaleGestureDetector): Boolean {
        Assert.assertTrue(detector.scaleFactor > 1f)
        return true
      }

      override fun onScaleEnd(detector: StandardScaleGestureDetector, velocityX: Float, velocityY: Float) {
        Assert.assertEquals(1f, detector.scaleFactor)
      }
    })

    val startSpan = 250f
    onView(withId(R.id.content)).perform(pinch(0f, startSpan))
  }

  @Test
  fun onScale_scalingOut_velocity() {
    gesturesManager.setStandardScaleGestureListener(object : StandardScaleGestureDetector.StandardOnScaleGestureListener {
      override fun onScaleBegin(detector: StandardScaleGestureDetector): Boolean {
        return true
      }

      override fun onScale(detector: StandardScaleGestureDetector): Boolean {
        return true
      }

      override fun onScaleEnd(detector: StandardScaleGestureDetector, velocityX: Float, velocityY: Float) {
        Assert.assertTrue(velocityX > 0)
        Assert.assertEquals(0, velocityY.toInt())
      }
    })

    val start = 700f
    onView(withId(R.id.content)).perform(pinch(start, 0f))
  }

  @Test
  fun onScale_scalingIn_velocity() {
    gesturesManager.setStandardScaleGestureListener(object : StandardScaleGestureDetector.StandardOnScaleGestureListener {
      override fun onScaleBegin(detector: StandardScaleGestureDetector): Boolean {
        return true
      }

      override fun onScale(detector: StandardScaleGestureDetector): Boolean {
        return true
      }

      override fun onScaleEnd(detector: StandardScaleGestureDetector, velocityX: Float, velocityY: Float) {
        Assert.assertTrue(velocityX < 0)
        Assert.assertEquals(0, velocityY.toInt())
      }
    })

    val endSpan = 700f
    onView(withId(R.id.content)).perform(pinch(0f, endSpan))
  }

  @Test
  fun quickScale_onScaleBegin_doNotStart_false() {
    gesturesManager.setStandardScaleGestureListener(object : StandardScaleGestureDetector.StandardOnScaleGestureListener {
      override fun onScaleBegin(detector: StandardScaleGestureDetector): Boolean {
        return false
      }

      override fun onScale(detector: StandardScaleGestureDetector): Boolean {
        Assert.fail("scale should not start")
        return false
      }

      override fun onScaleEnd(detector: StandardScaleGestureDetector, velocityX: Float, velocityY: Float) {
        Assert.fail("scale should not start")
      }
    })
    onView(withId(R.id.content)).perform(quickScale(300f))
  }

  @Test
  fun quickScale_onScaleBegin_doNotStart_thresholdNotMet() {
    gesturesManager.setStandardScaleGestureListener(object : StandardScaleGestureDetector.StandardOnScaleGestureListener {
      override fun onScaleBegin(detector: StandardScaleGestureDetector): Boolean {
        Assert.fail("scale should not start")
        return true
      }

      override fun onScale(detector: StandardScaleGestureDetector): Boolean {
        Assert.fail("scale should not start")
        return false
      }

      override fun onScaleEnd(detector: StandardScaleGestureDetector, velocityX: Float, velocityY: Float) {
        Assert.fail("scale should not start")
      }
    })

    val threshold = activityTestRule.activity.resources.getDimension(R.dimen.mapbox_defaultScaleSpanSinceStartThreshold)
    onView(withId(R.id.content)).perform(quickScale(threshold / 2f))
  }

  @Test
  @Throws(InterruptedException::class)
  fun quickScale_onScaleBegin_start_thresholdMet() {
    val latch = CountDownLatch(1)
    gesturesManager.setStandardScaleGestureListener(object : StandardScaleGestureDetector.StandardOnScaleGestureListener {
      override fun onScaleBegin(detector: StandardScaleGestureDetector): Boolean {
        return true
      }

      override fun onScale(detector: StandardScaleGestureDetector): Boolean {
        if (latch.count > 0) {
          latch.countDown()
        }
        return false
      }

      override fun onScaleEnd(detector: StandardScaleGestureDetector, velocityX: Float, velocityY: Float) {}
    })

    val threshold = activityTestRule.activity.resources.getDimension(R.dimen.mapbox_defaultScaleSpanSinceStartThreshold)

    onView(withId(R.id.content)).perform(quickScale(threshold * 2f))

    if (!latch.await(DEFAULT_GESTURE_DURATION, TimeUnit.MILLISECONDS)) {
      Assert.fail("scale was not called")
    }
  }

  @Test
  fun quickScale_onScale_interrupt() {
    val beginInvocations = intArrayOf(0)
    val endInvocations = intArrayOf(0)
    gesturesManager.setStandardScaleGestureListener(object : StandardScaleGestureDetector.StandardOnScaleGestureListener {

      private var interrupted: Boolean = false

      override fun onScaleBegin(detector: StandardScaleGestureDetector): Boolean {
        beginInvocations[0]++
        return true
      }

      override fun onScale(detector: StandardScaleGestureDetector): Boolean {
        if (!interrupted) {
          interrupted = true
          Handler().postDelayed({ detector.interrupt() }, DEFAULT_GESTURE_DURATION / 2)
        }
        return true
      }

      override fun onScaleEnd(detector: StandardScaleGestureDetector, velocityX: Float, velocityY: Float) {
        endInvocations[0]++
      }
    })

    onView(withId(R.id.content)).perform(quickScale(500f))

    Assert.assertEquals(1, beginInvocations[0])
    Assert.assertEquals(1, endInvocations[0])
  }

  @Test
  fun quickScale_onScale_interrupt_byGesture() {
    val beginInvocations = intArrayOf(0)
    val endInvocations = intArrayOf(0)
    var moveAfterInterruption = false
    gesturesManager.setStandardScaleGestureListener(object : StandardScaleGestureDetector.StandardOnScaleGestureListener {

      override fun onScaleBegin(detector: StandardScaleGestureDetector): Boolean {
        beginInvocations[0]++
        return true
      }

      override fun onScale(detector: StandardScaleGestureDetector): Boolean {
        return true
      }

      override fun onScaleEnd(detector: StandardScaleGestureDetector, velocityX: Float, velocityY: Float) {
        endInvocations[0]++
        gesturesManager.setMoveGestureListener(object : MoveGestureDetector.OnMoveGestureListener {
          override fun onMoveBegin(detector: MoveGestureDetector) = true

          override fun onMove(detector: MoveGestureDetector, distanceX: Float, distanceY: Float): Boolean {
            if (endInvocations[0] == 1) {
              moveAfterInterruption = true
            } else {
              throw AssertionError()
            }
            return true
          }

          override fun onMoveEnd(detector: MoveGestureDetector, velocityX: Float, velocityY: Float) {}
        })
      }
    })

    onView(withId(R.id.content)).perform(quickScale(500f, interrupt = true, interruptTemporarily = true))

    Assert.assertEquals(1, beginInvocations[0])
    Assert.assertEquals(1, endInvocations[0])
    Assert.assertTrue(moveAfterInterruption)
  }

  @Test
  fun quickScale_onScale_scalingOut() {
    gesturesManager.setStandardScaleGestureListener(object : StandardScaleGestureDetector.StandardOnScaleGestureListener {
      override fun onScaleBegin(detector: StandardScaleGestureDetector): Boolean {
        return true
      }

      override fun onScale(detector: StandardScaleGestureDetector): Boolean {
        Assert.assertTrue(detector.isScalingOut)
        return true
      }

      override fun onScaleEnd(detector: StandardScaleGestureDetector, velocityX: Float, velocityY: Float) {
        Assert.assertTrue(detector.isScalingOut)
      }
    })

    onView(withId(R.id.content)).perform(quickScale(-400f))
  }

  @Test
  fun quickScale_onScale_scalingIn() {
    gesturesManager.setStandardScaleGestureListener(object : StandardScaleGestureDetector.StandardOnScaleGestureListener {
      override fun onScaleBegin(detector: StandardScaleGestureDetector): Boolean {
        return true
      }

      override fun onScale(detector: StandardScaleGestureDetector): Boolean {
        Assert.assertFalse(detector.isScalingOut)
        return true
      }

      override fun onScaleEnd(detector: StandardScaleGestureDetector, velocityX: Float, velocityY: Float) {
        Assert.assertFalse(detector.isScalingOut)
      }
    })

    onView(withId(R.id.content)).perform(quickScale(400f))
  }

  @Test
  fun quickScale_onScale_scalingOut_scaleFactor() {
    gesturesManager.setStandardScaleGestureListener(object : StandardScaleGestureDetector.StandardOnScaleGestureListener {
      override fun onScaleBegin(detector: StandardScaleGestureDetector): Boolean {
        return true
      }

      override fun onScale(detector: StandardScaleGestureDetector): Boolean {
        Assert.assertTrue(detector.scaleFactor < 1f)
        return true
      }

      override fun onScaleEnd(detector: StandardScaleGestureDetector, velocityX: Float, velocityY: Float) {
        Assert.assertEquals(1f, detector.scaleFactor)
      }
    })

    onView(withId(R.id.content)).perform(quickScale(-400f))
  }

  @Test
  fun quickScale_onScale_scalingIn_scaleFactor() {
    gesturesManager.setStandardScaleGestureListener(object : StandardScaleGestureDetector.StandardOnScaleGestureListener {
      override fun onScaleBegin(detector: StandardScaleGestureDetector): Boolean {
        return true
      }

      override fun onScale(detector: StandardScaleGestureDetector): Boolean {
        Assert.assertTrue(detector.scaleFactor > 1f)
        return true
      }

      override fun onScaleEnd(detector: StandardScaleGestureDetector, velocityX: Float, velocityY: Float) {
        Assert.assertEquals(1f, detector.scaleFactor)
      }
    })

    onView(withId(R.id.content)).perform(quickScale(400f))
  }

  @Test
  fun quickScale_onScale_scalingOut_velocity() {
    gesturesManager.setStandardScaleGestureListener(object : StandardScaleGestureDetector.StandardOnScaleGestureListener {
      override fun onScaleBegin(detector: StandardScaleGestureDetector): Boolean {
        return true
      }

      override fun onScale(detector: StandardScaleGestureDetector): Boolean {
        return true
      }

      override fun onScaleEnd(detector: StandardScaleGestureDetector, velocityX: Float, velocityY: Float) {
        Assert.assertEquals(0, velocityX.toInt())
        Assert.assertTrue(velocityY < 0)
      }
    })

    val delta = 700f
    onView(withId(R.id.content)).perform(quickScale(-delta))
  }

  @Test
  fun quickScale_onScale_scalingIn_velocity() {
    gesturesManager.setStandardScaleGestureListener(object : StandardScaleGestureDetector.StandardOnScaleGestureListener {
      override fun onScaleBegin(detector: StandardScaleGestureDetector): Boolean {
        return true
      }

      override fun onScale(detector: StandardScaleGestureDetector): Boolean {
        return true
      }

      override fun onScaleEnd(detector: StandardScaleGestureDetector, velocityX: Float, velocityY: Float) {
        Assert.assertEquals(0, velocityX.toInt())
        Assert.assertTrue(velocityY > 0)
      }
    })

    val delta = 700f
    onView(withId(R.id.content)).perform(quickScale(delta))
  }

  @Test
  fun doubleTap_move_doNotQuickZoom() {
    gesturesManager.setStandardScaleGestureListener(object : StandardScaleGestureDetector.StandardOnScaleGestureListener {
      override fun onScaleBegin(detector: StandardScaleGestureDetector): Boolean {
        Assert.fail("scale detector should not be called")
        return true
      }

      override fun onScale(detector: StandardScaleGestureDetector): Boolean {
        Assert.fail("scale detector should not be called")
        return true
      }

      override fun onScaleEnd(detector: StandardScaleGestureDetector, velocityX: Float, velocityY: Float) {
        Assert.fail("scale detector should not be called")
      }
    })

    onView(withId(R.id.content)).perform(move(300f, 300f, withVelocity = false))
  }
}
