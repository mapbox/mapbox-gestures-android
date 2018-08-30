package com.mapbox.android.gestures;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.MotionEvent;

import com.mapbox.android.gestures.dollar.Dollar;
import com.mapbox.android.gestures.dollar.MatchResult;
import com.mapbox.android.gestures.dollar.Point;
import com.mapbox.android.gestures.dollar.Template;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import timber.log.Timber;

import static com.mapbox.android.gestures.AndroidGesturesManager.GESTURE_TYPE_STROKE;

public class StrokeGestureDetector extends ProgressiveGesture<StrokeGestureDetector.OnStrokeListener> {

  private static final Set<Integer> handledTypes = new HashSet<>();

  static {
    handledTypes.add(GESTURE_TYPE_STROKE);
  }

  private final Dollar dollar = new Dollar();
  private final List<Point> points = new ArrayList<>();

  public StrokeGestureDetector(Context context, AndroidGesturesManager gesturesManager) {
    super(context, gesturesManager);
//    Template.prepare(context);
  }

  @NonNull
  @Override
  protected Set<Integer> provideHandledTypes() {
    return handledTypes;
  }

  public interface OnStrokeListener {
    void onStrokeDetected(String match, double maxScore);
  }

  @Override
  protected boolean analyzeEvent(MotionEvent motionEvent) {
    switch (motionEvent.getActionMasked()) {
      case MotionEvent.ACTION_DOWN:
      case MotionEvent.ACTION_MOVE:
        points.add(new Point((int) motionEvent.getX(), (int) motionEvent.getY()));
        break;

      case MotionEvent.ACTION_UP:
        points.add(new Point((int) motionEvent.getX(), (int) motionEvent.getY()));

        MatchResult result = dollar.justDoIt(points, Arrays.asList(
          new Template("triangle", Template.triangle),
          new Template("triangle", Template.triangle2),
          new Template("triangle", Template.triangle3),
          new Template("triangle", Template.triangle4),
          new Template("circle", Template.circle),
          new Template("circle", Template.circle2),
          new Template("circle", Template.circle3),
          new Template("delete", Template.delete),
          new Template("delete", Template.delete2),
          new Template("delete", Template.delete3)
        ));
        listener.onStrokeDetected(result.getMatch(), result.getMaxScore());
        Timber.d("TEST: " + result.getMatch() + " " + result.getMaxScore());

        /*Gson gson = new Gson();
        try {
          File file = new File(context.getFilesDir().getAbsolutePath() + "/triangle.json");
          file.mkdirs();
          gson.toJson(points.toArray(), new FileWriter(file, false));
        } catch (IOException e) {
          e.printStackTrace();
        }*/

        points.clear();
        break;

      case MotionEvent.ACTION_CANCEL:
        points.clear();
        break;

      default:
        break;
    }

    return super.analyzeEvent(motionEvent);
  }
}
