# Mapbox Gestures for Android
This library wraps [GestureDetectorCompat](https://developer.android.com/reference/android/support/v4/view/GestureDetectorCompat.html) and [ScaleGestureDetector](https://developer.android.com/reference/android/view/ScaleGestureDetector.html) as well as introduces implementation of rotate, shove and tap gesture detectors.

## Usage
To start gestures processing you have to instantiate `AndroidGestureManager`, set any gesture listeners that you are interested in and pass all `MotionEvent` objects from your Activity/Fragment to `AndroidGestureManager#onTouchEvent()`.

#### Mutually exclusive gestures
Thanks to the single entry point to all gesture detectors with `AndroidGestureManager` class, we are able to introduce mutually exclusive gestures.

This means that you can pass a list of `GestureType` sets and whenever a gesture is detected it will check whether there are any `ProgressiveGesture`s currently started that are contained within the same set. If there are any, listener for our detected gesture will not be notified.

You can pass mutually exclusive gesture sets in a constructor of `AndroidGestureManager` or with `AndroidGestureManager#setMutuallyExclusiveGestures()`.

Example:
```
    Set<Integer> mutuallyExclusive1 = new HashSet<>();
    mutuallyExclusive1.add(AndroidGesturesManager.GESTURE_TYPE_SHOVE);
    mutuallyExclusive1.add(AndroidGesturesManager.GESTURE_TYPE_SCROLL);

    Set<Integer> mutuallyExclusive2 = new HashSet<>();
    mutuallyExclusive2.add(AndroidGesturesManager.GESTURE_TYPE_SHOVE);
    mutuallyExclusive2.add(AndroidGesturesManager.GESTURE_TYPE_SCALE);

    AndroidGesturesManager androidGesturesManager = new AndroidGesturesManager(
      context,
      mutuallyExclusive1,
      mutuallyExclusive2
    );
```

The first set makes it certain that when we detect shove, we will no longer be notified about scroll (shove will be able to execute because scroll is not a `ProgressiveGesture`).
The second, that when we detect either shove or scale we won't be notified about the other one until the first gesture finishes.

#### Thresholds
You can set thresholds for supported gestures, that allows you to personalize gestures experience however you like.

As an example, let's say that we want to make it harder to trigger rotate gesture whenever we are scaling (but not filter it out completely with mutually exclusive gestures). All we have to do is increase the threshold whenever the scaling starts and reset it when ends:

```
    androidGesturesManager.setStandardScaleGestureListener(new StandardScaleGestureDetector.SimpleStandardOnScaleGestureListener() {
      @Override
      public boolean onScale(StandardScaleGestureDetector detector) {
        float scaleFactor = detector.getScaleFactor();
        // scale
        return true;
      }

      @Override
      public boolean onScaleBegin(StandardScaleGestureDetector detector) {
        RotateGestureDetector rotateGestureDetector = androidGesturesManager.getRotateGestureDetector();
        rotateGestureDetector.setAngleThreshold(25f);
        return true;
      }

      @Override
      public void onScaleEnd(StandardScaleGestureDetector detector) {
        RotateGestureDetector rotateGestureDetector = androidGesturesManager.getRotateGestureDetector();
        rotateGestureDetector.setAngleThreshold(rotateGestureDetector.getDefaultAngleThreshold());
      }
    });
```

#### Velocity animators
Each gesture that supports velocity animators (`RotateGestureDetector` and `ShoveGestureDetector` at the moment) have a callback that will be invoked for a certain period of time after the fingers are lifted simulating a velocity stimulated progress of a gesture that will fade away with a provided [Interpolator](https://developer.android.com/reference/android/view/animation/Interpolator.html).

## Detectors
With this library you will be able to recognize gestures using detectors provided by the Support Library and more.

#### StandardGestureDetector
Wraps [GestureDetectorCompat](https://developer.android.com/reference/android/support/v4/view/GestureDetectorCompat.html) exposed via the Support Library that recognizes gestures like tap, double tap or scroll.

#### StandardScaleGestureDetector
Wraps [ScaleGestureDetector](https://developer.android.com/reference/android/view/ScaleGestureDetector.html) exposed via the Support Library that recognizes scale/pinch gesture.

#### MultiFingerTapGestureDetector
Simple gesture detector that notify listeners whenever a multi finger tap occurred and how many fingers where involved.

#### RotateGestureDetector
A detector that finds the angle difference between previous and current line made with two pointers (fingers).

#### ShoveGestureDetector
Detects a vertical movement of two pointers if they are placed within a certain horizontal angle.