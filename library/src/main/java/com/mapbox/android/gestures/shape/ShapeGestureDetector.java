package com.mapbox.android.gestures.shape;

import android.content.Context;
import android.graphics.Point;
import android.support.annotation.DimenRes;
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
    SHAPE_DASH,
    SHAPE_CROSS,
    SHAPE_CIRCLE
  })

  public @interface ShapeType {
  }

  public static final int SHAPE_NONE = 0;
  public static final int SHAPE_DASH = 1;
  public static final int SHAPE_CROSS = 2;
  public static final int SHAPE_CIRCLE = 3;

  private float minimumMovementThreshold;

  public final List<ShapeDetector> detectors = new ArrayList<>();
  public final List<Point> motionPoints = new ArrayList<>();

  private final DashDetector dashDetector = new DashDetector();
  private final CrossDetector crossDetector = new CrossDetector();

  /**
   * Listener for shape gesture callbacks.
   */
  public interface OnShapeDetectedListener {
    /**
     * Called whenever shape is detected after pointer left the screen.
     * <p>
     * Available shapes:
     * <br/>
     * {@link #SHAPE_DASH}
     * <br/>
     * {@link #SHAPE_CROSS}
     * <br/>
     * {@link #SHAPE_CIRCLE}
     *
     * @param detector this detector
     * @param shape    one of the shape types
     */
    void onShapeDetected(ShapeGestureDetector detector, @ShapeType int shape);
  }

  public ShapeGestureDetector(Context context, AndroidGesturesManager gesturesManager) {
    super(context, gesturesManager);

    detectors.add(dashDetector);
    detectors.add(crossDetector);
  }

  @Override
  protected boolean analyzeEvent(MotionEvent motionEvent) {
    motionPoints.add(new Point((int) motionEvent.getX(), (int) motionEvent.getY()));
    int action = motionEvent.getActionMasked();
    if (action == MotionEvent.ACTION_DOWN) {
      for (ShapeDetector detector : detectors) {
        detector.onDown(motionEvent);
      }
    } else if (action == MotionEvent.ACTION_UP) {
      for (ShapeDetector detector : detectors) {
        @ShapeType int shape = detector.onUp(motionEvent, motionPoints);
        if (shape != SHAPE_NONE && canExecute(AndroidGesturesManager.GESTURE_TYPE_SHAPE)) {
          listener.onShapeDetected(this, shape);
        }
      }
      motionPoints.clear();
    } else if (action == MotionEvent.ACTION_CANCEL) {
      cancel();
    }

    return true;
  }

  @Override
  protected boolean canExecute(int invokedGestureType) {
    return super.canExecute(invokedGestureType) && minimumMovementThresholdMet();
  }

  private boolean minimumMovementThresholdMet() {
    Point firstPoint = motionPoints.get(0);
    for (Point point : motionPoints) {
      if (Math.abs(firstPoint.x - point.x) >= minimumMovementThreshold
        || Math.abs(firstPoint.y - point.y) >= minimumMovementThreshold) {
        return true;
      }
    }
    return false;
  }

  private void cancel() {
    motionPoints.clear();
    for (ShapeDetector detector : detectors) {
      detector.cancel();
    }
  }

  /**
   * Get minimum position change in pixels required to consider shape gesture.
   *
   * @return position change threshold
   */
  public float getMinimumMovementThreshold() {
    return minimumMovementThreshold;
  }

  /**
   * Set minimum position change in pixels required to consider shape gesture.
   * <p>
   * We encourage to set those values from dimens to accommodate for various screen sizes.
   *
   * @param minimumMovementThreshold position change threshold
   */
  public void setMinimumMovementThreshold(float minimumMovementThreshold) {
    this.minimumMovementThreshold = minimumMovementThreshold;
  }

  /**
   * Set minimum position change in dp required to consider shape gesture.
   *
   * @param minimumMovementThreshold position change threshold
   */
  public void setMinimumMovementThresholdResource(@DimenRes int minimumMovementThreshold) {
    setMinimumMovementThreshold(context.getResources().getDimension(minimumMovementThreshold));
  }

  /**
   * Get maximum movement in pixels in vertical axis (calculated from average) allowed to accept dash shape.
   *
   * @return vertical threshold
   */
  public float getDashMovementBounds() {
    return dashDetector.getMovementBounds();
  }

  /**
   * Set maximum movement in pixels in vertical axis (calculated from average) allowed to accept dash shape.
   * <p>
   * We encourage to set those values from dimens to accommodate for various screen sizes.
   *
   * @param movementBounds vertical movementBounds
   */
  public void setDashMovementBounds(float movementBounds) {
    dashDetector.setMovementBounds(movementBounds);
  }

  /**
   * Set maximum movement in dp in vertical axis (calculated from average) allowed to accept dash shape.
   *
   * @param movementBounds vertical movementBounds
   */
  public void setDashMovementBoundsResource(@DimenRes int movementBounds) {
    setDashMovementBounds(context.getResources().getDimension(movementBounds));
  }

  /**
   * Get maximum movement in pixels in vertical or horizontal axis (calculated from average)
   * allowed to accept cross shape.
   *
   * @return vertical or horizontal threshold
   */
  public float getCrossMovementBounds() {
    return crossDetector.getMovementBounds();
  }

  /**
   * Set maximum movement in pixels in vertical or horizontal axis (calculated from average)
   * allowed to accept cross shape.
   * <p>
   * We encourage to set those values from dimens to accommodate for various screen sizes.
   *
   * @param movementBounds vertical or horizontal movementBounds
   */
  public void setCrossMovementBounds(float movementBounds) {
    crossDetector.setMovementBounds(movementBounds);
  }

  /**
   * Set maximum movement in dp in vertical or horizontal axis (calculated from average) allowed to accept cross shape.
   *
   * @param movementBounds vertical or horizontal movementBounds
   */
  public void setCrossMovementBoundsResource(@DimenRes int movementBounds) {
    setCrossMovementBounds(context.getResources().getDimension(movementBounds));
  }
}
