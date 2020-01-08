package com.mapbox.android.gestures;

import android.content.Context;
import android.graphics.PointF;
import androidx.annotation.DimenRes;
import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.core.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;

import java.util.HashSet;
import java.util.Set;

import static com.mapbox.android.gestures.AndroidGesturesManager.GESTURE_TYPE_QUICK_SCALE;
import static com.mapbox.android.gestures.AndroidGesturesManager.GESTURE_TYPE_SCALE;

/**
 * Gesture detector handling scale gesture.
 */
@UiThread
public class StandardScaleGestureDetector extends
  ProgressiveGesture<StandardScaleGestureDetector.StandardOnScaleGestureListener> {
  private static final Set<Integer> handledTypes = new HashSet<>();

  static {
    handledTypes.add(GESTURE_TYPE_SCALE);
    handledTypes.add(GESTURE_TYPE_QUICK_SCALE);
  }

  private static final float QUICK_SCALE_MULTIPLIER = 0.5f;

  private final GestureDetectorCompat innerGestureDetector;

  private boolean quickScale;
  private PointF quickScaleFocalPoint;
  private float startSpan;
  private float startSpanX;
  private float startSpanY;
  private float currentSpan;
  private float currentSpanX;
  private float currentSpanY;
  private float previousSpan;
  private float previousSpanX;
  private float previousSpanY;
  private float spanDeltaSinceStart;
  private float spanSinceStartThreshold;

  private boolean isScalingOut;
  private float scaleFactor;

  public StandardScaleGestureDetector(Context context, AndroidGesturesManager androidGesturesManager) {
    super(context, androidGesturesManager);
    GestureDetector.OnGestureListener doubleTapEventListener = new GestureDetector.SimpleOnGestureListener() {
      @Override
      public boolean onDoubleTapEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
          quickScale = true;
          quickScaleFocalPoint = new PointF(event.getX(), event.getY());
        }
        return true;
      }
    };
    innerGestureDetector = new GestureDetectorCompat(context, doubleTapEventListener);
  }

  @Override
  protected boolean analyzeMovement() {
    super.analyzeMovement();

    if (isInProgress() && quickScale && getPointersCount() > 1) {
      // additional pointer has been placed during quick scale
      // abort and start a traditional pinch next
      gestureStopped();
      return false;
    }

    PointF focal = quickScale ? quickScaleFocalPoint : getFocalPoint();

    currentSpanX = 0;
    currentSpanY = 0;
    for (int i = 0; i < getPointersCount(); i++) {
      currentSpanX += Math.abs(getCurrentEvent().getX(i) - focal.x);
      currentSpanY += Math.abs(getCurrentEvent().getY(i) - focal.y);
    }
    currentSpanX *= 2;
    currentSpanY *= 2;

    if (quickScale) {
      currentSpan = currentSpanY;
    } else {
      currentSpan = (float) Math.hypot(currentSpanX, currentSpanY);
    }

    if (startSpan == 0) {
      startSpan = currentSpan;
      startSpanX = currentSpanX;
      startSpanY = currentSpanY;
    }

    spanDeltaSinceStart = Math.abs(startSpan - currentSpan);

    scaleFactor = calculateScaleFactor();
    isScalingOut = scaleFactor < 1f;

    boolean handled = false;
    if (isInProgress() && currentSpan > 0) {
      handled = listener.onScale(this);
    } else if (canExecute(quickScale ? GESTURE_TYPE_QUICK_SCALE : GESTURE_TYPE_SCALE)
      && (spanDeltaSinceStart >= spanSinceStartThreshold)) {
      handled = listener.onScaleBegin(this);
      if (handled) {
        gestureStarted();
      }
    }
    previousSpan = currentSpan;
    previousSpanX = currentSpanX;
    previousSpanY = currentSpanY;
    return handled;
  }

  @Override
  protected void gestureStopped() {
    super.gestureStopped();
    listener.onScaleEnd(StandardScaleGestureDetector.this, velocityX, velocityY);
    quickScale = false;
  }

  @Override
  protected void reset() {
    super.reset();
    startSpan = 0;
    spanDeltaSinceStart = 0;
    currentSpan = 0;
    previousSpan = 0;
    scaleFactor = 1f;
  }

  @Override
  protected boolean analyzeEvent(@NonNull MotionEvent motionEvent) {
    int action = motionEvent.getActionMasked();

    if (quickScale) {
      if (action == MotionEvent.ACTION_POINTER_DOWN || action == MotionEvent.ACTION_CANCEL) {
        if (isInProgress()) {
          interrupt();
        } else {
          // since the double tap has been registered and canceled but the gesture wasn't started,
          // we need to mark it manually
          quickScale = false;
        }
      } else if (!isInProgress() && action == MotionEvent.ACTION_UP) {
        // if double tap has been registered but the threshold was not met and gesture is not in progress,
        // we need to manually mark the finish of a double tap
        quickScale = false;
      }
    }

    boolean handled = super.analyzeEvent(motionEvent);
    return handled | innerGestureDetector.onTouchEvent(motionEvent);
  }

  @Override
  protected int getRequiredPointersCount() {
    if (isInProgress()) {
      return quickScale ? 1 : 2;
    } else {
      return 1;
    }
  }

  @Override
  protected boolean isSloppyGesture() {
    // do not accept move events if there are less than 2 pointers and we are not quick scaling
    return super.isSloppyGesture() || (!quickScale && getPointersCount() < 2);
  }

  @NonNull
  @Override
  protected Set<Integer> provideHandledTypes() {
    return handledTypes;
  }

  /**
   * Listener for scale gesture callbacks.
   */
  public interface StandardOnScaleGestureListener {
    /**
     * Indicates that the scale gesture started.
     *
     * @param detector this detector
     * @return true if you want to receive subsequent {@link #onScale(StandardScaleGestureDetector)} callbacks,
     * false if you want to ignore this gesture.
     */
    boolean onScaleBegin(@NonNull StandardScaleGestureDetector detector);

    /**
     * Called for every scale change during the gesture.
     *
     * @param detector this detector
     * @return Whether or not the detector should consider this event as handled.
     */
    boolean onScale(@NonNull StandardScaleGestureDetector detector);

    /**
     * Indicates that the scale gesture ended.
     *
     * @param detector  this detector
     * @param velocityX velocityX of the gesture in the moment of lifting the fingers
     * @param velocityY velocityY of the gesture in the moment of lifting the fingers
     */
    void onScaleEnd(@NonNull StandardScaleGestureDetector detector, float velocityX, float velocityY);
  }

  public static class SimpleStandardOnScaleGestureListener implements StandardOnScaleGestureListener {

    @Override
    public boolean onScaleBegin(@NonNull StandardScaleGestureDetector detector) {
      return true;
    }

    @Override
    public boolean onScale(@NonNull StandardScaleGestureDetector detector) {
      return false;
    }

    @Override
    public void onScaleEnd(@NonNull StandardScaleGestureDetector detector, float velocityX, float velocityY) {
      // No implementation
    }
  }

  /**
   * Check whether user is scaling out.
   *
   * @return true if user is scaling out, false if user is scaling in
   */
  public boolean isScalingOut() {
    return isScalingOut;
  }

  /**
   * Get the threshold span in pixels between initial fingers position and current needed
   * for this detector to qualify it as a scale gesture.
   *
   * @return span threshold
   */
  public float getSpanSinceStartThreshold() {
    return spanSinceStartThreshold;
  }

  /**
   * Set the threshold span in pixels between initial fingers position and current needed
   * for this detector to qualify it as a scale gesture.
   * <p>
   * We encourage to set those values from dimens to accommodate for various screen sizes.
   *
   * @param spanSinceStartThreshold delta span threshold
   */
  public void setSpanSinceStartThreshold(float spanSinceStartThreshold) {
    this.spanSinceStartThreshold = spanSinceStartThreshold;
  }

  /**
   * Set the threshold span in dp between initial fingers position and current needed
   * for this detector to qualify it as a scale gesture.
   *
   * @param spanSinceStartThresholdDimen delta span threshold
   */
  public void setSpanSinceStartThresholdResource(@DimenRes int spanSinceStartThresholdDimen) {
    setSpanSinceStartThreshold(context.getResources().getDimension(spanSinceStartThresholdDimen));
  }

  /**
   * @return Scale factor.
   */
  public float getScaleFactor() {
    return scaleFactor;
  }

  /**
   * Return the average distance between each of the pointers forming the
   * gesture in progress through the focal point, when the gesture was started.
   *
   * @return Distance between pointers in pixels.
   */
  public float getStartSpan() {
    return startSpan;
  }

  /**
   * Return the average X distance between each of the pointers forming the
   * gesture in progress through the focal point, when the gesture was started.
   *
   * @return Distance between pointers in pixels.
   */
  public float getStartSpanX() {
    return startSpanX;
  }

  /**
   * Return the average Y distance between each of the pointers forming the
   * gesture in progress through the focal point, when the gesture was started.
   *
   * @return Distance between pointers in pixels.
   */
  public float getStartSpanY() {
    return startSpanY;
  }

  /**
   * Return the average distance between each of the pointers forming the
   * gesture in progress through the focal point.
   *
   * @return Distance between pointers in pixels.
   */
  public float getCurrentSpan() {
    return currentSpan;
  }

  /**
   * Return the average X distance between each of the pointers forming the
   * gesture in progress through the focal point.
   *
   * @return Distance between pointers in pixels.
   */
  public float getCurrentSpanX() {
    return currentSpanX;
  }

  /**
   * Return the average Y distance between each of the pointers forming the
   * gesture in progress through the focal point.
   *
   * @return Distance between pointers in pixels.
   */
  public float getCurrentSpanY() {
    return currentSpanY;
  }

  /**
   * Return the previous average distance between each of the pointers forming the
   * gesture in progress through the focal point.
   *
   * @return Previous distance between pointers in pixels.
   */
  public float getPreviousSpan() {
    return previousSpan;
  }

  /**
   * Return the previous average X distance between each of the pointers forming the
   * gesture in progress through the focal point.
   *
   * @return Previous distance between pointers in pixels.
   */
  public float getPreviousSpanX() {
    return previousSpanX;
  }

  /**
   * Return the previous average Y distance between each of the pointers forming the
   * gesture in progress through the focal point.
   *
   * @return Previous distance between pointers in pixels.
   */
  public float getPreviousSpanY() {
    return previousSpanY;
  }

  private float calculateScaleFactor() {
    if (quickScale) {
      final boolean scaleOut =
        // below focal point moving up
        getCurrentEvent().getY() < quickScaleFocalPoint.y && currentSpan < previousSpan
          // above focal point moving up
          || getCurrentEvent().getY() > quickScaleFocalPoint.y && currentSpan > previousSpan;
      final float spanDiff = Math.abs(1 - (currentSpan / previousSpan)) * QUICK_SCALE_MULTIPLIER;
      return previousSpan <= 0 ? 1 : scaleOut ? (1 + spanDiff) : (1 - spanDiff);
    } else {
      return previousSpan > 0 ? currentSpan / previousSpan : 1;
    }
  }
}
