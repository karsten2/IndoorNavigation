package com.indoornavigation.Math;

import android.annotation.SuppressLint;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.geometry.Point;
import com.indoornavigation.Math.Lateration.LinearLeastSquaresSolver;
import com.indoornavigation.Math.Lateration.NonLinearLeastSquaresSolver;
import com.indoornavigation.Math.Lateration.TrilaterationFunction;
import com.indoornavigation.Model.BaseStation;

import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import com.google.maps.android.projection.SphericalMercatorProjection;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Class to compute a position based on fixed base stations via lateration.
 * https://github.com/lemmingapex/Trilateration
 */
public abstract class Lateration_old {

    private static final String TAG = "Lateration_old";

    /**
     * Using the formula A*Pe = b to get Xe, Ye
     *
     * @param baseStations Fix base stations with coordinates and distance.
     * @return Calculated coordinates.
     */
    public static LatLng calculatePosition(ArrayList<BaseStation> baseStations) {

        LatLng ret = new LatLng(0, 0);

        /*if (baseStations.size() >= 3) try {
            /**
             * Formula for least squares method:
             *      |xe|
             * Pe = |ye| = (A^T * A)^(-1) * A^T * b
             */
            /*double[][] A = generateMatrixA(baseStations);
            double[][] _A = transformA(A);
            double[][] b = generateMatrixB(baseStations);

            // Multiply A^T with A to get a square matrix.
            double[][] aMult = multiplyByMatrix(_A, A);

            // Invert the square Matrix with Apache Commons math Package.
            // http://commons.apache.org/proper/commons-math/
            RealMatrix inv = new Array2DRowRealMatrix(aMult);
            DecompositionSolver solver = new LUDecomposition(inv).getSolver();
            double[][] aInv = solver.getInverse().getData();

            double[][] aPseudoInv = multiplyByMatrix(aInv, _A);

            // Multiply pseudo inverse of A with b
            double[][] resMatrix = multiplyByMatrix(aPseudoInv, b);

            ret = new LatLng(resMatrix[0][0], resMatrix[1][0]);

        } catch (IllegalArgumentException e) {
            //Log.e(TAG, e.getMessage());
        }*/

        if (baseStations.size() >= 3) {
            double[][] positions = new double[baseStations.size()][2];
            double[] distances = new double[baseStations.size()];

            // Math.cos(d/R)
            double earthRadius = 6371000.8; // in meter

            for (int i = 0; i < baseStations.size(); i ++) {

                Point p = latLngToMerc(baseStations.get(i).getLatLng().latitude,
                        baseStations.get(i).getLatLng().longitude);
                positions[i][0] = p.x;
                positions[i][1] = p.y;
                distances[i] = baseStations.get(i).getDistance();

            }

            TrilaterationFunction trilaterationFunction = new TrilaterationFunction(positions, distances);
            LinearLeastSquaresSolver lSolver = new LinearLeastSquaresSolver(trilaterationFunction);
            RealVector x = lSolver.solve();

            NonLinearLeastSquaresSolver solver = new NonLinearLeastSquaresSolver(new TrilaterationFunction(positions, distances), new LevenbergMarquardtOptimizer());
            LeastSquaresOptimizer.Optimum optimum = solver.solve();

            // the answer
            double[] centroid = optimum.getPoint().toArray();

            // error and geometry information; may throw SingularMatrixException depending the threshold argument provided
            RealVector standardDeviation = optimum.getSigma(0);
            RealMatrix covarianceMatrix = optimum.getCovariances(0);

            //Log.d("Lateration", "Position: " + Arrays.toString(centroid) + " || Distances: " + Arrays.toString(distances));
            //Log.d("Lateration", "Position: " + x.toString() + " || Distances: " + Arrays.toString(distances));

            double[] lin = x.toArray();

            ret = mercToLatLng(new Point(centroid[0], centroid[1]));
            Log.d("Lateration", ret.toString());


            return ret;
            //return mercToLatLng(new Point(lin[0], lin[1]));
        }

        return ret;
    }

    private static Point latLngToMerc(double lat, double lng) {
        final double earthR = 6371800;
        SphericalMercatorProjection sphericalMercatorProjection =
                new SphericalMercatorProjection(earthR);
        return sphericalMercatorProjection.toPoint(new LatLng(lat, lng));
    }

