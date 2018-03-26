package com.mapbox.android.gestures.testapp;

import android.app.Application;

import timber.log.Timber;

public class MyApplication extends Application {

  @Override
  public void onCreate() {
    super.onCreate();

    if (BuildConfig.DEBUG) {
      Timber.plant(new Timber.DebugTree());
    }
  }
}
