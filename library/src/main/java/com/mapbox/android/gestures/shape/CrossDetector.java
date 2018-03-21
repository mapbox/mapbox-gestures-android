package com.mapbox.android.gestures.shape;

import android.graphics.Point;
import android.view.MotionEvent;

import java.util.List;

class CrossDetector implements ShapeDetector {

  private float movementBounds;
  private boolean hasHorizontalDash;
  private boolean hasVerticalDash;

  @Override
  public void onDown(MotionEvent motionEvent) {
    // do nothing
  }

  @Override
  public int onUp(MotionEvent motionEvent, List<Point> pointerCoords) {
    boolean verticalDash = !isBeyondVerticalBounds(pointerCoords);
    boolean horizontalDash = !isBeyondHorizontalBounds(pointerCoords);

    // if none fits, abort
    if (!verticalDash && !horizontalDash) {
      clear();
      return ShapeGestureDetector.SHAPE_NONE;
    }

    // if line is duplicated, abort
    if ((hasVerticalDash && verticalDash) || (hasHorizontalDash && horizontalDash)) {
      clear();
      return ShapeGestureDetector.SHAPE_NONE;
    }

    if (!hasVerticalDash) {
      hasVerticalDash = verticalDash;
    }

    if (!hasHorizontalDash) {
      hasHorizontalDash = horizontalDash;
    }

    if (hasVerticalDash && hasHorizontalDash) {
      // success, deliver event
      clear();
      return ShapeGestureDetector.SHAPE_CROSS;
    } else {
      // waiting for the second line
      return ShapeGestureDetector.SHAPE_NONE;
    }
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

  private boolean isBeyondHorizontalBounds(List<Point> pointerCoords) {
    Point firstPoint = pointerCoords.get(0);
    for (Point point : pointerCoords) {
      if (Math.abs(firstPoint.x - point.x) > movementBounds) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void cancel() {
    clear();
  }

  private void clear() {
    hasVerticalDash = false;
    hasHorizontalDash = false;
  }

  float getMovementBounds() {
    return movementBounds;
  }

  void setMovementBounds(float movementBounds) {
    this.movementBounds = movementBounds;
  }
}
