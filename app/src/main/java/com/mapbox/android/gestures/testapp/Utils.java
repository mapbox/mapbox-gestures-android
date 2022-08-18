package com.mapbox.android.gestures.testapp;

import android.content.Context;

import androidx.annotation.NonNull;

public class Utils {

    /**
     * <p>
     * Returns the Mapbox access token set in the app resources.
     * </p>
     * It will attempt to load the access token from the
     * {@code res/values/developer-config.xml} development file.
     *
     * @param context The {@link Context} of the {@link android.app.Activity} or {@link android.app.Fragment}.
     * @return The Mapbox access token or null if not found.
     */
    public static String getMapboxAccessToken(@NonNull Context context) {
        int tokenResId = context.getResources()
                .getIdentifier("mapbox_access_token", "string", context.getPackageName());
        return tokenResId != 0 ? context.getString(tokenResId) : null;
    }
}
