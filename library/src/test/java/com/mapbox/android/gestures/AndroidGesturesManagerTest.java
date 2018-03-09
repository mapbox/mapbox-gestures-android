package com.mapbox.android.gestures;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RunWith(RobolectricTestRunner.class)
public class AndroidGesturesManagerTest {
  private AndroidGesturesManager androidGesturesManager;

  private Set<Integer> set1 = new HashSet<>();
  private Set<Integer> set2 = new HashSet<>();
  private Set<Integer> set3 = new HashSet<>();
  private List<Set<Integer>> mutuallyExclusivesList = new ArrayList<>();

  @Before
  public void setUp() throws Exception {
    set1.clear();
    set2.clear();
    set3.clear();
    mutuallyExclusivesList.clear();

    set1.add(AndroidGesturesManager.GESTURE_TYPE_ROTATE);
    set1.add(AndroidGesturesManager.GESTURE_TYPE_SCROLL);

    set2.add(AndroidGesturesManager.GESTURE_TYPE_ROTATE);
    set2.add(AndroidGesturesManager.GESTURE_TYPE_SCALE);

    set3.add(AndroidGesturesManager.GESTURE_TYPE_ROTATE);
    set3.add(AndroidGesturesManager.GESTURE_TYPE_SCALE);

    mutuallyExclusivesList.add(set1);
    mutuallyExclusivesList.add(set2);
    mutuallyExclusivesList.add(set3);

    androidGesturesManager =
      new AndroidGesturesManager(
        RuntimeEnvironment.application.getApplicationContext(),
        mutuallyExclusivesList, true);
  }

  @Test
  public void initializeGestureDetectorsTest() throws Exception {
    for (BaseGesture detector : androidGesturesManager.getDetectors()) {
      assertNotNull(detector);
    }
  }

  @Test
  public void setMutuallyExclusivesTest() throws Exception {
    assertEquals(androidGesturesManager.getMutuallyExclusiveGestures(), mutuallyExclusivesList);

    androidGesturesManager.setMutuallyExclusiveGestures(set1, set2, set3);
    assertEquals(androidGesturesManager.getMutuallyExclusiveGestures(), mutuallyExclusivesList);

    androidGesturesManager.setMutuallyExclusiveGestures();
    assertEquals(androidGesturesManager.getMutuallyExclusiveGestures(), new ArrayList<Set<Integer>>());

    androidGesturesManager.setMutuallyExclusiveGestures(mutuallyExclusivesList);
    assertEquals(androidGesturesManager.getMutuallyExclusiveGestures(), mutuallyExclusivesList);
  }
}
