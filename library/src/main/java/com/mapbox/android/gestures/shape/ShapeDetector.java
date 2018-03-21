package com.mapbox.android.gestures.shape;

import android.graphics.Point;
import android.view.MotionEvent;

import java.util.List;

public interface ShapeDetector {
  void onDown(MotionEvent motionEvent);

  @ShapeGestureDetector.ShapeType
  int onUp(MotionEvent motionEvent, List<Point> pointerCoords);

  void cancel();
}
