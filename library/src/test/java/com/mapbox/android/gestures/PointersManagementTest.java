package com.mapbox.android.gestures;

import android.view.MotionEvent;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static com.mapbox.android.gestures.TestUtils.getMotionEvent;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class PointersManagementTest extends
  AbstractGestureDetectorTest<StandardScaleGestureDetector,
    StandardScaleGestureDetector.StandardOnScaleGestureListener> {

  @Override
  StandardScaleGestureDetector getDetectorObject() {
    return spy(androidGesturesManager.getStandardScaleGestureDetector());
  }

  private void checkResult(int expected) {
    int pointersCount = androidGesturesManager.getStandardScaleGestureDetector().getPointersCount();
    Assert.assertEquals(
      String.format("Expected %d pointers, was %d.", expected, pointersCount), pointersCount, expected);
  }

  @Test
  public void missingDownTest() {
    MotionEvent pointerDownEvent = getMotionEvent(MotionEvent.ACTION_POINTER_DOWN, 0, 0);
    androidGesturesManager.onTouchEvent(pointerDownEvent);

    checkResult(0);
  }

  @Test
  public void missingUpTest() {
    MotionEvent downEvent = getMotionEvent(MotionEvent.ACTION_DOWN, 0, 0);
    androidGesturesManager.onTouchEvent(downEvent);

    downEvent = getMotionEvent(MotionEvent.ACTION_DOWN, 0, 0, downEvent);
    androidGesturesManager.onTouchEvent(downEvent);

    checkResult(1);
  }

  @Test
  public void missingPointerDownTest() {
    MotionEvent downEvent = getMotionEvent(MotionEvent.ACTION_DOWN, 0, 0);
    androidGesturesManager.onTouchEvent(downEvent);

    MotionEvent pointerDownEvent = getMotionEvent(MotionEvent.ACTION_POINTER_DOWN, 0, 0, downEvent);
    androidGesturesManager.onTouchEvent(pointerDownEvent);

    MotionEvent pointerUpEvent = getMotionEvent(MotionEvent.ACTION_POINTER_UP, 0, 0, pointerDownEvent);
    androidGesturesManager.onTouchEvent(pointerUpEvent);

    pointerUpEvent = getMotionEvent(MotionEvent.ACTION_POINTER_UP, 0, 0, pointerUpEvent);
    androidGesturesManager.onTouchEvent(pointerUpEvent);

    pointerDownEvent = getMotionEvent(MotionEvent.ACTION_POINTER_DOWN, 0, 0, pointerUpEvent);
    androidGesturesManager.onTouchEvent(pointerDownEvent);

    checkResult(0); //expecting 0, because we are waiting for ACTION_DOWN to synchronise again
  }

  @Test
  public void missingPointerUpTest() {
    MotionEvent downEvent = getMotionEvent(MotionEvent.ACTION_DOWN, 0, 0);
    androidGesturesManager.onTouchEvent(downEvent);

    MotionEvent pointerDownEvent = getMotionEvent(MotionEvent.ACTION_POINTER_DOWN, 0, 0, downEvent);
    androidGesturesManager.onTouchEvent(pointerDownEvent);

    pointerDownEvent = getMotionEvent(MotionEvent.ACTION_POINTER_DOWN, 0, 0, pointerDownEvent);
    androidGesturesManager.onTouchEvent(pointerDownEvent);

    MotionEvent pointerUpEvent = getMotionEvent(MotionEvent.ACTION_POINTER_UP, 0, 0, pointerDownEvent);
    androidGesturesManager.onTouchEvent(pointerUpEvent);

    MotionEvent upEvent = getMotionEvent(MotionEvent.ACTION_UP, 0, 0, pointerUpEvent);
    androidGesturesManager.onTouchEvent(upEvent);

    checkResult(0);
  }

  @Test
  public void addingRemovingPointersTest() {
    MotionEvent downEvent = getMotionEvent(MotionEvent.ACTION_DOWN, 0, 0);
    androidGesturesManager.onTouchEvent(downEvent);

    MotionEvent pointerDownEvent = getMotionEvent(MotionEvent.ACTION_POINTER_DOWN, 0, 0, downEvent);
    androidGesturesManager.onTouchEvent(pointerDownEvent);

    pointerDownEvent = getMotionEvent(MotionEvent.ACTION_POINTER_DOWN, 0, 0, pointerDownEvent);
    androidGesturesManager.onTouchEvent(pointerDownEvent);

    pointerDownEvent = getMotionEvent(MotionEvent.ACTION_POINTER_DOWN, 0, 0, pointerDownEvent);
    androidGesturesManager.onTouchEvent(pointerDownEvent);

    checkResult(4);

    MotionEvent pointerUpEvent = getMotionEvent(MotionEvent.ACTION_POINTER_UP, 0, 0, pointerDownEvent);
    androidGesturesManager.onTouchEvent(pointerUpEvent);

    pointerUpEvent = getMotionEvent(MotionEvent.ACTION_POINTER_UP, 0, 0, pointerUpEvent);
    androidGesturesManager.onTouchEvent(pointerUpEvent);

    pointerUpEvent = getMotionEvent(MotionEvent.ACTION_POINTER_UP, 0, 0, pointerUpEvent);
    androidGesturesManager.onTouchEvent(pointerUpEvent);

    MotionEvent upEvent = getMotionEvent(MotionEvent.ACTION_UP, 0, 0, pointerUpEvent);
    androidGesturesManager.onTouchEvent(upEvent);

    checkResult(0);
  }

  @Test
  public void eventCanceledTest() {
    MotionEvent downEvent = getMotionEvent(MotionEvent.ACTION_DOWN, 0, 0);
    androidGesturesManager.onTouchEvent(downEvent);

    MotionEvent pointerDownEvent = getMotionEvent(MotionEvent.ACTION_POINTER_DOWN, 0, 0, downEvent);
    androidGesturesManager.onTouchEvent(pointerDownEvent);

    pointerDownEvent = getMotionEvent(MotionEvent.ACTION_POINTER_DOWN, 0, 0, pointerDownEvent);
    androidGesturesManager.onTouchEvent(pointerDownEvent);

    MotionEvent pointerUpEvent = getMotionEvent(MotionEvent.ACTION_POINTER_UP, 0, 0, pointerDownEvent);
    androidGesturesManager.onTouchEvent(pointerUpEvent);

    MotionEvent cancelEvent = getMotionEvent(MotionEvent.ACTION_CANCEL, 0, 0, pointerUpEvent);
    androidGesturesManager.onTouchEvent(cancelEvent);

    checkResult(0);
  }

  @Test
  public void movingWithMissingPointersTest() {
    MotionEvent downEvent = getMotionEvent(MotionEvent.ACTION_DOWN, 0, 0);
    androidGesturesManager.onTouchEvent(downEvent);

    MotionEvent pointerDownEvent = getMotionEvent(MotionEvent.ACTION_POINTER_DOWN, 0, 0, downEvent);
    androidGesturesManager.onTouchEvent(pointerDownEvent);

    pointerDownEvent = getMotionEvent(MotionEvent.ACTION_POINTER_DOWN, 0, 0, pointerDownEvent);
    androidGesturesManager.onTouchEvent(pointerDownEvent);

    MotionEvent moveEvent = getMotionEvent(MotionEvent.ACTION_MOVE, 0, 0, pointerDownEvent);
    when(moveEvent.getPointerCount()).thenReturn(2);
    androidGesturesManager.onTouchEvent(moveEvent);

    checkResult(0);
  }

  @Test
  public void movingWithTooManyPointersTest() {
    MotionEvent downEvent = getMotionEvent(MotionEvent.ACTION_DOWN, 0, 0);
    androidGesturesManager.onTouchEvent(downEvent);

    MotionEvent pointerDownEvent = getMotionEvent(MotionEvent.ACTION_POINTER_DOWN, 0, 0, downEvent);
    androidGesturesManager.onTouchEvent(pointerDownEvent);

    pointerDownEvent = getMotionEvent(MotionEvent.ACTION_POINTER_DOWN, 0, 0, pointerDownEvent);
    androidGesturesManager.onTouchEvent(pointerDownEvent);

    MotionEvent moveEvent = getMotionEvent(MotionEvent.ACTION_MOVE, 0, 0, pointerDownEvent);
    when(moveEvent.getPointerCount()).thenReturn(4);
    androidGesturesManager.onTouchEvent(moveEvent);

    checkResult(0);
  }

  @Test
  public void movingWithRightAmountOfPointersTest() {
    MotionEvent downEvent = getMotionEvent(MotionEvent.ACTION_DOWN, 0, 0);
    androidGesturesManager.onTouchEvent(downEvent);

    MotionEvent pointerDownEvent = getMotionEvent(MotionEvent.ACTION_POINTER_DOWN, 0, 0, downEvent);
    androidGesturesManager.onTouchEvent(pointerDownEvent);

    pointerDownEvent = getMotionEvent(MotionEvent.ACTION_POINTER_DOWN, 0, 0, pointerDownEvent);
    androidGesturesManager.onTouchEvent(pointerDownEvent);

    MotionEvent moveEvent = getMotionEvent(MotionEvent.ACTION_MOVE, 0, 0, pointerDownEvent);
    when(moveEvent.getPointerCount()).thenReturn(3);
    androidGesturesManager.onTouchEvent(moveEvent);

    checkResult(3);
  }

  @Test
  public void missingPointerUpAndMovementTest() {
    PermittedActionsGuard guard = new PermittedActionsGuard();
    boolean result = guard.isMissingActions(MotionEvent.ACTION_MOVE, 1, 2);
    Assert.assertTrue("Should miss action", result);
  }

  @Test
  public void missingPointerDownAndMovementTest() {
    PermittedActionsGuard guard = new PermittedActionsGuard();
    boolean result = guard.isMissingActions(MotionEvent.ACTION_MOVE, 2, 1);
    Assert.assertTrue("Should miss action", result);
  }

  @Test
  public void noPointersAndMovementTest() {
    PermittedActionsGuard guard = new PermittedActionsGuard();
    boolean result = guard.isMissingActions(MotionEvent.ACTION_MOVE, 1, 0);
    Assert.assertTrue("Should miss action", result);
  }

  @Test
  public void onePointerAndDownTest() {
    PermittedActionsGuard guard = new PermittedActionsGuard();
    boolean result = guard.isMissingActions(MotionEvent.ACTION_DOWN, 0, 1);
    Assert.assertTrue("Should miss action", result);
  }

  @Test
  public void twoPointersAndDownTest() {
    PermittedActionsGuard guard = new PermittedActionsGuard();
    boolean result = guard.isMissingActions(MotionEvent.ACTION_DOWN, 0, 2);
    Assert.assertTrue("Should miss action", result);
  }

  @Test
  public void onePointerAndPointerUpTest() {
    PermittedActionsGuard guard = new PermittedActionsGuard();
    boolean result = guard.isMissingActions(MotionEvent.ACTION_POINTER_UP, 1, 1);
    Assert.assertTrue("Should miss action", result);
  }

  @Test
  public void noPointersAndPointerUpTest() {
    PermittedActionsGuard guard = new PermittedActionsGuard();
    boolean result = guard.isMissingActions(MotionEvent.ACTION_POINTER_DOWN, 2, 0);
    Assert.assertTrue("Should miss action", result);
  }

  @Test
  public void onePointerAndUpTest() {
    PermittedActionsGuard guard = new PermittedActionsGuard();
    boolean result = guard.isMissingActions(MotionEvent.ACTION_UP, 1, 1);
    Assert.assertFalse("Should not miss action", result);
  }
}
