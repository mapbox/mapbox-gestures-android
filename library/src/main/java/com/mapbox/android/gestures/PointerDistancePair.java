package com.mapbox.android.gestures;

import android.util.Pair;

public class PointerDistancePair extends Pair<Integer, Integer> {

  public PointerDistancePair(Integer first, Integer second) {
    super(first, second);
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof PointerDistancePair) {
      PointerDistancePair otherPair = (PointerDistancePair) o;
      if ((this.first.equals(otherPair.first) && this.second.equals(otherPair.second))
        || (this.first.equals(otherPair.second) && this.second.equals(otherPair.first))) {
        return true;
      }
    }

    return false;
  }
}
