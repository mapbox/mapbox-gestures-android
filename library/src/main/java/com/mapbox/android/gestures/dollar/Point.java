package com.mapbox.android.gestures.dollar;

public class Point {
  public double x, y;

  public Point(double x, double y) {
    this.x = x;
    this.y = y;
  }

  public void copy(Point src) {
    x = src.x;
    y = src.y;
  }

  @Override
  public String toString() {
    return "new Point(" + x + ", " + y + ")";
  }
}
