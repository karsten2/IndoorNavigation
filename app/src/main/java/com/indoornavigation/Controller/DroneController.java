package com.indoornavigation.Controller;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.SphericalUtil;
import com.indoornavigation.Helper.Utils;
import com.indoornavigation.Math.Lateration_old;
import com.indoornavigation.Math.SRegression;
import com.indoornavigation.Math.Statistics;
import com.parrot.arsdk.ARSDK;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_DEVICE_STATE_ENUM;
import com.parrot.arsdk.arcontroller.ARControllerCodec;
import com.parrot.arsdk.arcontroller.ARFrame;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.indoornavigation.Database.DbTables;
import com.indoornavigation.Database.SQLiteDBHelper;
import com.indoornavigation.Drone.BebopDrone;
import com.indoornavigation.Drone.DroneDiscoverer;
import com.indoornavigation.Model.BaseStation;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that handles all drone events and inputs.
 */
public class DroneController {

    private DroneDiscoverer droneDiscoverer;
    private BebopDrone mBebopDrone;
    private SQLiteDBHelper db;

    private Context context;

    private static final String TAG = "DroneController";

    private LatLng currentPositionGPS;
    private LatLng currentPosition = new LatLng(-1, -1);
    private float currentAttitudeYaw;

    private boolean autonomousFlight = false;
    private ArrayList<Marker> route = new ArrayList<>();
    private ArrayList<Marker> routeDone = new ArrayList<>();

    private ArrayList<BaseStation> dbBaseStations = new ArrayList<>();
    private ArrayList<Statistics> apStatistics = new ArrayList<>();
    private int statisticsWindowSize = 80;
    private Statistics droneStatisticsLat = new Statistics(20);
    private Statistics droneStatisticsLng = new Statistics(20);

    private EstimatePosition estimatePositionTask;

    private List<Listener> mListener;



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

        void positionChangedListener(LatLng latLng, float bearing);

