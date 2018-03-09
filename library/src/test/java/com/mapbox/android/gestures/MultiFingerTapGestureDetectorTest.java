package com.mapbox.android.gestures;

import android.view.MotionEvent;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashMap;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MultiFingerTapGestureDetectorTest
  extends AbstractGestureDetectorTest<MultiFingerTapGestureDetector,
  MultiFingerTapGestureDetector.OnMultiFingerTapGestureListener> {

  private MotionEvent downMotionEvent;
  private MotionEvent pointerDownMotionEvent;
  private MotionEvent pointerUpMotionEvent;
  private MotionEvent upMotionEvent;
  private MotionEvent moveMotionEvent;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    doReturn(true).when(gestureDetector).checkPressure();
    doReturn(false).when(gestureDetector).isSloppyGesture();
  }

  @Override
  MultiFingerTapGestureDetector getDetectorObject() {
    return Mockito.spy(androidGesturesManager.getMultiFingerTapGestureDetector());
  }

  @Test
  public void twoFingerTapTest() {
    dispatchDown();
    dispatchPointerDown(10, downMotionEvent);
    dispatchPointerUp((Constants.DEFAULT_MULTI_TAP_TIME_THRESHOLD / 2), pointerDownMotionEvent);
    dispatchUp(10, pointerUpMotionEvent);
    verify(listener, times(1)).onMultiFingerTap(gestureDetector, 2);
  }

  @Test
  public void threeFingerTapTest() {
    dispatchDown();
    dispatchPointerDown(10, downMotionEvent);
    dispatchPointerDown(10, pointerDownMotionEvent);
    dispatchPointerUp((Constants.DEFAULT_MULTI_TAP_TIME_THRESHOLD / 2), pointerDownMotionEvent);
    dispatchPointerUp(10, pointerUpMotionEvent);
    dispatchUp(10, pointerUpMotionEvent);
    verify(listener, times(1)).onMultiFingerTap(gestureDetector, 3);
  }

  @Test
  public void twoFingerTapExceededTest() {
    dispatchDown();
    dispatchPointerDown(25, downMotionEvent);
    dispatchPointerUp(Constants.DEFAULT_MULTI_TAP_TIME_THRESHOLD, pointerDownMotionEvent);
    dispatchUp(25, pointerUpMotionEvent);
    verify(listener, times(0)).onMultiFingerTap(gestureDetector, 2);
  }

  @Test
  public void twoFingerMissTapTest() {
    // Testing for two pointers placed, one lifted and placed again. Should fail.
    dispatchDown();
    dispatchPointerDown(10, downMotionEvent);
    dispatchPointerUp(10, pointerDownMotionEvent);
    dispatchPointerDown(10, pointerUpMotionEvent);
    dispatchPointerUp(10, pointerDownMotionEvent);
    dispatchUp(10, pointerUpMotionEvent);
    verify(listener, times(0)).onMultiFingerTap(gestureDetector, 2);
  }

  @Test
  public void twoFingerTapMovementTest() {
    when(
      gestureDetector.exceededMovementThreshold(gestureDetector.pointersDistanceMap))
      .thenReturn(false);
    dispatchMovement();
    verify(listener, times(1)).onMultiFingerTap(gestureDetector, 2);

    when(
      gestureDetector.exceededMovementThreshold(gestureDetector.pointersDistanceMap))
      .thenReturn(true);
    dispatchMovement();
    verify(listener, times(1)).onMultiFingerTap(gestureDetector, 2);
  }

  private void dispatchMovement() {
    dispatchDown();
    dispatchPointerDown(10, downMotionEvent);
    dispatchMove(10, pointerDownMotionEvent);
    dispatchPointerUp(Constants.DEFAULT_MULTI_TAP_TIME_THRESHOLD / 2, moveMotionEvent);
    dispatchUp(10, pointerUpMotionEvent);
  }

  @Test
  public void exceededMovementTest() {
    float prevX = 100;
    float prevY = 100;
    float currX = 100;
    float currY = 100;
    final HashMap<PointerDistancePair, MultiFingerDistancesObject> map = new HashMap<>();
    map.put(new PointerDistancePair(0, 1), new MultiFingerDistancesObject(
      prevX, prevY,
      currX, currY
    ));
    assertFalse(gestureDetector.exceededMovementThreshold(map));

    map.clear();
    map.put(new PointerDistancePair(0, 1), new MultiFingerDistancesObject(
      prevX, prevY,
      currX + gestureDetector.getMultiFingerTapMovementThreshold() + 0.5f, currY
    ));
    assertTrue(gestureDetector.exceededMovementThreshold(map));
  }

  private void dispatchDown() {
    downMotionEvent = TestUtils.getMotionEvent(MotionEvent.ACTION_DOWN, 0, 0);
    gestureDetector.onTouchEvent(downMotionEvent);
  }

  private void dispatchPointerDown(long delay, MotionEvent previous) {
    try {
      Thread.sleep(delay);
      pointerDownMotionEvent = TestUtils.getMotionEvent(MotionEvent.ACTION_POINTER_DOWN, 0, 0, previous);
      gestureDetector.onTouchEvent(pointerDownMotionEvent);
    } catch (InterruptedException ex) {
      ex.printStackTrace();
    }
  }

  private void dispatchPointerUp(long delay, MotionEvent previous) {
    try {
      Thread.sleep(delay);
      pointerUpMotionEvent = TestUtils.getMotionEvent(MotionEvent.ACTION_POINTER_UP, 0, 0, previous);
      gestureDetector.onTouchEvent(pointerUpMotionEvent);
    } catch (InterruptedException ex) {
      ex.printStackTrace();
    }
  }

  private void dispatchUp(long delay, MotionEvent previous) {
    try {
      Thread.sleep(delay);
      upMotionEvent = TestUtils.getMotionEvent(MotionEvent.ACTION_UP, 0, 0, previous);
      gestureDetector.onTouchEvent(upMotionEvent);
    } catch (InterruptedException ex) {
      ex.printStackTrace();
    }
  }

  private void dispatchMove(long delay, MotionEvent previous) {
    try {
      Thread.sleep(delay);
      moveMotionEvent = TestUtils.getMotionEvent(MotionEvent.ACTION_MOVE, 0, 0, previous);
      gestureDetector.onTouchEvent(moveMotionEvent);
    } catch (InterruptedException ex) {
      ex.printStackTrace();
    }
  }
}
