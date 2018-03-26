package com.mapbox.android.gestures.testapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;

import timber.log.Timber;

/**
 * Test activity showcasing a simple MapView with current mapbox-gestures-android library commit.
 */
public class MapboxActivity extends AppCompatActivity {

  private static final String DEFAULT_MAPBOX_ACCESS_TOKEN = "YOUR_MAPBOX_ACCESS_TOKEN_GOES_HERE";
  private static final String ACCESS_TOKEN_NOT_SET_MESSAGE =
    "In order to run the Mapbox map Activity you need to set a valid "
      + "access token. During development, you can set the MAPBOX_ACCESS_TOKEN environment variable for the SDK to "
      + "automatically include it in the Test App. Otherwise, you can manually include it in the "
      + "res/values/developer-config.xml file in the mapbox-gestures-android/app folder.";

  private MapView mapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_mapbox);

    String mapboxAccessToken = Utils.getMapboxAccessToken(getApplicationContext());
    if (TextUtils.isEmpty(mapboxAccessToken) || mapboxAccessToken.equals(DEFAULT_MAPBOX_ACCESS_TOKEN)) {
      Timber.e(ACCESS_TOKEN_NOT_SET_MESSAGE);
    }

    Mapbox.getInstance(getApplicationContext(), mapboxAccessToken);

    mapView = (MapView) findViewById(R.id.map_view);
    mapView.onCreate(savedInstanceState);
  }

  @Override
  protected void onStart() {
    super.onStart();
    mapView.onStart();
  }

  @Override
  protected void onResume() {
    super.onResume();
    mapView.onResume();
  }

  @Override
  protected void onPause() {
    super.onPause();
    mapView.onPause();
  }

  @Override
  protected void onStop() {
    super.onStop();
    mapView.onStop();
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();
    mapView.onLowMemory();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mapView.onDestroy();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }
}
