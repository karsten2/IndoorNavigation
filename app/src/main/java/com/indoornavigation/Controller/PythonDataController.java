package com.indoornavigation.Controller;

import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public abstract class PythonDataController {
    /**
     * variable to tell python what script to run
     *      state = 0:  end script
     *      state = 1:  idle, wait for command
     *      state = 2:  run moving average script
     *      state = 3:  weighted Average // TODO
     */
    private static int state = 1;

    public static int getState() {
        return state;
    }

    public static void setState(int state) {
        PythonDataController.state = state;
    }

    public static void endScript() {
        state = -1;
    }

    public static class MovingAverage {
        private static List<Double> movingAvgData = new ArrayList<>();
        private static Integer movingAvgWindowSize;
        private static Double result;

        public static List<Double> getMovingAvgData() {
            return movingAvgData;
        }

        public static void setMovingAvgData(List<Double> movingAvgData) {
            MovingAverage.movingAvgData = movingAvgData;
        }

        public static Integer getMovingAvgWindowSize() {
            return movingAvgWindowSize;
        }

        public static void setMovingAvgWindowSize(Integer movingAvgWindSize) {
            MovingAverage.movingAvgWindowSize = movingAvgWindSize;
        }

        public static Double getResult() {
            return result;
        }

        public static void setResult(Double result) {
            MovingAverage.result = result;
        }

        public static void TestMovingAverage() {
            movingAvgData.add(1.2);
            movingAvgData.add(2.0);
            movingAvgData.add(3.0);
            movingAvgData.add(4.0);
            movingAvgData.add(5.0);
            movingAvgWindowSize = 3;
            state = 2;
        }
    }

    public static class WeightedAverage {

    }

    public static class kNearestNeighbor {
        public static void testKNN() {
            state = 3;
        }
    }
}
