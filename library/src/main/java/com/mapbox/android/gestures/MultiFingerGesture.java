package com.mapbox.android.gestures;

import android.content.Context;
import android.graphics.PointF;
import android.support.annotation.DimenRes;
import android.support.annotation.UiThread;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Base class for all multi finger gesture detectors.
 *
 * @param <L> listener that will be called with gesture events/updates.
 */
@UiThread
public abstract class MultiFingerGesture<L> extends BaseGesture<L> {

  /**
   * This value is the threshold ratio between the previous combined pressure
   * and the current combined pressure. When pressure decreases rapidly
   * between events the position values can often be imprecise, as it usually
   * indicates that the user is in the process of lifting a pointer off of the
   * device. This value was tuned experimentally.
   * <p>
   * Thanks to Almer Thie (code.almeros.com).
   */
  private static final float PRESSURE_THRESHOLD = 0.67f;

  private static final int DEFAULT_REQUIRED_FINGERS_COUNT = 2;

  private final float edgeSlop;

  private float spanThreshold;

  private final PermittedActionsGuard permittedActionsGuard = new PermittedActionsGuard();

  /**
   * A list that holds IDs of currently active pointers in an order of activation.
   * First element is the oldest active pointer and last element is the most recently activated pointer.
   */
  final List<Integer> pointerIdList = new ArrayList<>();
  final HashMap<PointerDistancePair, MultiFingerDistancesObject> pointersDistanceMap = new HashMap<>();
  private PointF focalPoint = new PointF();

  public MultiFingerGesture(Context context, AndroidGesturesManager gesturesManager) {
    super(context, gesturesManager);

    ViewConfiguration config = ViewConfiguration.get(context);
    edgeSlop = config.getScaledEdgeSlop();
  }

  @Override
  protected boolean analyzeEvent(MotionEvent motionEvent) {
    int action = motionEvent.getActionMasked();

    boolean isMissingActions =
      permittedActionsGuard.isMissingActions(action, motionEvent.getPointerCount(), pointerIdList.size());

    if (isMissingActions) {
      // stopping ProgressiveGestures and clearing pointers
      if (this instanceof ProgressiveGesture && ((ProgressiveGesture) this).isInProgress()) {
        ((ProgressiveGesture) this).gestureStopped();
      }
      pointerIdList.clear();
      pointersDistanceMap.clear();
    }

    if (!isMissingActions || action == MotionEvent.ACTION_DOWN) {
      // if we are not missing any actions or the invalid one happens
      // to be ACTION_DOWN (therefore, we can start over immediately), then update pointers
      updatePointerList(motionEvent);
    }

    if (isMissingActions) {
      Log.w("MultiFingerGesture", "Some MotionEvents were not passed to the library.");
      return false;
    } else {
      if (action == MotionEvent.ACTION_MOVE) {
        if (pointerIdList.size() >= getRequiredPointersCount() && checkPressure()) {
          calculateDistances();
          if (!isSloppyGesture()) {
            focalPoint = Utils.determineFocalPoint(motionEvent);
            return analyzeMovement();
          }
        }
      }
    }

    return false;
  }

