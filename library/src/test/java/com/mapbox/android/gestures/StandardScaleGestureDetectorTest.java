package com.mapbox.android.gestures;

import org.junit.Test;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StandardScaleGestureDetectorTest extends
  AbstractGestureDetectorTest<StandardScaleGestureDetector,
    StandardScaleGestureDetector.StandardOnScaleGestureListener> {

  @Override
  StandardScaleGestureDetector getDetectorObject() {
    return spy(new StandardScaleGestureDetector(context, androidGesturesManager));
  }

  @Test
  public void analyzeMovementTest() throws Exception {
    when(listener.onScaleBegin(gestureDetector)).thenReturn(true);
    when(listener.onScale(gestureDetector)).thenReturn(true);
    doNothing().when(gestureDetector).startAnimation(0);

    // threshold not met
    gestureDetector.spanDeltaSinceStart = gestureDetector.getSpanSinceStartThreshold() / 2;
    gestureDetector.innerOnScaleBegin(gestureDetector.getUnderlyingScaleGestureDetector());
    verify(listener, times(0)).onScaleBegin(gestureDetector);
    verify(listener, times(0)).onScale(gestureDetector);
    verify(listener, times(0)).onScaleEnd(gestureDetector);

    // threshold met
    gestureDetector.startSpan = gestureDetector.getSpanSinceStartThreshold() * 2;
    gestureDetector.innerOnScale(gestureDetector.getUnderlyingScaleGestureDetector());
    verify(listener, times(1)).onScaleBegin(gestureDetector);
    verify(listener, times(0)).onScale(gestureDetector);
    verify(listener, times(0)).onScaleEnd(gestureDetector);

    gestureDetector.innerOnScale(gestureDetector.getUnderlyingScaleGestureDetector());
    verify(listener, times(1)).onScaleBegin(gestureDetector);
    verify(listener, times(1)).onScale(gestureDetector);
    verify(listener, times(0)).onScaleEnd(gestureDetector);

    // stopping
    gestureDetector.innerOnScaleEnd(gestureDetector.getUnderlyingScaleGestureDetector());
    verify(listener, times(1)).onScaleBegin(gestureDetector);
    verify(listener, times(1)).onScale(gestureDetector);
    verify(listener, times(1)).onScaleEnd(gestureDetector);

    // no threshold, starting immediately
    gestureDetector.setSpanSinceStartThreshold(0);
    gestureDetector.startSpan = 0;
    gestureDetector.innerOnScaleBegin(gestureDetector.getUnderlyingScaleGestureDetector());
    verify(listener, times(2)).onScaleBegin(gestureDetector);
    verify(listener, times(1)).onScale(gestureDetector);
    verify(listener, times(1)).onScaleEnd(gestureDetector);

    gestureDetector.innerOnScale(gestureDetector.getUnderlyingScaleGestureDetector());
    verify(listener, times(2)).onScaleBegin(gestureDetector);
    verify(listener, times(2)).onScale(gestureDetector);
    verify(listener, times(1)).onScaleEnd(gestureDetector);

    // stopping
    gestureDetector.innerOnScaleEnd(gestureDetector.getUnderlyingScaleGestureDetector());
    verify(listener, times(2)).onScaleBegin(gestureDetector);
    verify(listener, times(2)).onScale(gestureDetector);
    verify(listener, times(2)).onScaleEnd(gestureDetector);

    // threshold not met
    gestureDetector.setSpanSinceStartThreshold(gestureDetector.getDefaultSpanSinceStartThreshold());
    gestureDetector.startSpan = gestureDetector.getSpanThreshold() / 2;
    gestureDetector.innerOnScaleBegin(gestureDetector.getUnderlyingScaleGestureDetector());
    gestureDetector.innerOnScale(gestureDetector.getUnderlyingScaleGestureDetector());
    // stopping without surpassing threshold, no callback invocations
    gestureDetector.innerOnScaleEnd(gestureDetector.getUnderlyingScaleGestureDetector());
    verify(listener, times(2)).onScaleBegin(gestureDetector);
    verify(listener, times(2)).onScale(gestureDetector);
    verify(listener, times(2)).onScaleEnd(gestureDetector);
  }
}