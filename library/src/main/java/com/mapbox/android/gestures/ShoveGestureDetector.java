package com.mapbox.android.gestures;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

import java.util.HashSet;
import java.util.Set;

import static com.mapbox.android.gestures.AndroidGesturesManager.GESTURE_TYPE_SHOVE;

/**
 * Gesture detector handling shove gesture.
 */
@UiThread
public class ShoveGestureDetector extends ProgressiveGesture<ShoveGestureDetector.OnShoveGestureListener> {
  private static final Set<Integer> handledTypes = new HashSet<>();

  static {
    handledTypes.add(GESTURE_TYPE_SHOVE);
  }

  private float maxShoveAngle = Constants.DEFAULT_SHOVE_MAX_ANGLE;
  private float pixelDeltaThreshold = Constants.DEFAULT_SHOVE_PIXEL_THRESHOLD;
  float deltaPixelsSinceStart;
  float deltaPixelSinceLast;

  public ShoveGestureDetector(Context context, AndroidGesturesManager gesturesManager) {
    super(context, gesturesManager);
  }

  @NonNull
  @Override
  protected Set<Integer> provideHandledTypes() {
    return handledTypes;
  }

  public interface OnShoveGestureListener {
    /**
     * Indicates that the shove gesture started.
     *
     * @param detector this detector
     * @return true if you want to receive subsequent {@link #onShove(ShoveGestureDetector, float, float)} callbacks,
     * false if you want to ignore this gesture.
     */
    boolean onShoveBegin(ShoveGestureDetector detector);

    /**
     * Called for every shove change during the gesture.
     *
     * @param detector              this detector
     * @param deltaPixelsSinceLast  pixels delta change since the last call
     * @param deltaPixelsSinceStart pixels delta change since the start of the gesture
     * @return true if the gesture was handled, false otherwise
     */
    boolean onShove(ShoveGestureDetector detector, float deltaPixelsSinceLast, float deltaPixelsSinceStart);

    /**
     * Indicates that the shove gesture ended.
     *
     * @param velocityX velocityX of the gesture in the moment of lifting the fingers
     * @param velocityY velocityY of the gesture in the moment of lifting the fingers
     * @param detector this detector
     */
    void onShoveEnd(ShoveGestureDetector detector, float velocityX, float velocityY);
  }

  public static class SimpleOnShoveGestureListener implements OnShoveGestureListener {
    @Override
    public boolean onShoveBegin(ShoveGestureDetector detector) {
      return true;
    }

    @Override
    public boolean onShove(ShoveGestureDetector detector, float deltaPixelsSinceLast, float deltaPixelsSinceStart) {
      return false;
    }

    @Override
    public void onShoveEnd(ShoveGestureDetector detector, float velocityX, float velocityY) {
      // No Implementation
    }
  }

  @Override
  protected boolean analyzeMovement() {
    super.analyzeMovement();

    deltaPixelSinceLast = getDeltaPixelsSinceLast();
    deltaPixelsSinceStart += deltaPixelSinceLast;

    if (isInProgress() && deltaPixelSinceLast != 0) {
      return listener.onShove(this, deltaPixelSinceLast, deltaPixelsSinceStart);
    } else if (canExecute(GESTURE_TYPE_SHOVE)) {
      if (listener.onShoveBegin(this)) {
        gestureStarted();
        return true;
      }
    }

    return false;
  }

  @Override
  protected boolean canExecute(int invokedGestureType) {
    return Math.abs(deltaPixelsSinceStart) > pixelDeltaThreshold
      && super.canExecute(invokedGestureType);
  }

  @Override
  protected boolean isSloppyGesture() {
    return super.isSloppyGesture() || !isAngleAcceptable();
  }

  @Override
  protected void gestureStopped() {
    super.gestureStopped();
    listener.onShoveEnd(this, velocityX, velocityY);
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

    return angle <= maxShoveAngle || 180f - angle <= maxShoveAngle;
  }

  float getDeltaPixelsSinceLast() {
    float py0 = getPreviousEvent().getY(getPreviousEvent().findPointerIndex(pointerIdList.get(0)));
    float py1 = getPreviousEvent().getY(getPreviousEvent().findPointerIndex(pointerIdList.get(1)));
    float prevAverageY = (py0 + py1) / 2.0f;

    float cy0 = getCurrentEvent().getY(getCurrentEvent().findPointerIndex(pointerIdList.get(0)));
    float cy1 = getCurrentEvent().getY(getCurrentEvent().findPointerIndex(pointerIdList.get(1)));
    float currAverageY = (cy0 + cy1) / 2.0f;

    return currAverageY - prevAverageY;
  }

  /**
   * Get the delta pixel threshold required to qualify it as a shove gesture.
   *
   * @return delta pixel threshold
   */
  public float getPixelDeltaThreshold() {
    return pixelDeltaThreshold;
  }

  /**
   * Set the delta pixel threshold required to qualify it as a shove gesture.
   *
   * @param pixelDeltaThreshold delta pixel threshold
   */
  public void setPixelDeltaThreshold(float pixelDeltaThreshold) {
    this.pixelDeltaThreshold = pixelDeltaThreshold;
  }

  /**
   * Get the default delta pixel threshold required to qualify it as a shove gesture.
   *
   * @return delta pixel threshold
   * @see Constants#DEFAULT_SHOVE_PIXEL_THRESHOLD
   */
  public float getDefaultPixelDeltaThreshold() {
    return Constants.DEFAULT_SHOVE_PIXEL_THRESHOLD;
  }

  /**
   * Get the maximum allowed angle between fingers to qualify it as a shove gesture.
   *
   * @return maximum allowed angle
   */
  public float getMaxShoveAngle() {
    return maxShoveAngle;
  }

  /**
   * Set the maximum allowed angle between fingers to qualify it as a shove gesture.
   *
   * @param maxShoveAngle maximum allowed angle
   */
  public void setMaxShoveAngle(float maxShoveAngle) {
    this.maxShoveAngle = maxShoveAngle;
  }

  /**
   * Get the default maximum allowed angle between fingers to qualify it as a shove gesture.
   *
   * @return default maximum allowed angle
   * @see Constants#DEFAULT_SHOVE_MAX_ANGLE
   */
  public float getDefaultMaxShoveAngle() {
    return Constants.DEFAULT_SHOVE_MAX_ANGLE;
  }
}