    private static LatLng mercToLatLng(Point xy) {
        final double earthR = 6371800;
        SphericalMercatorProjection sphericalMercatorProjection =
                new SphericalMercatorProjection(earthR);
        return sphericalMercatorProjection.toLatLng(xy);
    }

    /**
     * Create Matrix A (2 columns, n rows).
     * |x1 - x2    y1 - y2|
     * A = |x1 - x3    y1 - y3|
     * |x1 - x4    y1 - y4|
     * extensible to n stations.
     *
     * @param baseStations that contains distance and coordinate.
     * @return transformed matrix.
     */
    private static double[][] generateMatrixA(ArrayList<BaseStation> baseStations) {

        double[][] ret = new double[baseStations.size() - 1][2];

        for (int i = 0; i < baseStations.size() - 1; i++) {
            ret[i][0] = baseStations.get(0).getLatLng().latitude
                    - baseStations.get(i + 1).getLatLng().latitude;

            ret[i][1] = baseStations.get(0).getLatLng().longitude
                    - baseStations.get(i + 1).getLatLng().longitude;
        }

        return ret;
    }

    /**
     * Function to transform a matrix.
     * <p/>
     * | 2 -2|
     * A = |-2  2| => A' = | 2 -2  5|
     * | 5  3|         |-2  2  3|
     *
     * @param A matrix to transform.
     * @return transformed matrix.
     */
    private static double[][] transformA(double[][] A) {
        double[][] ret = new double[A[0].length][A.length];

        // for each column in A
        for (int col = 0; col < A[0].length; col++) {
            // for each row in a column in A
            for (int row = 0; row < A.length; row++) {
                ret[col][row] = A[row][col];
            }
        }

        return ret;
    }

    /**
     * Create Matrix b (1 column, n rows).
     * |x1^2 - x2^2 + y1^2 - y2^2 + D2^2 - D1|
     * b = 1/2 * |x1^2 - x3^2 + y1^2 - y3^2 + D3^2 - D1|
     * |x1^2 - x4^2 + y1^2 - y4^2 + D4^2 - D1|
     *
     * @param baseStations that contains distance and coordinate.
     * @return transformed matrix.
     */
    private static double[][] generateMatrixB(ArrayList<BaseStation> baseStations) {
        double[][] ret = new double[baseStations.size() - 1][1];

        for (int i = 0; i < baseStations.size() - 1; i++) {
            final double x1 = baseStations.get(0).getLatLng().latitude;
            final double y1 = baseStations.get(0).getLatLng().longitude;
            final double D1 = baseStations.get(0).getDistance();

            double xn = baseStations.get(i + 1).getLatLng().latitude;
            double yn = baseStations.get(i + 1).getLatLng().longitude;
            double Dn = baseStations.get(i + 1).getDistance();

            ret[i][0] = 0.5 * (Math.pow(x1, 2) - Math.pow(xn, 2)
                    + Math.pow(y1, 2) - Math.pow(yn, 2)
                    + Math.pow(Dn, 2) - D1);
        }

        return ret;
    }

    /**
     * Function to multiply two matrices.
     *
     * @param m1 First matrix.
     * @param m2 Second matrix.
     * @return Multiplied matrix.
     */
    private static double[][] multiplyByMatrix(double[][] m1, double[][] m2) {
        int m1ColLength = m1[0].length;
        int m2RowLength = m2.length;
        if (m1ColLength != m2RowLength)
            throw new IllegalArgumentException("Matrix 1 column length != Matrix 2 row length.");
        int mRRowLength = m1.length;
        int mRColLength = m2[0].length;
        double[][] mResult = new double[mRRowLength][mRColLength];

        for (int i = 0; i < mRRowLength; i++) {         // rows from m1
            for (int j = 0; j < mRColLength; j++) {     // columns from m2
                for (int k = 0; k < m1ColLength; k++) { // columns from m1
                    mResult[i][j] += m1[i][k] * m2[k][j];
                }
            }
        }
        return mResult;
    }

    /**
     * Function to display a matrix in a string.
     *
     * @param m Matrix to display.
     * @return String with Matrix.
     */
    @SuppressLint("DefaultLocale")
    private static String matrixToString(double[][] m) {
        String result = "";
        for (double[] aM : m) {
            for (double anAM : aM) {
                result += String.format("%11.2f", anAM);
            }
            result += "\n";
        }
        return result;
    }



}
