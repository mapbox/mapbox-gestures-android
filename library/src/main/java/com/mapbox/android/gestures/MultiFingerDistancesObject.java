package com.mapbox.android.gestures;

/**
 * Object that holds pixel current and previous distances between a pair of fingers.
 */
public class MultiFingerDistancesObject {
  private final float prevFingersDiffX;
  private final float prevFingersDiffY;
  private final float currFingersDiffX;
  private final float currFingersDiffY;

  public MultiFingerDistancesObject(float prevFingersDiffX, float prevFingersDiffY, float currFingersDiffX, float currFingersDiffY) {
    this.prevFingersDiffX = prevFingersDiffX;
    this.prevFingersDiffY = prevFingersDiffY;
    this.currFingersDiffX = currFingersDiffX;
    this.currFingersDiffY = currFingersDiffY;
  }

  public float getPrevFingersDiffX() {
    return prevFingersDiffX;
  }

  public float getPrevFingersDiffY() {
    return prevFingersDiffY;
  }

  public float getCurrFingersDiffX() {
    return currFingersDiffX;
  }

  public float getCurrFingersDiffY() {
    return currFingersDiffY;
  }
}
