package com.mapbox.android.gestures;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SidewaysShoveGestureDetectorTest extends
  AbstractGestureDetectorTest<SidewaysShoveGestureDetector,
    SidewaysShoveGestureDetector.OnSidewaysShoveGestureListener> {

  @Override
  SidewaysShoveGestureDetector getDetectorObject() {
    return spy(androidGesturesManager.getSidewaysShoveGestureDetector());
  }

  @Test
  public void analyzeMovementTest() throws Exception {
    when(listener.onSidewaysShoveBegin(gestureDetector)).thenReturn(true);
    when(listener.onSidewaysShove(
      gestureDetector, gestureDetector.deltaPixelSinceLast, gestureDetector.deltaPixelsSinceStart)
    ).thenReturn(true);
    doReturn(true).when(gestureDetector).isAngleAcceptable();

    // threshold not met
    doReturn(gestureDetector.getPixelDeltaThreshold() / 2)
      .when(gestureDetector).calculateDeltaPixelsSinceLast();
    gestureDetector.analyzeMovement();

    // threshold met, starting
    doReturn(gestureDetector.getPixelDeltaThreshold() * 2)
      .when(gestureDetector).calculateDeltaPixelsSinceLast();
    gestureDetector.analyzeMovement();

    // new event, executing onShove()
    gestureDetector.analyzeMovement();
    verify(listener, times(1)).onSidewaysShove(gestureDetector,
      gestureDetector.deltaPixelSinceLast, gestureDetector.deltaPixelsSinceStart);

    // new event, executing onShove() even though below threshold because already started
    doReturn(gestureDetector.getPixelDeltaThreshold() / 2)
      .when(gestureDetector).calculateDeltaPixelsSinceLast();
    gestureDetector.analyzeMovement();
    //still 1 invocation because parameters changed, but technically 2
    verify(listener, times(1)).onSidewaysShove(gestureDetector,
      gestureDetector.deltaPixelSinceLast, gestureDetector.deltaPixelsSinceStart);

    // angle exceeded don't execute
    doReturn(false)
      .when(gestureDetector).isAngleAcceptable();
    gestureDetector.analyzeMovement();

    // angle acceptable again
    doReturn(true)
      .when(gestureDetector).isAngleAcceptable();
    gestureDetector.analyzeMovement();
    // 1 invocation because parameters changed, but technically 3
    verify(listener, times(1)).onSidewaysShove(gestureDetector,
      gestureDetector.deltaPixelSinceLast, gestureDetector.deltaPixelsSinceStart);

    // stopping
    gestureDetector.gestureStopped();

    // not starting because threshold not met
    gestureDetector.analyzeMovement();

    // threshold met again, starting
    doReturn(gestureDetector.getPixelDeltaThreshold() * 2)
      .when(gestureDetector).calculateDeltaPixelsSinceLast();
    gestureDetector.analyzeMovement();

    verify(listener, times(2)).onSidewaysShoveBegin(gestureDetector);
    verify(listener, times(1)).onSidewaysShoveEnd(
      gestureDetector, gestureDetector.velocityX, gestureDetector.velocityY);
  }

  @Test
  public void isAngleAcceptableTest() {
    gestureDetector.pointerIdList.add(0);
    gestureDetector.pointerIdList.add(1);
    gestureDetector.pointersDistanceMap.put(
      new PointerDistancePair(0, 1),
      new MultiFingerDistancesObject(0, 300, 15, 275)
    );
    assertTrue(gestureDetector.isAngleAcceptable());

    gestureDetector.pointerIdList.clear();
    gestureDetector.pointersDistanceMap.clear();
    gestureDetector.pointerIdList.add(0);
    gestureDetector.pointerIdList.add(1);
    gestureDetector.pointersDistanceMap.put(
      new PointerDistancePair(0, 1),
      new MultiFingerDistancesObject(0, 300, 150, 275)
    );
    assertFalse(gestureDetector.isAngleAcceptable());
  }
}