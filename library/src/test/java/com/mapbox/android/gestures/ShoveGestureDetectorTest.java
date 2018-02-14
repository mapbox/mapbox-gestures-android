package com.mapbox.android.gestures;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ShoveGestureDetectorTest extends
  AbstractGestureDetectorTest<ShoveGestureDetector, ShoveGestureDetector.OnShoveGestureListener> {

  @Override
  ShoveGestureDetector getDetectorObject() {
    return spy(new ShoveGestureDetector(context, androidGesturesManager));
  }

  @Test
  public void analyzeMovement() throws Exception {
    when(listener.onShoveBegin(gestureDetector)).thenReturn(true);
    when(listener.onShove(gestureDetector, gestureDetector.deltaPixelSinceLast, gestureDetector.deltaPixelsSinceStart))
      .thenReturn(true);

    doReturn(true)
      .when(gestureDetector).isAngleAcceptable();

    // threshold not met
    doReturn(Constants.DEFAULT_SHOVE_PIXEL_THRESHOLD / 2)
      .when(gestureDetector).getDeltaPixelsSinceLast();
    gestureDetector.analyzeMovement();
    verify(listener, times(0)).onShoveBegin(gestureDetector);
    verify(listener, times(0)).onShove(gestureDetector, 0, 0);
    verify(listener, times(0)).onShoveEnd(gestureDetector);

    // threshold met, starting
    doReturn(Constants.DEFAULT_SHOVE_PIXEL_THRESHOLD * 2)
      .when(gestureDetector).getDeltaPixelsSinceLast();
    gestureDetector.analyzeMovement();
    verify(listener, times(1)).onShoveBegin(gestureDetector);
    verify(listener, times(0)).onShove(gestureDetector, 0, 0);
    verify(listener, times(0)).onShoveEnd(gestureDetector);

    // new event, executing onShove()
    gestureDetector.analyzeMovement();
    verify(listener, times(1)).onShoveBegin(gestureDetector);
    verify(listener, times(1)).onShove(gestureDetector,
      gestureDetector.deltaPixelSinceLast, gestureDetector.deltaPixelsSinceStart);
    verify(listener, times(0)).onShoveEnd(gestureDetector);

    // new event, executing onShove() even though below threshold because already started
    doReturn(Constants.DEFAULT_SHOVE_PIXEL_THRESHOLD / 2)
      .when(gestureDetector).getDeltaPixelsSinceLast();
    gestureDetector.analyzeMovement();
    verify(listener, times(1)).onShoveBegin(gestureDetector);
    //still 1 invocation because parameters changed, but technically 2
    verify(listener, times(1)).onShove(gestureDetector,
      gestureDetector.deltaPixelSinceLast, gestureDetector.deltaPixelsSinceStart);
    verify(listener, times(0)).onShoveEnd(gestureDetector);

    // angle exceeded don't execute
    doReturn(false)
      .when(gestureDetector).isAngleAcceptable();
    gestureDetector.analyzeMovement();
    verify(listener, times(1)).onShoveBegin(gestureDetector);
    //still 1 invocation because parameters changed, but technically 2
    verify(listener, times(1)).onShove(gestureDetector,
      gestureDetector.deltaPixelSinceLast, gestureDetector.deltaPixelsSinceStart);
    verify(listener, times(0)).onShoveEnd(gestureDetector);

    // angle acceptable again
    doReturn(true)
      .when(gestureDetector).isAngleAcceptable();
    gestureDetector.analyzeMovement();
    verify(listener, times(1)).onShoveBegin(gestureDetector);
    // 1 invocation because parameters changed, but technically 3
    verify(listener, times(1)).onShove(gestureDetector,
      gestureDetector.deltaPixelSinceLast, gestureDetector.deltaPixelsSinceStart);
    verify(listener, times(0)).onShoveEnd(gestureDetector);

    // stopping
    gestureDetector.gestureStopped();
    verify(listener, times(1)).onShoveBegin(gestureDetector);
    verify(listener, times(1)).onShoveEnd(gestureDetector);

    // not starting because threshold not met
    gestureDetector.analyzeMovement();
    verify(listener, times(1)).onShoveBegin(gestureDetector);
    verify(listener, times(1)).onShoveEnd(gestureDetector);

    // threshold met again, starting
    doReturn(Constants.DEFAULT_SHOVE_PIXEL_THRESHOLD * 2)
      .when(gestureDetector).getDeltaPixelsSinceLast();
    gestureDetector.analyzeMovement();
    verify(listener, times(2)).onShoveBegin(gestureDetector);
    verify(listener, times(1)).onShoveEnd(gestureDetector);
  }

  @Test
  public void isAngleAcceptableTest() {
    gestureDetector.pointerIdList.add(0);
    gestureDetector.pointerIdList.add(1);
    gestureDetector.pointersDistanceMap.put(
      new PointerDistancePair(0, 1),
      new MultiFingerDistancesObject(300, 0, 275, 15)
    );
    assertTrue(gestureDetector.isAngleAcceptable());

    gestureDetector.pointerIdList.clear();
    gestureDetector.pointersDistanceMap.clear();
    gestureDetector.pointerIdList.add(0);
    gestureDetector.pointerIdList.add(1);
    gestureDetector.pointersDistanceMap.put(
      new PointerDistancePair(0, 1),
      new MultiFingerDistancesObject(300, 0, 275, 150)
    );
    assertFalse(gestureDetector.isAngleAcceptable());
  }
}