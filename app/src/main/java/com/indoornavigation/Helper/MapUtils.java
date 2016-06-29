package com.indoornavigation.Helper;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.indoor.navigation.indoornavigation.R;

/**
 * Static helper functions for the usage of the map.
 */
public class MapUtils {

    //public static LatLng startPosition = new LatLng(54.33845259, 13.074251823);
    public static LatLng startPosition = new LatLng(54.338455139, 13.07425316423);
    public static LatLng currentPosition = new LatLng(-1, -1);
    public static float currentZoom = 0;

    public static void addGroundOverlay(GoogleMap mMap) {
        LatLng position = new LatLng(54.33845083, 13.074249811);
        GroundOverlayOptions overlay = new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.floorplan))
                .position(position, 18)
                .bearing(346)
                .anchor(0, 1);

        mMap.addGroundOverlay(overlay);
    }

    public static LatLng getStartPosition() {
        if (currentPosition.equals(new LatLng(-1, -1)))
            return startPosition;

        return currentPosition;
    }
}
