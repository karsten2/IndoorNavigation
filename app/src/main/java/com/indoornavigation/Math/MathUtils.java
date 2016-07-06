package com.indoornavigation.Math;

import java.util.ArrayList;

/**
 * Helper Functions for math operations.
 */
public class MathUtils {
    public static ArrayList<Double> normalizeVector(ArrayList<Double> vector) {
        ArrayList<Double> returnValue = new ArrayList<>();

        if (vector.size() > 0) {
            Double first = vector.get(0);
            for (Double d : vector) {
                returnValue.add(d - first);
            }
        }

        return returnValue;
    }

    /**
     * Subtract to vectors.
     * The size of v1 and v2 must be equal.
     *
     * @param v1 ArrayList with double values.
     * @param v2 ArrayList with double values.
     * @return new vector form v1 and v2.
     */
    public static ArrayList<Double> subtractVector(ArrayList<Double> v1, ArrayList<Double> v2) {
        ArrayList<Double> returnValue  = new ArrayList<>();

        if (v1.size() == v2.size()) {
            for (int i = 0; i < v1.size(); i ++) {
                returnValue.add(v1.get(i) - v2.get(i));
            }
        }

        return returnValue;
    }

    /**
     * Get the vectors magnitude.
     *      v {1, 2, 3}
     *      |v| 3.74...
     *
     * @param v values of vector as arraylist.
     * @return magnitude
     */
    public static double magnitudeVector(ArrayList<Double> v) {

        double vectorContent = 0;

        for (Double d : v) {
            vectorContent += Math.pow(d, 2);
        }

        return Math.sqrt(vectorContent);
    }

    /**
     * Function to calculate the distance in meters from dbm rssi values.
     * http://rvmiller.com/2013/05/part-1-wifi-based-trilateration-on-android/
     *
     * The function is based on Free Space Path Loss, and may not work with
     * indoor signal propagation.
     *
     * @param levelInDb RSSI value.
     * @param freqInMHz Frequency of the sending device.
     * @return Distance in meters.
     */
    public static double distanceFSPL(double levelInDb, double freqInMHz) {
        double exp = (27.55 - (20 * Math.log10(freqInMHz)) + Math.abs(levelInDb)) / 20.0;
        return Math.pow(10.0, exp);
    }

    /**
     * Function calculates the distance from a given RSSI value.
     *      RSSI                = -(10 * n)log_10(distance) - A     | + A
     *      RSSI - A            = -10n * log_10(distance)           | / -10n
     *      (RSSI - A) / -10N   = log_10(distance)
     *      10 ^(RSSI - A) / -10N) = distance
     *
     * @param currentRSSI   live value from the receiver.
     * @param A             Fix value at 1m distance.
     * @param n             Signal propagation constant.
     * @return              Distance in m.
     */
    public static double distance(double currentRSSI, double A, double n) {
        double exp = (currentRSSI - A) / (-10 * n);

        return Math.pow(10, exp);
    }

    /**
     * Calculates the signal propagation constant, which is required for
     * @see MathUtils#distance(double, double, double).
     *
     *      RSSI = -(10 * n)log_10(distance) - A
     *      n    = - ((RSSI - A) / (10log_10(d))
     *
     * @param currentDistance   Distance between sender and receiver.
     * @param currentRSSI       Current signal strength in dBm.
     * @param A                 Recorded signal strength at 1m.
     * @return                  Signal propagation constant.
     */
    public static double distancePropConst(double currentDistance,
                                           double currentRSSI,
                                           double A) {
        return -((currentRSSI - A) / (10 * Math.log10(currentDistance)));
    }
}
