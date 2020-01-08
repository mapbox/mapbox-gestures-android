package com.mapbox.android.gestures;

import android.content.Context;
import androidx.annotation.DimenRes;
import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

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

  private float maxShoveAngle;
  private float pixelDeltaThreshold;
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

  /**
   * Listener for shove callbacks.
   */
  public interface OnShoveGestureListener {
    /**
     * Indicates that the shove gesture started.
     *
     * @param detector this detector
     * @return true if you want to receive subsequent {@link #onShove(ShoveGestureDetector, float, float)} callbacks,
     * false if you want to ignore this gesture.
     */
    boolean onShoveBegin(@NonNull ShoveGestureDetector detector);

    /**
     * Called for every shove change during the gesture.
     *
     * @param detector              this detector
     * @param deltaPixelsSinceLast  pixels delta change since the last call
     * @param deltaPixelsSinceStart pixels delta change since the start of the gesture
     * @return true if the gesture was handled, false otherwise
     */
    boolean onShove(@NonNull ShoveGestureDetector detector, float deltaPixelsSinceLast, float deltaPixelsSinceStart);

    /**
     * Indicates that the shove gesture ended.
     *
     * @param velocityX velocityX of the gesture in the moment of lifting the fingers
     * @param velocityY velocityY of the gesture in the moment of lifting the fingers
     * @param detector  this detector
     */
    void onShoveEnd(@NonNull ShoveGestureDetector detector, float velocityX, float velocityY);
  }

  public static class SimpleOnShoveGestureListener implements OnShoveGestureListener {
    @Override
    public boolean onShoveBegin(@NonNull ShoveGestureDetector detector) {
      return true;
    }

    @Override
    public boolean onShove(@NonNull ShoveGestureDetector detector,
                           float deltaPixelsSinceLast,
                           float deltaPixelsSinceStart) {
      return false;
    }

    @Override
    public void onShoveEnd(@NonNull ShoveGestureDetector detector, float velocityX, float velocityY) {
      // No Implementation
    }
  }

  @Override
  protected boolean analyzeMovement() {
    super.analyzeMovement();

    deltaPixelSinceLast = calculateDeltaPixelsSinceLast();
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

  float calculateDeltaPixelsSinceLast() {
    float py0 = getPreviousEvent().getY(getPreviousEvent().findPointerIndex(pointerIdList.get(0)));
    float py1 = getPreviousEvent().getY(getPreviousEvent().findPointerIndex(pointerIdList.get(1)));
    float prevAverageY = (py0 + py1) / 2.0f;

    float cy0 = getCurrentEvent().getY(getCurrentEvent().findPointerIndex(pointerIdList.get(0)));
    float cy1 = getCurrentEvent().getY(getCurrentEvent().findPointerIndex(pointerIdList.get(1)));
    float currAverageY = (cy0 + cy1) / 2.0f;

    return currAverageY - prevAverageY;
  }

  /**
   * Returns vertical pixel delta change since the start of the gesture.
   *
   * @return pixels delta change since the start of the gesture
   */
  public float getDeltaPixelsSinceStart() {
    return deltaPixelsSinceStart;
  }

  /**
   * Returns last vertical pixel delta change
   * calculated in {@link OnShoveGestureListener#onShove(ShoveGestureDetector, float, float)}.
   *
   * @return pixels delta change since the last call
   */
  public float getDeltaPixelSinceLast() {
    return deltaPixelSinceLast;
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
   * <p>
   * We encourage to set those values from dimens to accommodate for various screen sizes.
   *
   * @param pixelDeltaThreshold delta threshold
   */
  public void setPixelDeltaThreshold(float pixelDeltaThreshold) {
    this.pixelDeltaThreshold = pixelDeltaThreshold;
  }

  /**
   * Set the delta dp threshold required to qualify it as a shove gesture.
   *
   * @param pixelDeltaThresholdDimen delta threshold
   */
  public void setPixelDeltaThresholdResource(@DimenRes int pixelDeltaThresholdDimen) {
    setPixelDeltaThreshold(context.getResources().getDimension(pixelDeltaThresholdDimen));
  }

  /**
   * Get the maximum allowed angle between fingers, measured from the horizontal line, to qualify it as a shove gesture.
   *
   * @return maximum allowed angle
   */
  public float getMaxShoveAngle() {
    return maxShoveAngle;
  }

  /**
   * Set the maximum allowed angle between fingers, measured from the horizontal line, to qualify it as a shove gesture.
   *
   * @param maxShoveAngle maximum allowed angle
   */
  public void setMaxShoveAngle(float maxShoveAngle) {
    this.maxShoveAngle = maxShoveAngle;
  }
}
