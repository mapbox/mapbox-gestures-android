package com.mapbox.android.gestures;

import android.content.Context;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.view.MotionEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.mapbox.android.gestures.AndroidGesturesManager.GESTURE_TYPE_MOVE;

/**
 * Gesture detector handling move gesture.
 * <p>
 * {@link MoveGestureDetector} serves similar purpose to
 * {@link com.mapbox.android.gestures.StandardGestureDetector.StandardOnGestureListener
 * #onScroll(MotionEvent, MotionEvent, float, float)}, however, it's a {@link ProgressiveGesture} that
 * introduces {@link OnMoveGestureListener#onMoveBegin(MoveGestureDetector)},
 * {@link OnMoveGestureListener#onMoveEnd(MoveGestureDetector)},
 * threshold with {@link MoveGestureDetector#setMoveThreshold(float)} and multi finger support thanks to
 * {@link MoveDistancesObject}.
 */
@UiThread
public class MoveGestureDetector extends ProgressiveGesture<MoveGestureDetector.OnMoveGestureListener> {
  private static final int MOVE_REQUIRED_POINTERS_COUNT = 1;
  private static final Set<Integer> handledTypes = new HashSet<>();
  private PointF previousFocalPoint;
  private boolean resetFocal;
  float focalDistanceX;
  float focalDistanceY;

  static {
    handledTypes.add(GESTURE_TYPE_MOVE);
  }

  private float moveThreshold = Constants.DEFAULT_MOVE_THRESHOLD;
  private final Map<Integer, MoveDistancesObject> moveDistancesObjectMap = new HashMap<>();

  protected MoveGestureDetector(Context context, AndroidGesturesManager gesturesManager) {
    super(context, gesturesManager);
  }

  @NonNull
  @Override
  protected Set<Integer> provideHandledTypes() {
    return handledTypes;
  }

  public interface OnMoveGestureListener {
    /**
     * Indicates that the move gesture started.
     *
     * @param detector this detector
     * @return true if you want to receive subsequent {@link #onMove(MoveGestureDetector, float, float)} callbacks,
     * false if you want to ignore this gesture.
     */
    boolean onMoveBegin(MoveGestureDetector detector);

    /**
     * Called for every move change during the gesture.
     *
     * @param detector this detector
     * @return true if the gesture was handled, false otherwise
     */
    boolean onMove(MoveGestureDetector detector, float distanceX, float distanceY);

    /**
     * Indicates that the move gesture ended.
     *
     * @param detector this detector
     */
    void onMoveEnd(MoveGestureDetector detector);
  }

  public static class SimpleOnMoveGestureListener implements OnMoveGestureListener {

    @Override
    public boolean onMoveBegin(MoveGestureDetector detector) {
      return true;
    }

    @Override
    public boolean onMove(MoveGestureDetector detector, float distanceX, float distanceY) {
      return false;
    }

    @Override
    public void onMoveEnd(MoveGestureDetector detector) {
      // No implementation
    }
  }

  @Override
  protected boolean analyzeEvent(MotionEvent motionEvent) {
    switch (motionEvent.getActionMasked()) {
      case MotionEvent.ACTION_DOWN:
      case MotionEvent.ACTION_POINTER_DOWN:
        resetFocal = true; //recalculating focal point

        float x = motionEvent.getX(motionEvent.getActionIndex());
        float y = motionEvent.getY(motionEvent.getActionIndex());
        moveDistancesObjectMap.put(
          motionEvent.getPointerId(motionEvent.getActionIndex()),
          new MoveDistancesObject(x, y)
        );
        break;

      case MotionEvent.ACTION_UP:
      case MotionEvent.ACTION_POINTER_UP:
        resetFocal = true; //recalculating focal point

        moveDistancesObjectMap.remove(motionEvent.getPointerId(motionEvent.getActionIndex()));
        break;

      default:
        break;
    }

    return super.analyzeEvent(motionEvent);
  }

  @Override
  protected boolean analyzeMovement() {
    super.analyzeMovement();
    updateMoveDistancesObjects();

    if (isInProgress()) {
      PointF currentFocalPoint = getFocalPoint();
      focalDistanceX = previousFocalPoint.x - currentFocalPoint.x;
      focalDistanceY = previousFocalPoint.y - currentFocalPoint.y;
      previousFocalPoint = currentFocalPoint;
      if (resetFocal) {
        resetFocal = false;
        return listener.onMove(this, 0, 0);
      }
      return listener.onMove(this, focalDistanceX, focalDistanceY);
    } else if (canExecute(GESTURE_TYPE_MOVE)) {
      if (listener.onMoveBegin(this)) {
        gestureStarted();
        previousFocalPoint = getFocalPoint();
        resetFocal = false;
        return true;
      }
    }
    return false;
  }

  private void updateMoveDistancesObjects() {
    for (int pointerId : pointerIdList) {
      moveDistancesObjectMap.get(pointerId).addNewPosition(
        getCurrentEvent().getX(getCurrentEvent().findPointerIndex(pointerId)),
        getCurrentEvent().getY(getCurrentEvent().findPointerIndex(pointerId))
      );
    }
  }

  boolean checkAnyMoveAboveThreshold() {
    for (MoveDistancesObject moveDistancesObject : moveDistancesObjectMap.values()) {
      if (Math.abs(moveDistancesObject.getDistanceXSinceStart()) > moveThreshold
        || Math.abs(moveDistancesObject.getDistanceYSinceStart()) > moveThreshold) {
        return true;
      }
    }
    return false;
  }

  @Override
  protected boolean canExecute(int invokedGestureType) {
    return super.canExecute(invokedGestureType) && checkAnyMoveAboveThreshold();
  }

  @Override
  protected void reset() {
    super.reset();
    moveDistancesObjectMap.clear();
  }

  @Override
  protected void gestureStopped() {
    super.gestureStopped();
    listener.onMoveEnd(this);
  }

  @Override
  protected int getRequiredPointersCount() {
    return MOVE_REQUIRED_POINTERS_COUNT;
  }

  /**
   * Get the delta pixel threshold required to qualify it as a move gesture.
   *
   * @return delta pixel threshold
   */
  public float getMoveThreshold() {
    return moveThreshold;
  }

  /**
   * Set the delta pixel threshold required to qualify it as a move gesture.
   *
   * @param moveThreshold delta pixel threshold
   */
  public void setMoveThreshold(float moveThreshold) {
    this.moveThreshold = moveThreshold;
  }

  /**
   * Get the default delta pixel threshold required to qualify it as a move gesture.
   *
   * @return delta pixel threshold
   * @see Constants#DEFAULT_MOVE_THRESHOLD
   */
  public float getDefaultMoveThreshold() {
    return Constants.DEFAULT_MOVE_THRESHOLD;
  }

  /**
   * Returns {@link MoveDistancesObject} referencing the pointer held under passed index.
   * <p>
   * Pointers are sorted by the time they were placed on the screen until lifted up.
   * This means that index 0 will reflect the oldest added, still active pointer
   * and index ({@link #getPointersCount()} - 1) will reflect the latest added, still active pointer.
   * <p>
   *
   * @param pointerIndex pointer's index
   * @return distances object of the referenced pointer
   */
  public MoveDistancesObject getMoveObject(int pointerIndex) {
    if (isInProgress()) {
      if (pointerIndex >= 0 && pointerIndex < getPointersCount()) {
        return moveDistancesObjectMap.get(pointerIdList.get(pointerIndex));
      }
    }
    return null;
  }
}
