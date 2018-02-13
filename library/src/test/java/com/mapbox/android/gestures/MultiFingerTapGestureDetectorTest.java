package com.mapbox.android.gestures;

import android.view.MotionEvent;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MultiFingerTapGestureDetectorTest extends AbstractGestureDetectorTest {

  private MultiFingerTapGestureDetector multiFingerTapGestureDetector;
  @Mock
  private MultiFingerTapGestureDetector.OnMultiFingerTapGestureListener listener;

  private MotionEvent downMotionEvent;
  private MotionEvent pointerDownMotionEvent;
  private MotionEvent pointerUpMotionEvent;
  private MotionEvent upMotionEvent;
  private MotionEvent moveMotionEvent;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    MockitoAnnotations.initMocks(this);
    multiFingerTapGestureDetector = Mockito.spy(new MultiFingerTapGestureDetector(context, androidGesturesManager));
    multiFingerTapGestureDetector.setListener(listener);
    doReturn(true).when(multiFingerTapGestureDetector).checkPressure();
    doReturn(false).when(multiFingerTapGestureDetector).isSloppyGesture();
  }

  @Test
  public void twoFingerTapTest() {
    dispatchMultiFingerTap(false);
  }


  @Test
  public void threeFingerTapTest() {
    dispatchMultiFingerTap(true);
  }

  private void dispatchMultiFingerTap(boolean isThreeFinger) {
    dispatchDown();
    dispatchPointerDown(10, downMotionEvent);
    if (isThreeFinger) {
      dispatchPointerDown(10, pointerDownMotionEvent);
    }
    dispatchPointerUp((Constants.DEFAULT_MULTI_TAP_TIME_THRESHOLD / 2), pointerDownMotionEvent);
    if (isThreeFinger) {
      dispatchPointerUp(10, pointerUpMotionEvent);
    }
    dispatchUp(10, pointerUpMotionEvent);

    verify(listener, times(1)).onMultiFingerTap(
      multiFingerTapGestureDetector, isThreeFinger ? 3 : 2);
  }

  @Test
  public void twoFingerTapExceededTest() {
    dispatchDown();
    dispatchPointerDown(25, downMotionEvent);
    dispatchPointerUp(Constants.DEFAULT_MULTI_TAP_TIME_THRESHOLD, pointerDownMotionEvent);
    dispatchUp(25, pointerUpMotionEvent);

    verify(listener, times(0)).onMultiFingerTap(multiFingerTapGestureDetector, 2);
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

    verify(listener, times(0)).onMultiFingerTap(multiFingerTapGestureDetector, 2);
  }

  @Test
  public void twoFingerTapMovementTest() {
    when(
      multiFingerTapGestureDetector.exceededMovementThreshold(multiFingerTapGestureDetector.pointersDistanceMap))
      .thenReturn(false);
    dispatchMovement();
    verify(listener, times(1)).onMultiFingerTap(multiFingerTapGestureDetector, 2);

    when(
      multiFingerTapGestureDetector.exceededMovementThreshold(multiFingerTapGestureDetector.pointersDistanceMap))
      .thenReturn(true);
    dispatchMovement();
    verify(listener, times(1)).onMultiFingerTap(multiFingerTapGestureDetector, 2);
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
      currX, currY,
      (float) Math.sqrt(prevX * prevX + prevY * prevY),
      (float) Math.sqrt(currX * currX + currY * currY)
    ));
    assertFalse(multiFingerTapGestureDetector.exceededMovementThreshold(map));

    map.clear();
    map.put(new PointerDistancePair(0, 1), new MultiFingerDistancesObject(
      prevX, prevY,
      currX + Constants.DEFAULT_MULTI_TAP_MOVEMENT_THRESHOLD + 0.5f, currY,
      (float) Math.sqrt(prevX * prevX + prevY * prevY),
      (float) Math.sqrt((currX + Constants.DEFAULT_MULTI_TAP_MOVEMENT_THRESHOLD + 0.5f)
        * (currX + Constants.DEFAULT_MULTI_TAP_MOVEMENT_THRESHOLD + 0.5f) + currY * currY)
    ));
    assertTrue(multiFingerTapGestureDetector.exceededMovementThreshold(map));
  }

  private void dispatchDown() {
    downMotionEvent = TestUtils.getMotionEvent(MotionEvent.ACTION_DOWN, 0, 0);
    multiFingerTapGestureDetector.onTouchEvent(downMotionEvent);
  }

  private void dispatchPointerDown(long delay, MotionEvent previous) {
    try {
      Thread.sleep(delay);
      pointerDownMotionEvent = TestUtils.getMotionEvent(MotionEvent.ACTION_POINTER_DOWN, 0, 0, previous);
      multiFingerTapGestureDetector.onTouchEvent(pointerDownMotionEvent);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  private void dispatchPointerUp(long delay, MotionEvent previous) {
    try {
      Thread.sleep(delay);
      pointerUpMotionEvent = TestUtils.getMotionEvent(MotionEvent.ACTION_POINTER_UP, 0, 0, previous);
      multiFingerTapGestureDetector.onTouchEvent(pointerUpMotionEvent);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  private void dispatchUp(long delay, MotionEvent previous) {
    try {
      Thread.sleep(delay);
      upMotionEvent = TestUtils.getMotionEvent(MotionEvent.ACTION_UP, 0, 0, previous);
      multiFingerTapGestureDetector.onTouchEvent(upMotionEvent);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  private void dispatchMove(long delay, MotionEvent previous) {
    try {
      Thread.sleep(delay);
      moveMotionEvent = TestUtils.getMotionEvent(MotionEvent.ACTION_MOVE, 0, 0, previous);
      multiFingerTapGestureDetector.onTouchEvent(moveMotionEvent);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
