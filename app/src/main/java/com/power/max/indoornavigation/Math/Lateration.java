package com.power.max.indoornavigation.Math;

import android.annotation.SuppressLint;
import android.util.Log;

import com.power.max.indoornavigation.Model.BaseStation;
import com.power.max.indoornavigation.Model.Coordinate;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.ArrayList;

/**
 * Class to compute a position based on fixed base stations via lateration.
 */
public abstract class Lateration {

    private static final String TAG = "Lateration";

    /**
     * Using the formula A*Pe = b to get Xe, Ye
     *
     * @param baseStations Fix base stations with coordinates and distance.
     * @return Calculated coordinates.
     */
    public static Coordinate calculatePosition(ArrayList<BaseStation> baseStations) {

        baseStations.add(new BaseStation(new Coordinate( 3, 2), 2.5));
        baseStations.add(new BaseStation(new Coordinate( -2, 3), 3.2));
        baseStations.add(new BaseStation(new Coordinate( -3, -1), 4.8));
        baseStations.add(new BaseStation(new Coordinate( 2, -1), 2.5));
        baseStations.add(new BaseStation(new Coordinate( 0, 0), 3.5));

        Coordinate ret = new Coordinate();

        // At least 3 base stations are required.
        if (baseStations.size() < 3)
            throw new IllegalArgumentException("At least 3 base stations are required.");
        else {
            try {
                /**
                 * Formula for least squares method:
                 *      |xe|
                 * Pe = |ye| = (A^T * A)^(-1) * A^T * b
                 */
                double[][] A = generateMatrixA(baseStations);
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

                ret = new Coordinate(resMatrix);

            } catch (IllegalArgumentException e) {
                Log.e(TAG, e.getMessage());
            }
        }

        return ret;
    }

    /**
     * Create Matrix A (2 columns, n rows).
     *          |x1 - x2    y1 - y2|
     *      A = |x1 - x3    y1 - y3|
     *          |x1 - x4    y1 - y4|
     * extensible to n stations.
     *
     * @param baseStations that contains distance and coordinate.
     * @return transformed matrix.
     */
    private static double[][] generateMatrixA(ArrayList<BaseStation> baseStations) {

        double[][] ret = new double[baseStations.size() - 1][2];

        for (int i = 0; i < baseStations.size() - 1; i ++) {
            ret[i][0] = baseStations.get(0).getLatLng().getLat()
                    - baseStations.get(i + 1).getLatLng().getLat();

            ret[i][1] = baseStations.get(0).getLatLng().getLng()
                    - baseStations.get(i + 1).getLatLng().getLng();
        }

        return ret;
    }

    /**
     * Function to transform a matrix.
     *
     *          | 2 -2|
     *      A = |-2  2| => A' = | 2 -2  5|
     *          | 5  3|         |-2  2  3|
     *
     * @param A matrix to transform.
     * @return transformed matrix.
     */
    private static double[][] transformA(double[][] A) {
        double[][] ret = new double[A[0].length][A.length];

        // for each column in A
        for (int col = 0; col < A[0].length; col ++) {
            // for each row in a column in A
            for (int row = 0; row < A.length; row ++) {
                ret[col][row] = A[row][col];
            }
        }

        return ret;
    }

    /**
     * Create Matrix b (1 column, n rows).
     *           |x1^2 - x2^2 + y1^2 - y2^2 + D2^2 - D1|
     * b = 1/2 * |x1^2 - x3^2 + y1^2 - y3^2 + D3^2 - D1|
     *           |x1^2 - x4^2 + y1^2 - y4^2 + D4^2 - D1|
     * @param baseStations that contains distance and coordinate.
     * @return transformed matrix.
     */
    private static double[][] generateMatrixB(ArrayList<BaseStation> baseStations) {
        double[][] ret = new double[baseStations.size() - 1][1];

        for (int i = 0; i < baseStations.size() - 1; i ++) {
            final double x1 = baseStations.get(0).getLatLng().getLat();
            final double y1 = baseStations.get(0).getLatLng().getLng();
            final double D1 = baseStations.get(0).getDistance();

            double xn = baseStations.get(i + 1).getLatLng().getLat();
            double yn = baseStations.get(i + 1).getLatLng().getLng();
            double Dn = baseStations.get(i + 1).getDistance();

            ret[i][0] = 0.5 * (Math.pow(x1, 2) - Math.pow(xn, 2)
                    + Math.pow(y1, 2) - Math.pow(yn, 2)
                    + Math.pow(Dn, 2) - D1);
        }

        return ret;
    }

    /**
     * Function to multiply two matrices.
     * @param m1 First matrix.
     * @param m2 Second matrix.
     * @return Multiplied matrix.
     */
    private static double[][] multiplyByMatrix(double[][] m1, double[][] m2) {
        int m1ColLength = m1[0].length;
        int m2RowLength = m2.length;
        if(m1ColLength != m2RowLength)
            throw new IllegalArgumentException("Matrix 1 column length != Matrix 2 row length.");
        int mRRowLength = m1.length;
        int mRColLength = m2[0].length;
        double[][] mResult = new double[mRRowLength][mRColLength];

        for(int i = 0; i < mRRowLength; i++) {         // rows from m1
            for(int j = 0; j < mRColLength; j++) {     // columns from m2
                for(int k = 0; k < m1ColLength; k++) { // columns from m1
                    mResult[i][j] += m1[i][k] * m2[k][j];
                }
            }
        }
        return mResult;
    }

    /**
     * Function to display a matrix in a string.
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
