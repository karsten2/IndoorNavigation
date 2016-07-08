package com.indoornavigation.Math;

import android.annotation.SuppressLint;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.geometry.Point;
import com.indoornavigation.Math.Lateration.LinearLeastSquaresSolver;
import com.indoornavigation.Math.Lateration.NonLinearLeastSquaresSolver;
import com.indoornavigation.Math.Lateration.TrilaterationFunction;
import com.indoornavigation.Model.BaseStation;

import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.linear.RealVector;

import com.google.maps.android.projection.SphericalMercatorProjection;

import java.util.ArrayList;

/**
 * Class to compute a position based on fixed base stations via lateration.
 * https://github.com/lemmingapex/Trilateration
 */
public abstract class Trilateration {

    private static final String TAG = "Trilateration";

    /**
     * Using the formula A*Pe = b to get Xe, Ye
     *
     * @param baseStations Fix base stations with coordinates and distance.
     * @return Calculated coordinates.
     */
    public static LatLng calculatePosition(ArrayList<BaseStation> baseStations) {

        LatLng ret = new LatLng(0, 0);

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

            TrilaterationFunction trilaterationFunction =
                    new TrilaterationFunction(positions, distances);
            LinearLeastSquaresSolver lSolver =
                    new LinearLeastSquaresSolver(trilaterationFunction);
            RealVector x = lSolver.solve();

            NonLinearLeastSquaresSolver solver = new NonLinearLeastSquaresSolver(
                    new TrilaterationFunction(positions, distances),
                    new LevenbergMarquardtOptimizer());
            LeastSquaresOptimizer.Optimum optimum = solver.solve();

            double[] centroid = optimum.getPoint().toArray();

            double[] lin = x.toArray();

            ret = mercToLatLng(new Point(centroid[0], centroid[1]));
            return ret;
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
}
