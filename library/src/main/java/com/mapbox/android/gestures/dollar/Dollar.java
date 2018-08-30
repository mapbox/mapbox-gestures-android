package com.mapbox.android.gestures.dollar;

import android.graphics.RectF;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.atan2;

public class Dollar {
  public static int squareSize = 1800;

  public static List<Double> preparePoints(List<Point> points) {
    points = resample(points, 120);
    Point centroid = Utils.centroid(points);
    double indicativeAngle = atan2(centroid.y - points.get(0).y, centroid.x - points.get(0).x);
    points = rotateBy(points, -indicativeAngle);
    points = scaleTo(points);
    points = translateTo(points, new Point(0, 0));
    return vectorize(points);
  }

  public MatchResult justDoIt(List<Point> points, List<Template> templates) {
    List<Double> vector = preparePoints(points);

    double b = Double.MAX_VALUE;
    int u = -1;
    for (int i = 0; i < templates.size(); i++) {
      double d = optimalCosineDistance(templates.get(i).getVector(), vector);

      if (d < b) {
        b = d;
        u = i;
      }
    }

    return u == -1 ? new MatchResult("No Match.", 0) :
      new MatchResult(templates.get(u).getName(), 1.0 / b);
  }

  private static List<Point> resample(List<Point> points, int n) {

    int I = pathLength(points) / (n - 1);
    double D = 0;

    List<Point> newPoints = new ArrayList<>();
    newPoints.add(points.get(0));

    for (int i = 1; i < points.size(); i++) {
      double d = Utils.distance(points.get(i - 1), points.get(i));

      if (D + d > I) {
        Point q = new Point(
          points.get(i - 1).x + ((I - D) / d) * (points.get(i).x - points.get(i - 1).x),
          points.get(i - 1).y + ((I - D) / d) * (points.get(i).y - points.get(i - 1).y)
        );
        newPoints.add(q);
        points.add(i, q);
        D = 0;
      } else {
        D += d;
      }
    }

    return newPoints;
  }

  private static int pathLength(List<Point> points) {
    int distance = 0;
    for (int i = 1; i < points.size(); i++) {
      distance += Utils.distance(points.get(i - 1), points.get(i));
    }
    return distance;
  }

  private static List<Point> rotateBy(List<Point> points, double radians) {
    Point centroid = Utils.centroid(points);
    List<Point> newList = new ArrayList<>(points.size() * 2);
    double cos = Math.cos(radians);
    double sin = Math.sin(radians);
    for (Point point : points) {
      double newX = (point.x - centroid.x) * cos - (point.y - centroid.y) * sin + centroid.x;
      double newY = (point.x - centroid.x) * sin + (point.y - centroid.y) * cos + centroid.y;
      newList.add(new Point(newX, newY));
    }

    return newList;
  }

  private static List<Point> scaleTo(List<Point> points) {
    RectF box = bbox(points);
    List<Point> newPoints = new ArrayList<>();
    for (Point point : points) {
      double newX = point.x * (squareSize / box.width());
      double newY = point.y * (squareSize / box.height());
      newPoints.add(new Point(newX, newY));
    }

    return newPoints;
  }

  private static RectF bbox(List<Point> points) {
    int maxX = 0;
    int minX = Integer.MAX_VALUE;
    int maxY = 0;
    int minY = Integer.MAX_VALUE;

    for (Point point : points) {
      minX = (int) Math.min(minX, point.x);
      minY = (int) Math.min(minY, point.y);
      maxX = (int) Math.max(maxX, point.x);
      maxY = (int) Math.max(maxY, point.y);
    }

    return new RectF(minX, minY, maxX - minX, maxY - minY);
  }

  private static List<Point> translateTo(List<Point> points, Point origin) {
    Point centroid = Utils.centroid(points);
    List<Point> newPoints = new ArrayList<>();
    for (Point point : points) {
      double newX = point.x + origin.x - centroid.x;
      double newY = point.y + origin.y - centroid.y;
      newPoints.add(new Point(newX, newY));
    }

    return newPoints;
  }

  private static List<Double> vectorize(List<Point> points) {
    double sum = 0;
    List<Double> vector = new ArrayList<>();

    for (Point point : points) {
      vector.add(point.x);
      vector.add(point.y);
      sum += point.x * point.x + point.y * point.y;
    }

    double magnitude = Math.sqrt(sum);
    for (int i = 0; i < vector.size(); i++) {
      double element = vector.get(i);
      element /= magnitude;
      vector.set(i, element);
    }

    return vector;
  }

  private double optimalCosineDistance(List<Double> vector1, List<Double> vector2) {
    double a = 0;
    double b = 0;
    for (int i = 0; i < Math.min(vector1.size(), vector2.size()); i += 2) {
      a += (vector1.get(i) * vector2.get(i) + vector1.get(i + 1) * vector2.get(i + 1));
      b += (vector1.get(i) * vector2.get(i + 1) - vector1.get(i + 1) * vector2.get(i));
    }

    double angle = Math.atan(b / a);
    return Math.acos(a * Math.cos(angle) + b * Math.sin(angle));
  }
}
