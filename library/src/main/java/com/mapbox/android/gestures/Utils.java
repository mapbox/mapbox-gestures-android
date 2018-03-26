package com.mapbox.android.gestures;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.TypedValue;
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
   * Converts DIP to PX.
   *
   * @param dp initial value
   * @return converted value
   */
  public static float dpToPx(float dp) {
    return dp * Resources.getSystem().getDisplayMetrics().density;
  }

  /**
   * Converts PX to DIP.
   *
   * @param px initial value
   * @return converted value
   */
  public static float pxToDp(float px) {
    return px / Resources.getSystem().getDisplayMetrics().density;
  }

  /**
   * Converts PX to MM (millimeters).
   *
   * @param px      initial value
   * @param context context
   * @return converted value
   */
  public static float pxToMm(final float px, final Context context) {
    final DisplayMetrics dm = context.getResources().getDisplayMetrics();
    return px / TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, 1, dm);
  }
}
