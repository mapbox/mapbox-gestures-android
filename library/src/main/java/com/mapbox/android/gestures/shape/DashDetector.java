package com.mapbox.android.gestures.shape;

import android.graphics.Point;
import android.view.MotionEvent;

import java.util.List;

class DashDetector implements ShapeDetector {

  private float movementBounds;

  @Override
  public void onDown(MotionEvent motionEvent) {
    // do nothing
  }

  @Override
  public int onUp(MotionEvent motionEvent, List<Point> pointerCoords) {
    if (isBeyondVerticalBounds(pointerCoords)) {
      return ShapeGestureDetector.SHAPE_NONE;
    }

    return ShapeGestureDetector.SHAPE_DASH;
  }

  private boolean isBeyondVerticalBounds(List<Point> pointerCoords) {
    Point firstPoint = pointerCoords.get(0);
    for (Point point : pointerCoords) {
      if (Math.abs(firstPoint.y - point.y) > movementBounds) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void cancel() {
    // do nothing
  }

  float getMovementBounds() {
    return movementBounds;
  }

  void setMovementBounds(float movementBounds) {
    this.movementBounds = movementBounds;
  }
}
