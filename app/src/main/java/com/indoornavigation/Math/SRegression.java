package com.indoornavigation.Math;

import org.apache.commons.math3.stat.regression.SimpleRegression;

public class SRegression {

    SimpleRegression regression = new SimpleRegression();

    public SRegression() { }

    /**
     * Adding testdata, used in the process of development to the regression.
     * @param useDefaultData 2d double array.
     */
    public SRegression(boolean useDefaultData) {

        regression.addData(-39.5, 0.5);
        regression.addData(-43.8, 1);
        regression.addData(-49.4, 2);
        regression.addData(-52, 3);
        regression.addData(-55, 4);
        regression.addData(-57, 5);
    }

    /**
     * Constructor
     * @param data 2d double array.
     */
    public SRegression(double[][] data) {
        regression.addData(data);
    }

    /**
     * Adding data to Dataset.
     * @param x value 1
     * @param y value 2
     */
    public void addData(double x, double y) {
        regression.addData(x, y);
    }

    /**
     * Adding data to Dataset.
     * @param data 2d array of double values.
     */
    public void addData(double[][] data) {
        regression.addData(data);
    }

    /**
     * Try to run for unkown data.
     * @param predict prediction value.
     * @return result.
     */
    public double getPrediction(double predict) {
        return this.regression.predict(predict);
    }

    /**
     * The slope.
     * @return slopey mcslopeslope.
     */
    public double getSlope() {
        return this.regression.getSlope();
    }

    /**
     * The Intercept.
     * @return Intercept.
     */
    public double getIntercept() {
        return this.regression.getIntercept();
    }

    public SimpleRegression get() {
        return this.regression;
    }

}