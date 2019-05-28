package com.mapbox.android.gestures;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.mapbox.android.gestures.testapp.OverlaidScrollActivity;
import com.mapbox.android.gestures.testapp.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.mapbox.android.gestures.UiTestUtils.pinchIn;

@RunWith(AndroidJUnit4.class)
public class MultipleViewTreesInputTest {

  @Rule
  public ActivityTestRule<OverlaidScrollActivity> activityTestRule =
    new ActivityTestRule<>(OverlaidScrollActivity.class);

  @Test
  public void testPinch() {
    onView(withId(R.id.testView)).perform(pinchIn());
  }
}
