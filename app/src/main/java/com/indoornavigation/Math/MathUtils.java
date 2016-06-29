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
}
