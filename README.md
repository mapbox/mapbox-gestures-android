# Mapbox Gestures for Android

The Mapbox Gestures for Android library wraps [GestureDetectorCompat](https://developer.android.com/reference/android/support/v4/view/GestureDetectorCompat.html) and [ScaleGestureDetector](https://developer.android.com/reference/android/view/ScaleGestureDetector.html) as well as introduces implementation of rotate, move, shove and tap gesture detectors.

Mapbox Gestures for Android was inspired by [Android Gesture Detector Framework](https://github.com/Almeros/android-gesture-detectors) and offers the same functionality with some additional features on top.

The library is implemented in the projects found below, where you can head for more examples:

- [The Mapbox Maps SDK for Android](https://github.com/mapbox/mapbox-gl-native)
- [This library's sample app](https://github.com/mapbox/mapbox-gestures-android/tree/master/app/src/main/java/com/mapbox/android/gestures/testapp) included in this repository

Are you using the library in your project as well? Let us know or create a PR, we'll be more than happy to add it to the list!


## Documentation

You'll find all of this library's documentation on [our Mapbox Gestures page](https://www.mapbox.com/android-docs/map-sdk/overview/gestures). This includes information on installation, using the API, and links to the API reference.


## Getting Started

If you are looking to include Mapbox Gestures for Android inside of your project, please take a look at [the detailed instructions](https://www.mapbox.com/android-docs/map-sdk/overview/gestures/) found in our docs. If you are interested in building from source, read the contributing guide inside of this project.

To use the Gestures library, include it in your app-level `build.gradle` file.

```java
// In the root build.gradle file
repositories {
    mavenCentral()
}
```

```java
// In the app build.gradle file
dependencies {
    implementation 'com.mapbox.mapboxsdk:mapbox-android-gestures:0.4.0'
}
```

The library is published to Maven Central and SNAPSHOTS are available whenever new code is pushed to this repo's `master` branch for testing the latest build:

```java
// In the root build.gradle file
repositories {
    mavenCentral()
    maven { url "http://oss.sonatype.org/content/repositories/snapshots/" }
}
```
```java
// In the app build.gradle file
dependencies {
	implementation 'com.mapbox.mapboxsdk:mapbox-android-gestures:0.5.0-SNAPSHOT'
}
```

To run the specific Mapbox activity in this repo's test application, include your [developer access token](https://www.mapbox.com/help/define-access-token/) in the `developer-config.xml` file. An access token is not required to run this repo's test application.

## Getting Help

- **Need help with your code?**: Look for previous questions on the [#mapbox tag](https://stackoverflow.com/questions/tagged/mapbox+android) — or [ask a new question](https://stackoverflow.com/questions/tagged/mapbox+android).
- **Have a bug to report?** [Open an issue](https://github.com/mapbox/mapbox-gestures-android/issues). If possible, include the version of Mapbox Core that you're using, a full log, and a project that shows the issue.
- **Have a feature request?** [Open an issue](https://github.com/mapbox/mapbox-gestures-android/issues/new). Tell us what the feature should do and why you want the feature.

## Sample code

[This repo's test app](https://github.com/mapbox/mapbox-gestures-android/tree/master/app/src/main/java/com/mapbox/android/gestures/testapp) can also help you get started with the Gestures library.

## Contributing

We welcome feedback, translations, and code contributions! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for details.

## Version

Noting here, that `0.x` versions series of `Mapbox Gestures for Android` is still in an experimental phase. Breaking changes can occur with every iteration.



