package com.mapbox.android.gestures;

import android.content.Context;
import android.support.annotation.UiThread;
import android.view.MotionEvent;

import java.util.Set;

/**
 * Base class for all of the gesture detectors.
 *
 * @param <L> listener that will be called with gesture events/updates.
 */
@UiThread
public abstract class BaseGesture<L> {
  protected final Context context;
  private final AndroidGesturesManager gesturesManager;
  private MotionEvent currentEvent;
  private MotionEvent previousEvent;
  private long gestureDuration;
  private boolean isEnabled = true;

  /**
   * Listener that will be called with gesture events/updates.
   */
  protected L listener;

  public BaseGesture(Context context, AndroidGesturesManager gesturesManager) {
    this.context = context;
    this.gesturesManager = gesturesManager;
  }

  protected boolean onTouchEvent(MotionEvent motionEvent) {
    return analyze(motionEvent);
  }

  private boolean analyze(MotionEvent motionEvent) {
    if (motionEvent == null) {
      return false;
    }

    if (previousEvent != null) {
      previousEvent.recycle();
      previousEvent = null;
    }

    if (currentEvent != null) {
      previousEvent = MotionEvent.obtain(currentEvent);
      currentEvent.recycle();
      currentEvent = null;
    }

    currentEvent = MotionEvent.obtain(motionEvent);
    gestureDuration = currentEvent.getEventTime() - currentEvent.getDownTime();

    return analyzeEvent(motionEvent);
  }

  protected abstract boolean analyzeEvent(MotionEvent motionEvent);

  protected boolean canExecute(@AndroidGesturesManager.GestureType int invokedGestureType) {
    if (listener == null || !isEnabled) {
      return false;
    }

    for (Set<Integer> exclusives : gesturesManager.getMutuallyExclusiveGestures()) {
      if (exclusives.contains(invokedGestureType)) {
        for (@AndroidGesturesManager.GestureType int gestureType : exclusives) {
          for (BaseGesture detector : gesturesManager.getDetectors()) {
            if (detector instanceof ProgressiveGesture) {
              ProgressiveGesture progressiveDetector = (ProgressiveGesture) detector;
              if (progressiveDetector.getHandledTypes().contains(gestureType)
                && progressiveDetector.isInProgress()) {
                return false;
              }
            }
          }
        }
      }
    }

    return true;
  }

  protected void setListener(L listener) {
    this.listener = listener;
  }

  protected void removeListener() {
    listener = null;
  }

  /**
   * Returns a difference in millis between {@link MotionEvent#getDownTime()} and {@link MotionEvent#getEventTime()}
   * (most recent event's time) associated with this gesture.
   * <p>
   * This is a duration of the user's total interaction with the touch screen,
   * accounting for the time before the gesture was recognized by the detector.
   *
   * @return duration of the gesture in millis.
   */
  public long getGestureDuration() {
    return gestureDuration;
  }

  /**
   * Returns most recent event in this gesture chain.
   *
   * @return most recent event
   */
  public MotionEvent getCurrentEvent() {
    return currentEvent;
  }

  /**
   * Returns previous event in this gesture chain.
   *
   * @return previous event
   */
  public MotionEvent getPreviousEvent() {
    return previousEvent;
  }

  /**
   * Check whether this detector accepts and analyzes motion events. Default is true.
   * @return true if it analyzes, false otherwise
   */
  public boolean isEnabled() {
    return isEnabled;
  }

  /**
   * Set whether this detector should accept and analyze motion events. Default is true.
   * @param enabled true if it should analyze, false otherwise
   */
  public void setEnabled(boolean enabled) {
    isEnabled = enabled;
  }
}
