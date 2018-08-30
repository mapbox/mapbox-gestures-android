package com.mapbox.android.gestures.dollar;

import java.util.ArrayList;
import java.util.List;

public class Utils {

  public static List<Point> translate(List<Point> points, Point centroid) {
    List<Point> newPoints = new ArrayList<>(points.size());

    for (Point p : points) {
      double qx = p.x - centroid.x;
      double qy = p.y - centroid.y;
      newPoints.add(new Point(qx, qy));
    }
    return newPoints;
  }

  public static double distance(Point p1, Point p2) {
    double dx = p2.x - p1.x;
    double dy = p2.y - p1.y;
    return Math.sqrt(dx * dx + dy * dy);
  }

  public static Point centroid(List<Point> points) {
    double sumX = 0.0;
    double sumY = 0.0;

    for (Point p : points) {
      sumX += p.x;
      sumY += p.y;
    }
    return new Point(sumX / points.size(), sumY / points.size());
  }
}

