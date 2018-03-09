package com.mapbox.android.gestures;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.VelocityTracker;

import java.util.HashSet;
import java.util.Set;

import static com.mapbox.android.gestures.AndroidGesturesManager.GESTURE_TYPE_SCALE;

/**
 * Detector that wraps {@link ScaleGestureDetector}.
 * <p>
 * To get access to all the methods found in {@link ScaleGestureDetector}
 * use {@link #getUnderlyingScaleGestureDetector()}.
 */
@UiThread
public class StandardScaleGestureDetector extends
  ProgressiveGesture<StandardScaleGestureDetector.StandardOnScaleGestureListener> {
  private static final Set<Integer> handledTypes = new HashSet<>();

  static {
    handledTypes.add(GESTURE_TYPE_SCALE);
  }

  private ScaleGestureDetector scaleGestureDetector;
  ScaleGestureDetector.OnScaleGestureListener innerListener;
  private boolean stopConfirmed;
  private boolean isScalingOut;

  float startSpan;
  float spanDeltaSinceStart;
  private float spanSinceStartThreshold = Constants.DEFAULT_SCALE_SPAN_SINCE_START_THRESHOLD;

  public StandardScaleGestureDetector(Context context, AndroidGesturesManager androidGesturesManager) {
    super(context, androidGesturesManager);

    innerListener = new ScaleGestureDetector.OnScaleGestureListener() {
      @Override
      public boolean onScale(ScaleGestureDetector detector) {
        return innerOnScale(detector);
      }

      @Override
      public boolean onScaleBegin(ScaleGestureDetector detector) {
        return innerOnScaleBegin(detector);
      }

      @Override
      public void onScaleEnd(final ScaleGestureDetector detector) {
        innerOnScaleEnd(detector);
      }
    };

    scaleGestureDetector = new ScaleGestureDetector(context, innerListener);
  }

  boolean innerOnScale(ScaleGestureDetector detector) {
    if (startSpan == 0) {
      startSpan = detector.getCurrentSpan();
    }

    spanDeltaSinceStart = Math.abs(startSpan - detector.getCurrentSpan());

    // If we can execute but haven't started immediately because there is a threshold as well, check it
    if (!isInProgress() && canExecute(GESTURE_TYPE_SCALE) && spanDeltaSinceStart >= spanSinceStartThreshold) {
      if (listener.onScaleBegin(StandardScaleGestureDetector.this)) {
        gestureStarted();
        return true;
      } else {
        return false;
      }
    }

    if (isInProgress()) {
      isScalingOut = detector.getScaleFactor() < 1.0f;
      return listener.onScale(StandardScaleGestureDetector.this);
    }

    return true;
  }

  boolean innerOnScaleBegin(ScaleGestureDetector detector) {
    startSpan = detector.getCurrentSpan();
    if (canExecute(GESTURE_TYPE_SCALE)) {
      // Obtaining velocity animator to start gathering gestures for short, quick movements
      velocityTracker = VelocityTracker.obtain();

      // If scale can execute and there is no threshold, start gesture
      if (spanSinceStartThreshold == 0) {
        if (listener.onScaleBegin(StandardScaleGestureDetector.this)) {
          gestureStarted();
        }
      }

      return true;
    }
    return false;
  }

  void innerOnScaleEnd(ScaleGestureDetector detector) {
    stopConfirmed = true;
    gestureStopped();
  }

  @Override
  protected boolean analyzeEvent(MotionEvent motionEvent) {
    super.analyzeEvent(motionEvent);
    return scaleGestureDetector.onTouchEvent(motionEvent);
  }

  @Override
  protected void gestureStopped() {
    if (!isInProgress()) {
      // Cleaning up resources after a gesture that did not exceed the threshold
      super.gestureStopped();
      return;
    }

    if (stopConfirmed) {
      super.gestureStopped();
      listener.onScaleEnd(StandardScaleGestureDetector.this, velocityX, velocityY);
      stopConfirmed = false;
    }
  }

  @Override
  public void interrupt() {
    super.interrupt();
    stopConfirmed = true;
  }

  @NonNull
  @Override
  protected Set<Integer> provideHandledTypes() {
    return handledTypes;
  }

  public interface StandardOnScaleGestureListener {
    /**
     * You can retrieve the base {@link ScaleGestureDetector} via {@link #getUnderlyingScaleGestureDetector()}.
     *
     * @param detector this detector
     * @see android.view.ScaleGestureDetector.OnScaleGestureListener#onScaleBegin(ScaleGestureDetector)
     */
    boolean onScaleBegin(StandardScaleGestureDetector detector);

    /**
     * You can retrieve the base {@link ScaleGestureDetector} via {@link #getUnderlyingScaleGestureDetector()}.
     *
     * @param detector this detector
     * @see android.view.ScaleGestureDetector.OnScaleGestureListener#onScale(ScaleGestureDetector)
     */
    boolean onScale(StandardScaleGestureDetector detector);

    /**
     * You can retrieve the base {@link ScaleGestureDetector} via {@link #getUnderlyingScaleGestureDetector()}.
     *
     * @param detector  this detector
     * @param velocityX velocityX of the gesture in the moment of lifting the fingers
     * @param velocityY velocityY of the gesture in the moment of lifting the fingers
     * @see android.view.ScaleGestureDetector.OnScaleGestureListener#onScaleEnd(ScaleGestureDetector)
     */
    void onScaleEnd(StandardScaleGestureDetector detector, float velocityX, float velocityY);
  }

  public static class SimpleStandardOnScaleGestureListener implements StandardOnScaleGestureListener {

    @Override
    public boolean onScaleBegin(StandardScaleGestureDetector detector) {
      return true;
    }

    @Override
    public boolean onScale(StandardScaleGestureDetector detector) {
      return false;
    }

    @Override
    public void onScaleEnd(StandardScaleGestureDetector detector, float velocityX, float velocityY) {
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
   * Get the underlying {@link ScaleGestureDetector}.
   *
   * @return underlying {@link ScaleGestureDetector}
   */
  public ScaleGestureDetector getUnderlyingScaleGestureDetector() {
    return scaleGestureDetector;
  }

  /**
   * Get the threshold span between initial fingers position and current needed
   * for this detector to qualify it as a scale gesture.
   *
   * @return span threshold
   */
  public float getSpanSinceStartThreshold() {
    return spanSinceStartThreshold;
  }

  /**
   * Set the threshold span between initial fingers position and current needed
   * for this detector to qualify it as a scale gesture.
   *
   * @param spanSinceStartThreshold delta span threshold
   */
  public void setSpanSinceStartThreshold(float spanSinceStartThreshold) {
    this.spanSinceStartThreshold = spanSinceStartThreshold;
  }

  /**
   * Get the default threshold span between initial fingers position and current needed
   * for this detector to qualify it as a scale gesture.
   *
   * @return default span threshold
   * @see Constants#DEFAULT_SCALE_SPAN_SINCE_START_THRESHOLD
   */
  public float getDefaultSpanSinceStartThreshold() {
    return Constants.DEFAULT_SCALE_SPAN_SINCE_START_THRESHOLD;
  }

  /**
   * @see ScaleGestureDetector#getScaleFactor()
   */
  public float getScaleFactor() {
    return scaleGestureDetector.getScaleFactor();
  }
}
