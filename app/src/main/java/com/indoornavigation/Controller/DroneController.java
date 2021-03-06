package com.indoornavigation.Controller;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Debug;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.SphericalUtil;
import com.indoornavigation.Database.DbTables;
import com.indoornavigation.Database.SQLiteDBHelper;
import com.indoornavigation.Drone.BebopDrone;
import com.indoornavigation.Drone.DroneDiscoverer;
import com.indoornavigation.Helper.Utils;
import com.indoornavigation.Math.Trilateration;
import com.indoornavigation.Math.MathUtils;
import com.indoornavigation.Math.Statistics;
import com.indoornavigation.Model.BaseStation;
import com.indoornavigation.Model.CustomVector;
import com.indoornavigation.Model.CustomVectorPair;
import com.parrot.arsdk.ARSDK;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_DEVICE_STATE_ENUM;
import com.parrot.arsdk.arcontroller.ARControllerCodec;
import com.parrot.arsdk.arcontroller.ARFrame;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadFactory;

/**
 * Class that handles all drone events and inputs.
 */
public class DroneController {

    private DroneDiscoverer droneDiscoverer;
    private BebopDrone mBebopDrone;
    private SQLiteDBHelper db;

    private Context context;

    private static final String TAG = "DroneController";

    private LatLng currentPosition = new LatLng(-1, -1);
    private float currentAttitudeYaw;

    private boolean autonomousFlight = false;
    private ArrayList<Marker> route = new ArrayList<>();
    private ArrayList<Marker> routeDone = new ArrayList<>();

    private ArrayList<BaseStation> dbBaseStations = new ArrayList<>();
    private ArrayList<Statistics> apStatistics = new ArrayList<>();
    private int statisticsWindowSize = 10;
    private Statistics droneStatisticsLat = new Statistics(20);
    private Statistics droneStatisticsLng = new Statistics(20);

    private LatLng target;

    private EstimatePosition estimatePositionTask;

    private List<Listener> mListener;

    public boolean estimatePosition = false;

    private Handler mHandler;

    // Load native libraries, mandatory!
    static {
        ARSDK.loadSDKLibs();
    }

    /**
     * Listener that fires, if the drone reached a checkpoint.
     */
    public interface Listener {
        void checkPointReachedListener(Marker marker);

        void videoReceivedListener(ARControllerCodec codec);

        void onDroneConnectionChangedListener(boolean connected);

        void onBearingChangedListener(float bearing);

        void positionChangedListener(LatLng latLng);

        void onWifiScanlistChanged(ArrayList<BaseStation> baseStations);
    }

    public void setListener(Listener listener) {
        if (mListener == null)
            mListener = new ArrayList<>();

        mListener.add(listener);
    }

    public void removeListener(Listener listener) {
        if (mListener != null)
            mListener.remove(listener);
    }

    /**
     * Event fires when a marker is added to array routeDone.
     *
     * @param marker the last added Marker in array routeDone.
     */
    private void notifyCheckPointReached(Marker marker) {
        for (Listener listener : mListener) {
            listener.checkPointReachedListener(marker);
        }
    }

    /**
     * Event fires when the drone sends a video.
     *
     * @param codec that contains the video from the drone.
     */
    private void notifyVideoReceived(ARControllerCodec codec) {
        for (Listener listener : mListener) {
            listener.videoReceivedListener(codec);
        }
    }

    /**
     * Event fires when the dronecontroller connects to a drone.
     *
     * @param connected true if drone is connected, otherwise false.
     */
    private void notifyDroneConnected(boolean connected) {
        for (Listener listener : mListener) {
            listener.onDroneConnectionChangedListener(connected);
        }
    }

    private void notifyBearingChanged(float bearing) {
        for (Listener listener : mListener) {
            listener.onBearingChangedListener(bearing);
        }
    }

