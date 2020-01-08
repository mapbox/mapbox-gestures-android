package com.mapbox.android.gestures.testapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MotionEvent;

import com.mapbox.android.gestures.AndroidGesturesManager;

public class TestActivity extends AppCompatActivity {

  public AndroidGesturesManager gesturesManager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_test);
    gesturesManager = new AndroidGesturesManager(this);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    return gesturesManager.onTouchEvent(event) || super.onTouchEvent(event);
  }
}
