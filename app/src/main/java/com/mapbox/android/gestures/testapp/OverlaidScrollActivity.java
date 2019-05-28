package com.mapbox.android.gestures.testapp;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;

public class OverlaidScrollActivity extends AppCompatActivity {

  private TestView testView;

  @SuppressLint("ClickableViewAccessibility")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_overlaid_scroll);

    testView = findViewById(R.id.testView);
    findViewById(R.id.scroll).setOnTouchListener((v, event) -> {
      if (isTouchInView(findViewById(R.id.spacer), event)) {
        testView.onTouchEvent(event);
        return true;
      }
      return false;
    });
  }

  public static boolean isTouchInView(View view, MotionEvent event) {
    if (view == null || event == null) {
      return false;
    }
    Rect hitBox = new Rect();
    view.getGlobalVisibleRect(hitBox);
    return hitBox.contains((int) event.getRawX(), (int) event.getRawY());
  }
}
