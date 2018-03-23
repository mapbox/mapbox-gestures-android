package com.mapbox.android.gestures;

import org.junit.Test;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StandardScaleGestureDetectorTest extends
  AbstractGestureDetectorTest<StandardScaleGestureDetector,
    StandardScaleGestureDetector.StandardOnScaleGestureListener> {

  @Override
  StandardScaleGestureDetector getDetectorObject() {
    return spy(androidGesturesManager.getStandardScaleGestureDetector());
  }

  @Test
  public void analyzeMovementTest() throws Exception {
    when(listener.onScaleBegin(gestureDetector)).thenReturn(true);
    when(listener.onScale(gestureDetector)).thenReturn(true);
    doReturn(false).when(gestureDetector).isSloppyGesture();

    // threshold not met
    gestureDetector.spanDeltaSinceStart = gestureDetector.getSpanSinceStartThreshold() / 2;
    gestureDetector.innerOnScaleBegin(gestureDetector.getUnderlyingScaleGestureDetector());

    // threshold met, starting
    gestureDetector.startSpan = gestureDetector.getSpanSinceStartThreshold() * 2;
    gestureDetector.innerOnScale(gestureDetector.getUnderlyingScaleGestureDetector());

    // scale
    gestureDetector.innerOnScale(gestureDetector.getUnderlyingScaleGestureDetector());

    // stopping
    gestureDetector.innerOnScaleEnd(gestureDetector.getUnderlyingScaleGestureDetector());

    //interrupt
    gestureDetector.interrupt();

    float threshold = gestureDetector.getSpanSinceStartThreshold();

    // no threshold, starting immediately
    gestureDetector.setSpanSinceStartThreshold(0f);
    gestureDetector.startSpan = 0;
    gestureDetector.innerOnScaleBegin(gestureDetector.getUnderlyingScaleGestureDetector());

    doReturn(emptyMotionEvent).when(gestureDetector).getCurrentEvent();
    //doesn't call stop because wasn't in progress when interrupted
    gestureDetector.analyzeEvent(emptyMotionEvent);

    //scale
    gestureDetector.innerOnScale(gestureDetector.getUnderlyingScaleGestureDetector());

    //interrupt
    gestureDetector.interrupt();
    //stopping because was interrupted
    gestureDetector.analyzeEvent(emptyMotionEvent);

    //starting again because was interrupted
    gestureDetector.innerOnScale(gestureDetector.getUnderlyingScaleGestureDetector());

    //scale
    gestureDetector.innerOnScale(gestureDetector.getUnderlyingScaleGestureDetector());

    // stopping
    gestureDetector.innerOnScaleEnd(gestureDetector.getUnderlyingScaleGestureDetector());

    // threshold not met
    gestureDetector.setSpanSinceStartThreshold(threshold);
    gestureDetector.startSpan = gestureDetector.getSpanSinceStartThreshold() / 2;
    gestureDetector.innerOnScaleBegin(gestureDetector.getUnderlyingScaleGestureDetector());
    gestureDetector.innerOnScale(gestureDetector.getUnderlyingScaleGestureDetector());
    // stopping without surpassing threshold, no callback invocations
    gestureDetector.innerOnScaleEnd(gestureDetector.getUnderlyingScaleGestureDetector());

    verify(listener, times(3)).onScaleBegin(gestureDetector);
    verify(listener, times(3)).onScale(gestureDetector);
    verify(listener, times(3)).onScaleEnd(
      gestureDetector, gestureDetector.velocityX, gestureDetector.velocityY);
  }

  @Test
  public void internalMinSpanSetterTest() throws Exception {
    // future-proofing against API changes
    gestureDetector.modifyInternalMinSpanValues();
  }
}