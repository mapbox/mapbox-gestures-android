package com.mapbox.android.gestures;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

import java.util.HashSet;
import java.util.Set;

import static com.mapbox.android.gestures.AndroidGesturesManager.GESTURE_TYPE_ROTATE;

/**
 * Gesture detector handling rotation gesture.
 */
@UiThread
public class RotateGestureDetector extends ProgressiveGesture<RotateGestureDetector.OnRotateGestureListener> {
  private static final Set<Integer> handledTypes = new HashSet<>();

  static {
    handledTypes.add(GESTURE_TYPE_ROTATE);
  }

  private float angleThreshold;
  float deltaSinceStart;
  float deltaSinceLast;

  public RotateGestureDetector(Context context, AndroidGesturesManager gesturesManager) {
    super(context, gesturesManager);
  }

  @NonNull
  @Override
  protected Set<Integer> provideHandledTypes() {
    return handledTypes;
  }

  /**
   * Listener for rotate gesture callbacks.
   */
  public interface OnRotateGestureListener {
    /**
     * Indicates that the rotation gesture started.
     *
     * @param detector this detector
     * @return true if you want to receive subsequent {@link #onRotate(RotateGestureDetector, float, float)} callbacks,
     * false if you want to ignore this gesture.
     */
    boolean onRotateBegin(RotateGestureDetector detector);

    /**
     * Called for every rotation change during the gesture.
     *
     * @param detector                  this detector
     * @param rotationDegreesSinceLast  rotation change since the last call
     * @param rotationDegreesSinceFirst rotation change since the start of the gesture
     * @return true if the gesture was handled, false otherwise
     */
    boolean onRotate(RotateGestureDetector detector, float rotationDegreesSinceLast, float rotationDegreesSinceFirst);

    /**
     * Indicates that the rotation gesture ended.
     *
     * @param velocityX       velocityX of the gesture in the moment of lifting the fingers
     * @param velocityY       velocityY of the gesture in the moment of lifting the fingers
     * @param angularVelocity angularVelocity of the gesture in the moment of lifting the fingers
     * @param detector        this detector
     */
    void onRotateEnd(RotateGestureDetector detector, float velocityX, float velocityY, float angularVelocity);
  }

  public static class SimpleOnRotateGestureListener implements OnRotateGestureListener {

    @Override
    public boolean onRotateBegin(RotateGestureDetector detector) {
      return true;
    }

    @Override
    public boolean onRotate(RotateGestureDetector detector, float rotationDegreesSinceLast,
                            float rotationDegreesSinceFirst) {
      return true;
    }

    @Override
    public void onRotateEnd(RotateGestureDetector detector, float velocityX, float velocityY, float angularVelocity) {
      // No implementation
    }
  }

  @Override
  protected boolean analyzeMovement() {
    super.analyzeMovement();

    deltaSinceLast = getRotationDegreesSinceLast();
    deltaSinceStart += deltaSinceLast;

    if (isInProgress() && deltaSinceLast != 0) {
      return listener.onRotate(this, deltaSinceLast, deltaSinceStart);
    } else if (canExecute(GESTURE_TYPE_ROTATE)) {
      if (listener.onRotateBegin(this)) {
        gestureStarted();
        return true;
      }
    }

    return false;
  }

  @Override
  protected boolean canExecute(int invokedGestureType) {
    return Math.abs(deltaSinceStart) >= angleThreshold && super.canExecute(invokedGestureType);
  }

  @Override
  protected void gestureStopped() {
    super.gestureStopped();

    if (deltaSinceLast == 0) {
      velocityX = 0;
      velocityY = 0;
    }

    float angularVelocity = calculateAngularVelocityVector(velocityX, velocityY);
    listener.onRotateEnd(this, velocityX, velocityY, angularVelocity);
  }

  @Override
  protected void reset() {
    super.reset();
    deltaSinceStart = 0f;
  }

  float getRotationDegreesSinceLast() {
    MultiFingerDistancesObject distancesObject =
      pointersDistanceMap.get(new PointerDistancePair(pointerIdList.get(0), pointerIdList.get(1)));

    double diffRadians = Math.atan2(distancesObject.getPrevFingersDiffY(),
      distancesObject.getPrevFingersDiffX()) - Math.atan2(
      distancesObject.getCurrFingersDiffY(),
      distancesObject.getCurrFingersDiffX());
    return (float) Math.toDegrees(diffRadians);
  }

  float calculateAngularVelocityVector(float velocityX, float velocityY) {
    float angularVelocity = Math.abs((float) ((getFocalPoint().x * velocityY + getFocalPoint().y * velocityX)
      / (Math.pow(getFocalPoint().x, 2.0) + Math.pow(getFocalPoint().y, 2.0))));

    if (deltaSinceLast < 0) {
      angularVelocity = -angularVelocity;
    }

    return angularVelocity;
  }

  /**
   * Returns rotation change in degrees since the start of the gesture.
   *
   * @return rotation change since the start of the gesture
   */
  public float getDeltaSinceStart() {
    return deltaSinceStart;
  }

  /**
   * Returns last rotation change difference in degrees
   * calculated in {@link OnRotateGestureListener#onRotate(RotateGestureDetector, float, float)}
   *
   * @return rotation change since last callback
   */
  public float getDeltaSinceLast() {
    return deltaSinceLast;
  }

  /**
   * Get the threshold angle between first and current fingers position
   * for this detector to actually qualify it as a rotation gesture.
   *
   * @return Angle threshold for rotation gesture
   */
  public float getAngleThreshold() {
    return angleThreshold;
  }

  /**
   * Set the threshold angle between first and current fingers position
   * for this detector to actually qualify it as a rotation gesture.
   *
   * @param angleThreshold angle threshold for rotation gesture
   */
  public void setAngleThreshold(float angleThreshold) {
    this.angleThreshold = angleThreshold;
  }
}
