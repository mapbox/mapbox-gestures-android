package com.mapbox.android.gestures;

import android.content.Context;
import android.view.MotionEvent;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

@RunWith(RobolectricTestRunner.class)
abstract class AbstractGestureDetectorTest<K extends BaseGesture> {
  AndroidGesturesManager androidGesturesManager;
  Context context;
  MotionEvent emptyMotionEvent;

  @Before
  public void setUp() throws Exception {
    context = RuntimeEnvironment.application.getApplicationContext();
    androidGesturesManager = new AndroidGesturesManager(context);
    emptyMotionEvent = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 250.0f, 250.0f, 0);
  }
}