        void onWifiScanlistChanged(ArrayList<BaseStation> baseStations);
    }

    public void setListener(Listener listener) {
        if (mListener == null)
            mListener = new ArrayList<>();

        mListener.add(listener);
    }

    /**
     * Event that fires when a marker is added to array routeDone.
     *
     * @param marker the last added Marker in array routeDone.
     */
    private void notifyCheckPointReached(Marker marker) {
        for (Listener listener : mListener) {
            listener.checkPointReachedListener(marker);
        }
    }

    /**
     * Event that fires when the drone sends a video.
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

    /**
     * Event fires when the computed position of the drone changed.
     *
     * @param latLng the app computed.
     */
    private void notifyPositionChanged(LatLng latLng, float bearing) {
        for (Listener listener : mListener) {
            listener.positionChangedListener(latLng, bearing);
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
        this.dbBaseStations = getCursorData(db.rawQuery(DbTables.RadioMap.SQL_SELECT_ALL));

        droneDiscoverer = new DroneDiscoverer(context);
        droneDiscoverer.setup();
        droneDiscoverer.addListener(mDiscovererListener);
        droneDiscoverer.startDiscovering();
    }

    private Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            mBebopDrone.scanWifi();
            handler.postDelayed(runnable, 5000);
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

            Log.d(TAG, state.toString());
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
        public void onWifiScanListChanged(ArrayList<BaseStation> baseStations) {
            notifyWifiScanlistChanged(baseStations);
            //Log.d(TAG, "wifi found: " + baseStations.size());

            if (estimatePositionTask == null ||
                    estimatePositionTask.getStatus() == AsyncTask.Status.FINISHED) {
                estimatePositionTask = new EstimatePosition();
                estimatePositionTask.execute(baseStations);
            }
        }

        @Override
        public void onAttitudeChanged(float roll, float pitch, float yaw) {
            currentAttitudeYaw = radToDeg(yaw);

            //Log.d(TAG, String.valueOf(currentAttitudeYaw) );
        }

        @Override
        public void onPositionChanged(double latitude, double longitude, double altitude) {
            currentPositionGPS = new LatLng(latitude, longitude);
            //Log.d(TAG, currentPositionGPS.toString());
        }
    };

    public BebopDrone.Listener getBebopListener() {
        return this.mBebopListener;
    }

    /**
     * Function to move the connected drone.
     * pitch Forward/ backward angle of the drone. Value in percentage from -100 to 100.
     * flag 1 if the pitch and roll values should be used, 0 otherwise.
     * <p>
     * The drone is moved forward, until the coordinates of the drone matches the destination.
     * <p>
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
                    currentPosition, destination) > toleranceDistance) ;

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
        this.droneLand();
    }

    /**
     * Function to get data from database cursor.
     *
     * @param cursor from Database.
     * @return List with all BaseStations from Database.
     */
    public ArrayList<BaseStation> getCursorData(Cursor cursor) {
        ArrayList<BaseStation> ret = new ArrayList<>();

        if (cursor != null && !cursor.isClosed()) {
            if (cursor.moveToFirst()) {
                do {
                    try {
                        BaseStation bs = new BaseStation();
                        bs.setSsid(cursor.getString(cursor.getColumnIndex(DbTables.RadioMap.COL_SSID)));
                        bs.setLatLng(new LatLng(
                                cursor.getDouble(cursor.getColumnIndex(DbTables.RadioMap.COL_LAT)),
                                cursor.getDouble(cursor.getColumnIndex(DbTables.RadioMap.COL_LNG))
                        ));
                        ret.add(bs);
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                    }
                } while (cursor.moveToNext());

                cursor.close();
            }
        }
        return ret;
    }

    private ArrayList<BaseStation> getBsFromDb(ArrayList<BaseStation> baseStations) {
        ArrayList<BaseStation> foundBaseSations = new ArrayList<>();

        // find base stations in database.
        for (BaseStation bs : baseStations) {
            Cursor c = db.sqlSelect(
                    DbTables.RadioMap.TABLE_NAME,
                    null,
                    DbTables.RadioMap.COL_SSID + " = ?",
                    new String[]{(bs.getSsid() != null ? bs.getSsid() : "")},
                    null, null, null
            );

            Cursor c2 = db.rawQuery("select * from radiomap where ssid = '" + bs.getSsid() + "'");
            foundBaseSations.addAll(getCursorData(c));
        }

        return foundBaseSations;
    }

    /**
     * Function to estimate the drones position with scanned wifi points.
     *
     * @param baseStations scanned from the drone.
     * @return true if the position changed, false otherwise.
     */
    private boolean getPositionFromWifi(ArrayList<BaseStation> baseStations) {
        SRegression sRegression = new SRegression(true);
        double freqInMhz = 2457;
        ArrayList<BaseStation> foundBaseSations = new ArrayList<>();

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

                        //double distance = (double) ((int)Math.abs(sRegression.getPrediction(bs.getRssi())) * 10000) / 10000;
                        double distance = (double) ((int)Math.abs(Utils.calculateDistance(bs.getRssi(), freqInMhz)) * 10000) / 10000;

                        //temp.setDistance(Utils.calculateDistance(bs.getRssi(), freqInMhz));
                        temp.setDistance(distance);
                        foundBaseSations.add(temp);
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
            //Log.d(TAG, "basestations in database found: " + foundBaseSations.size());
            LatLng newPosition = Lateration_old.calculatePosition(foundBaseSations);
            if (!newPosition.equals(new LatLng(0, 0)) && !newPosition.equals(currentPosition)) {

                droneStatisticsLat.add(newPosition.latitude);
                droneStatisticsLng.add(newPosition.longitude);
                currentPosition = new LatLng(droneStatisticsLat.getMean(), droneStatisticsLng.getMean());
                return true;
            }
            Log.d("drone",
                    "\nwifi found: " + baseStations.size()
                            + "\nwifi in db found: " + foundBaseSations.size()
                            + "\ncalculated Position: " + currentPosition.toString());
        } catch (Exception e) {
            //Log.e(TAG, e.getMessage());
        }

        return false;
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

        /*if (mBebopDrone != null && route.size() > 0) {
            if (!droneIsFlying()) {
                droneTakeOff();
            }
            autonomousFlight = true;
        }*/

        if (mBebopDrone != null) {
            autonomousFlight = true;
            droneTakeOff();
        }
    }

    /**
     * Function is executed when autonomousFlight is true, and the drone entered hovering mode.
     */
    private void startAutopilot() {
        //yawTask.execute(0);
        new YawTaskTest().execute(90);

    }

    /**
     * Function to turn the drone around the z-Axis for a specific angle.
     * <p>
     * lat/lng: (0.0,1.0) atan2:     90.0
     * lat/lng: (1.0,0.0) atan2:     0.0
     * lat/lng: (-1.0,0.0)atan2:    -180.0
     * lat/lng: (0.0,-1.0)atan2:    -90.0
     * <p>
     * 90
     * |
     * |
     * +-180 _______|_______ 0
     * |
     * |
     * |
     * -90
     * <p>
     * Use modulo to get the correct degrees.
     * degrees = degrees % 360.
     * 0 to 180    will return the same numbers you put in.
     * -180 to -1  return values between 180 to 359 degrees.
     * <p>
     * The function will find out the shortest way to turn the drone (left or right) to reach
     * the wanted bearing.
     *
     */
    AsyncTask yawTask = new AsyncTask<Void, Void, Marker>() {
        @Override
        protected Marker doInBackground(Void... params) {
            if (mBebopDrone != null && droneIsFlying() && route.size() > 0) {
                final Marker marker = route.get(0);
                Log.d(TAG, "Next target: " + marker.getPosition().toString());
                final double bearing = SphericalUtil.computeHeading(
                        currentPosition, marker.getPosition());
                double startAngle = currentAttitudeYaw;
                final double tolerance = 3.5;
                final double result = (bearing % 360) - (startAngle % 360);

                Log.d(TAG, "droneYawTo: startAngle: " + currentAttitudeYaw + " destBearing: " + bearing
                        + "result = " + result);

                droneYaw(40 * (result < 0 ? -1 : 1));

                while (!(currentAttitudeYaw >= bearing - tolerance
                        && currentAttitudeYaw <= bearing + tolerance)) {
                }

                Log.d(TAG, "Drone yawed for: " + result);

                // end turning.
                droneYaw(0);

                return marker;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Marker marker) {
            moveTask.execute(marker);
            super.onPostExecute(marker);
        }
    };

    AsyncTask moveTask = new AsyncTask<Marker, Void, Marker>() {
        @Override
        protected Marker doInBackground(Marker... markers) {
            Marker destination = (Marker) markers[0];
            dronePitch(30, 1);
            final double toleranceDistance = 1;

            while (SphericalUtil.computeDistanceBetween(
                    currentPosition, destination.getPosition()) > toleranceDistance) ;

            dronePitch(0, 0);

            return destination;
        }

        @Override
        protected void onPostExecute(Marker marker) {
            super.onPostExecute(marker);

            route.remove(marker);

            if (route.size() > 0)
                yawTask.execute();
            else
                droneLand();
        }
    };


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
     * @param divisor Divisor.
     * @return modulo. 
     */
    private double mod(double dividend, int divisor) {
        if (dividend >= 0)
            return dividend % divisor;
        else
            return (((dividend % divisor) + divisor) % divisor);
    }

    private class YawTaskTest extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] params) {
            if (mBebopDrone != null && droneIsFlying()) {
                autonomousFlight = false;
                final double bearing = Double.valueOf(params[0].toString());
                double startAngle = currentAttitudeYaw;
                final double tolerance = 3.5;
                final double result = mod(bearing, 360) - mod(startAngle, 360);


                Log.d(TAG, "droneYawTo: startAngle: " + currentAttitudeYaw + " destBearing: " + bearing
                        + "result = " + result);

                // start turning
                droneYaw(40 * (result < 0 ? -1 : 1));

                while (!(currentAttitudeYaw >= bearing - tolerance
                        && currentAttitudeYaw <= bearing + tolerance)) {
                    // wait until drone is in position
                }

                Log.d(TAG, "Drone yawed for: " + result);

                // end turning.
                droneYaw(0);

                return bearing * -1;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            new MoveTaskTest().execute(o);
        }
    }

    private class MoveTaskTest extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] params) {
            dronePitch(20, 1);

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
//
            }

            dronePitch(0, 0);

            //droneLand();

            return params[0];
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            new YawTaskTest().execute(o);
        }
    }

    private class EstimatePosition extends AsyncTask<ArrayList<BaseStation>, Void, Boolean> {
        @SafeVarargs
        @Override
        protected final Boolean doInBackground(ArrayList<BaseStation>... baseStations) {
            if (baseStations != null && baseStations[0] != null) {
                try {
                    return getPositionFromWifi(new ArrayList<>(baseStations[0]));
                } catch (Exception e) {
                    Log.e("estimate position", e.getMessage());
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean notify) {
            super.onPostExecute(notify);
            if (notify != null && notify)
                notifyPositionChanged(currentPosition, currentAttitudeYaw);
        }
    }
}
