package com.mapbox.android.gestures;

import android.util.Pair;

public class PointerDistancePair extends Pair<Integer, Integer> {

  public PointerDistancePair(Integer first, Integer second) {
    super(first, second);
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof PointerDistancePair) {
      if ((this.first.equals(((PointerDistancePair) o).first) && this.second.equals(((PointerDistancePair) o).second))
        || (this.first.equals(((PointerDistancePair) o).second) && this.second.equals(((PointerDistancePair) o).first))) {
        return true;
      }
    }

    return false;
  }
}
