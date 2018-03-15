package com.mapbox.android.gestures;

import android.view.MotionEvent;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static com.mapbox.android.gestures.TestUtils.getMotionEvent;
import static org.mockito.Mockito.spy;

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
    Assert.assertTrue(
      String.format("Expected %d pointers, was %d.", expected, pointersCount),
      pointersCount == expected
    );
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
}
