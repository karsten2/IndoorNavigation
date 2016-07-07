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

    private DescriptiveStatistics stats = new DescriptiveStatistics();
    private String name = "";

    public Statistics() {}

    public Statistics(int windowSize) {
        stats.setWindowSize(windowSize);
    }

    public Statistics(int windowSize, String name) {
        stats.setWindowSize(windowSize);
        this.name = name;
    }

    public void add(Double value) {
        stats.addValue(value);
    }

    public void add(List<Double> values) {
        for (double d : values) {
            stats.addValue(d);
        }
    }

    public void setValues (List<Double> values) {
        stats.clear();
        for (double d : values) {
            stats.addValue(d);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void clear() {
        stats.clear();
    }

    /**
     * Set window size to get rolling statistics.
     *      If window size = 100: creates the statistics of the most recent 100 values.
     * @param windowSize Size of the window.
     */
    public void setWindowSize(int windowSize) {
        stats.setWindowSize(windowSize);
    }

    public double getMean() {
        return stats.getMean();
    }

    public double getStandardDeviation() {
        return stats.getStandardDeviation();
    }

    public double getMedian() {
        return stats.getPercentile(50);
    }

}
