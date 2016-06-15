package com.indoornavigation.Math;

import org.apache.commons.math3.stat.regression.SimpleRegression;

/**
 * Class to smooth the data coming from the drone.
 */
public class DataSmoothing {

    public class Regression {

        SimpleRegression regression = new SimpleRegression();

        public void addData(double x, double y) {
            regression.addData(x, y);
        }

        public SimpleRegression getRegression() { return regression; }
    }
}
