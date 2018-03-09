package com.mapbox.android.gestures;

/**
 * Class that holds initial, previous and current X and Y on-screen coordinates for active pointers.
 */
public final class MoveDistancesObject {
  private final float initialX;
  private final float initialY;

  private float prevX;
  private float prevY;
  private float currX;
  private float currY;

  private float distanceXSinceLast;
  private float distanceYSinceLast;
  private float distanceXSinceStart;
  private float distanceYSinceStart;

  public MoveDistancesObject(float initialX, float initialY) {
    this.initialX = initialX;
    this.initialY = initialY;
  }

  /**
   * Add a new position of this pointer and recalculate distances.
   * @param x new X coordinate
   * @param y new Y coordinate
   */
  public void addNewPosition(float x, float y) {
    prevX = currX;
    prevY = currY;

    currX = x;
    currY = y;

    distanceXSinceLast = prevX - currX;
    distanceYSinceLast = prevY - currY;

    distanceXSinceStart = initialX - currX;
    distanceYSinceStart = initialY - currY;
  }

  /**
   * Get X coordinate of this pointer when it was first register.
   * @return X coordinate
   */
  public float getInitialX() {
    return initialX;
  }

  /**
   * Get Y coordinate of this pointer when it was first register.
   * @return Y coordinate
   */
  public float getInitialY() {
    return initialY;
  }

  /**
   * Get previous X coordinate of this pointer.
   * @return X coordinate
   */
  public float getPreviousX() {
    return prevX;
  }

  /**
   * Get previous Y coordinate of this pointer.
   * @return Y coordinate
   */
  public float getPreviousY() {
    return prevY;
  }

  /**
   * Get current X coordinate of this pointer.
   * @return X coordinate
   */
  public float getCurrentX() {
    return currX;
  }

  /**
   * Get current Y coordinate of this pointer.
   * @return Y coordinate
   */
  public float getCurrentY() {
    return currY;
  }

  /**
   * Get X distance covered by this pointer since previous position.
   * @return X distance
   */
  public float getDistanceXSinceLast() {
    return distanceXSinceLast;
  }

  /**
   * Get Y distance covered by this pointer since previous position.
   * @return Y distance
   */
  public float getDistanceYSinceLast() {
    return distanceYSinceLast;
  }

  /**
   * Get X distance covered by this pointer since start position.
   * @return X distance
   */
  public float getDistanceXSinceStart() {
    return distanceXSinceStart;
  }

  /**
   * Get Y distance covered by this pointer since start position.
   * @return Y distance
   */
  public float getDistanceYSinceStart() {
    return distanceYSinceStart;
  }
}
