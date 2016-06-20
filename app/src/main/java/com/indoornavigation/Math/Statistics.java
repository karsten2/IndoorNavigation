package com.indoornavigation.Math;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.regression.SimpleRegression;

import java.util.List;

/**
 * Function to do statistic stuff.
 * See apache commons math user guide:
 *      http://commons.apache.org/proper/commons-math/userguide/stat.html
 */
public class Statistics {

    private static DescriptiveStatistics stats = new DescriptiveStatistics();

    public static void add(Double value) {
        stats.addValue(value);
    }

    public static void add(List<Double> values) {
        for (double d : values) {
            stats.addValue(d);
        }
    }

    public static void setValues (List<Double> values) {
        stats.clear();
        for (double d : values) {
            stats.addValue(d);
        }
    }

    public static void clear() {
        stats.clear();
    }

    /**
     * Set window size to get rolling statistics.
     *      If window size = 100: creates the statistics of the most recent 100 values.
     * @param windowSize Size of the window.
     */
    public static void setWindowSize(int windowSize) {
        stats.setWindowSize(windowSize);
    }

    public static double getMean() {
        return stats.getMean();
    }

    public static double getStandardDeviation() {
        return stats.getStandardDeviation();
    }

    public static double getMedian() {
        return stats.getPercentile(50);
    }

    public static class SRegression {

        SimpleRegression regression = new SimpleRegression();

        /**
         * Adding data to Dataset.
         * @param x value 1
         * @param y value 2
         */
        public void addData(double x, double y) {
            regression.addData(x, y);
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
}