    /**
     * Event fires when the computed position of the drone changed.
     *
     * @param latLng the app computed.
     */
    private void notifyPositionChanged(LatLng latLng) {
        for (Listener listener : mListener) {
            listener.positionChangedListener(latLng);
        }
    }

    /**
     * Event fires when the list of scanned wifi networks changed.
     *
     * @param baseStations the drone scanned.
     */
    private void notifyWifiScanlistChanged(ArrayList<BaseStation> baseStations) {
        for (Listener listener : mListener) {
            listener.onWifiScanlistChanged(baseStations);
        }
    }

    public DroneController(Context context) {
        this.context = context;

        db = new SQLiteDBHelper(context);
        this.dbBaseStations = db.getBaseStations();

        droneDiscoverer = new DroneDiscoverer(context);
        droneDiscoverer.setup();
        droneDiscoverer.addListener(mDiscovererListener);
        droneDiscoverer.startDiscovering();

        mHandler = new Handler(context.getMainLooper());
    }

    private Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (mBebopDrone != null) {
                mBebopDrone.scanWifi();
                handler.postDelayed(runnable, 3000);
            }
        }
    };

    private final DroneDiscoverer.Listener mDiscovererListener = new DroneDiscoverer.Listener() {
        @Override
        public void onDronesListUpdated(List<ARDiscoveryDeviceService> dronesList) {
            if (dronesList.size() > 0) {
                ARDiscoveryDeviceService service = new ARDiscoveryDeviceService(
                        dronesList.get(0).getName(),
                        dronesList.get(0).getDevice(),
                        dronesList.get(0).getProductID()
                );

                mBebopDrone = new BebopDrone(context, service);
                mBebopDrone.addListener(mBebopListener);
                if (mBebopDrone.connect()) {
                    mBebopDrone.setWifiBandToAll();
                    runnable.run();
                    notifyDroneConnected(true);
                }

                Log.d(TAG, "drone found" + dronesList.get(0).getName());
            }
        }
    };

    private final BebopDrone.Listener mBebopListener = new BebopDrone.Listener() {
        @Override
        public void onDroneConnectionChanged(ARCONTROLLER_DEVICE_STATE_ENUM state) {
            Log.d(TAG, "onDroneConnectionChanged" + state.toString());
            switch (state) {
                case ARCONTROLLER_DEVICE_STATE_RUNNING:
                    notifyDroneConnected(true);
                    //handler.postDelayed(runnable, 2000);
                    break;
                case ARCONTROLLER_DEVICE_STATE_STOPPED:
                    notifyDroneConnected(false);
            }
        }

        @Override
        public void onBatteryChargeChanged(int batteryPercentage) {
            switch (batteryPercentage) {
                case 75:
                case 50:
                    Toast.makeText(context,
                            "Battery has " + batteryPercentage + "% left.",
                            Toast.LENGTH_LONG).show();
                    break;
                case 25:
                case 15:
                    Toast.makeText(context,
                            "Battery low! " + batteryPercentage + "% left",
                            Toast.LENGTH_LONG).show();
                    break;
                case 5:
                    Toast.makeText(context,
                            "Battery low, landing now...",
                            Toast.LENGTH_LONG).show();
                    droneLand();
                    Log.d(TAG, "battery on " + batteryPercentage + "%. Drone landing...");
            }
            Log.d("Battery", " on " + batteryPercentage + " %");
        }

        @Override
        public void onPilotingStateChanged(
                ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM state) {
            switch (state) {
                case ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_HOVERING:
                    Log.d(TAG, "PilotingStateChanged to hovering.");
                    if (autonomousFlight) startAutopilot();
                    break;
            }

            //Log.d(TAG, state.toString());
        }

        @Override
        public void onPictureTaken(
                ARCOMMANDS_ARDRONE3_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR_ENUM error) {
        }

        @Override
        public void configureDecoder(ARControllerCodec codec) {
            //notifyVideoReceived(codec);
        }

        @Override
        public void onFrameReceived(ARFrame frame) {
        }

        @Override
        public void onMatchingMediasFound(int nbMedias) {
        }

        @Override
        public void onDownloadProgressed(String mediaName, int progress) {
        }

        @Override
        public void onDownloadComplete(String mediaName) {
        }

        @Override
        public void onWifiScanListChanged(final ArrayList<BaseStation> baseStations) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    notifyWifiScanlistChanged(new ArrayList<>(baseStations));
                }
            });

            //Log.d(TAG, "wifi found: " + baseStations.size());

            if (estimatePosition) {
                if (estimatePositionTask == null ||
                        estimatePositionTask.getStatus() == AsyncTask.Status.FINISHED) {
                    estimatePositionTask = new EstimatePosition();
                    estimatePositionTask.execute(baseStations);
                }
            }
        }

        @Override
        public void onAttitudeChanged(float roll, float pitch, float yaw) {
            currentAttitudeYaw = radToDeg(yaw);
            notifyBearingChanged(currentAttitudeYaw);
        }

        @Override
        public void onPositionChanged(double latitude, double longitude, double altitude) {
            Log.d(TAG, "Current Position: " + currentPosition);
        }
    };

    private ArrayList<CustomVector> getVectorTable(int size, Double bearing) {
        return db.getVectorTable(size, bearing);
    }

    public BebopDrone.Listener getBebopListener() {
        return this.mBebopListener;
    }

    /**
     * Function to move the connected drone.
     * pitch Forward/ backward angle of the drone. Value in percentage from -100 to 100.
     * flag 1 if the pitch and roll values should be used, 0 otherwise.
     * <p/>
     * The drone is moved forward, until the coordinates of the drone matches the destination.
     * <p/>
     * This function should be started async.
     *
     * @param destination the drone should fly.
     */
    private void droneMove(final LatLng destination) {
        if (mBebopDrone != null) {
            mBebopDrone.setPitch((byte) 30);
            mBebopDrone.setFlag((byte) 1);
            final double toleranceDistance = 1;

            while (SphericalUtil.computeDistanceBetween(
                    getCurrentPosition(), destination) > toleranceDistance) ;

            mBebopDrone.setPitch((byte) 0);
            mBebopDrone.setFlag((byte) 0);
        }
    }

    /**
     * Function to move the drone.
     *
     * @param speed as byte value +- 100. positive = forward, negative = backward.
     * @param flag
     */
    private void dronePitch(double speed, double flag) {
        if (mBebopDrone != null) {
            mBebopDrone.setPitch((byte) speed);
            mBebopDrone.setFlag((byte) flag);
        }
    }

    /**
     * Function to turn the drone around the z-axis.
     *
     * @param speed as byte value +- 100 positive = right; negative = left turn.
     */
    private void droneYaw(double speed) {
        if (mBebopDrone != null) {
            mBebopDrone.setYaw((byte) speed);
        }
    }

    /**
     * Function to start the drone.
     */
    private void droneTakeOff() {
        if (mBebopDrone != null
                && mBebopDrone.getFlyingState() ==
                ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM
                        .ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_LANDED) {
            mBebopDrone.takeOff();
            mBebopDrone.calibrateMagnetometer((byte) 1);
        }
    }

    /**
     * Function to land the drone.
     */
    private void droneLand() {
        if (mBebopDrone != null && droneIsFlying()) {
            mBebopDrone.land();
        }
    }

    /**
     * Landing the drone from outside the class.
     */
    public void EmergencyLand() {
        this.autonomousFlight = false;

        stopMove();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        this.droneLand();
    }

    /**
     * Function to estimate the drones position with scanned wifi points.
     *
     * @param baseStations scanned from the drone.
     * @return true if the position changed, false otherwise.
     */
    private boolean getPositionFromRSS(ArrayList<BaseStation> baseStations) {
        ArrayList<BaseStation> foundBaseStations = new ArrayList<>();

        // smooth rssi data for every baseStation
        for (BaseStation bs : baseStations) {
            boolean found = false;

            if (dbBaseStations.contains(bs)) {
                BaseStation temp = dbBaseStations.get(dbBaseStations.indexOf(bs));

                for (Statistics stat : apStatistics) {
                    if (stat.getName().equals(bs.getSsid())) {
                        found = true;
                        stat.add(bs.getRssi());
                        temp.setRssi(stat.getMedian());

                        double distance = -1;
                        try {
                            /*if (temp.getRss1_1m() != 1 && temp.getLat_const() != -1)
                                distance = MathUtils.distance(
                                        temp.getRssi(), temp.getRss1_1m(), temp.getLat_const());*/
                            distance = MathUtils.distanceFSPL(temp.getRssi(), 2412);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        temp.setDistance(distance);
                        foundBaseStations.add(temp);
                        break;
                    }
                }

                if (!found) {
                    Statistics statistics = new Statistics(statisticsWindowSize, bs.getSsid());
                    statistics.add(bs.getRssi());
                    apStatistics.add(statistics);
                }
            }
        }

        // laterate Position.
        try {
            for (int i = foundBaseStations.size() - 1; i >= 0; i--) {
                if (foundBaseStations.get(i).getDistance() == -1) {
                    foundBaseStations.remove(i);
                }
            }
            LatLng newPosition = Trilateration.calculatePosition(foundBaseStations);
            if (!newPosition.equals(new LatLng(0, 0)) && !newPosition.equals(getCurrentPosition())) {

                droneStatisticsLat.add(newPosition.latitude);
                droneStatisticsLng.add(newPosition.longitude);
                setCurrentPosition(new LatLng(droneStatisticsLat.getMean(), droneStatisticsLng.getMean()));
                return true;
            }
            Log.d(TAG, "Position: " + currentPosition);
        } catch (Exception e) {
            //Log.e(TAG, e.getMessage());
        }

        return false;
    }


    private boolean getPositionFromRadiomap(ArrayList<BaseStation> scanResults) {

        ArrayList<BaseStation> foundBaseStations = new ArrayList<>();
        ArrayList<Integer> foundIds = new ArrayList<>();
        ArrayList<Double> foundRSS = new ArrayList<>();

        // check which base stations in the scan results are part of the db base stations.
        for (BaseStation bs : scanResults) {
            boolean found = false;

            if (dbBaseStations.contains(bs)) {
                BaseStation temp = dbBaseStations.get(dbBaseStations.indexOf(bs));

                for (Statistics stat : apStatistics) {
                    // find the matching statistics for each base station and add the values.
                    if (stat.getName().equals(bs.getSsid())) {
                        found = true;
                        stat.add(bs.getRssi());
                        temp.setRssi(stat.getMean());
                        foundBaseStations.add(temp);
                        foundIds.add(temp.getDbId());
                        break;
                    }
                }

                if (!found) {
                    // create statistics if not exist.
                    Statistics statistics = new Statistics(statisticsWindowSize, bs.getSsid());
                    statistics.add(bs.getRssi());
                    apStatistics.add(statistics);
                }

            }
        }

        // Get ids from the database
        if (foundBaseStations.size() >= 3) {
            if (db.hasData(
                    DbTables.tableContainsAps(
                            "radiomap_" + foundBaseStations.size(), foundIds))) {
                ArrayList<CustomVector> vectorTable =
                        getVectorTable(foundBaseStations.size(), (double) this.currentAttitudeYaw);
                ArrayList<CustomVectorPair> vectorDifferences = new ArrayList<>();

                Collections.sort(foundBaseStations);

                for (BaseStation bs : foundBaseStations) {
                    foundRSS.add(bs.getRssi());
                }

                foundRSS = MathUtils.normalizeVector(foundRSS);

                for (CustomVector v : vectorTable) {
                    // subtract vector and get magnitude.
                    double magnitude = MathUtils.magnitudeVector(
                            MathUtils.subtractVector(foundRSS, v.getValues()));

                    // put difference in new table
                    vectorDifferences.add(new CustomVectorPair(v.getLatLng(), magnitude));
                }

                // find the N smallest values:
                int N = Utils.getPrefKnn(context);
                Collections.sort(vectorDifferences);
                LatLng newPosition;
                if (N > vectorDifferences.size())
                    newPosition = averageLatLng(vectorDifferences);
                else
                    newPosition = averageLatLng(vectorDifferences.subList(0, N));

                if (!newPosition.equals(new LatLng(0, 0)) && !newPosition.equals(getCurrentPosition())) {

                    droneStatisticsLat.add(newPosition.latitude);
                    droneStatisticsLng.add(newPosition.longitude);
                    setCurrentPosition(new LatLng(droneStatisticsLat.getMean(),
                            droneStatisticsLng.getMean()));

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            notifyPositionChanged(currentPosition);
                        }
                    });

                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Function to get the average latlng coordinates out of n latlng values.
     * Used when knn returns k nearest neighbors.
     * lat values are summed up
     * lng values are summed up
     * divided by number of coordinates in vectorPair array.
     *
     * @param vectorPairs Array with coordinates.
     * @return averaged latlng.
     */
    private LatLng averageLatLng(List<CustomVectorPair> vectorPairs) {
        double lat = 0;
        double lng = 0;
        int n = vectorPairs.size();
        for (CustomVectorPair v : vectorPairs) {
            lat += v.getLatLng().latitude;
            lng += v.getLatLng().longitude;
        }

        return new LatLng(lat / n, lng / n);
    }

    /**
     * Function converts radians to degrees.
     *
     * @param value in radiant.
     * @return value in degrees.
     */
    private float radToDeg(float value) {
        return (float) (value * (180 / Math.PI));
    }

    /**
     * Function to start an autonomous flight with the drone.
     *
     * @param route the drone has to fly.
     */
    public void startAutopilot(ArrayList<Marker> route) {
        this.route = route;

        if (mBebopDrone != null) {
            //autonomousFlight = true;
            droneTakeOff();


            mBebopDrone.setFlag((byte) 0);

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                //e.printStackTrace();
            }

            Log.d(TAG, "start sinking");

            mBebopDrone.setGaz((byte) -30);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                //e.printStackTrace();
            }

            Log.d(TAG, "stopped sinking, starting autonomous task");

            mBebopDrone.setGaz((byte) 0);

            autonomousFlight = true;
        }
    }

    /**
     * Function is executed when autonomousFlight is true, and the drone entered hovering mode.
     */
    private void startAutopilot() {
        Log.d(TAG, "starting autopilot: " + route.size());
        if (route.size() > 0) {
            YawTask yawTask = new YawTask(route.get(0).getTitle(), route.get(0).getPosition());
            yawTask.execute();
        } else {
            mBebopDrone.land();
        }
    }

    /**
     * Function to turn the drone around the z-Axis for a specific angle.
     * <p/>
     * lat/lng: (0.0,1.0) atan2:     90.0
     * lat/lng: (1.0,0.0) atan2:     0.0
     * lat/lng: (-1.0,0.0)atan2:    -180.0
     * lat/lng: (0.0,-1.0)atan2:    -90.0
     * <p/>
     * 90
     * |
     * |
     * +-180 _______|_______ 0
     * |
     * |
     * |
     * -90
     * <p/>
     * Use modulo to get the correct degrees.
     * degrees = degrees % 360.
     * 0 to 180    will return the same numbers you put in.
     * -180 to -1  return values between 180 to 359 degrees.
     * <p/>
     * The function will find out the shortest way to turn the drone (left or right) to reach
     * the wanted bearing.
     */
    private class YawTask extends AsyncTask<Void, Void, Void> {

        private LatLng mLatLng;
        private String mTitle;

        public YawTask(String title, LatLng latLng) {
            mLatLng = latLng;
            mTitle = title;
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (mBebopDrone != null && droneIsFlying() && route.size() > 0) {
                mBebopDrone.setFlag((byte) 0);
                double bearing = SphericalUtil.computeHeading(
                        getCurrentPosition(), mLatLng);
                double tolerance = 3.5;
                double result = bearing - currentAttitudeYaw;

                result += (result > 180) ? -360 : (result < -180) ? 360 : 0;
                Log.d(TAG, "start yawing");
                droneYaw(40 * (result < 0 ? -1 : 1));

                while (!(currentAttitudeYaw >= bearing - tolerance
                        && currentAttitudeYaw <= bearing + tolerance)) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        Log.e(TAG, e.getMessage());
                    }
                }

                droneYaw(0);
                Log.d(TAG, "End yawing");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            target = mLatLng;
            startMove();
        }

    }

    private void startMove() {
        Log.d(TAG, "start moving");
        dronePitch(5, 1);
    }

    private void stopMove() {
        Log.d(TAG, "stop moving");
        dronePitch(0, 0);
    }

    private boolean droneIsFlying() {
        if (mBebopDrone != null) {
            switch (mBebopDrone.getFlyingState()) {
                case ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_FLYING:
                case ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_HOVERING:
                    return true;
            }
        }

        return false;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();

        Log.d("drone", "finalized");

        if (droneDiscoverer != null) {
            droneDiscoverer.stopDiscovering();
            droneDiscoverer.cleanup();
            droneDiscoverer.removeListener(mDiscovererListener);
        }

        db.close();
    }

    public void destroy() {
        try {

            if (mBebopDrone != null) {
                this.droneLand();
            }

            if (droneDiscoverer != null) {
                droneDiscoverer.stopDiscovering();
                droneDiscoverer.cleanup();
                droneDiscoverer.removeListener(mDiscovererListener);
                this.mListener.clear();
            }

            db.close();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    /**
     * Function to get the modulo, without getting negative values.
     *
     * @param dividend Dividend.
     * @param divisor  Divisor.
     * @return modulo.
     */
    private double mod(double dividend, int divisor) {
        if (dividend >= 0)
            return dividend % divisor;
        else
            return (((dividend % divisor) + divisor) % divisor);
    }

    private class EstimatePosition extends AsyncTask<ArrayList<BaseStation>, Void, Boolean> {
        @SafeVarargs
        @Override
        protected final Boolean doInBackground(ArrayList<BaseStation>... baseStations) {
            if (baseStations != null && baseStations[0] != null) {
                try {
                    switch (Utils.getPrefPositioningMethod(context)) {
                        case 1:
                            return getPositionFromRSS(new ArrayList<>(baseStations[0]));
                        case 2:
                            return getPositionFromRadiomap(new ArrayList<>(baseStations[0]));
                    }
                } catch (Exception e) {
                    Log.e("estimate position", " " + e.getMessage());
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean notify) {
            super.onPostExecute(notify);
            if (notify != null && notify) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        //notifyPositionChanged(currentPosition);
                    }
                });
            }
        }
    }

    public LatLng getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(LatLng currentPosition) {
        this.currentPosition = currentPosition;

        if (target != null) {
            double toleranceDistance = 1.25;
            Log.d(TAG, "Distanz: " + SphericalUtil.computeDistanceBetween(
                    getCurrentPosition(), target));
            if (SphericalUtil.computeDistanceBetween(
                    getCurrentPosition(), target) <= toleranceDistance) {

                stopMove();

                target = null;

                if (route.size() > 0)
                    route.remove(0);

                if (route.size() > 0) {
                    YawTask yawTask = new YawTask(route.get(0).getTitle(),
                            new LatLng(route.get(0).getPosition().latitude,
                                    route.get(0).getPosition().longitude));
                    yawTask.execute();
                } else
                    droneLand();
            }
        }
    }
}
