# Mapbox Gestures for Android

The Mapbox Gestures for Android library wraps [GestureDetectorCompat](https://developer.android.com/reference/android/support/v4/view/GestureDetectorCompat.html) and introduces implementation of scale, rotate, move, shove and tap gesture detectors.

Mapbox Gestures for Android was inspired by [Android Gesture Detector Framework](https://github.com/Almeros/android-gesture-detectors) and offers the same functionality with some additional features on top.

The library is implemented in the projects found below, where you can head for more examples:

- [The Mapbox Maps SDK for Android](https://github.com/mapbox/mapbox-maps-android)
- [This library's sample app](https://github.com/mapbox/mapbox-gestures-android/tree/master/app/src/main/java/com/mapbox/android/gestures/testapp) included in this repository

Are you using the library in your project as well? Let us know or create a PR, we'll be more than happy to add it to the list!


## Documentation

You'll find all of this library's documentation on [our Maps Guides user interaction page](https://docs.mapbox.com/android/maps/guides/user-interaction/). This includes information on installation, using the API, and links to the API reference.


## Getting Started

If you are looking to include Mapbox Gestures for Android inside of your project, please take a look at [the detailed instructions](https://docs.mapbox.com/android/maps/guides/user-interaction/) found in our docs. If you are interested in building from source, read the contributing guide inside of this project.

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
    implementation 'com.mapbox.mapboxsdk:mapbox-android-gestures:0.8.0'
}
```

The library is published to Maven Central and SNAPSHOTS are available whenever new code is pushed to this repo's `master` branch for testing the latest build:

```java
// In the root build.gradle file
repositories {
	jcenter()
	maven { url 'https://oss.jfrog.org/artifactory/oss-snapshot-local/' }
}
```
```java
// In the app build.gradle file
dependencies {
	implementation 'com.mapbox.mapboxsdk:mapbox-android-gestures:0.9.0-SNAPSHOT'
}
```

#### Mapbox access tokens

To build test application you need to configure Mapbox access tokens as described at https://docs.mapbox.com/android/maps/guides/install/#configure-credentials.
To build the project you need to specify SDK_REGISTRY_TOKEN as an environmental variable or a gradle property. It is a secret token used to access the SDK Registry (Mapbox Maven instance) during compile time, with a scope set to `DOWNLOADS:READ`.
To run the specific Mapbox activity in this repo's test application, you need to include public token in the [app/src/main/res/values/developer-config.xml] resource file.

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



