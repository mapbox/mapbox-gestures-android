package com.mapbox.android.gestures.dollar;

public class MatchResult {
  private String match;
  private double maxScore;

  public MatchResult(String match, double maxScore) {
    this.match = match;
    this.maxScore = maxScore;
  }

  public String getMatch() {
    return match;
  }

  public double getMaxScore() {
    return maxScore;
  }
}
