package com.mapbox.android.gestures;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.annotation.UiThread;
import android.view.MotionEvent;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Entry point for all of the detectors. Set listener for gestures you'd like to be notified about
 * and pass all of the {@link MotionEvent}s through {@link #onTouchEvent(MotionEvent)} to start processing gestures.
 */
@UiThread
public class AndroidGesturesManager {
  @Retention(RetentionPolicy.SOURCE)
  @IntDef( {GESTURE_TYPE_SCROLL,
    GESTURE_TYPE_SCALE,
    GESTURE_TYPE_ROTATE,
    GESTURE_TYPE_SHOVE,
    GESTURE_TYPE_MULTI_FINGER_TAP,
    GESTURE_TYPE_SINGLE_TAP_UP,
    GESTURE_TYPE_LONG_PRESS,
    GESTURE_TYPE_FLING,
    GESTURE_TYPE_SHOW_PRESS,
    GESTURE_TYPE_DOWN,
    GESTURE_TYPE_DOUBLE_TAP,
    GESTURE_TYPE_DOUBLE_TAP_EVENT,
    GESTURE_TYPE_SINGLE_TAP_CONFIRMED,
    GESTURE_TYPE_MOVE,
    GESTURE_TYPE_SIDEWAYS_SHOVE
  })
  public @interface GestureType {
  }

  public static final int GESTURE_TYPE_SCROLL = 0;
  public static final int GESTURE_TYPE_SCALE = 1;
  public static final int GESTURE_TYPE_ROTATE = 2;
  public static final int GESTURE_TYPE_SHOVE = 3;
  public static final int GESTURE_TYPE_MULTI_FINGER_TAP = 4;
  public static final int GESTURE_TYPE_SINGLE_TAP_UP = 5;
  public static final int GESTURE_TYPE_LONG_PRESS = 6;
  public static final int GESTURE_TYPE_FLING = 7;
  public static final int GESTURE_TYPE_SHOW_PRESS = 8;
  public static final int GESTURE_TYPE_DOWN = 9;
  public static final int GESTURE_TYPE_DOUBLE_TAP = 10;
  public static final int GESTURE_TYPE_DOUBLE_TAP_EVENT = 11;
  public static final int GESTURE_TYPE_SINGLE_TAP_CONFIRMED = 12;
  public static final int GESTURE_TYPE_MOVE = 13;
  public static final int GESTURE_TYPE_SIDEWAYS_SHOVE = 14;

  private final List<Set<Integer>> mutuallyExclusiveGestures = new ArrayList<>();
  private final List<BaseGesture> detectors = new ArrayList<>();

  private final StandardGestureDetector standardGestureDetector;
  private final StandardScaleGestureDetector standardScaleGestureDetector;
  private final RotateGestureDetector rotateGestureDetector;
  private final ShoveGestureDetector shoveGestureDetector;
  private final MultiFingerTapGestureDetector multiFingerTapGestureDetector;
  private final MoveGestureDetector moveGestureDetector;
  private final SidewaysShoveGestureDetector sidewaysShoveGestureDetector;

  /**
   * Creates a new instance of the {@link AndroidGesturesManager}.
   *
   * @param context activity's context
   */
  public AndroidGesturesManager(Context context) {
    this(context, true);
  }

  /**
   * Creates a new instance of the {@link AndroidGesturesManager}.
   *
   * @param context                activity's context
   * @param applyDefaultThresholds if true, default gestures thresholds and adjustments will be applied
   */
  public AndroidGesturesManager(Context context, boolean applyDefaultThresholds) {
    this(context, new ArrayList<Set<Integer>>(), applyDefaultThresholds);
  }

  /**
   * Creates a new instance of the {@link AndroidGesturesManager}.
   *
   * @param context           Activity's context
   * @param exclusiveGestures a number of sets of {@link GestureType}s that <b>should not</b> be invoked at the same.
   *                          This means that when a set contains a {@link ProgressiveGesture} and this gestures
   *                          is in progress no other gestures from the set will be invoked.
   *                          <p>
   *                          At the moment {@link #GESTURE_TYPE_SCROLL} is not interpreted as a progressive gesture
   *                          because it is not implemented this way by the
   *                          {@link android.support.v4.view.GestureDetectorCompat}.
   */
  @SafeVarargs
  public AndroidGesturesManager(Context context, Set<Integer>... exclusiveGestures) {
    this(context, Arrays.asList(exclusiveGestures), true);
  }

  /**
   * Creates a new instance of the {@link AndroidGesturesManager}.
   *
   * @param context                Activity's context
   * @param exclusiveGestures      a list of sets of {@link GestureType}s that <b>should not</b> be invoked at the same.
   *                               This means that when a set contains a {@link ProgressiveGesture} and this gestures
   *                               is in progress no other gestures from the set will be invoked.
   *                               <p>
   *                               At the moment {@link #GESTURE_TYPE_SCROLL} is not interpreted as
   *                               a progressive gesture because it is not implemented this way by the
   *                               {@link android.support.v4.view.GestureDetectorCompat}.
   * @param applyDefaultThresholds if true, default gestures thresholds and adjustments will be applied
   */
  public AndroidGesturesManager(Context context, List<Set<Integer>> exclusiveGestures, boolean applyDefaultThresholds) {
    this.mutuallyExclusiveGestures.addAll(exclusiveGestures);

    rotateGestureDetector = new RotateGestureDetector(context, this);
    standardScaleGestureDetector = new StandardScaleGestureDetector(context, this);
    shoveGestureDetector = new ShoveGestureDetector(context, this);
    sidewaysShoveGestureDetector = new SidewaysShoveGestureDetector(context, this);
    multiFingerTapGestureDetector = new MultiFingerTapGestureDetector(context, this);
    moveGestureDetector = new MoveGestureDetector(context, this);
    standardGestureDetector = new StandardGestureDetector(context, this);

    detectors.add(rotateGestureDetector);
    detectors.add(standardScaleGestureDetector);
    detectors.add(shoveGestureDetector);
    detectors.add(sidewaysShoveGestureDetector);
    detectors.add(multiFingerTapGestureDetector);
    detectors.add(moveGestureDetector);
    detectors.add(standardGestureDetector);

    if (applyDefaultThresholds) {
      initDefaultThresholds();
    }
  }

  private void initDefaultThresholds() {
    for (BaseGesture detector : detectors) {
      if (detector instanceof MultiFingerTapGestureDetector) {
        ((MultiFingerGesture) detector).setSpanThresholdResource(R.dimen.mapbox_defaultMutliFingerSpanThreshold);
      }

      if (detector instanceof StandardScaleGestureDetector) {
        ((StandardScaleGestureDetector) detector).setSpanSinceStartThresholdResource(
          R.dimen.mapbox_defaultScaleSpanSinceStartThreshold);
      }

      if (detector instanceof ShoveGestureDetector) {
        ((ShoveGestureDetector) detector).setPixelDeltaThresholdResource(R.dimen.mapbox_defaultShovePixelThreshold);
        ((ShoveGestureDetector) detector).setMaxShoveAngle(Constants.DEFAULT_SHOVE_MAX_ANGLE);
      }

      if (detector instanceof SidewaysShoveGestureDetector) {
        ((SidewaysShoveGestureDetector) detector).setPixelDeltaThresholdResource(
          R.dimen.mapbox_defaultShovePixelThreshold);
        ((SidewaysShoveGestureDetector) detector).setMaxShoveAngle(Constants.DEFAULT_SHOVE_MAX_ANGLE);
      }

      if (detector instanceof MultiFingerTapGestureDetector) {
        ((MultiFingerTapGestureDetector) detector).setMultiFingerTapMovementThresholdResource(
          R.dimen.mapbox_defaultMultiTapMovementThreshold);

        ((MultiFingerTapGestureDetector) detector).setMultiFingerTapTimeThreshold(
          Constants.DEFAULT_MULTI_TAP_TIME_THRESHOLD);
      }

      if (detector instanceof RotateGestureDetector) {
        ((RotateGestureDetector) detector).setAngleThreshold(Constants.DEFAULT_ROTATE_ANGLE_THRESHOLD);
      }
    }
  }

  /**
   * Passes motion events to all gesture detectors.
   *
   * @param motionEvent event provided by the Android OS.
   * @return true if the touch event is handled by any gesture, false otherwise.
   */
  public boolean onTouchEvent(MotionEvent motionEvent) {
    boolean isHandled = false;
    for (BaseGesture detector : detectors) {
      if (detector.onTouchEvent(motionEvent)) {
        isHandled = true;
      }
    }
    return isHandled;
  }

  /**
   * Sets a listener for all the events normally returned by the {@link android.support.v4.view.GestureDetectorCompat}.
   *
   * @param listener your gestures listener
   * @see <a href="https://developer.android.com/training/gestures/index.html">Using Touch Gestures</a>
   * @see <a href="https://developer.android.com/reference/android/support/v4/view/GestureDetectorCompat.html">GestureDetectorCompat</a>
   */
  public void setStandardGestureListener(StandardGestureDetector.StandardOnGestureListener listener) {
    standardGestureDetector.setListener(listener);
  }

  /**
   * Removes a listener for all the events normally returned by the
   * {@link android.support.v4.view.GestureDetectorCompat}.
   */
  public void removeStandardGestureListener() {
    standardGestureDetector.removeListener();
  }

  /**
   * Sets a listener for all the events normally returned by the {@link android.view.ScaleGestureDetector}.
   *
   * @param listener your gestures listener
   * @see <a href="https://developer.android.com/training/gestures/index.html">Using Touch Gestures</a>
   * @see <a href="https://developer.android.com/reference/android/view/ScaleGestureDetector.html">ScaleGestureDetector</a>
   */
  public void setStandardScaleGestureListener(StandardScaleGestureDetector.StandardOnScaleGestureListener listener) {
    standardScaleGestureDetector.setListener(listener);
  }

  /**
   * Removes a listener for all the events normally returned by the {@link android.view.ScaleGestureDetector}.
   */
  public void removeStandardScaleGestureListener() {
    standardScaleGestureDetector.removeListener();
  }

  /**
   * Sets a listener for rotate gestures.
   *
   * @param listener your gestures listener
   */
  public void setRotateGestureListener(RotateGestureDetector.OnRotateGestureListener listener) {
    rotateGestureDetector.setListener(listener);
  }

  /**
   * Removes a listener for rotate gestures.
   */
  public void removeRotateGestureListener() {
    rotateGestureDetector.removeListener();
  }

  /**
   * Sets a listener for shove gestures.
   *
   * @param listener your gestures listener
   */
  public void setShoveGestureListener(ShoveGestureDetector.OnShoveGestureListener listener) {
    shoveGestureDetector.setListener(listener);
  }

  /**
   * Removes a listener for shove gestures.
   */
  public void removeShoveGestureListener() {
    shoveGestureDetector.removeListener();
  }

  /**
   * Sets a listener for multi finger tap gestures.
   *
   * @param listener your gestures listener
   */
  public void setMultiFingerTapGestureListener(MultiFingerTapGestureDetector.OnMultiFingerTapGestureListener listener) {
    multiFingerTapGestureDetector.setListener(listener);
  }

  /**
   * Removes a listener for multi finger tap gestures.
   */
  public void removeMultiFingerTapGestureListener() {
    multiFingerTapGestureDetector.removeListener();
  }

  /**
   * Sets a listener for move gestures.
   * <p>
   * {@link MoveGestureDetector} serves similar purpose to
   * {@link com.mapbox.android.gestures.StandardGestureDetector.StandardOnGestureListener
   * #onScroll(MotionEvent, MotionEvent, float, float)}, however, it's a {@link ProgressiveGesture} that
   * introduces {@link MoveGestureDetector.OnMoveGestureListener#onMoveBegin(MoveGestureDetector)},
   * {@link MoveGestureDetector.OnMoveGestureListener#onMoveEnd(MoveGestureDetector, float, float)},
   * threshold with {@link MoveGestureDetector#setMoveThreshold(float)} and multi finger support thanks to
   * {@link MoveDistancesObject}.
   *
   * @param listener your gestures listener
   */
  public void setMoveGestureListener(MoveGestureDetector.OnMoveGestureListener listener) {
    moveGestureDetector.setListener(listener);
  }

  /**
   * Removes a listener for move gestures.
   */
  public void removeMoveGestureListener() {
    moveGestureDetector.removeListener();
  }

  /**
   * Sets a listener for sideways shove gestures.
   *
   * @param listener your gestures listener
   */
  public void setSidewaysShoveGestureListener(SidewaysShoveGestureDetector.OnSidewaysShoveGestureListener listener) {
    sidewaysShoveGestureDetector.setListener(listener);
  }

  /**
   * Removes a listener for sideways shove gestures.
   */
  public void removeSidewaysShoveGestureListener() {
    sidewaysShoveGestureDetector.removeListener();
  }

  /**
   * Get a list of all active gesture detectors.
   *
   * @return list of all gesture detectors
   */
  public List<BaseGesture> getDetectors() {
    return detectors;
  }

  /**
   * Get gesture detector that wraps {@link android.support.v4.view.GestureDetectorCompat}.
   *
   * @return gesture detector
   */
  public StandardGestureDetector getStandardGestureDetector() {
    return standardGestureDetector;
  }

  /**
   * Get gesture detector that wraps {@link android.view.ScaleGestureDetector}.
   *
   * @return gesture detector
   */
  public StandardScaleGestureDetector getStandardScaleGestureDetector() {
    return standardScaleGestureDetector;
  }

  /**
   * Get rotate gesture detector.
   *
   * @return gesture detector
   */
  public RotateGestureDetector getRotateGestureDetector() {
    return rotateGestureDetector;
  }

  /**
   * Get shove gesture detector.
   *
   * @return gesture detector
   */
  public ShoveGestureDetector getShoveGestureDetector() {
    return shoveGestureDetector;
  }

  /**
   * Get multi finger tap gesture detector.
   *
   * @return gesture detector
   */
  public MultiFingerTapGestureDetector getMultiFingerTapGestureDetector() {
    return multiFingerTapGestureDetector;
  }

  /**
   * Get move gesture detector.
   *
   * @return gesture detector
   */
  public MoveGestureDetector getMoveGestureDetector() {
    return moveGestureDetector;
  }

  /**
   * Get sideways shove gesture detector.
   *
   * @return gesture detector
   */
  public SidewaysShoveGestureDetector getSidewaysShoveGestureDetector() {
    return sidewaysShoveGestureDetector;
  }

  /**
   * Sets a number of sets containing mutually exclusive gestures.
   *
   * @param exclusiveGestures a number of sets of {@link GestureType}s that <b>should not</b> be invoked at the same.
   *                          This means that when a set contains a {@link ProgressiveGesture} and this gestures
   *                          is in progress no other gestures from the set will be invoked.
   *                          <p>
   *                          At the moment {@link #GESTURE_TYPE_SCROLL} is not interpreted as a progressive gesture
   *                          because it is not interpreted this way by the
   *                          {@link android.support.v4.view.GestureDetectorCompat}.
   */
  @SafeVarargs
  public final void setMutuallyExclusiveGestures(Set<Integer>... exclusiveGestures) {
    setMutuallyExclusiveGestures(Arrays.asList(exclusiveGestures));
  }

  /**
   * Sets a list of sets containing mutually exclusive gestures.
   *
   * @param exclusiveGestures a list of sets of {@link GestureType}s that <b>should not</b> be invoked at the same.
   *                          This means that when a set contains a {@link ProgressiveGesture} and this gestures
   *                          is in progress no other gestures from the set will be invoked.
   *                          <p>
   *                          At the moment {@link #GESTURE_TYPE_SCROLL} is not interpreted as a progressive gesture
   *                          because it is not interpreted this way by the
   *                          {@link android.support.v4.view.GestureDetectorCompat}.
   */
  public void setMutuallyExclusiveGestures(List<Set<Integer>> exclusiveGestures) {
    this.mutuallyExclusiveGestures.clear();
    this.mutuallyExclusiveGestures.addAll(exclusiveGestures);
  }

  /**
   * Returns a list of sets containing mutually exclusive gestures.
   *
   * @return mutually exclusive gestures
   * @see #setMutuallyExclusiveGestures(List)
   */
  public List<Set<Integer>> getMutuallyExclusiveGestures() {
    return mutuallyExclusiveGestures;
  }
}
