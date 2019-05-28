package com.mapbox.android.gestures;

import android.graphics.Point;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.test.espresso.InjectEventSecurityException;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.matcher.ViewMatchers;
import android.view.MotionEvent;
import android.view.View;

import org.hamcrest.Matcher;

public class UiTestUtils {

  public static ViewAction pinchOut() {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return ViewMatchers.isEnabled();
      }

      @Override
      public String getDescription() {
        return "Pinch out";
      }

      @Override
      public void perform(UiController uiController, View view) {
        Point middlePosition = getCenterPoint(view);

        final int startDelta = 0;
        final int endDelta = 500;

        Point startPoint1 = new Point(middlePosition.x - startDelta, middlePosition.y);
        Point startPoint2 = new Point(middlePosition.x + startDelta, middlePosition.y);
        Point endPoint1 = new Point(middlePosition.x - endDelta, middlePosition.y);
        Point endPoint2 = new Point(middlePosition.x + endDelta, middlePosition.y);

        performPinch(uiController, startPoint1, startPoint2, endPoint1, endPoint2);
      }
    };
  }

  public static ViewAction pinchIn() {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return ViewMatchers.isEnabled();
      }

      @Override
      public String getDescription() {
        return "Pinch in";
      }

      @Override
      public void perform(UiController uiController, View view) {
        Point middlePosition = getCenterPoint(view);

        final int startDelta = 500;
        final int endDelta = 0;

        Point startPoint1 = new Point(middlePosition.x - startDelta, middlePosition.y);
        Point startPoint2 = new Point(middlePosition.x + startDelta, middlePosition.y);
        Point endPoint1 = new Point(middlePosition.x - endDelta, middlePosition.y);
        Point endPoint2 = new Point(middlePosition.x + endDelta, middlePosition.y);

        performPinch(uiController, startPoint1, startPoint2, endPoint1, endPoint2);
      }
    };
  }

  @NonNull
  private static Point getCenterPoint(View view) {
    int[] locationOnScreen = new int[2];
    view.getLocationOnScreen(locationOnScreen);
    float viewHeight = view.getHeight() * view.getScaleY();
    float viewWidth = view.getWidth() * view.getScaleX();
    return new Point(
      (int) (locationOnScreen[0] + viewWidth / 2),
      (int) (locationOnScreen[1] + viewHeight / 2));
  }

  // https://stackoverflow.com/a/46443628/9126211
  private static void performPinch(UiController uiController, Point startPoint1, Point startPoint2, Point endPoint1,
                                   Point endPoint2) {
    final int duration = 500;
    final long eventMinInterval = 10;
    final long startTime = SystemClock.uptimeMillis();
    long eventTime = startTime;
    MotionEvent event;
    float eventX1;
    float eventY1;
    float eventX2;
    float eventY2;

    eventX1 = startPoint1.x;
    eventY1 = startPoint1.y;
    eventX2 = startPoint2.x;
    eventY2 = startPoint2.y;

    // Specify the property for the two touch points
    MotionEvent.PointerProperties[] properties = new MotionEvent.PointerProperties[2];
    MotionEvent.PointerProperties pp1 = new MotionEvent.PointerProperties();
    pp1.id = 0;
    pp1.toolType = MotionEvent.TOOL_TYPE_FINGER;
    MotionEvent.PointerProperties pp2 = new MotionEvent.PointerProperties();
    pp2.id = 1;
    pp2.toolType = MotionEvent.TOOL_TYPE_FINGER;

    properties[0] = pp1;
    properties[1] = pp2;

    // Specify the coordinations of the two touch points
    // NOTE: you MUST set the pressure and size value, or it doesn't work
    MotionEvent.PointerCoords[] pointerCoords = new MotionEvent.PointerCoords[2];
    MotionEvent.PointerCoords pc1 = new MotionEvent.PointerCoords();
    pc1.x = eventX1;
    pc1.y = eventY1;
    pc1.pressure = 1;
    pc1.size = 1;
    MotionEvent.PointerCoords pc2 = new MotionEvent.PointerCoords();
    pc2.x = eventX2;
    pc2.y = eventY2;
    pc2.pressure = 1;
    pc2.size = 1;
    pointerCoords[0] = pc1;
    pointerCoords[1] = pc2;

    /*
     * Events sequence of zoom gesture:
     *
     * 1. Send ACTION_DOWN event of one start point
     * 2. Send ACTION_POINTER_DOWN of two start points
     * 3. Send ACTION_MOVE of two middle points
     * 4. Repeat step 3 with updated middle points (x,y), until reach the end points
     * 5. Send ACTION_POINTER_UP of two end points
     * 6. Send ACTION_UP of one end point
     */

    try {
      // Step 1
      event = MotionEvent.obtain(startTime, eventTime,
        MotionEvent.ACTION_DOWN, 1, properties,
        pointerCoords, 0, 0, 1, 1, 0, 0, 0, 0);
      injectMotionEventToUiController(uiController, event);

      // Step 2
      event = MotionEvent.obtain(startTime, eventTime,
        MotionEvent.ACTION_POINTER_DOWN + (pp2.id << MotionEvent.ACTION_POINTER_INDEX_SHIFT), 2,
        properties, pointerCoords, 0, 0, 1, 1, 0, 0, 0, 0);
      injectMotionEventToUiController(uiController, event);

      // Step 3, 4
      long moveEventNumber = duration / eventMinInterval;

      float stepX1;
      float stepY1;
      float stepX2;
      float stepY2;

      stepX1 = (endPoint1.x - startPoint1.x) / moveEventNumber;
      stepY1 = (endPoint1.y - startPoint1.y) / moveEventNumber;
      stepX2 = (endPoint2.x - startPoint2.x) / moveEventNumber;
      stepY2 = (endPoint2.y - startPoint2.y) / moveEventNumber;

      for (int i = 0; i < moveEventNumber; i++) {
        // Update the move events
        eventTime += eventMinInterval;
        eventX1 += stepX1;
        eventY1 += stepY1;
        eventX2 += stepX2;
        eventY2 += stepY2;

        pc1.x = eventX1;
        pc1.y = eventY1;
        pc2.x = eventX2;
        pc2.y = eventY2;

        pointerCoords[0] = pc1;
        pointerCoords[1] = pc2;

        event = MotionEvent.obtain(startTime, eventTime,
          MotionEvent.ACTION_MOVE, 2, properties,
          pointerCoords, 0, 0, 1, 1, 0, 0, 0, 0);
        injectMotionEventToUiController(uiController, event);
      }

      // Step 5
      pc1.x = endPoint1.x;
      pc1.y = endPoint1.y;
      pc2.x = endPoint2.x;
      pc2.y = endPoint2.y;
      pointerCoords[0] = pc1;
      pointerCoords[1] = pc2;

      eventTime += eventMinInterval;
      event = MotionEvent.obtain(startTime, eventTime,
        MotionEvent.ACTION_POINTER_UP + (pp2.id << MotionEvent.ACTION_POINTER_INDEX_SHIFT), 2, properties,
        pointerCoords, 0, 0, 1, 1, 0, 0, 0, 0);
      injectMotionEventToUiController(uiController, event);

      // Step 6
      eventTime += eventMinInterval;
      event = MotionEvent.obtain(startTime, eventTime,
        MotionEvent.ACTION_UP, 1, properties,
        pointerCoords, 0, 0, 1, 1, 0, 0, 0, 0);
      injectMotionEventToUiController(uiController, event);
    } catch (InjectEventSecurityException ex) {
      throw new RuntimeException("Could not perform pinch", ex);
    }
  }

  /**
   * Safely call uiController.injectMotionEvent(event): Detect any error and "convert" it to an
   * IllegalStateException
   */
  private static void injectMotionEventToUiController(UiController uiController, MotionEvent event)
    throws InjectEventSecurityException {
    boolean injectEventSucceeded = uiController.injectMotionEvent(event);
    if (!injectEventSucceeded) {
      throw new IllegalStateException("Error performing event " + event);
    }
  }
}
