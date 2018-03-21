package com.mapbox.android.gestures.shape;

import android.content.Context;
import android.graphics.Point;
import android.support.annotation.IntDef;
import android.support.annotation.UiThread;
import android.view.MotionEvent;

import com.mapbox.android.gestures.AndroidGesturesManager;
import com.mapbox.android.gestures.BaseGesture;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

@UiThread
public class ShapeGestureDetector extends BaseGesture<ShapeGestureDetector.OnShapeDetectedListener> {

  @Retention(RetentionPolicy.SOURCE)
  @IntDef( {
    SHAPE_NONE,
    SHAPE_PLUS_SIGN,
    SHAPE_MINUS_SIGN,
    SHAPE_CIRCLE
  })

  public @interface ShapeType {
  }

  public static final int SHAPE_NONE = 0;
  public static final int SHAPE_PLUS_SIGN = 1;
  public static final int SHAPE_MINUS_SIGN = 2;
  public static final int SHAPE_CIRCLE = 3;

  public final List<ShapeDetector> detectors = new ArrayList<>();
  public final List<Point> pointerCoords = new ArrayList<>();

  public ShapeGestureDetector(Context context, AndroidGesturesManager gesturesManager) {
    super(context, gesturesManager);

    detectors.add(new MinusSignDetector());
  }

  @Override
  protected boolean analyzeEvent(MotionEvent motionEvent) {
    pointerCoords.add(new Point((int) motionEvent.getX(), (int) motionEvent.getY()));
    int action = motionEvent.getActionMasked();
    if (action == MotionEvent.ACTION_DOWN) {
      for (ShapeDetector detector : detectors) {
        detector.onDown(motionEvent);
      }
    } else if (action == MotionEvent.ACTION_UP) {
      for (ShapeDetector detector : detectors) {
        @ShapeType int shape = detector.onUp(motionEvent, pointerCoords);
        if (shape != SHAPE_NONE && canExecute(AndroidGesturesManager.GESTURE_TYPE_SHAPE)) {
          listener.onShapeDetected(this, shape);
        }
      }
      pointerCoords.clear();
    } else if (action == MotionEvent.ACTION_CANCEL) {
      cancel();
    }

    return true;
  }

  public interface OnShapeDetectedListener {
    void onShapeDetected(ShapeGestureDetector detector, @ShapeType int shape);
  }

  private void cancel() {
    pointerCoords.clear();
    for (ShapeDetector detector : detectors) {
      detector.cancel();
    }
  }
}
