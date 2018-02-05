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
    GESTURE_TYPE_SINGLE_TAP_CONFIRMED
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

  private final List<Set<Integer>> mutuallyExclusiveGestures = new ArrayList<>();
  private final List<BaseGesture> detectors = new ArrayList<>();

  private final StandardGestureDetector standardGestureDetector;
  private final StandardScaleGestureDetector standardScaleGestureDetector;
  private final RotateGestureDetector rotateGestureDetector;
  private final ShoveGestureDetector shoveGestureDetector;
  private final MultiFingerTapGestureDetector multiFingerTapGestureDetector;

  /**
   * Creates a new instance of the {@link AndroidGesturesManager}.
   *
   * @param context activity's context
   */
  public AndroidGesturesManager(Context context) {
    this(context, new ArrayList<Set<Integer>>());
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
    this(context, Arrays.asList(exclusiveGestures));
  }

  /**
   * Creates a new instance of the {@link AndroidGesturesManager}.
   *
   * @param context           Activity's context
   * @param exclusiveGestures a list of sets of {@link GestureType}s that <b>should not</b> be invoked at the same.
   *                          This means that when a set contains a {@link ProgressiveGesture} and this gestures
   *                          is in progress no other gestures from the set will be invoked.
   *                          <p>
   *                          At the moment {@link #GESTURE_TYPE_SCROLL} is not interpreted as a progressive gesture
   *                          because it is not implemented this way by the
   *                          {@link android.support.v4.view.GestureDetectorCompat}.
   */
  public AndroidGesturesManager(Context context, List<Set<Integer>> exclusiveGestures) {
    this.mutuallyExclusiveGestures.addAll(exclusiveGestures);

    standardGestureDetector = new StandardGestureDetector(context, this);
    standardScaleGestureDetector = new StandardScaleGestureDetector(context, this);
    rotateGestureDetector = new RotateGestureDetector(context, this);
    shoveGestureDetector = new ShoveGestureDetector(context, this);
    multiFingerTapGestureDetector = new MultiFingerTapGestureDetector(context, this);

    detectors.add(standardGestureDetector);
    detectors.add(standardScaleGestureDetector);
    detectors.add(rotateGestureDetector);
    detectors.add(shoveGestureDetector);
    detectors.add(multiFingerTapGestureDetector);
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

  public List<Set<Integer>> getMutuallyExclusiveGestures() {
    return mutuallyExclusiveGestures;
  }
}
