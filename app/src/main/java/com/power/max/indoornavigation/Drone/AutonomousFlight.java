package com.power.max.indoornavigation.Drone;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Class to perform the autonomous flight.
 */
public class AutonomousFlight {
    // TODO: let it fly captain

    public AutonomousFlight(BebopDrone bebopDrone, ArrayList<LatLng> route) {

    }

    /**
     * Function to get the angle between two given points.
     *      Usage:  Drone calculates own Position and knows the next point to fly to.
     *              The drone also knows the direction of north etc. By calculating the angle
     *              between the two points, the drone can be adjusted, to point to the next
     *              destination, and than just have to be moved forward.
     * @param p1 Position of the drone.
     * @param p2 Next target.
     * @return Angle between p1 and p2.
     */
    private double PointsToAngle(LatLng p1, LatLng p2) {
        double deltaLat = p2.latitude - p1.latitude;
        double deltaLng = p2.longitude - p1.longitude;

        return Math.atan2(deltaLat, deltaLng) * 180 / Math.PI;
    }
}
