package com.mapbox.android.gestures.testapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;

import com.mapbox.maps.MapView;
import com.mapbox.maps.Style;

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

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    String mapboxAccessToken = Utils.getMapboxAccessToken(getApplicationContext());
    if (TextUtils.isEmpty(mapboxAccessToken) || mapboxAccessToken.equals(DEFAULT_MAPBOX_ACCESS_TOKEN)) {
      Timber.e(ACCESS_TOKEN_NOT_SET_MESSAGE);
    }

    setContentView(R.layout.activity_mapbox);

    MapView mapView = (MapView) findViewById(R.id.map_view);
    mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS);
  }
}
