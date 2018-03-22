package com.mapbox.android.gestures.shape;

import android.graphics.Point;
import android.view.MotionEvent;

import java.util.List;

// TODO: 22.03.18 they need to intersect
class CrossDetector extends DashDetector {

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

    // if line is duplicated, clear but don't abort
    if ((hasVerticalDash && verticalDash) || (hasHorizontalDash && horizontalDash)) {
      clear();
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
    super.cancel();
    clear();
  }

  private void clear() {
    hasVerticalDash = false;
    hasHorizontalDash = false;
  }
}
