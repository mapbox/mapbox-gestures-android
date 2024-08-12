package com.mapbox.android.gestures.testapp;

import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.maplibre.android.MapLibre;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.OnMapReadyCallback;

/**
 * Test activity showcasing a simple MapView with current mapbox-gestures-android library commit.
 */
public class MapboxActivity extends AppCompatActivity implements OnMapReadyCallback {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    MapLibre.getInstance(this);

    setContentView(R.layout.activity_mapbox);
    MapView mapView = findViewById(R.id.map_view);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(@NonNull MapLibreMap mapLibreMap) {
    mapLibreMap.setStyle("https://demotiles.maplibre.org/style.json");
  }
}
