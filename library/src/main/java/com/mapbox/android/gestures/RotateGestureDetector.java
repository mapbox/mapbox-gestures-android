package com.mapbox.android.gestures;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.view.animation.DecelerateInterpolator;

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

  private float angleThreshold = Constants.DEFAULT_ROTATE_ANGLE_THRESHOLD;
  private float deltaSinceStart;
  private float deltaSinceLast;

  public RotateGestureDetector(Context context, AndroidGesturesManager gesturesManager) {
    super(context, gesturesManager);

    setInterpolator(new DecelerateInterpolator());

    valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator animation) {
        boolean canContinue = listener.rotationVelocityAnimator(
          RotateGestureDetector.this,
          velocityX,
          velocityY,
          (Float) animation.getAnimatedValue()
        );

        if (!canContinue) {
          animation.cancel();
        }
      }
    });

    valueAnimator.addListener(new AnimatorListenerAdapter() {
      @Override
      public void onAnimationEnd(Animator animation) {
        listener.onRotateEnd(RotateGestureDetector.this);
        deltaSinceStart = 0;
      }
    });
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
     * Continuous callback after user has ended rotation gesture by lifting the fingers.
     * Value animation is based on the velocity of the gesture when it ended and this callback will be invoked for each
     * animation value change until the value animation finishes.
     * <p>
     * {@link #onRotateEnd(RotateGestureDetector)} will not be called until the value animation finishes.
     * You can return false here to end the gesture immediately.
     *
     * @param detector                      this detector
     * @param velocityX                     velocityX of the gesture in the moment of lifting the fingers
     * @param velocityY                     velocityY of the gesture in the moment of lifting the fingers
     * @param rotationVelocityAnimatorValue current animation value of the gesture
     * @return true if you want to receive the rest of the animation callbacks
     * or false to end the rotation gesture immediately.
     */
    boolean rotationVelocityAnimator(RotateGestureDetector detector, float velocityX, float velocityY,
                                     float rotationVelocityAnimatorValue);

    /**
     * Indicates that the rotation gesture ended.
     *
     * @param detector this detector
     */
    void onRotateEnd(RotateGestureDetector detector);
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
    public boolean rotationVelocityAnimator(RotateGestureDetector detector, float velocityX, float velocityY,
                                            float rotationVelocityAnimatorValue) {
      return false;
    }

    @Override
    public void onRotateEnd(RotateGestureDetector detector) {
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
    return Math.abs(deltaSinceStart) > angleThreshold && super.canExecute(invokedGestureType);
  }

  @Override
  protected void gestureStopped() {
    super.gestureStopped();

    if (deltaSinceLast == 0) {
      listener.onRotateEnd(this);
      return;
    }

    float angularVelocity = Math.abs(calculateAngularVelocityVector(velocityX, velocityY));
    if (deltaSinceLast < 0) {
      angularVelocity = -angularVelocity;
    }

    valueAnimator.setFloatValues(angularVelocity, 0f);
    valueAnimator.setDuration((long) (Math.abs(angularVelocity) * 100));
    valueAnimator.setInterpolator(getInterpolator());
    valueAnimator.start();
  }

  private float getRotationDegreesSinceLast() {
    MultiFingerDistancesObject distancesObject =
      pointersDistanceMap.get(new PointerDistancePair(pointerIdList.get(0), pointerIdList.get(1)));

    double diffRadians = Math.atan2(distancesObject.getPrevFingersDiffY(),
      distancesObject.getPrevFingersDiffX()) - Math.atan2(
      distancesObject.getCurrFingersDiffY(),
      distancesObject.getCurrFingersDiffX());
    return (float) Math.toDegrees(diffRadians);
  }

  private float calculateAngularVelocityVector(float velocityX, float velocityY) {
    return (float) ((getFocalPoint().x * velocityY + getFocalPoint().y * velocityX)
      / (Math.pow(getFocalPoint().x, 2.0) + Math.pow(getFocalPoint().y, 2.0)));
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

  /**
   * Get the threshold angle between first and current fingers position
   * for this detector to actually qualify it as a rotation gesture.
   *
   * @return Angle threshold for rotation gesture
   * @see Constants#DEFAULT_ROTATE_ANGLE_THRESHOLD
   */
  public float getDefaultAngleThreshold() {
    return Constants.DEFAULT_ROTATE_ANGLE_THRESHOLD;
  }
}
