package com.mapbox.android.gestures;

public final class Constants {

  /**
   * Default angle change required in rotation movement to register rotate gesture./
   */
  public static final float DEFAULT_ROTATE_ANGLE_THRESHOLD = 15.3f;

  /**
   * Default angle between pointers (starting from horizontal line) required to abort shove gesture.
   */
  public static final float DEFAULT_SHOVE_MAX_ANGLE = 20f;

  /**
   * Default time within which pointers need to leave the screen to register tap gesture.
   */
  public static final long DEFAULT_MULTI_TAP_TIME_THRESHOLD = 150L;


  /*Private constants*/
  static final String internal_scaleGestureDetectorMinSpanField = "mMinSpan";
  static final String internal_scaleGestureDetectorSpanSlopField = "mSpanSlop";
}
