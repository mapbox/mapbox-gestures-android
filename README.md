# Mapbox Gestures for Android
This library wraps [GestureDetectorCompat](https://developer.android.com/reference/android/support/v4/view/GestureDetectorCompat.html) and [ScaleGestureDetector](https://developer.android.com/reference/android/view/ScaleGestureDetector.html) as well as introduces implementation of rotate, move, shove and tap gesture detectors.

`Mapbox Gestures for Android` was inspired by [Android Gesture Detector Framework](https://github.com/Almeros/android-gesture-detectors) and offers the same functionality with some additional features on top.

The library is implemented in the projects found below where you can head for more examples:
- [Mapbox Maps SDK for Android](https://github.com/mapbox/mapbox-gl-native)
- [Sample App](https://github.com/mapbox/mapbox-gestures-android/tree/master/TestApp) included in this repository

Are you using the library in your project as well? Let us know or create a PR, we'll be more than happy to add it to the list!

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
You can set thresholds for supported gestures, which means that gesture detector won't fire until the threshold (like minimum rotation angle) is met. This allows you to personalize gestures experience however you like.

We encourage to set thresholds using `dimen` values, rather than raw pixels, to accommodate for various screen sizes and pixel densities across Android devices. For example:

```
androidGesturesManager.getStandardScaleGestureDetector()
.setSpanSinceStartThresholdResource(R.dimen.scaleSpanSinceStartThreshold);
```
and for thresholds that are not expressed in pixels:
```
androidGesturesManager.getRotateGestureDetector().setAngleThreshold(ROTATE_ANGLE_THRESHOLD);
```

#### Velocity
Each progressive gesture with its respective `#onEnd()` callback will provide `X velocity` and `Y velocity` of the gesture at the moment of pointers leaving the screen.

#### Enable/disable and interrupt
Every gesture detector can be enabled/disable at any point in time using `#setEnabled()` method.

Additionally, every progressive gesture can be interrupted, which will force it to meet start conditions again in order to resume. Popular use case would be to increase gesture's threshold when other is detected:
```
    @Override
    public boolean onScaleBegin(StandardScaleGestureDetector detector) {
      // forbid movement when scaling
      androidGesturesManager.getMoveGestureDetector().setEnabled(false); // this interrupts a gesture as well
    
      // increase rotate angle threshold when scale is detected, then interrupt to force re-check
      RotateGestureDetector rotateGestureDetector = androidGesturesManager.getRotateGestureDetector();
      rotateGestureDetector.setAngleThreshold(ROTATION_THRESHOLD_WHEN_SCALING);
      rotateGestureDetector.interrupt();

      return true;
    }
        
    @Override
    public boolean onScale(StandardScaleGestureDetector detector) {
      float scaleFactor = detector.getScaleFactor();
      ...
      ...
      return true;
    }
    
    @Override
    public void onScaleEnd(StandardScaleGestureDetector detector) {
      // revert thresholds values
      RotateGestureDetector rotateGestureDetector = androidGesturesManager.getRotateGestureDetector();
      rotateGestureDetector.setAngleThreshold(Constants.DEFAULT_ROTATE_ANGLE_THRESHOLD);
    }
```

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

#### MoveGestureDetector
Behaves similarly to `#onScroll()` contained in the `StandardGestureDetector`, however, its a `ProgressiveGesture` that enables better filtering options, as well as thresholds.

## Version
Current stable version available on maven is `0.1.0`:
```
implementation 'com.mapbox.mapboxsdk:mapbox-android-gestures:0.1.0'
```
Noting here, that `0.x` versions series of `Mapbox Gestures for Android` is still in experimental faze and breaking changes can occur with every iteration.

## Snapshots
Feel free to test out snapshots that are built with every new commit to the `master` branch.

Add snapshot repository path
```
allprojects {
    repositories {
        google()
        jcenter()
        maven { url "http://oss.sonatype.org/content/repositories/snapshots/" }
    }
}
```
and include snapshot dependency
```
implementation 'com.mapbox.mapboxsdk:mapbox-android-gestures:0.2.0-SNAPSHOT'
```