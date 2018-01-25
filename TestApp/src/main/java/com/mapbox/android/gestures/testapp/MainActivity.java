package com.mapbox.android.gestures.testapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;

import com.mapbox.android.gestures.AndroidGesturesManager;
import com.mapbox.android.gestures.MultiFingerTapGestureDetector;
import com.mapbox.android.gestures.RotateGestureDetector;
import com.mapbox.android.gestures.ShoveGestureDetector;
import com.mapbox.android.gestures.StandardGestureDetector;
import com.mapbox.android.gestures.StandardScaleGestureDetector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

  private ImageView icon;
  private Button velocityButton;
  private Spinner mutuallyExclusiveSpinner;
  private SeekBar rotateThresholdProgress;
  private SeekBar scaleThresholdProgress;

  private AndroidGesturesManager androidGesturesManager;
  private boolean velocityEnabled;

  private final ExclusiveObject emptyExclusiveObject = new ExclusiveObject(-1); // (none)

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    setupGesturesManager();
    setupViews();
  }

  private void setupViews() {
    icon = findViewById(R.id.icon);

    mutuallyExclusiveSpinner = findViewById(R.id.spinner_exclusives);
    ArrayAdapter<ExclusiveObject> adapter = new ArrayAdapter<>(
      this,
      android.R.layout.simple_spinner_item,
      new ExclusiveObject[] {
        emptyExclusiveObject, // For (none)
        new ExclusiveObject(AndroidGesturesManager.GESTURE_TYPE_SHOVE, AndroidGesturesManager.GESTURE_TYPE_SCROLL),
        new ExclusiveObject(AndroidGesturesManager.GESTURE_TYPE_ROTATE, AndroidGesturesManager.GESTURE_TYPE_SCALE),
        new ExclusiveObject(AndroidGesturesManager.GESTURE_TYPE_ROTATE, AndroidGesturesManager.GESTURE_TYPE_SCROLL),
        new ExclusiveObject(AndroidGesturesManager.GESTURE_TYPE_ROTATE, AndroidGesturesManager.GESTURE_TYPE_SHOVE),
        new ExclusiveObject(AndroidGesturesManager.GESTURE_TYPE_SCALE, AndroidGesturesManager.GESTURE_TYPE_SCROLL),
        new ExclusiveObject(AndroidGesturesManager.GESTURE_TYPE_ROTATE, AndroidGesturesManager.GESTURE_TYPE_SCROLL, AndroidGesturesManager.GESTURE_TYPE_SCALE),
        new ExclusiveObject(AndroidGesturesManager.GESTURE_TYPE_ROTATE, AndroidGesturesManager.GESTURE_TYPE_SCROLL, AndroidGesturesManager.GESTURE_TYPE_SHOVE),
        new ExclusiveObject(AndroidGesturesManager.GESTURE_TYPE_SCALE, AndroidGesturesManager.GESTURE_TYPE_SCROLL, AndroidGesturesManager.GESTURE_TYPE_SHOVE),
      });
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    mutuallyExclusiveSpinner.setAdapter(adapter);
    mutuallyExclusiveSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        ExclusiveObject exclusiveObject = (ExclusiveObject) parent.getItemAtPosition(position);
        if (exclusiveObject.equals(emptyExclusiveObject)) {
          androidGesturesManager.setMutuallyExclusiveGestures(new ArrayList<Set<Integer>>());
        } else {
          androidGesturesManager.setMutuallyExclusiveGestures(exclusiveObject.getExclusivesList());
        }
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {
      }
    });

    velocityButton = findViewById(R.id.button_velocity);
    velocityButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        setVelocityEnabled(!velocityEnabled);
      }
    });

    rotateThresholdProgress = findViewById(R.id.progress_threshold_rotate);
    rotateThresholdProgress.setProgress((int) androidGesturesManager.getRotateGestureDetector().getDefaultAngleThreshold());
    rotateThresholdProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        androidGesturesManager.getRotateGestureDetector().setAngleThreshold(progress);
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
      }
    });

    scaleThresholdProgress = findViewById(R.id.progress_threshold_scale);
    scaleThresholdProgress.setProgress((int) androidGesturesManager.getStandardScaleGestureDetector().getDefaultSpanDeltaThreshold());
    scaleThresholdProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        androidGesturesManager.getStandardScaleGestureDetector().setSpanDeltaThreshold(progress);
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
      }
    });
  }

  private void setupGesturesManager() {
    androidGesturesManager = new AndroidGesturesManager(this);

    androidGesturesManager.setStandardScaleGestureListener(new StandardScaleGestureDetector.SimpleStandardOnScaleGestureListener() {
      @Override
      public boolean onScale(StandardScaleGestureDetector detector) {
        rescaleIcon(detector.getScaleFactor());
        return true;
      }

      @Override
      public boolean scaleVelocityAnimator(StandardScaleGestureDetector detector, float velocityX, float velocityY, float scaleVelocityAnimatorValue) {
        if (!velocityEnabled) {
          return false;
        }
        rescaleIcon(scaleVelocityAnimatorValue);
        return true;
      }
    });

    androidGesturesManager.setRotateGestureListener(new RotateGestureDetector.SimpleOnRotateGestureListener() {
      @Override
      public boolean onRotate(RotateGestureDetector detector, float rotationDegreesSinceLast, float rotationDegreesSinceFirst) {
        icon.setRotation(icon.getRotation() - rotationDegreesSinceLast);
        return true;
      }

      @Override
      public boolean rotationVelocityAnimator(RotateGestureDetector detector, float velocityX, float velocityY, float rotationVelocityAnimatorValue) {
        if (!velocityEnabled) {
          return false;
        }
        icon.setRotation(icon.getRotation() - rotationVelocityAnimatorValue * 5);
        return true;
      }
    });

    androidGesturesManager.setStandardGestureListener(new StandardGestureDetector.SimpleStandardOnGestureListener() {
      @Override
      public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        icon.setTranslationX(icon.getTranslationX() - distanceX);
        icon.setTranslationY(icon.getTranslationY() - distanceY);
        return true;
      }

      @Override
      public boolean onDoubleTap(MotionEvent e) {
        rescaleIcon(1.40f);
        return true;
      }
    });

    androidGesturesManager.setMultiFingerTapGestureListener(new MultiFingerTapGestureDetector.OnMultiFingerTapGestureListener() {
      @Override
      public boolean onMultiFingerTap(MultiFingerTapGestureDetector detector, int pointersCount) {
        if (pointersCount == 2) {
          rescaleIcon(0.65f);
        }
        return true;
      }
    });

    androidGesturesManager.setShoveGestureListener(new ShoveGestureDetector.SimpleOnShoveGestureListener() {
      @Override
      public boolean onShove(ShoveGestureDetector detector, float deltaPixelsSinceLast, float deltaPixelsSinceStart) {
        icon.setRotationX(icon.getRotationX() - deltaPixelsSinceLast);
        return true;
      }
    });
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    return androidGesturesManager.onTouchEvent(event) || super.onTouchEvent(event);
  }

  private void rescaleIcon(float scaleFactor) {
    ViewGroup.LayoutParams layoutParams = icon.getLayoutParams();
    layoutParams.width = (int) (layoutParams.width * scaleFactor);
    layoutParams.height = (int) (layoutParams.height * scaleFactor);
    icon.setLayoutParams(layoutParams);
  }

  private void setVelocityEnabled(boolean enabled) {
    if (enabled) {
      velocityEnabled = true;
      velocityButton.setText(R.string.velocity_on);
    } else {
      velocityEnabled = false;
      velocityButton.setText(R.string.velocity_off);
    }
  }

  private class ExclusiveObject {
    private final Set<Integer> exclusivesList = new LinkedHashSet<>();

    ExclusiveObject(Integer... exclusives) {
      this.exclusivesList.addAll(Arrays.asList(exclusives));
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append("(");
      boolean isNotFirst = false;
      for (@AndroidGesturesManager.GestureType int gestureType : exclusivesList) {
        if (isNotFirst) {
          builder.append(", ");
        } else {
          isNotFirst = true;
        }

        builder.append(getStringForType(gestureType));
      }
      builder.append(")");

      return builder.toString();
    }

    private String getStringForType(@AndroidGesturesManager.GestureType int gestureType) {
      switch (gestureType) {
        case AndroidGesturesManager.GESTURE_TYPE_SCROLL:
          return "Scroll";
        case AndroidGesturesManager.GESTURE_TYPE_ROTATE:
          return "Rotate";
        case AndroidGesturesManager.GESTURE_TYPE_SCALE:
          return "Scale";
        case AndroidGesturesManager.GESTURE_TYPE_SHOVE:
          return "Shove";
        case -1: // (none)
        default:
          return "none";
      }
    }

    Set<Integer> getExclusivesList() {
      return exclusivesList;
    }
  }
}
