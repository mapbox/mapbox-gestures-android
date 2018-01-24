package com.mapbox.android.gestures;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.VelocityTracker;

import java.util.HashSet;
import java.util.Set;

import static com.mapbox.android.gestures.AndroidGesturesManager.GESTURE_TYPE_SCALE;

/**
 * Detector that wraps {@link ScaleGestureDetector}.
 * <p>
 * To get access to all the methods found in {@link ScaleGestureDetector} use {@link #getUnderlyingScaleGestureDetector()}.
 */
@UiThread
public class StandardScaleGestureDetector extends ProgressiveGesture<StandardScaleGestureDetector.StandardOnScaleGestureListener> {
  private static final Set<Integer> handledTypes = new HashSet<>();

  static {
    handledTypes.add(GESTURE_TYPE_SCALE);
  }

  private ScaleGestureDetector scaleGestureDetector;
  private boolean stopConfirmed;
  private boolean isScalingOut;
  private float scaleVelocityThreshold = 1500f;
  private long maxScaleVelocityAnimationDuration = 1000L;

  private float startSpan;
  private float spanDeltaSinceStart;
  private float spanDeltaThreshold = Constants.DEFAULT_SCALE_SPAN_THRESHOLD;

  public StandardScaleGestureDetector(Context context, AndroidGesturesManager androidGesturesManager) {
    super(context, androidGesturesManager);

    setInterpolator(new FastOutSlowInInterpolator());

    valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator animation) {
        boolean canContinue = listener.scaleVelocityAnimator(StandardScaleGestureDetector.this, velocityX, velocityY, (Float) animation.getAnimatedValue());
        if (!canContinue) {
          animation.cancel();
        }
      }
    });

    valueAnimator.addListener(new AnimatorListenerAdapter() {
      @Override
      public void onAnimationEnd(Animator animation) {
        listener.onScaleEnd(StandardScaleGestureDetector.this);
      }
    });

    ScaleGestureDetector.OnScaleGestureListener innerListener = new ScaleGestureDetector.OnScaleGestureListener() {
      @Override
      public boolean onScale(ScaleGestureDetector detector) {
        spanDeltaSinceStart = Math.abs(startSpan - detector.getCurrentSpan());

        // If we can execute but haven't started immediately because there is a threshold as well, check it
        if (!isInProgress() && spanDeltaSinceStart > spanDeltaThreshold) {
          if (listener.onScaleBegin(StandardScaleGestureDetector.this)) {
            gestureStarted();
            return true;
          } else {
            return false;
          }
        }

        if (isInProgress()) {
          isScalingOut = detector.getScaleFactor() < 1.0f;
          return !isSloppyGesture(getCurrentEvent()) && listener.onScale(StandardScaleGestureDetector.this);
        }

        return true;
      }

      @Override
      public boolean onScaleBegin(ScaleGestureDetector detector) {
        stopConfirmed = false;
        startSpan = detector.getCurrentSpan();
        if (canExecute(GESTURE_TYPE_SCALE)) {
          // Obtaining velocity animator to start gathering gestures for short, quick movements
          velocityTracker = VelocityTracker.obtain();

          // If scale can execute and there is no threshold, start gesture
          if (spanDeltaThreshold == 0) {
            if (listener.onScaleBegin(StandardScaleGestureDetector.this)) {
              gestureStarted();
            }
          }

          return true;
        }
        return false;
      }

      @Override
      public void onScaleEnd(final ScaleGestureDetector detector) {
        stopConfirmed = true;

        if (!isInProgress()) {
          // Invoked when gesture finished but never reached the threshold. Cleaning up the VelocityAnimator
          gestureStopped();
          return;
        }

        gestureStopped();

        float velocityXY = Math.abs(velocityX) + Math.abs(velocityY);
        if (velocityXY < scaleVelocityThreshold) {
          listener.onScaleEnd(StandardScaleGestureDetector.this);
          return;
        }

        float logVelocityXY = (float) Math.log10(velocityXY);
        logVelocityXY = isScalingOut ? -logVelocityXY : logVelocityXY;
        valueAnimator.setFloatValues(1f + logVelocityXY / 100, 1.0f);
        valueAnimator.setDuration(Math.abs((long) Math.min(velocityXY / 7, maxScaleVelocityAnimationDuration)));
        valueAnimator.setInterpolator(getInterpolator());
        valueAnimator.start();
      }
    };

    scaleGestureDetector = new ScaleGestureDetector(context, innerListener);
  }

  @Override
  protected boolean analyzeEvent(MotionEvent motionEvent) {
    super.analyzeEvent(motionEvent);
    return scaleGestureDetector.onTouchEvent(motionEvent);
  }

  @Override
  protected void gestureStopped() {
    if (stopConfirmed) {
      super.gestureStopped();
    }
  }

  @NonNull
  @Override
  protected Set<Integer> provideHandledTypes() {
    return handledTypes;
  }

  public interface StandardOnScaleGestureListener {

    /**
     * Continuous callback after user has ended scale gesture by lifting the fingers.
     * Value animation is based on the velocity of the gesture when it ended and this callback will be invoked for each
     * animation value change until the value animation finishes.
     * <p>
     * {@link #onScaleEnd(StandardScaleGestureDetector)} will not be called until the value animation finishes. You can return false here to end the gesture immediately.
     *
     * @param detector                   this detector
     * @param velocityX                  velocityX of the gesture in the moment of lifting the fingers
     * @param velocityY                  velocityY of the gesture in the moment of lifting the fingers
     * @param scaleVelocityAnimatorValue current animation value of the gesture
     * @return true if you want to receive the rest of the animation callbacks or false to end the rotation gesture immediately.
     */
    boolean scaleVelocityAnimator(StandardScaleGestureDetector detector, float velocityX, float velocityY, float scaleVelocityAnimatorValue);

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
     * @param detector this detector
     * @see android.view.ScaleGestureDetector.OnScaleGestureListener#onScaleBegin(ScaleGestureDetector)
     */
    boolean onScaleBegin(StandardScaleGestureDetector detector);

    /**
     * You can retrieve the base {@link ScaleGestureDetector} via {@link #getUnderlyingScaleGestureDetector()}.
     *
     * @param detector this detector
     * @see android.view.ScaleGestureDetector.OnScaleGestureListener#onScaleEnd(ScaleGestureDetector)
     */
    void onScaleEnd(StandardScaleGestureDetector detector);
  }

  public static class SimpleStandardOnScaleGestureListener implements StandardOnScaleGestureListener {

    @Override
    public boolean onScale(StandardScaleGestureDetector detector) {
      return false;
    }

    @Override
    public boolean onScaleBegin(StandardScaleGestureDetector detector) {
      return true;
    }

    @Override
    public void onScaleEnd(StandardScaleGestureDetector detector) {
      // No implementation
    }

    @Override
    public boolean scaleVelocityAnimator(StandardScaleGestureDetector detector, float velocityX, float velocityY, float scaleVelocityAnimatorValue) {
      return false;
    }
  }

  /**
   * Get minimum XY velocity of the gesture required to start value animation.
   *
   * @return minimum XY velocity to start value animation
   */
  public float getScaleVelocityThreshold() {
    return scaleVelocityThreshold;
  }

  /**
   * Set minimum XY velocity of the gesture required to start value animation.
   *
   * @param scaleVelocityThreshold minimum XY velocity to start value animation
   */
  public void setScaleVelocityThreshold(float scaleVelocityThreshold) {
    this.scaleVelocityThreshold = scaleVelocityThreshold;
  }

  /**
   * Get maximum scale velocity animation duration.
   *
   * @return maximum scale velocity animation duration
   */
  public long getMaxScaleVelocityAnimationDuration() {
    return maxScaleVelocityAnimationDuration;
  }

  /**
   * Set maximum scale velocity animation duration.
   *
   * @param maxScaleVelocityAnimationDuration maximum scale velocity animation duration
   */
  public void setMaxScaleVelocityAnimationDuration(long maxScaleVelocityAnimationDuration) {
    this.maxScaleVelocityAnimationDuration = maxScaleVelocityAnimationDuration;
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
   * Get the threshold delta span between initial fingers position and current needed for this detector to qualify it as a scale gesture.
   *
   * @return delta span threshold
   */
  public float getSpanDeltaThreshold() {
    return spanDeltaThreshold;
  }

  /**
   * Set the threshold delta span between initial fingers position and current needed for this detector to qualify it as a scale gesture.
   *
   * @param spanDeltaThreshold delta span threshold
   */
  public void setSpanDeltaThreshold(float spanDeltaThreshold) {
    this.spanDeltaThreshold = spanDeltaThreshold;
  }

  /**
   * Get the default threshold delta span between initial fingers position and current needed for this detector to qualify it as a scale gesture.
   *
   * @return default delta span threshold
   */
  public float getDefaultSpanDeltaThreshold() {
    return Constants.DEFAULT_SCALE_SPAN_THRESHOLD;
  }

  /**
   * @see ScaleGestureDetector#getScaleFactor()
   */
  public float getScaleFactor() {
    return scaleGestureDetector.getScaleFactor();
  }
}