  private void updatePointerList(MotionEvent motionEvent) {
    int action = motionEvent.getActionMasked();

    if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
      pointerIdList.add(motionEvent.getPointerId(motionEvent.getActionIndex()));
    } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP) {
      pointerIdList.remove(Integer.valueOf(motionEvent.getPointerId(motionEvent.getActionIndex())));
    }
  }

  boolean checkPressure() {
    float currentPressure = getCurrentEvent().getPressure();
    float previousPressure = getPreviousEvent().getPressure();
    return currentPressure / previousPressure > PRESSURE_THRESHOLD;
  }

  private boolean checkSpanBelowThreshold() {
    for (MultiFingerDistancesObject distancesObject : pointersDistanceMap.values()) {
      if (distancesObject.getCurrFingersDiffXY() < spanThreshold) {
        return true;
      }
    }

    return false;
  }

  protected int getRequiredPointersCount() {
    return DEFAULT_REQUIRED_FINGERS_COUNT;
  }

  protected boolean analyzeMovement() {
    return false;
  }

  /**
   * Check if we have a sloppy gesture. Sloppy gestures can happen if the edge
   * of the user's hand is touching the screen, for example.
   * <p>
   * Thanks to Almer Thie (code.almeros.com).
   *
   * @return true if we detect sloppy gesture, false otherwise
   */
  protected boolean isSloppyGesture() {
    // As orientation can change, query the metrics in touch down
    DisplayMetrics metrics = context.getResources().getDisplayMetrics();
    float rightSlopEdge = metrics.widthPixels - edgeSlop;
    float bottomSlopEdge = metrics.heightPixels - edgeSlop;

    final float edgeSlop = this.edgeSlop;

    for (int pointerId : pointerIdList) {
      int pointerIndex = getCurrentEvent().findPointerIndex(pointerId);
      float x = Utils.getRawX(getCurrentEvent(), pointerIndex);
      float y = Utils.getRawY(getCurrentEvent(), pointerIndex);

      boolean isSloppy = x < edgeSlop || y < edgeSlop || x > rightSlopEdge
        || y > bottomSlopEdge;

      if (isSloppy) {
        return true;
      }
    }

    return checkSpanBelowThreshold();
  }

  @Override
  protected boolean canExecute(int invokedGestureType) {
    return super.canExecute(invokedGestureType) && !isSloppyGesture();
  }

  protected void reset() {
  }

  private void calculateDistances() {
    pointersDistanceMap.clear();

    for (int i = 0; i < pointerIdList.size() - 1; i++) {
      for (int j = i + 1; j < pointerIdList.size(); j++) {
        int primaryPointerId = pointerIdList.get(i);
        int secondaryPointerId = pointerIdList.get(j);

        float px0 = getPreviousEvent().getX(getPreviousEvent().findPointerIndex(primaryPointerId));
        float py0 = getPreviousEvent().getY(getPreviousEvent().findPointerIndex(primaryPointerId));
        float px1 = getPreviousEvent().getX(getPreviousEvent().findPointerIndex(secondaryPointerId));
        float py1 = getPreviousEvent().getY(getPreviousEvent().findPointerIndex(secondaryPointerId));
        float prevFingersDiffX = px1 - px0;
        float prevFingersDiffY = py1 - py0;

        float cx0 = getCurrentEvent().getX(getCurrentEvent().findPointerIndex(primaryPointerId));
        float cy0 = getCurrentEvent().getY(getCurrentEvent().findPointerIndex(primaryPointerId));
        float cx1 = getCurrentEvent().getX(getCurrentEvent().findPointerIndex(secondaryPointerId));
        float cy1 = getCurrentEvent().getY(getCurrentEvent().findPointerIndex(secondaryPointerId));
        float currFingersDiffX = cx1 - cx0;
        float currFingersDiffY = cy1 - cy0;

        pointersDistanceMap.put(new PointerDistancePair(primaryPointerId, secondaryPointerId),
          new MultiFingerDistancesObject(
            prevFingersDiffX, prevFingersDiffY,
            currFingersDiffX, currFingersDiffY)
        );
      }
    }
  }

  /**
   * Returns the current distance between the two pointers forming the
   * gesture in progress.
   * <p>
   * Pointers are sorted by the time they were placed on the screen until lifted up.
   * This means that index 0 will reflect the oldest added, still active pointer
   * and index ({@link #getPointersCount()} - 1) will reflect the latest added, still active pointer.
   * <p>
   * The order of parameters is irrelevant.
   *
   * @param firstPointerIndex  one of pointers indexes
   * @param secondPointerIndex one of pointers indexes
   * @return Distance between pointers in pixels.
   * @see #pointerIdList
   */
  public float getCurrentSpan(int firstPointerIndex, int secondPointerIndex) {
    if (!verifyPointers(firstPointerIndex, secondPointerIndex)) {
      throw new NoSuchElementException("There is no such pair of pointers!");
    }

    MultiFingerDistancesObject distancesObject = pointersDistanceMap.get(
      new PointerDistancePair(pointerIdList.get(firstPointerIndex), pointerIdList.get(secondPointerIndex)));

    return distancesObject.getCurrFingersDiffXY();
  }

  /**
   * Returns the previous distance between the two pointers forming the
   * gesture in progress.
   * <p>
   * Pointers are sorted by the time they were placed on the screen until lifted up.
   * This means that index 0 will reflect the oldest added, still active pointer
   * and index ({@link #getPointersCount()} - 1) will reflect the latest added, still active pointer.
   * <p>
   * The order of parameters is irrelevant.
   *
   * @param firstPointerIndex  one of pointers indexes
   * @param secondPointerIndex one of pointers indexes
   * @return Previous distance between pointers in pixels.
   * @see #pointerIdList
   */
  public float getPreviousSpan(int firstPointerIndex, int secondPointerIndex) {
    if (!verifyPointers(firstPointerIndex, secondPointerIndex)) {
      throw new NoSuchElementException("There is no such pair of pointers!");
    }

    MultiFingerDistancesObject distancesObject = pointersDistanceMap.get(
      new PointerDistancePair(pointerIdList.get(firstPointerIndex), pointerIdList.get(secondPointerIndex)));

    return distancesObject.getPrevFingersDiffXY();
  }

  /**
   * Returns current X distance between pointers in pixels.
   * <p>
   * Pointers are sorted by the time they were placed on the screen until lifted up.
   * This means that index 0 will reflect the oldest added, still active pointer
   * and index ({@link #getPointersCount()} - 1) will reflect the latest added, still active pointer.
   * <p>
   * The order of parameters is irrelevant.
   *
   * @param firstPointerIndex  one of pointers indexes
   * @param secondPointerIndex one of pointers indexes
   * @return Current X distance between pointers in pixels.
   * @see #pointerIdList
   */
  public float getCurrentSpanX(int firstPointerIndex, int secondPointerIndex) {
    if (!verifyPointers(firstPointerIndex, secondPointerIndex)) {
      throw new NoSuchElementException("There is no such pair of pointers!");
    }

    MultiFingerDistancesObject distancesObject = pointersDistanceMap.get(
      new PointerDistancePair(pointerIdList.get(firstPointerIndex), pointerIdList.get(secondPointerIndex)));

    return Math.abs(distancesObject.getCurrFingersDiffX());
  }

  /**
   * Returns current Y distance between pointers in pixels.
   * <p>
   * Pointers are sorted by the time they were placed on the screen until lifted up.
   * This means that index 0 will reflect the oldest added, still active pointer
   * and index ({@link #getPointersCount()} - 1) will reflect the latest added, still active pointer.
   * <p>
   * The order of parameters is irrelevant.
   *
   * @param firstPointerIndex  one of pointers indexes
   * @param secondPointerIndex one of pointers indexes
   * @return Current Y distance between pointers in pixels.
   * @see #pointerIdList
   */
  public float getCurrentSpanY(int firstPointerIndex, int secondPointerIndex) {
    if (!verifyPointers(firstPointerIndex, secondPointerIndex)) {
      throw new NoSuchElementException("There is no such pair of pointers!");
    }

    MultiFingerDistancesObject distancesObject = pointersDistanceMap.get(
      new PointerDistancePair(pointerIdList.get(firstPointerIndex), pointerIdList.get(secondPointerIndex)));

    return Math.abs(distancesObject.getCurrFingersDiffY());
  }

  /**
   * Returns previous X distance between pointers in pixels.
   * <p>
   * Pointers are sorted by the time they were placed on the screen until lifted up.
   * This means that index 0 will reflect the oldest added, still active pointer
   * and index ({@link #getPointersCount()} - 1) will reflect the latest added, still active pointer.
   * <p>
   * The order of parameters is irrelevant.
   *
   * @param firstPointerIndex  one of pointers indexes
   * @param secondPointerIndex one of pointers indexes
   * @return Previous X distance between pointers in pixels.
   * @see #pointerIdList
   */
  public float getPreviousSpanX(int firstPointerIndex, int secondPointerIndex) {
    if (!verifyPointers(firstPointerIndex, secondPointerIndex)) {
      throw new NoSuchElementException("There is no such pair of pointers!");
    }

    MultiFingerDistancesObject distancesObject = pointersDistanceMap.get(
      new PointerDistancePair(pointerIdList.get(firstPointerIndex), pointerIdList.get(secondPointerIndex)));

    return Math.abs(distancesObject.getPrevFingersDiffX());
  }

  /**
   * Returns previous Y distance between pointers in pixels.
   * <p>
   * Pointers are sorted by the time they were placed on the screen until lifted up.
   * This means that index 0 will reflect the oldest added, still active pointer
   * and index ({@link #getPointersCount()} - 1) will reflect the latest added, still active pointer.
   * <p>
   * The order of parameters is irrelevant.
   *
   * @param firstPointerIndex  one of pointers indexes
   * @param secondPointerIndex one of pointers indexes
   * @return Previous Y distance between pointers in pixels.
   * @see #pointerIdList
   */
  public float getPreviousSpanY(int firstPointerIndex, int secondPointerIndex) {
    if (!verifyPointers(firstPointerIndex, secondPointerIndex)) {
      throw new NoSuchElementException("There is no such pair of pointers!");
    }

    MultiFingerDistancesObject distancesObject = pointersDistanceMap.get(
      new PointerDistancePair(pointerIdList.get(firstPointerIndex), pointerIdList.get(secondPointerIndex)));

    return Math.abs(distancesObject.getPrevFingersDiffY());
  }

  private boolean verifyPointers(int firstPointerIndex, int secondPointerIndex) {
    return firstPointerIndex != secondPointerIndex && firstPointerIndex >= 0 && secondPointerIndex >= 0
      && firstPointerIndex < getPointersCount() && secondPointerIndex < getPointersCount();
  }

  /**
   * Returns the number of active pointers.
   *
   * @return number of active pointers.
   */
  public int getPointersCount() {
    return pointerIdList.size();
  }

  /**
   * Returns a center point of this gesture.
   *
   * @return center point of this gesture.
   */
  public PointF getFocalPoint() {
    return focalPoint;
  }

  /**
   * Get minimum span between any pair of finger that is required to pass motion events to this detector.
   *
   * @return minimum span
   */
  public float getSpanThreshold() {
    return spanThreshold;
  }

  /**
   * Set minimum span in pixels between any pair of finger that is required to pass motion events to this detector.
   * <p>
   * We encourage to set those values from dimens to accommodate for various screen sizes.
   *
   * @param spanThreshold minimum span
   */
  public void setSpanThreshold(float spanThreshold) {
    this.spanThreshold = spanThreshold;
  }

  /**
   * Set minimum span in dp between any pair of finger that is required to pass motion events to this detector.
   *
   * @param spanThresholdDimen minimum span
   */
  public void setSpanThresholdResource(@DimenRes int spanThresholdDimen) {
    setSpanThreshold(context.getResources().getDimension(spanThresholdDimen));
  }
}
