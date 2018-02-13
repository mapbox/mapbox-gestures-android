package com.mapbox.android.gestures;

import android.view.MotionEvent;

import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
final class TestUtils {

  public static MotionEvent getMotionEvent(int action, float x, float y, MotionEvent previousEvent) {
    long currentTime = System.currentTimeMillis();
    long downTime = previousEvent != null ? previousEvent.getDownTime() : System.currentTimeMillis();

    return MotionEvent.obtain(
      downTime,
      currentTime,
      action,
      x,
      y,
      0
    );
  }

  public static MotionEvent getMotionEvent(int action, float x, float y) {
    return getMotionEvent(action, x, y, null);
  }
}
