# Changelog for the Mapbox Gestures for Android

## 0.7.0 - April 30, 2020
#### Minor features
 - Expose a `MoveGestureDetector#moveThresholdRect`. When set, the defined screen area prohibits move gesture to be started. [#96](https://github.com/mapbox/mapbox-gestures-android/pull/96)

## 0.6.0 - January 8, 2020
#### Major changes
 - Replace appcompat dependencies with AndroidX [#94](https://github.com/mapbox/mapbox-gestures-android/pull/94)

#### Minor features and bug fixes
 - Use NonNull in gesture listener interfaces to improve consumption with the Kotlin programming language [#95](https://github.com/mapbox/mapbox-gestures-android/pull/95)

## 0.5.1 - August 20, 2019
#### Bug fixes
- Fixed a bug where quick-scale was registered during a move gesture that followed a double-tap [#88](https://github.com/mapbox/mapbox-gestures-android/pull/88)

## 0.5.0 - August 14, 2019
#### Major changes
- Introduce a custom scale gesture detector implementation. The library doesn't rely on the compat gesture detector anymore. This is a breaking changing because the underlying scale gesture detector reference has been removed. [#73](https://github.com/mapbox/mapbox-gestures-android/pull/73)

#### Minor features and bug fixes
- Calculate focal point for every motion event, not only MOVE. Fixes an issue where detectors that do not rely on movement would return cached, historic focal points. [#77](https://github.com/mapbox/mapbox-gestures-android/pull/77)
- Adjust scale gesture's required pointer count based on type. Fixes an issue where quick-scale was not properly interrupted. [#74](https://github.com/mapbox/mapbox-gestures-android/pull/74)
- Guard against move events coming from different view trees. Might prevent rare crashes that are out of control of the gestures library. [#71](https://github.com/mapbox/mapbox-gestures-android/pull/71)
- Expose scale span getters. [#75](https://github.com/mapbox/mapbox-gestures-android/pull/75)

## 0.4.2 - April 26, 2019
 - Query display metrics only in touch down [#67](https://github.com/mapbox/mapbox-gestures-android/pull/67)

## 0.4.1 - April 16, 2019
 - Try getting real device display metrics for sloppy gesture calculations [#61](https://github.com/mapbox/mapbox-gestures-android/pull/61)
 - Remove obsolete string values [#62](https://github.com/mapbox/mapbox-gestures-android/pull/62)

## 0.4.0 - January 31, 2019
 - Removed Timber dependency [#54](https://github.com/mapbox/mapbox-gestures-android/pull/54)
 - Prepare the project to be consumed as a submodule [#55](https://github.com/mapbox/mapbox-gestures-android/pull/55)
 - Update tooling and CI image [#56](https://github.com/mapbox/mapbox-gestures-android/pull/56)
 - Remove deprecated javadoc source declaration [#58](https://github.com/mapbox/mapbox-gestures-android/pull/58)
 - Exclude maven plugin and checkstyle from the child build.gradle [#57](https://github.com/mapbox/mapbox-gestures-android/pull/57)

## 0.3.0 - October 30, 2018
 - Increase missing events protection [#46](https://github.com/mapbox/mapbox-gestures-android/pull/46)
 - Limit support library usage [#47](https://github.com/mapbox/mapbox-gestures-android/pull/47)

## 0.2.0 - March 27, 2018
 - SidewaysShoveGestureDetector [#27](https://github.com/mapbox/mapbox-gestures-android/pull/27)
 - Decrease minimum span required to register scale gesture [#30](https://github.com/mapbox/mapbox-gestures-android/pull/30)

## 0.1.0 - March 19, 2018
 - Initial release!