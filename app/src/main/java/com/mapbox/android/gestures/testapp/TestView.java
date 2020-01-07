package com.mapbox.android.gestures.testapp;

import android.content.Context;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.mapbox.android.gestures.AndroidGesturesManager;

public class TestView extends View {

  private AndroidGesturesManager androidGesturesManager;

  public TestView(Context context) {
    this(context, null);
  }

  public TestView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  private void init(Context context) {
    androidGesturesManager = new AndroidGesturesManager(context);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    return androidGesturesManager.onTouchEvent(event) || super.onTouchEvent(event);
  }
}
