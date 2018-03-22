package com.mapbox.android.gestures;

import android.graphics.Point;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.view.MotionEvent;

public class Utils {

  /**
   * Calculates the center point of the multi finger gesture.
   *
   * @param motionEvent event
   * @return center point of the gesture
   */
  public static PointF determineFocalPoint(@NonNull MotionEvent motionEvent) {
    int pointersCount = motionEvent.getPointerCount();
    float x = 0;
    float y = 0;

    for (int i = 0; i < pointersCount; i++) {
      x += motionEvent.getX(i);
      y += motionEvent.getY(i);
    }

    return new PointF(x / pointersCount, y / pointersCount);
  }

  /**
   * @param event        motion event
   * @param pointerIndex pointer's index
   * @return rawY for a pointer
   * @author Almer Thie (code.almeros.com)
   * <p>
   * MotionEvent has no getRawY(int) method; simulate it pending future API approval.
   */
  public static float getRawX(MotionEvent event, int pointerIndex) {
    float offset = event.getRawX() - event.getX();
    if (pointerIndex < event.getPointerCount()) {
      return event.getX(pointerIndex) + offset;
    }
    return 0.0f;
  }

  /**
   * @param event        motion event
   * @param pointerIndex pointer's index
   * @return rawX for a pointer
   * @author Almer Thie (code.almeros.com)
   * <p>
   * MotionEvent has no getRawX(int) method; simulate it pending future API approval.
   */
  public static float getRawY(MotionEvent event, int pointerIndex) {
    float offset = event.getRawY() - event.getY();
    if (pointerIndex < event.getPointerCount()) {
      return event.getY(pointerIndex) + offset;
    }
    return 0.0f;
  }

  /**
   * Returns relative distance in pixels between two points on screen.
   *
   * @param point1 first point
   * @param point2 second point
   * @return relative distance in pixels
   */
  public static float calcualteRelativeDistance(@NonNull Point point1, @NonNull Point point2) {
    float diffX = point2.x - point1.x;
    float diffY = point2.y - point1.y;
    return (float) Math.sqrt(diffX * diffX + diffY * diffY);
  }
}
