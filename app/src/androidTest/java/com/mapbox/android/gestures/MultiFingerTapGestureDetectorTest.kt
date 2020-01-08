package com.mapbox.android.gestures

import GesturesUiTestUtils.DEFAULT_GESTURE_DURATION
import GesturesUiTestUtils.twoTap
import androidx.test.espresso.Espresso
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.rule.ActivityTestRule
import androidx.test.ext.junit.runners.AndroidJUnit4
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
class MultiFingerTapGestureDetectorTest {

  @Rule
  @JvmField
  val activityTestRule = ActivityTestRule(TestActivity::class.java)

  private lateinit var gesturesManager: AndroidGesturesManager

  @Before
  fun setup() {
    gesturesManager = activityTestRule.activity.gesturesManager
  }

  @Test
  fun noMove_focalPoint_invalidated() {
    val latch = CountDownLatch(1)
    gesturesManager.setMultiFingerTapGestureListener { detector, pointersCount ->
      Assert.assertEquals(2, pointersCount)
      Assert.assertEquals(Utils.determineFocalPoint(detector.currentEvent), detector.focalPoint)
      latch.countDown()
      true
    }
    Espresso.onView(ViewMatchers.withId(R.id.content)).perform(twoTap(300f))

    if (!latch.await(DEFAULT_GESTURE_DURATION, TimeUnit.MILLISECONDS)) {
      Assert.fail("two-tap was not called")
    }
  }
}