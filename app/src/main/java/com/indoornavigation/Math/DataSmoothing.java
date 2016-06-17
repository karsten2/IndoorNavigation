package com.indoornavigation.Math;

import org.apache.commons.math3.stat.descriptive.rank.Median;
import org.apache.commons.math3.stat.regression.SimpleRegression;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Class to smooth the data coming from the drone.
 */
public class DataSmoothing {

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

    }

    public static class MovingAverage {

        private final Queue<BigDecimal> window = new LinkedList<>();
        private final int period;
        private BigDecimal sum = BigDecimal.ZERO;

        public MovingAverage(int period) {
            if (period <= 0)
                throw new IllegalArgumentException("period must be > 0");

            this.period = period;
        }

        public void add(double num) {
            sum = sum.add(BigDecimal.valueOf(num));
            window.add(BigDecimal.valueOf(num));
            if (window.size() > period) {
                sum = sum.subtract(window.remove());
            }
        }

        public double getAverage() {
            if (window.isEmpty()) return 0.0;
            BigDecimal divisor = BigDecimal.valueOf(window.size());
            return (sum.divide(divisor, 2, RoundingMode.HALF_UP)).doubleValue();
        }
    }

    /**
     * Function to get the median of a bunch of values
     * @param values array with doubles.
     * @return median.
     */
    public static double getMedian(double[] values) {
        Median median = new Median();
        return median.evaluate(values);
    }
}
