package com.mapbox.android.gestures;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class StandardGestureDetectorTest extends AbstractGestureDetectorTest {
  private StandardGestureDetector standardGestureDetector;

  @Mock
  private StandardGestureDetector.StandardOnGestureListener listener;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    MockitoAnnotations.initMocks(this);
    standardGestureDetector = new StandardGestureDetector(context, androidGesturesManager);
    standardGestureDetector.setListener(listener);
  }

  @Test
  public void onSingleTapUpTest() {
    standardGestureDetector.innerListener.onSingleTapUp(emptyMotionEvent);
    Mockito.verify(listener, Mockito.times(1)).onSingleTapUp(emptyMotionEvent);
  }

  @Test
  public void onLongPressTest() {
    standardGestureDetector.innerListener.onLongPress(emptyMotionEvent);
    Mockito.verify(listener, Mockito.times(1)).onLongPress(emptyMotionEvent);
  }

  @Test
  public void onScrollTest() {
    standardGestureDetector.innerListener.onScroll(emptyMotionEvent, emptyMotionEvent, 0, 0);
    Mockito.verify(listener, Mockito.times(1))
      .onScroll(emptyMotionEvent, emptyMotionEvent, 0, 0);
  }

  @Test
  public void onFlingTest() {
    standardGestureDetector.innerListener.onFling(emptyMotionEvent, emptyMotionEvent, 0, 0);
    Mockito.verify(listener, Mockito.times(1))
      .onFling(emptyMotionEvent, emptyMotionEvent, 0, 0);
  }

  @Test
  public void onShowPressTest() {
    standardGestureDetector.innerListener.onShowPress(emptyMotionEvent);
    Mockito.verify(listener, Mockito.times(1)).onShowPress(emptyMotionEvent);
  }

  @Test
  public void onDownTest() {
    standardGestureDetector.innerListener.onDown(emptyMotionEvent);
    Mockito.verify(listener, Mockito.times(1)).onDown(emptyMotionEvent);
  }

  @Test
  public void onDoubleTapTest() {
    standardGestureDetector.innerListener.onDoubleTap(emptyMotionEvent);
    Mockito.verify(listener, Mockito.times(1)).onDoubleTap(emptyMotionEvent);
  }

  @Test
  public void onDoubleTapEventTest() {
    standardGestureDetector.innerListener.onDoubleTapEvent(emptyMotionEvent);
    Mockito.verify(listener, Mockito.times(1)).onDoubleTapEvent(emptyMotionEvent);
  }

  @Test
  public void onSingleTapConfirmedTest() {
    standardGestureDetector.innerListener.onSingleTapConfirmed(emptyMotionEvent);
    Mockito.verify(listener, Mockito.times(1)).onSingleTapConfirmed(emptyMotionEvent);
  }

  @Test
  public void analyzeEventTest() throws Exception {
  }

  @Test
  public void longpressEnabledTest() throws Exception {
    assertTrue(standardGestureDetector.isLongpressEnabled());
    standardGestureDetector.setIsLongpressEnabled(false);
    assertFalse(standardGestureDetector.isLongpressEnabled());
    standardGestureDetector.setIsLongpressEnabled(true);
    assertTrue(standardGestureDetector.isLongpressEnabled());
  }
}