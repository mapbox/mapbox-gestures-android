package com.mapbox.android.gestures;

import android.content.Context;
import android.support.annotation.DimenRes;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

import java.util.HashSet;
import java.util.Set;

import static com.mapbox.android.gestures.AndroidGesturesManager.GESTURE_TYPE_SIDEWAYS_SHOVE;

/**
 * Gesture detector handling sideways shove gesture.
 */
@UiThread
public class SidewaysShoveGestureDetector extends
  ProgressiveGesture<SidewaysShoveGestureDetector.OnSidewaysShoveGestureListener> {
  private static final Set<Integer> handledTypes = new HashSet<>();

  static {
    handledTypes.add(GESTURE_TYPE_SIDEWAYS_SHOVE);
  }

  private float maxShoveAngle;
  private float pixelDeltaThreshold;
  float deltaPixelsSinceStart;
  float deltaPixelSinceLast;

  public SidewaysShoveGestureDetector(Context context, AndroidGesturesManager gesturesManager) {
    super(context, gesturesManager);
  }

  @NonNull
  @Override
  protected Set<Integer> provideHandledTypes() {
    return handledTypes;
  }

  /**
   * Listener for sideways shove gesture callbacks.
   */
  public interface OnSidewaysShoveGestureListener {
    /**
     * Indicates that the sideways shove gesture started.
     *
     * @param detector this detector
     * @return true if you want to receive subsequent
     * {@link #onSidewaysShove(SidewaysShoveGestureDetector, float, float)} callbacks,
     * false if you want to ignore this gesture.
     */
    boolean onSidewaysShoveBegin(SidewaysShoveGestureDetector detector);

    /**
     * Called for every sideways shove change during the gesture.
     *
     * @param detector              this detector
     * @param deltaPixelsSinceLast  pixels delta change since the last call
     * @param deltaPixelsSinceStart pixels delta change since the start of the gesture
     * @return true if the gesture was handled, false otherwise
     */
    boolean onSidewaysShove(SidewaysShoveGestureDetector detector, float deltaPixelsSinceLast,
                            float deltaPixelsSinceStart);

    /**
     * Indicates that the sideways shove gesture ended.
     *
     * @param velocityX velocityX of the gesture in the moment of lifting the fingers
     * @param velocityY velocityY of the gesture in the moment of lifting the fingers
     * @param detector  this detector
     */
    void onSidewaysShoveEnd(SidewaysShoveGestureDetector detector, float velocityX, float velocityY);
  }

  public static class SimpleOnSidewaysShoveGestureListener implements OnSidewaysShoveGestureListener {
    @Override
    public boolean onSidewaysShoveBegin(SidewaysShoveGestureDetector detector) {
      return true;
    }

    @Override
    public boolean onSidewaysShove(SidewaysShoveGestureDetector detector, float deltaPixelsSinceLast,
                                   float deltaPixelsSinceStart) {
      return false;
    }

    @Override
    public void onSidewaysShoveEnd(SidewaysShoveGestureDetector detector, float velocityX, float velocityY) {
      // No Implementation
    }
  }

  @Override
  protected boolean analyzeMovement() {
    super.analyzeMovement();

    deltaPixelSinceLast = calculateDeltaPixelsSinceLast();
    deltaPixelsSinceStart += deltaPixelSinceLast;

    if (isInProgress() && deltaPixelSinceLast != 0) {
      return listener.onSidewaysShove(this, deltaPixelSinceLast, deltaPixelsSinceStart);
    } else if (canExecute(GESTURE_TYPE_SIDEWAYS_SHOVE)) {
      if (listener.onSidewaysShoveBegin(this)) {
        gestureStarted();
        return true;
      }
    }

    return false;
  }

  @Override
  protected boolean canExecute(int invokedGestureType) {
    return Math.abs(deltaPixelsSinceStart) >= pixelDeltaThreshold
      && super.canExecute(invokedGestureType);
  }

  @Override
  protected boolean isSloppyGesture() {
    return super.isSloppyGesture() || !isAngleAcceptable();
  }

  @Override
  protected void gestureStopped() {
    super.gestureStopped();
    listener.onSidewaysShoveEnd(this, velocityX, velocityY);
  }

  @Override
  protected void reset() {
    super.reset();
    deltaPixelsSinceStart = 0;
  }

  boolean isAngleAcceptable() {
    MultiFingerDistancesObject distancesObject =
      pointersDistanceMap.get(new PointerDistancePair(pointerIdList.get(0), pointerIdList.get(1)));

    // Takes values from 0 to 180
    double angle = Math.toDegrees(Math.abs(Math.atan2(
      distancesObject.getCurrFingersDiffY(), distancesObject.getCurrFingersDiffX())));

    // Making the axis vertical
    angle = Math.abs(angle - 90);

    return angle <= maxShoveAngle;
  }

  float calculateDeltaPixelsSinceLast() {
    float px0 = getPreviousEvent().getX(getPreviousEvent().findPointerIndex(pointerIdList.get(0)));
    float px1 = getPreviousEvent().getX(getPreviousEvent().findPointerIndex(pointerIdList.get(1)));
    float prevAverageX = (px0 + px1) / 2.0f;

    float cx0 = getCurrentEvent().getX(getCurrentEvent().findPointerIndex(pointerIdList.get(0)));
    float cx1 = getCurrentEvent().getX(getCurrentEvent().findPointerIndex(pointerIdList.get(1)));
    float currAverageX = (cx0 + cx1) / 2.0f;

    return currAverageX - prevAverageX;
  }

  /**
   * Returns horizontal pixel delta change since the start of the gesture.
   *
   * @return pixels delta change since the start of the gesture
   */
  public float getDeltaPixelsSinceStart() {
    return deltaPixelsSinceStart;
  }

  /**
   * Returns last horizontal pixel delta change
   * calculated in {@link OnSidewaysShoveGestureListener#onSidewaysShove(SidewaysShoveGestureDetector, float, float)}.
   *
   * @return pixels delta change since the last call
   */
  public float getDeltaPixelSinceLast() {
    return deltaPixelSinceLast;
  }

  /**
   * Get the delta pixel threshold required to qualify it as a sideways shove gesture.
   *
   * @return delta pixel threshold
   */
  public float getPixelDeltaThreshold() {
    return pixelDeltaThreshold;
  }

  /**
   * Set the delta pixel threshold required to qualify it as a sideways shove gesture.
   * <p>
   * We encourage to set those values from dimens to accommodate for various screen sizes.
   *
   * @param pixelDeltaThreshold delta threshold
   */
  public void setPixelDeltaThreshold(float pixelDeltaThreshold) {
    this.pixelDeltaThreshold = pixelDeltaThreshold;
  }

  /**
   * Set the delta dp threshold required to qualify it as a sideways shove gesture.
   *
   * @param pixelDeltaThresholdDimen delta threshold
   */
  public void setPixelDeltaThresholdResource(@DimenRes int pixelDeltaThresholdDimen) {
    setPixelDeltaThreshold(context.getResources().getDimension(pixelDeltaThresholdDimen));
  }

  /**
   * Get the maximum allowed angle between fingers, measured from the vertical line,
   * to qualify it as a sideways shove gesture.
   *
   * @return maximum allowed angle
   */
  public float getMaxShoveAngle() {
    return maxShoveAngle;
  }

  /**
   * Set the maximum allowed angle between fingers, measured from the vertical line,
   * to qualify it as a sideways shove gesture.
   *
   * @param maxShoveAngle maximum allowed angle
   */
  public void setMaxShoveAngle(float maxShoveAngle) {
    this.maxShoveAngle = maxShoveAngle;
  }
}
