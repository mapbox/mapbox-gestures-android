package com.mapbox.android.gestures.testapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import com.mapbox.android.gestures.MoveGestureDetector;
import com.mapbox.android.gestures.MultiFingerTapGestureDetector;
import com.mapbox.android.gestures.RotateGestureDetector;
import com.mapbox.android.gestures.ShoveGestureDetector;
import com.mapbox.android.gestures.SidewaysShoveGestureDetector;
import com.mapbox.android.gestures.StandardGestureDetector;
import com.mapbox.android.gestures.StandardScaleGestureDetector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

  private ImageView icon;
  private Button mapboxButton;
  private Spinner mutuallyExclusiveSpinner;
  private SeekBar rotateThresholdProgress;
  private SeekBar scaleThresholdProgress;

  private AndroidGesturesManager androidGesturesManager;

  private boolean isScrollChosen;

  private final ExclusiveSetSpinnerObject emptyExclusiveSetSpinnerObject = new ExclusiveSetSpinnerObject(-1); // (none)

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    setupGesturesManager();
    setupViews();
  }

  private void setupViews() {
    icon = (ImageView) findViewById(R.id.icon);

    mutuallyExclusiveSpinner = (Spinner) findViewById(R.id.spinner_exclusives);
    ArrayAdapter<ExclusiveSetSpinnerObject> adapter = new ArrayAdapter<>(
      this,
      android.R.layout.simple_spinner_item,
      new ExclusiveSetSpinnerObject[] {
        emptyExclusiveSetSpinnerObject, // For (none)

        new ExclusiveSetSpinnerObject(AndroidGesturesManager.GESTURE_TYPE_SHOVE,
          AndroidGesturesManager.GESTURE_TYPE_SCROLL),

        new ExclusiveSetSpinnerObject(AndroidGesturesManager.GESTURE_TYPE_ROTATE,
          AndroidGesturesManager.GESTURE_TYPE_SCALE),

        new ExclusiveSetSpinnerObject(AndroidGesturesManager.GESTURE_TYPE_ROTATE,
          AndroidGesturesManager.GESTURE_TYPE_SCROLL),

        new ExclusiveSetSpinnerObject(AndroidGesturesManager.GESTURE_TYPE_ROTATE,
          AndroidGesturesManager.GESTURE_TYPE_SHOVE),

        new ExclusiveSetSpinnerObject(AndroidGesturesManager.GESTURE_TYPE_SCALE,
          AndroidGesturesManager.GESTURE_TYPE_SCROLL),

        new ExclusiveSetSpinnerObject(AndroidGesturesManager.GESTURE_TYPE_ROTATE,
          AndroidGesturesManager.GESTURE_TYPE_SCROLL,
          AndroidGesturesManager.GESTURE_TYPE_SCALE),

        new ExclusiveSetSpinnerObject(AndroidGesturesManager.GESTURE_TYPE_ROTATE,
          AndroidGesturesManager.GESTURE_TYPE_SCROLL,
          AndroidGesturesManager.GESTURE_TYPE_SHOVE),

        new ExclusiveSetSpinnerObject(AndroidGesturesManager.GESTURE_TYPE_SCALE,
          AndroidGesturesManager.GESTURE_TYPE_SCROLL,
          AndroidGesturesManager.GESTURE_TYPE_SHOVE),

        new ExclusiveSetSpinnerObject(AndroidGesturesManager.GESTURE_TYPE_SCALE,
          AndroidGesturesManager.GESTURE_TYPE_MOVE,
          AndroidGesturesManager.GESTURE_TYPE_SHOVE),

        new ExclusiveSetSpinnerObject(AndroidGesturesManager.GESTURE_TYPE_ROTATE,
          AndroidGesturesManager.GESTURE_TYPE_MOVE),

        new ExclusiveSetSpinnerObject(AndroidGesturesManager.GESTURE_TYPE_SCALE,
          AndroidGesturesManager.GESTURE_TYPE_SCROLL,
          AndroidGesturesManager.GESTURE_TYPE_SIDEWAYS_SHOVE),
      });
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    mutuallyExclusiveSpinner.setAdapter(adapter);
    mutuallyExclusiveSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        ExclusiveSetSpinnerObject exclusiveSetSpinnerObject =
          (ExclusiveSetSpinnerObject) parent.getItemAtPosition(position);

        if (exclusiveSetSpinnerObject.equals(emptyExclusiveSetSpinnerObject)) {
          androidGesturesManager.setMutuallyExclusiveGestures(new ArrayList<Set<Integer>>());
        } else {
          Set<Integer> exclusiveSet = exclusiveSetSpinnerObject.getExclusiveSet();
          androidGesturesManager.setMutuallyExclusiveGestures(exclusiveSetSpinnerObject.getExclusiveSet());
          if (exclusiveSet.contains(AndroidGesturesManager.GESTURE_TYPE_SCROLL)) {
            isScrollChosen = true;
            androidGesturesManager.removeMoveGestureListener();
          } else if (exclusiveSet.contains(AndroidGesturesManager.GESTURE_TYPE_MOVE)) {
            isScrollChosen = false;
            androidGesturesManager.setMoveGestureListener(onMoveGestureListener);
          }
        }
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {
      }
    });

    mapboxButton = (Button) findViewById(R.id.button_mapbox);
    mapboxButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mapboxButton.setEnabled(false);
        Intent intent = new Intent(MainActivity.this, MapboxActivity.class);
        startActivity(intent);
      }
    });

    rotateThresholdProgress = (SeekBar) findViewById(R.id.progress_threshold_rotate);
    rotateThresholdProgress.setProgress(
      (int) androidGesturesManager.getRotateGestureDetector().getAngleThreshold());

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

    scaleThresholdProgress = (SeekBar) findViewById(R.id.progress_threshold_scale);
    scaleThresholdProgress.setProgress(
      (int) androidGesturesManager.getStandardScaleGestureDetector().getSpanSinceStartThreshold());

    scaleThresholdProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        androidGesturesManager.getStandardScaleGestureDetector().setSpanSinceStartThreshold(progress);
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

    androidGesturesManager.setStandardScaleGestureListener(
      new StandardScaleGestureDetector.SimpleStandardOnScaleGestureListener() {

        @Override
        public boolean onScale(StandardScaleGestureDetector detector) {
          rescaleIcon(detector.getScaleFactor());
          return true;
        }
      });

    androidGesturesManager.setRotateGestureListener(new RotateGestureDetector.SimpleOnRotateGestureListener() {
      @Override
      public boolean onRotate(RotateGestureDetector detector, float rotationDegreesSinceLast,
                              float rotationDegreesSinceFirst) {
        icon.setRotation(icon.getRotation() - rotationDegreesSinceLast);
        return true;
      }
    });

    androidGesturesManager.setStandardGestureListener(new StandardGestureDetector.SimpleStandardOnGestureListener() {
      @Override
      public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (isScrollChosen) {
          icon.setTranslationX(icon.getTranslationX() - distanceX);
          icon.setTranslationY(icon.getTranslationY() - distanceY);
          return true;
        }
        return false;
      }

      @Override
      public boolean onDoubleTap(MotionEvent e) {
        rescaleIcon(1.40f);
        return true;
      }
    });

    androidGesturesManager.setMultiFingerTapGestureListener(
      new MultiFingerTapGestureDetector.OnMultiFingerTapGestureListener() {

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

    androidGesturesManager.setSidewaysShoveGestureListener(
      new SidewaysShoveGestureDetector.SimpleOnSidewaysShoveGestureListener() {
        @Override
        public boolean onSidewaysShove(SidewaysShoveGestureDetector detector, float deltaPixelsSinceLast,
                                       float deltaPixelsSinceStart) {
          icon.setRotationY(icon.getRotationY() + deltaPixelsSinceLast);
          return true;
        }
      });

    androidGesturesManager.setMoveGestureListener(onMoveGestureListener);
  }

  private final MoveGestureDetector.OnMoveGestureListener onMoveGestureListener =
    new MoveGestureDetector.OnMoveGestureListener() {
      @Override
      public boolean onMoveBegin(MoveGestureDetector detector) {
        return true;
      }

      @Override
      public boolean onMove(MoveGestureDetector detector, float distanceX, float distanceY) {
        icon.setTranslationX(icon.getTranslationX() - distanceX);
        icon.setTranslationY(icon.getTranslationY() - distanceY);
        return true;
      }

      @Override
      public void onMoveEnd(MoveGestureDetector detector, float velocityX, float velocityY) {

      }
    };

  @Override
  protected void onStart() {
    super.onStart();
    mapboxButton.setEnabled(true);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    return androidGesturesManager.onTouchEvent(event) || super.onTouchEvent(event);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.menu_help) {
      HelpDialogFragment helpDialogFragment =
        (HelpDialogFragment) getFragmentManager().findFragmentByTag(HelpDialogFragment.TAG);

      if (helpDialogFragment == null) {
        helpDialogFragment = HelpDialogFragment.newInstance();
        getFragmentManager()
          .beginTransaction()
          .add(helpDialogFragment, HelpDialogFragment.TAG)
          .commit();
      }
      return true;
    }
    return false;
  }

  private void rescaleIcon(float scaleFactor) {
    DisplayMetrics metrics = new DisplayMetrics();
    getWindowManager().getDefaultDisplay().getMetrics(metrics);

    ViewGroup.LayoutParams layoutParams = icon.getLayoutParams();
    int width = (int) (layoutParams.width * scaleFactor);
    int height = (int) (layoutParams.height * scaleFactor);
    if (width > 100 && height > 100 && width < metrics.widthPixels && height < metrics.heightPixels) {
      layoutParams.width = width;
      layoutParams.height = height;
      icon.setLayoutParams(layoutParams);
    }
  }

  private class ExclusiveSetSpinnerObject {
    private final Set<Integer> exclusiveSet = new LinkedHashSet<>();

    ExclusiveSetSpinnerObject(Integer... exclusives) {
      this.exclusiveSet.addAll(Arrays.asList(exclusives));
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append("(");
      boolean isNotFirst = false;
      for (@AndroidGesturesManager.GestureType int gestureType : exclusiveSet) {
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
        case AndroidGesturesManager.GESTURE_TYPE_SIDEWAYS_SHOVE:
          return "Sideways shove";
        case AndroidGesturesManager.GESTURE_TYPE_MOVE:
          return "Move";
        case -1: // (none)
        default:
          return "none";
      }
    }

    Set<Integer> getExclusiveSet() {
      return exclusiveSet;
    }
  }
}
