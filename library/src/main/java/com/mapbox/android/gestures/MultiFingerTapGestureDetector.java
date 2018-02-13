package com.mapbox.android.gestures;

import android.content.Context;
import android.support.annotation.UiThread;
import android.view.MotionEvent;

import static com.mapbox.android.gestures.AndroidGesturesManager.GESTURE_TYPE_MULTI_FINGER_TAP;

/**
 * Gesture detector handling multi tap gesture.
 */
@UiThread
public class MultiFingerTapGestureDetector extends
  MultiFingerGesture<MultiFingerTapGestureDetector.OnMultiFingerTapGestureListener> {

  /**
   * Maximum time in millis to lift the fingers to register a tap event.
   */
  private long multiFingerTapTimeThreshold = Constants.DEFAULT_MULTI_TAP_TIME_THRESHOLD;

  /**
   * Maximum movement in pixels allowed for any finger before rejecting this gesture.
   */
  private float multiFingerTapMovementThreshold = Constants.DEFAULT_MULTI_TAP_MOVEMENT_THRESHOLD;
  private boolean movementOccurred;
  private int lastPointersDownCount;

  public MultiFingerTapGestureDetector(Context context, AndroidGesturesManager gesturesManager) {
    super(context, gesturesManager);
  }

  public interface OnMultiFingerTapGestureListener {
    boolean onMultiFingerTap(MultiFingerTapGestureDetector detector, int pointersCount);
  }

  @Override
  protected boolean analyzeEvent(MotionEvent motionEvent) {
    super.analyzeEvent(motionEvent);

    int action = motionEvent.getActionMasked();
    switch (action) {
      case MotionEvent.ACTION_POINTER_DOWN:
        lastPointersDownCount = pointerIdList.size();
        break;

      case MotionEvent.ACTION_UP:
        boolean canExecute = canExecute(GESTURE_TYPE_MULTI_FINGER_TAP);
        boolean handled = false;
        if (canExecute) {
          handled = listener.onMultiFingerTap(this, lastPointersDownCount);
        }
        reset();
        return handled;

      case MotionEvent.ACTION_MOVE:
        if (movementOccurred) {
          break;
        }

        for (MultiFingerDistancesObject distancesObject : pointersDistanceMap.values()) {
          float diffX = Math.abs(distancesObject.getCurrFingersDiffX() - distancesObject.getPrevFingersDiffX());
          float diffY = Math.abs(distancesObject.getCurrFingersDiffY() - distancesObject.getPrevFingersDiffY());

          movementOccurred = diffX > multiFingerTapMovementThreshold || diffY > multiFingerTapMovementThreshold;

          if (movementOccurred) {
            break;
          }
        }
        break;

      default:
        break;
    }

    return false;
  }

  @Override
  protected boolean canExecute(int invokedGestureType) {
    return lastPointersDownCount > 1 && !movementOccurred && getGestureDuration() < multiFingerTapTimeThreshold
      && super.canExecute(invokedGestureType);
  }

  @Override
  protected void reset() {
    super.reset();
    lastPointersDownCount = 0;
    movementOccurred = false;
  }

  /**
   * Get maximum time in millis that fingers can have a contact with the screen before rejecting this gesture.
   *
   * @return maximum touch time for tap gesture.
   */
  public long getMultiFingerTapTimeThreshold() {
    return multiFingerTapTimeThreshold;
  }

  /**
   * Set maximum time in millis that fingers can have a contact with the screen before rejecting this gesture.
   *
   * @param multiFingerTapTimeThreshold maximum touch time for tap gesture.
   */
  public void setMultiFingerTapTimeThreshold(long multiFingerTapTimeThreshold) {
    this.multiFingerTapTimeThreshold = multiFingerTapTimeThreshold;
  }

  /**
   * Get default maximum time in millis that fingers can have a contact with the screen before rejecting this gesture.
   *
   * @return default maximum touch time for tap gesture.
   */
  public long getDefaultMultiFingerTapTimeThreshold() {
    return Constants.DEFAULT_MULTI_TAP_TIME_THRESHOLD;
  }

  /**
   * Get maximum movement allowed for any finger before rejecting this gesture.
   *
   * @return movement threshold in pixels.
   */
  public float getMultiFingerTapMovementThreshold() {
    return multiFingerTapMovementThreshold;
  }

  /**
   * Get maximum movement allowed for any finger before rejecting this gesture.
   *
   * @param multiFingerTapMovementThreshold movement threshold in pixels.
   */
  public void setMultiFingerTapMovementThreshold(float multiFingerTapMovementThreshold) {
    this.multiFingerTapMovementThreshold = multiFingerTapMovementThreshold;
  }

  /**
   * Get default maximum movement allowed for any finger before rejecting this gesture.
   *
   * @return default movement threshold in pixels.
   */
  public float getDefaultMultiFingerTapMovementThreshold() {
    return Constants.DEFAULT_MULTI_TAP_MOVEMENT_THRESHOLD;
  }
}
