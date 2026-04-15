package com.mapbox.android.gestures;

import android.view.MotionEvent;

import org.junit.Test;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.junit.Assert.assertFalse;

public class MoveGestureDetectorTest extends
  AbstractGestureDetectorTest<MoveGestureDetector, MoveGestureDetector.OnMoveGestureListener> {

  @Override
  MoveGestureDetector getDetectorObject() {
    return spy(androidGesturesManager.getMoveGestureDetector());
  }

  @Test
  public void analyzeMovementTest() {
    doReturn(true).when(listener).onMoveBegin(gestureDetector);
    doReturn(true).when(gestureDetector).checkAnyMoveAboveThreshold();

    gestureDetector.analyzeMovement();
    gestureDetector.analyzeMovement();
    gestureDetector.gestureStopped();

    verify(listener, times(1)).onMoveBegin(gestureDetector);
    verify(listener, times(1)).onMove(gestureDetector, 0, 0);
    verify(listener, times(1)).onMoveEnd(
      gestureDetector, gestureDetector.velocityX, gestureDetector.velocityY);
  }

  @Test
  public void analyzeEventTest() {
    doReturn(true).when(listener).onMoveBegin(gestureDetector);
    doReturn(true).when(gestureDetector).checkAnyMoveAboveThreshold();
    doReturn(true).when(gestureDetector).checkPressure();
    doReturn(false).when(gestureDetector).isSloppyGesture();

    MotionEvent downEvent = TestUtils.INSTANCE.getMotionEvent(MotionEvent.ACTION_DOWN, 100, 100, null);
    gestureDetector.onTouchEvent(downEvent);
    MotionEvent moveEvent = TestUtils.INSTANCE.getMotionEvent(MotionEvent.ACTION_MOVE, 105, 100, downEvent);
    gestureDetector.onTouchEvent(moveEvent);
    verify(listener, times(1)).onMoveBegin(gestureDetector);

    moveEvent = TestUtils.INSTANCE.getMotionEvent(MotionEvent.ACTION_MOVE, 110, 100, moveEvent);
    gestureDetector.onTouchEvent(moveEvent);
    verify(listener, times(1)).onMove(
      gestureDetector, gestureDetector.lastDistanceX, gestureDetector.lastDistanceY);

    MotionEvent pointerDownEvent = TestUtils.INSTANCE.getMotionEvent(MotionEvent.ACTION_POINTER_DOWN, 200, 100,
      moveEvent);
    gestureDetector.onTouchEvent(pointerDownEvent);
    moveEvent = TestUtils.INSTANCE.getMotionEvent(MotionEvent.ACTION_MOVE, 115, 100, pointerDownEvent);
    gestureDetector.onTouchEvent(moveEvent);
    // checking for 0, 0 difference because focal point should be reset with pointer count change
    verify(listener, times(1)).onMove(gestureDetector, 0, 0);

    MotionEvent pointerUpEvent = TestUtils.INSTANCE.getMotionEvent(MotionEvent.ACTION_POINTER_UP, 200, 100, moveEvent);
    gestureDetector.onTouchEvent(pointerUpEvent);
    MotionEvent upEvent = TestUtils.INSTANCE.getMotionEvent(MotionEvent.ACTION_POINTER_UP, 200, 100, pointerUpEvent);
    gestureDetector.onTouchEvent(upEvent);
    verify(listener, times(1)).onMoveEnd(
      gestureDetector, gestureDetector.velocityX, gestureDetector.velocityY
    );
  }

  @Test
  public void multiFingerMoveThreshold_fallsBackToMoveThreshold() {
    doReturn(true).when(listener).onMoveBegin(gestureDetector);
    doReturn(true).when(gestureDetector).checkPressure();
    doReturn(false).when(gestureDetector).isSloppyGesture();
    gestureDetector.setMoveThreshold(10f);

    MotionEvent downEvent = TestUtils.INSTANCE.getMotionEvent(MotionEvent.ACTION_DOWN, 100, 100, null);
    gestureDetector.onTouchEvent(downEvent);
    MotionEvent pointerDownEvent = TestUtils.INSTANCE.getMotionEvent(MotionEvent.ACTION_POINTER_DOWN, 110, 100, downEvent);
    gestureDetector.onTouchEvent(pointerDownEvent);
    MotionEvent moveEvent = TestUtils.INSTANCE.getMotionEvent(MotionEvent.ACTION_MOVE, 122, 100, pointerDownEvent);
    gestureDetector.onTouchEvent(moveEvent);

    verify(listener, times(1)).onMoveBegin(gestureDetector);
  }

  @Test
  public void multiFingerMoveThreshold_overridesMoveThresholdForMultiFingerMoves() {
    doReturn(true).when(listener).onMoveBegin(gestureDetector);
    doReturn(true).when(gestureDetector).checkPressure();
    doReturn(false).when(gestureDetector).isSloppyGesture();
    gestureDetector.setMoveThreshold(10f);
    gestureDetector.setMultiFingerMoveThreshold(30f);

    MotionEvent downEvent = TestUtils.INSTANCE.getMotionEvent(MotionEvent.ACTION_DOWN, 100, 100, null);
    gestureDetector.onTouchEvent(downEvent);
    MotionEvent pointerDownEvent = TestUtils.INSTANCE.getMotionEvent(MotionEvent.ACTION_POINTER_DOWN, 110, 100, downEvent);
    gestureDetector.onTouchEvent(pointerDownEvent);

    MotionEvent moveBelowMultiThreshold = TestUtils.INSTANCE.getMotionEvent(MotionEvent.ACTION_MOVE, 125, 100, pointerDownEvent);
    gestureDetector.onTouchEvent(moveBelowMultiThreshold);
    verify(listener, never()).onMoveBegin(gestureDetector);

    MotionEvent moveAboveMultiThreshold = TestUtils.INSTANCE.getMotionEvent(MotionEvent.ACTION_MOVE, 145, 100, moveBelowMultiThreshold);
    gestureDetector.onTouchEvent(moveAboveMultiThreshold);
    verify(listener, times(1)).onMoveBegin(gestureDetector);
  }

  /**
   * Regression test: moving 400px with 2 fingers should NOT trigger onMoveBegin for single-finger
   * after one finger is lifted, even though 400px exceeds the single-finger threshold (100px).
   * The distances must be reset when the pointer configuration changes.
   */
  @Test
  public void noFalseBeginOnPointerUp_distancesResetAfterLiftingFinger() {
    doReturn(true).when(listener).onMoveBegin(gestureDetector);
    doReturn(true).when(gestureDetector).checkPressure();
    doReturn(false).when(gestureDetector).isSloppyGesture();

    // single-finger threshold: 100px, multi-finger threshold: 500px
    gestureDetector.setMoveThreshold(100f);
    gestureDetector.setMultiFingerMoveThreshold(500f);

    // Put first finger down
    MotionEvent downEvent = TestUtils.INSTANCE.getMotionEvent(MotionEvent.ACTION_DOWN, 100, 100, null);
    gestureDetector.onTouchEvent(downEvent);

    // Put second finger down
    MotionEvent pointerDownEvent = TestUtils.INSTANCE.getMotionEvent(MotionEvent.ACTION_POINTER_DOWN, 200, 100, downEvent);
    gestureDetector.onTouchEvent(pointerDownEvent);

    // Move 400px — below multi-finger threshold (500px), so no onMoveBegin yet
    MotionEvent moveEvent = TestUtils.INSTANCE.getMotionEvent(MotionEvent.ACTION_MOVE, 500, 100, pointerDownEvent);
    gestureDetector.onTouchEvent(moveEvent);
    verify(listener, never()).onMoveBegin(gestureDetector);

    // Lift one finger — the remaining pointer's accumulated distance must be reset.
    // After the pointer-up, checkAnyMoveAboveThreshold() must return false because distances
    // were reset, so a subsequent MOVE event does NOT cross the single-finger threshold.
    MotionEvent pointerUpEvent = TestUtils.INSTANCE.getMotionEvent(MotionEvent.ACTION_POINTER_UP, 500, 100, moveEvent);
    gestureDetector.onTouchEvent(pointerUpEvent);

    // checkAnyMoveAboveThreshold() should now return false since distances were reset
    assertFalse("Distances should be reset after lifting a finger",
            gestureDetector.checkAnyMoveAboveThreshold());
  }

  /**
   * Regression test: moving 400px with 1 finger should NOT trigger onMoveBegin for multi-finger
   * right after a second finger is added, even though 400px exceeds the single-finger threshold.
   * The distances of existing pointers must be reset when a new pointer is added.
   */
  @Test
  public void noFalseBeginOnPointerDown_distancesResetAfterAddingFinger() {
    doReturn(true).when(listener).onMoveBegin(gestureDetector);
    doReturn(true).when(gestureDetector).checkPressure();
    doReturn(false).when(gestureDetector).isSloppyGesture();

    // single-finger threshold: 500px, multi-finger threshold: 100px
    gestureDetector.setMoveThreshold(500f);
    gestureDetector.setMultiFingerMoveThreshold(100f);

    // Put first finger down and move 400px — below single-finger threshold (500px), no begin yet
    MotionEvent downEvent = TestUtils.INSTANCE.getMotionEvent(MotionEvent.ACTION_DOWN, 100, 100, null);
    gestureDetector.onTouchEvent(downEvent);
    MotionEvent moveEvent = TestUtils.INSTANCE.getMotionEvent(MotionEvent.ACTION_MOVE, 500, 100, downEvent);
    gestureDetector.onTouchEvent(moveEvent);
    verify(listener, never()).onMoveBegin(gestureDetector);

    // Add a second finger — existing pointer's accumulated distance must be reset.
    // After the pointer-down, checkAnyMoveAboveThreshold() must return false because distances
    // were reset, so a subsequent MOVE event does NOT immediately cross the multi-finger threshold.
    MotionEvent pointerDownEvent = TestUtils.INSTANCE.getMotionEvent(MotionEvent.ACTION_POINTER_DOWN, 500, 100, moveEvent);
    gestureDetector.onTouchEvent(pointerDownEvent);

    // checkAnyMoveAboveThreshold() should now return false since distances were reset
    assertFalse("Distances should be reset after adding a finger",
            gestureDetector.checkAnyMoveAboveThreshold());
  }
}