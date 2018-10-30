package com.mapbox.android.gestures;

import android.support.annotation.IntRange;
import android.view.MotionEvent;

class PermittedActionsGuard {

  /**
   * Enables to store up to 8 permitted actions in the long type (64 bits / 8 bits per action).
   */
  private static final int BITS_PER_PERMITTED_ACTION = 8;
  private static final int PERMITTED_ACTION_MASK = ((1 << BITS_PER_PERMITTED_ACTION) - 1);
  private static final int NO_ACTION_PERMITTED = 0b11111111;

  boolean isMissingActions(int action,
                           @IntRange(from = 0) int eventPointerCount,
                           @IntRange(from = 0) int internalPointerCount) {
    long permittedActions = updatePermittedActions(eventPointerCount, internalPointerCount);
    if (action == permittedActions) {
      // this will only happen for action == permittedActions == ACTION_DOWN
      return false;
    }

    while (permittedActions != 0) {
      // get one of actions, the one on the first BITS_PER_PERMITTED_ACTION bits
      long testCase = permittedActions & PERMITTED_ACTION_MASK;
      if (action == testCase) {
        // we got a match, all good
        return false;
      }

      // remove the one we just checked and iterate
      permittedActions = permittedActions >> BITS_PER_PERMITTED_ACTION;
    }

    // no available matching actions, we are missing some!
    return true;
  }

  /**
   * Returns all acceptable at this point MotionEvent actions based on the pointers state.
   * Each one of them is written on {@link #BITS_PER_PERMITTED_ACTION} successive bits.
   */
  private long updatePermittedActions(@IntRange(from = 0) int eventPointerCount,
                                      @IntRange(from = 0) int internalPointerCount) {
    long permittedActions = MotionEvent.ACTION_DOWN;

    if (internalPointerCount == 0) {
      // only ACTION_DOWN available when no other pointers registered
      permittedActions = permittedActions << BITS_PER_PERMITTED_ACTION;
      permittedActions += MotionEvent.ACTION_DOWN;
    } else {
      if (Math.abs(eventPointerCount - internalPointerCount) > 1) {
        // missing a pointer up/down event, required to start over
        return NO_ACTION_PERMITTED;
      } else {
        if (eventPointerCount > internalPointerCount) {
          // event holds one more pointer than we have locally
          permittedActions = permittedActions << BITS_PER_PERMITTED_ACTION;
          permittedActions += MotionEvent.ACTION_POINTER_DOWN;
        } else if (eventPointerCount < internalPointerCount) {
          // event holds one less pointer than we have locally. This indicates that we are missing events,
          // because ACTION_UP and ACTION_POINTER_UP events still return not decremented pointer count
          return NO_ACTION_PERMITTED;
        } else {
          // event holds an equal number of pointers compared to the local count
          if (eventPointerCount == 1) {
            permittedActions = permittedActions << BITS_PER_PERMITTED_ACTION;
            permittedActions += MotionEvent.ACTION_UP;
          } else {
            permittedActions = permittedActions << BITS_PER_PERMITTED_ACTION;
            permittedActions += MotionEvent.ACTION_POINTER_UP;
          }
          permittedActions = permittedActions << BITS_PER_PERMITTED_ACTION;
          permittedActions += MotionEvent.ACTION_MOVE;
        }
      }
    }

    return permittedActions;
  }
}
