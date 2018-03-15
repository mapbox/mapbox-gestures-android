package com.mapbox.android.gestures;

import android.view.MotionEvent;

import org.junit.Test;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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

    MotionEvent downEvent = TestUtils.getMotionEvent(MotionEvent.ACTION_DOWN, 100, 100);
    MotionEvent moveEvent = TestUtils.getMotionEvent(MotionEvent.ACTION_MOVE, 105, 100, downEvent);
    gestureDetector.onTouchEvent(downEvent);
    gestureDetector.onTouchEvent(moveEvent);
    verify(listener, times(1)).onMoveBegin(gestureDetector);

    moveEvent = TestUtils.getMotionEvent(MotionEvent.ACTION_MOVE, 110, 100, moveEvent);
    gestureDetector.onTouchEvent(moveEvent);
    verify(listener, times(1)).onMove(
      gestureDetector, gestureDetector.lastDistanceX, gestureDetector.lastDistanceY);

    MotionEvent pointerDownEvent = TestUtils.getMotionEvent(MotionEvent.ACTION_POINTER_DOWN, 200, 100, moveEvent);
    gestureDetector.onTouchEvent(pointerDownEvent);
    moveEvent = TestUtils.getMotionEvent(MotionEvent.ACTION_MOVE, 115, 100, downEvent);
    gestureDetector.onTouchEvent(moveEvent);
    // checking for 0, 0 difference because focal point should be reset with pointer count change
    verify(listener, times(1)).onMove(gestureDetector, 0, 0);

    MotionEvent pointerUpEvent = TestUtils.getMotionEvent(MotionEvent.ACTION_POINTER_UP, 200, 100, pointerDownEvent);
    gestureDetector.onTouchEvent(pointerUpEvent);
    MotionEvent upEvent = TestUtils.getMotionEvent(MotionEvent.ACTION_POINTER_UP, 200, 100, pointerUpEvent);
    gestureDetector.onTouchEvent(upEvent);
    verify(listener, times(1)).onMoveEnd(
      gestureDetector, gestureDetector.velocityX, gestureDetector.velocityY
    );
  }
}