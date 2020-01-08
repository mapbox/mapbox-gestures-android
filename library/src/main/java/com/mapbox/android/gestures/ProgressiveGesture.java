package com.mapbox.android.gestures;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import android.view.MotionEvent;
import android.view.VelocityTracker;

import java.util.Set;

/**
 * Base class for all progressive gesture detectors.
 *
 * @param <L> listener that will be called with gesture events/updates.
 */
@UiThread
public abstract class ProgressiveGesture<L> extends MultiFingerGesture<L> {

  private final Set<Integer> handledTypes = provideHandledTypes();

  private boolean isInProgress;
  private boolean interrupted;

  VelocityTracker velocityTracker;
  float velocityX;
  float velocityY;

  public ProgressiveGesture(Context context, AndroidGesturesManager gesturesManager) {
    super(context, gesturesManager);
  }

  @NonNull
  protected abstract Set<Integer> provideHandledTypes();

  @Override
  protected boolean analyzeEvent(@NonNull MotionEvent motionEvent) {
    int action = motionEvent.getActionMasked();
    if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN
      || action == MotionEvent.ACTION_POINTER_UP
      || action == MotionEvent.ACTION_CANCEL) {
      // configuration changed, reset data
      reset();
    }

    if (interrupted) {
      interrupted = false;
      reset();
      gestureStopped();
    }

    if (velocityTracker != null) {
      velocityTracker.addMovement(getCurrentEvent());
    }

    boolean movementHandled = super.analyzeEvent(motionEvent);

    if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP) {
      if (pointerIdList.size() < getRequiredPointersCount() && isInProgress) {
        gestureStopped();
        return true;
      }
    } else if (action == MotionEvent.ACTION_CANCEL) {
      if (isInProgress) {
        gestureStopped();
        return true;
      }
    }

    return movementHandled;
  }

  protected void gestureStarted() {
    isInProgress = true;
    if (velocityTracker == null) {
      velocityTracker = VelocityTracker.obtain();
    }
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

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    if (!enabled) {
      interrupt();
    }
  }

  /**
   * Interrupt a gesture by stopping it's execution immediately.
   * Forces gesture detector to meet start conditions again in order to resume.
   */
  public void interrupt() {
    if (isInProgress()) {
      this.interrupted = true;
    }
  }
}
