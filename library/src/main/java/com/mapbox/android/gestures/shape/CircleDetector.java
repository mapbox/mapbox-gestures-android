package com.mapbox.android.gestures.shape;

import android.graphics.Point;
import android.view.MotionEvent;

import com.mapbox.android.gestures.Utils;

import java.util.List;

class CircleDetector implements ShapeDetector {

  private float movementBounds;

  @Override
  public void onDown(MotionEvent motionEvent) {
    // do nothing
  }

  @Override
  public int onUp(MotionEvent motionEvent, List<Point> pointerCoords) {
    Point centerPoint = determineCenterPoint(pointerCoords);

    // calculating distance to the first point which will serve as a reference
    float distance = Utils.calcualteRelativeDistance(pointerCoords.get(0), centerPoint);

    if (verifyPointsWithinBounds(pointerCoords, centerPoint, distance, movementBounds)
      && isClosed(pointerCoords, movementBounds)) {
      return ShapeGestureDetector.SHAPE_CIRCLE;
    } else {
      return ShapeGestureDetector.SHAPE_NONE;
    }
  }

  private Point determineCenterPoint(List<Point> pointerCoords) {
    int x = 0;
    int y = 0;
    for (Point point : pointerCoords) {
      x += point.x;
      y += point.y;
    }

    x /= pointerCoords.size();
    y /= pointerCoords.size();

    return new Point(x, y);
  }

  private boolean verifyPointsWithinBounds(List<Point> pointerCoords, Point centerPoint, float desiredDistance,
                                           float bounds) {
    for (Point point : pointerCoords) {
      float distance = Utils.calcualteRelativeDistance(point, centerPoint);
      if (distance < desiredDistance - bounds || distance > desiredDistance + bounds) {
        return false;
      }
    }

    return true;
  }

  private boolean isClosed(List<Point> pointerCoords, float bounds) {
    Point firstPoint = pointerCoords.get(0);
    Point lastPoint = pointerCoords.get(pointerCoords.size() - 1);

    float distance = Utils.calcualteRelativeDistance(firstPoint, lastPoint);
    return distance < bounds;
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
