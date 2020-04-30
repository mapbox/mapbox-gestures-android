package com.mapbox.android.gestures

import GesturesUiTestUtils.DEFAULT_GESTURE_DURATION
import GesturesUiTestUtils.move
import android.graphics.PointF
import android.graphics.RectF
import androidx.test.espresso.Espresso
import androidx.test.espresso.matcher.ViewMatchers
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
class MoveGestureDetectorTest {

  @Rule
  @JvmField
  val activityTestRule = ActivityTestRule(TestActivity::class.java)

  private lateinit var gesturesManager: AndroidGesturesManager

  @Before
  fun setup() {
    gesturesManager = activityTestRule.activity.gesturesManager
  }

  @Test
  fun move_ignoredWithRectThreshold() {
    val rect = RectF(400f, 400f, 600f, 600f)
    gesturesManager.setMoveGestureListener(object : MoveGestureDetector.OnMoveGestureListener {
      override fun onMoveBegin(detector: MoveGestureDetector) = true

      override fun onMove(
        detector: MoveGestureDetector,
        distanceX: Float,
        distanceY: Float
      ): Boolean = throw AssertionError("onMove shouldn't be called if threshold was not met")

      override fun onMoveEnd(detector: MoveGestureDetector, velocityX: Float, velocityY: Float) = Unit

    })
    gesturesManager.moveGestureDetector.moveThresholdRect = rect
    Espresso.onView(ViewMatchers.withId(R.id.content)).perform(
      move(
        deltaX = 50f,
        deltaY = 50f,
        startPoint = PointF(rect.right - 100f, rect.bottom - 100f)
      )
    )
  }

  @Test
  fun move_executedWhenOutsideOfRect() {
    val latch = CountDownLatch(1)
    val rect = RectF(400f, 400f, 600f, 600f)
    gesturesManager.setMoveGestureListener(object : MoveGestureDetector.OnMoveGestureListener {
      override fun onMoveBegin(detector: MoveGestureDetector) = true

      override fun onMove(
        detector: MoveGestureDetector,
        distanceX: Float,
        distanceY: Float
      ): Boolean {
        Assert.assertFalse(rect.contains(detector.focalPoint.x, detector.focalPoint.y))
        latch.countDown()
        return true
      }

      override fun onMoveEnd(detector: MoveGestureDetector, velocityX: Float, velocityY: Float) = Unit

    })
    gesturesManager.moveGestureDetector.moveThresholdRect = rect
    Espresso.onView(ViewMatchers.withId(R.id.content)).perform(
      move(
        deltaX = 100f,
        deltaY = 100f,
        startPoint = PointF(rect.right + 50f, rect.bottom + 50f)
      )
    )
    if (!latch.await(DEFAULT_GESTURE_DURATION, TimeUnit.MILLISECONDS)) {
      Assert.fail("move was not called")
    }
  }

  @Test
  fun move_executedWhenRectThresholdMet() {
    val latch = CountDownLatch(1)
    val rect = RectF(400f, 400f, 600f, 600f)
    gesturesManager.setMoveGestureListener(object : MoveGestureDetector.OnMoveGestureListener {
      override fun onMoveBegin(detector: MoveGestureDetector) = true

      override fun onMove(
        detector: MoveGestureDetector,
        distanceX: Float,
        distanceY: Float
      ): Boolean {
        Assert.assertFalse(rect.contains(detector.focalPoint.x, detector.focalPoint.y))
        latch.countDown()
        return true
      }

      override fun onMoveEnd(detector: MoveGestureDetector, velocityX: Float, velocityY: Float) = Unit

    })
    gesturesManager.moveGestureDetector.moveThresholdRect = rect
    Espresso.onView(ViewMatchers.withId(R.id.content)).perform(
      move(
        deltaX = -150f,
        deltaY = -150f,
        startPoint = PointF(500f, 500f)
      )
    )
    if (!latch.await(DEFAULT_GESTURE_DURATION, TimeUnit.MILLISECONDS)) {
      Assert.fail("move was not called")
    }
  }

  @Test
  fun move_whenOutsideOfRect_executeWhenMoveThresholdMet() {
    val latch = CountDownLatch(1)
    val rect = RectF(400f, 400f, 600f, 600f)
    gesturesManager.setMoveGestureListener(object : MoveGestureDetector.OnMoveGestureListener {
      override fun onMoveBegin(detector: MoveGestureDetector) = true

      override fun onMove(
        detector: MoveGestureDetector,
        distanceX: Float,
        distanceY: Float
      ): Boolean {
        Assert.assertFalse(rect.contains(detector.focalPoint.x, detector.focalPoint.y))
        latch.countDown()
        return true
      }

      override fun onMoveEnd(detector: MoveGestureDetector, velocityX: Float, velocityY: Float) = Unit

    })
    gesturesManager.moveGestureDetector.moveThresholdRect = rect
    gesturesManager.moveGestureDetector.moveThreshold = 50f
    Espresso.onView(ViewMatchers.withId(R.id.content)).perform(
      move(
        deltaX = 100f,
        deltaY = 100f,
        startPoint = PointF(rect.right + 50f, rect.bottom + 50f)
      )
    )
    if (!latch.await(DEFAULT_GESTURE_DURATION, TimeUnit.MILLISECONDS)) {
      Assert.fail("move was not called")
    }
  }

  @Test
  fun move_whenOutsideOfRect_ignoredWhenMoveThreshold() {
    val rect = RectF(400f, 400f, 600f, 600f)
    gesturesManager.setMoveGestureListener(object : MoveGestureDetector.OnMoveGestureListener {
      override fun onMoveBegin(detector: MoveGestureDetector) = true

      override fun onMove(
        detector: MoveGestureDetector,
        distanceX: Float,
        distanceY: Float
      ): Boolean = throw AssertionError("onMove shouldn't be called if threshold was not met")

      override fun onMoveEnd(detector: MoveGestureDetector, velocityX: Float, velocityY: Float) = Unit

    })
    gesturesManager.moveGestureDetector.moveThresholdRect = rect
    gesturesManager.moveGestureDetector.moveThreshold = 50f
    Espresso.onView(ViewMatchers.withId(R.id.content)).perform(
      move(
        deltaX = 25f,
        deltaY = 25f,
        startPoint = PointF(rect.right + 50f, rect.bottom + 50f)
      )
    )
  }
}