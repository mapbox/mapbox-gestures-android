package com.mapbox.android.gestures;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.animation.Interpolator;

import java.util.Set;

/**
 * Base class for all progressive gesture detectors.
 *
 * @param <L> listener that will be called with gesture events/updates.
 */
@UiThread
public abstract class ProgressiveGesture<L> extends MultiFingerGesture<L> {

  private final Set<Integer> handledTypes = provideHandledTypes();

  private boolean stopOnPointerDown = true;
  private boolean isInProgress;

  VelocityTracker velocityTracker;
  float velocityX;
  float velocityY;
  final ValueAnimator valueAnimator = new ValueAnimator();
  private Interpolator interpolator;

  protected ProgressiveGesture(Context context, AndroidGesturesManager gesturesManager) {
    super(context, gesturesManager);
  }

  @NonNull
  protected abstract Set<Integer> provideHandledTypes();

  @Override
  protected boolean analyzeEvent(MotionEvent motionEvent) {
    if (velocityTracker != null) {
      velocityTracker.addMovement(getCurrentEvent());
    }

    boolean movementHandled = super.analyzeEvent(motionEvent);

    if (!movementHandled) {
      int action = motionEvent.getActionMasked();
      switch (action) {
        case MotionEvent.ACTION_DOWN:
          if (isVelocityAnimating() && stopOnPointerDown) {
            valueAnimator.cancel();
          }
          break;

        case MotionEvent.ACTION_POINTER_DOWN:
          if (velocityTracker != null) {
            velocityTracker.clear();
          }
          break;

        case MotionEvent.ACTION_POINTER_UP:
          if (pointerIdList.size() <= 1 && isInProgress) {
            gestureStopped();
            return true;
          }
          break;

        case MotionEvent.ACTION_CANCEL:
          if (velocityTracker != null) {
            velocityTracker.clear();
          }
          if (isInProgress) {
            gestureStopped();
            return true;
          }
          break;

        default:
          break;
      }
    }

    return movementHandled;
  }

  protected void gestureStarted() {
    isInProgress = true;
    if (velocityTracker == null) {
      velocityTracker = VelocityTracker.obtain();
    }
    reset();
  }

  protected void gestureStopped() {
    isInProgress = false;
    if (velocityTracker != null) {
      velocityTracker.computeCurrentVelocity(1000);
      velocityX = velocityTracker.getXVelocity();
      velocityY = velocityTracker.getYVelocity();
      velocityTracker.recycle();
      velocityTracker = null;
    }
    reset();
  }

  protected boolean isVelocityAnimating() {
    return valueAnimator != null && valueAnimator.isStarted();
  }

  Set<Integer> getHandledTypes() {
    return handledTypes;
  }

  /**
   * Check whether a gesture has started and is in progress.
   *
   * @return true if gesture is in progress, false otherwise.
   */
  public boolean isInProgress() {
    return isInProgress;
  }

  /**
   * Check whether velocity animation should be stopped when new gesture starts. True by default.
   *
   * @return true if animation should stop on pointer down, false otherwise.
   */
  public boolean isStopOnPointerDown() {
    return stopOnPointerDown;
  }

  /**
   * Set whether velocity animation should be stopped when new gesture starts. True by default.
   *
   * @param stopOnPointerDown true if animation should stop on pointer down, false otherwise.
   */
  public void setStopOnPointerDown(boolean stopOnPointerDown) {
    this.stopOnPointerDown = stopOnPointerDown;
  }

  /**
   * Get current interpolator used for velocity animations.
   *
   * @return Currently used interpolator
   */
  public Interpolator getInterpolator() {
    return interpolator;
  }

  /**
   * Set new interpolator used for velocity animations.
   *
   * @param interpolator new interpolator
   */
  public void setInterpolator(Interpolator interpolator) {
    this.interpolator = interpolator;
  }
}
