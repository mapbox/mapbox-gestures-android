package com.mapbox.android.gestures;

import android.graphics.PointF;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RotateGestureDetectorTest extends
  AbstractGestureDetectorTest<RotateGestureDetector, RotateGestureDetector.OnRotateGestureListener> {

  @Override
  RotateGestureDetector getDetectorObject() {
    return spy(androidGesturesManager.getRotateGestureDetector());
  }

  @Test
  public void analyzeMovementTest() throws Exception {
    when(listener.onRotateBegin(gestureDetector)).thenReturn(true);
    when(listener.onRotate(gestureDetector, gestureDetector.deltaSinceLast, gestureDetector.deltaSinceStart))
      .thenReturn(true);

    // threshold not met
    doReturn(Constants.DEFAULT_ROTATE_ANGLE_THRESHOLD / 2)
      .when(gestureDetector).getRotationDegreesSinceLast();
    gestureDetector.analyzeMovement();

    // threshold met, starting
    doReturn(Constants.DEFAULT_ROTATE_ANGLE_THRESHOLD * 2)
      .when(gestureDetector).getRotationDegreesSinceLast();
    gestureDetector.analyzeMovement();

    // new event, executing onRotate()
    gestureDetector.analyzeMovement();
    verify(listener, times(1)).onRotate(gestureDetector,
      gestureDetector.deltaSinceLast, gestureDetector.deltaSinceStart);

    // new event, executing onRotate() even though below threshold because already started
    doReturn(Constants.DEFAULT_ROTATE_ANGLE_THRESHOLD / 2)
      .when(gestureDetector).getRotationDegreesSinceLast();
    gestureDetector.analyzeMovement();
    //still 1 invocation because parameters changed, but technically 2
    verify(listener, times(1)).onRotate(gestureDetector,
      gestureDetector.deltaSinceLast, gestureDetector.deltaSinceStart);

    // stopping
    gestureDetector.gestureStopped();

    // not starting because threshold not met
    gestureDetector.analyzeMovement();

    verify(listener, times(1)).onRotateBegin(gestureDetector);
    verify(listener, times(1)).onRotateEnd(
      gestureDetector, gestureDetector.velocityX, gestureDetector.velocityY,
      gestureDetector.calculateAngularVelocityVector(gestureDetector.velocityX, gestureDetector.velocityY));
  }

  @Test
  public void getRotationDegreesSinceLastTest() {
    gestureDetector.pointerIdList.add(0);
    gestureDetector.pointerIdList.add(1);
    gestureDetector.pointersDistanceMap.put(
      new PointerDistancePair(0, 1),
      new MultiFingerDistancesObject(300, 0, 275, 15)
    );

    assertEquals(-3.1221304f, gestureDetector.getRotationDegreesSinceLast());
  }

  @Test
  public void calculateAngularVelocityVectorTest() {
    when(gestureDetector.getFocalPoint()).thenReturn(new PointF(150f, 150f));
    assertEquals(3.6f, gestureDetector.calculateAngularVelocityVector(725, 355));
  }
}