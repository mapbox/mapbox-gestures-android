package com.mapbox.android.gestures.shape;

import android.graphics.Point;
import android.view.MotionEvent;

import java.util.List;

public class DashDetector implements ShapeDetector {

  private float verticalThreshold;

  @Override
  public void onDown(MotionEvent motionEvent) {
    // do nothing
  }

  @Override
  public int onUp(MotionEvent motionEvent, List<Point> pointerCoords) {
    Point firstPoint = pointerCoords.get(0);
    for (Point point : pointerCoords) {
      if (Math.abs(firstPoint.y - point.y) > verticalThreshold) {
        return ShapeGestureDetector.SHAPE_NONE;
      }
    }

    return ShapeGestureDetector.SHAPE_DASH;
  }

  @Override
  public void cancel() {
    // do nothing
  }

  float getVerticalThreshold() {
    return verticalThreshold;
  }

  void setVerticalThreshold(float verticalThreshold) {
    this.verticalThreshold = verticalThreshold;
  }
}
