package com.mapbox.android.gestures;

import android.content.Context;
import android.support.annotation.DimenRes;
import android.support.annotation.UiThread;
import android.view.MotionEvent;

import java.util.HashMap;

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
  private long multiFingerTapTimeThreshold;

  /**
   * Maximum movement in pixels allowed for any finger before rejecting this gesture.
   */
  private float multiFingerTapMovementThreshold;
  private boolean invalidMovement;
  private boolean pointerLifted;
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
        if (pointerLifted) {
          invalidMovement = true;
        }
        lastPointersDownCount = pointerIdList.size();
        break;

      case MotionEvent.ACTION_POINTER_UP:
        pointerLifted = true;
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
        if (invalidMovement) {
          break;
        }
        invalidMovement = exceededMovementThreshold(pointersDistanceMap);
        break;

      default:
        break;
    }

    return false;
  }

  boolean exceededMovementThreshold(HashMap<PointerDistancePair, MultiFingerDistancesObject> map) {
    for (MultiFingerDistancesObject distancesObject : map.values()) {
      float diffX = Math.abs(distancesObject.getCurrFingersDiffX() - distancesObject.getPrevFingersDiffX());
      float diffY = Math.abs(distancesObject.getCurrFingersDiffY() - distancesObject.getPrevFingersDiffY());

      invalidMovement = diffX > multiFingerTapMovementThreshold || diffY > multiFingerTapMovementThreshold;

      if (invalidMovement) {
        return true;
      }
    }

    return false;
  }

  @Override
  protected boolean canExecute(int invokedGestureType) {
    return lastPointersDownCount > 1 && !invalidMovement && getGestureDuration() < multiFingerTapTimeThreshold
      && super.canExecute(invokedGestureType);
  }

  @Override
  protected void reset() {
    super.reset();
    lastPointersDownCount = 0;
    invalidMovement = false;
    pointerLifted = false;
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
   * Get maximum movement allowed for any finger before rejecting this gesture.
   *
   * @return movement threshold in pixels.
   */
  public float getMultiFingerTapMovementThreshold() {
    return multiFingerTapMovementThreshold;
  }

  /**
   * Set maximum movement allowed in pixels for any finger before rejecting this gesture.
   * <p>
   * We encourage to set those values from dimens to accommodate for various screen sizes.
   *
   * @param multiFingerTapMovementThreshold movement threshold.
   */
  public void setMultiFingerTapMovementThreshold(float multiFingerTapMovementThreshold) {
    this.multiFingerTapMovementThreshold = multiFingerTapMovementThreshold;
  }

  /**
   * Set maximum movement allowed in dp for any finger before rejecting this gesture.
   *
   * @param multiFingerTapMovementThresholdDimen movement threshold.
   */
  public void setMultiFingerTapMovementThresholdResource(@DimenRes int multiFingerTapMovementThresholdDimen) {
    setMultiFingerTapMovementThreshold(context.getResources().getDimension(multiFingerTapMovementThresholdDimen));
  }
}
