package com.mapbox.android.gestures;

import android.content.Context;
import android.view.MotionEvent;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.lang.reflect.ParameterizedType;

@RunWith(RobolectricTestRunner.class)
public abstract class AbstractGestureDetectorTest<K extends BaseGesture<L>, L> {
  AndroidGesturesManager androidGesturesManager;
  Context context;
  MotionEvent emptyMotionEvent;

  K gestureDetector;
  L listener;

  @Before
  @SuppressWarnings("unchecked")
  public void setUp() throws Exception {
    // Working around Mockito class cast issue when initializing mocks form annotations
    Class<L> listenerClazz =
      (Class<L>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];

    listener = Mockito.mock(listenerClazz);

    context = RuntimeEnvironment.application.getApplicationContext();
    androidGesturesManager = new AndroidGesturesManager(context);

    // reinitialize dimen thresholds
    for (BaseGesture detector : androidGesturesManager.getDetectors()) {
      if (detector instanceof MultiFingerTapGestureDetector) {
        ((MultiFingerGesture) detector).setSpanThreshold(290f);
      }

      if (detector instanceof StandardScaleGestureDetector) {
        ((StandardScaleGestureDetector) detector).setSpanSinceStartThreshold(20f);
      }

      if (detector instanceof ShoveGestureDetector) {
        ((ShoveGestureDetector) detector).setPixelDeltaThreshold(100f);
      }

      if (detector instanceof SidewaysShoveGestureDetector) {
        ((SidewaysShoveGestureDetector) detector).setPixelDeltaThreshold(100f);
      }

      if (detector instanceof MultiFingerTapGestureDetector) {
        ((MultiFingerTapGestureDetector) detector).setMultiFingerTapMovementThreshold(15f);
      }
    }

    gestureDetector = getDetectorObject();
    gestureDetector.setListener(listener);
    emptyMotionEvent = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 250.0f, 250.0f, 0);
  }

  abstract K getDetectorObject();
}