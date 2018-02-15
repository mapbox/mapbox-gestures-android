package com.mapbox.android.gestures;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
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

  VelocityTracker velocityTracker;
  float velocityX;
  float velocityY;

  protected ProgressiveGesture(Context context, AndroidGesturesManager gesturesManager) {
    super(context, gesturesManager);
  }

  @NonNull
  protected abstract Set<Integer> provideHandledTypes();

  @Override
  protected boolean analyzeEvent(MotionEvent motionEvent) {
    if (!isEnabled()) {
      gestureStopped();
    }

    if (velocityTracker != null) {
      velocityTracker.addMovement(getCurrentEvent());
    }

    boolean movementHandled = super.analyzeEvent(motionEvent);

    if (!movementHandled) {
      int action = motionEvent.getActionMasked();
      switch (action) {
        case MotionEvent.ACTION_DOWN:
        case MotionEvent.ACTION_POINTER_DOWN:

          if (velocityTracker != null) {
            velocityTracker.clear();
          }
          break;

        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_POINTER_UP:
          if (pointerIdList.size() < getRequiredPointersCount() && isInProgress) {
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
}
