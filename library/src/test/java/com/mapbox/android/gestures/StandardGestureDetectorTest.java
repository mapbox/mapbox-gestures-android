package com.mapbox.android.gestures;

import org.junit.Test;
import org.mockito.Mockito;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class StandardGestureDetectorTest extends
  AbstractGestureDetectorTest<StandardGestureDetector, StandardGestureDetector.StandardOnGestureListener> {

  @Override
  StandardGestureDetector getDetectorObject() {
    return androidGesturesManager.getStandardGestureDetector();
  }

  @Test
  public void onSingleTapUpTest() {
    gestureDetector.innerListener.onSingleTapUp(emptyMotionEvent);
    Mockito.verify(listener, Mockito.times(1)).onSingleTapUp(emptyMotionEvent);
  }

  @Test
  public void onLongPressTest() {
    gestureDetector.innerListener.onLongPress(emptyMotionEvent);
    Mockito.verify(listener, Mockito.times(1)).onLongPress(emptyMotionEvent);
  }

  @Test
  public void onScrollTest() {
    gestureDetector.innerListener.onScroll(emptyMotionEvent, emptyMotionEvent, 0, 0);
    Mockito.verify(listener, Mockito.times(1))
      .onScroll(emptyMotionEvent, emptyMotionEvent, 0, 0);
  }

  @Test
  public void onFlingTest() {
    gestureDetector.innerListener.onFling(emptyMotionEvent, emptyMotionEvent, 0, 0);
    Mockito.verify(listener, Mockito.times(1))
      .onFling(emptyMotionEvent, emptyMotionEvent, 0, 0);
  }

  @Test
  public void onShowPressTest() {
    gestureDetector.innerListener.onShowPress(emptyMotionEvent);
    Mockito.verify(listener, Mockito.times(1)).onShowPress(emptyMotionEvent);
  }

  @Test
  public void onDownTest() {
    gestureDetector.innerListener.onDown(emptyMotionEvent);
    Mockito.verify(listener, Mockito.times(1)).onDown(emptyMotionEvent);
  }

  @Test
  public void onDoubleTapTest() {
    gestureDetector.innerListener.onDoubleTap(emptyMotionEvent);
    Mockito.verify(listener, Mockito.times(1)).onDoubleTap(emptyMotionEvent);
  }

  @Test
  public void onDoubleTapEventTest() {
    gestureDetector.innerListener.onDoubleTapEvent(emptyMotionEvent);
    Mockito.verify(listener, Mockito.times(1)).onDoubleTapEvent(emptyMotionEvent);
  }

  @Test
  public void onSingleTapConfirmedTest() {
    gestureDetector.innerListener.onSingleTapConfirmed(emptyMotionEvent);
    Mockito.verify(listener, Mockito.times(1)).onSingleTapConfirmed(emptyMotionEvent);
  }

  @Test
  public void longpressEnabledTest() throws Exception {
    assertTrue(gestureDetector.isLongpressEnabled());
    gestureDetector.setIsLongpressEnabled(false);
    assertFalse(gestureDetector.isLongpressEnabled());
    gestureDetector.setIsLongpressEnabled(true);
    assertTrue(gestureDetector.isLongpressEnabled());
  }
}