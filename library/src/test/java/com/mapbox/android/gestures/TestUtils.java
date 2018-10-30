package com.mapbox.android.gestures;

import android.view.MotionEvent;

import org.mockito.Mockito;

public final class TestUtils {

  public static MotionEvent getMotionEvent(int action, float x, float y, MotionEvent previousEvent) {
    long currentTime = System.currentTimeMillis();
    long downTime = previousEvent != null ? previousEvent.getDownTime() : System.currentTimeMillis();

    MotionEvent event = Mockito.mock(MotionEvent.class);
    Mockito.when(event.getDownTime()).thenReturn(downTime);
    Mockito.when(event.getEventTime()).thenReturn(currentTime);
    Mockito.when(event.getActionMasked()).thenReturn(action);
    Mockito.when(event.getX()).thenReturn(x);
    Mockito.when(event.getY()).thenReturn(y);

    int pointerCount = previousEvent != null ? previousEvent.getPointerCount() : 0;
    if (previousEvent != null && previousEvent.getActionMasked() == MotionEvent.ACTION_POINTER_UP) {
      pointerCount--;
    } else if (action == MotionEvent.ACTION_POINTER_DOWN) {
      pointerCount++;
    } else if (action == MotionEvent.ACTION_DOWN) {
      pointerCount = 1;
    }
    Mockito.when(event.getPointerCount()).thenReturn(pointerCount);

    return event;
  }

  public static MotionEvent getMotionEvent(int action, float x, float y) {
    return getMotionEvent(action, x, y, null);
  }
}
