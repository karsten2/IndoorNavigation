package com.power.max.indoornavigation.Controller;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.SphericalUtil;
import com.parrot.arsdk.ARSDK;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_DEVICE_STATE_ENUM;
import com.parrot.arsdk.arcontroller.ARControllerCodec;
import com.parrot.arsdk.arcontroller.ARFrame;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.power.max.indoornavigation.Database.DbTables;
import com.power.max.indoornavigation.Database.SQLiteDBHelper;
import com.power.max.indoornavigation.Drone.BebopDrone;
import com.power.max.indoornavigation.Drone.DroneDiscoverer;
import com.power.max.indoornavigation.Math.Lateration;
import com.power.max.indoornavigation.Model.BaseStation;

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
    private LatLng currentPosition;
    private float currentAttitudeYaw;

    private boolean autonomousFlight = false;
    private ArrayList<Marker> route = new ArrayList<>();
    private ArrayList<Marker> routeDone = new ArrayList<>();

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
    }

    public void setListener(Listener listener) {
        if (mListener == null)
            mListener = new ArrayList<>();

        mListener.add(listener);
    }

    /**
     * Event that fires when a marker is added to array routeDone.
     * @param marker the last added Marker in array routeDone.
     */
    private void notifyCheckPointReached(Marker marker) {
        for (Listener listener : mListener) {
            listener.checkPointReachedListener(marker);
        }
    }

    /**
     * Event that fires when the drone sends a video.
     * @param codec that contains the video from the drone.
     */
    private void notifyVideoReceived(ARControllerCodec codec) {
        for (Listener listener : mListener) {
            listener.videoReceivedListener(codec);
        }
    }

    /**
     * Event fires when the dronecontroller connects to a drone.
     * @param connected true if drone is connected, otherwise false.
     */
    private void notifyDroneConnected(boolean connected) {
        for (Listener listener : mListener) {
            listener.onDroneConnectionChangedListener(connected);
        }
    }

    /**
     * Event fires when the computed position of the drone changed.
     * @param latLng the app computed.
     */
    private void notifyPositionChanged(LatLng latLng, float bearing) {
        for (Listener listener : mListener) {
            listener.positionChangedListener(latLng, bearing);
        }
    }



    public DroneController(Context context) {
        this.context = context;

        db = new SQLiteDBHelper(context);

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
                    startAutopilot();
                    break;
            }
        }

        @Override
        public void onPictureTaken(
                ARCOMMANDS_ARDRONE3_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR_ENUM error) { }

        @Override
        public void configureDecoder(ARControllerCodec codec) {
            notifyVideoReceived(codec);
        }

        @Override
        public void onFrameReceived(ARFrame frame) { }

        @Override
        public void onMatchingMediasFound(int nbMedias) { }

        @Override
        public void onDownloadProgressed(String mediaName, int progress) { }

        @Override
        public void onDownloadComplete(String mediaName) { }

        @Override
        public void onWifiScanListChanged(ArrayList<BaseStation> baseStations) {
            getPositionFromWifi(new ArrayList<BaseStation>(baseStations));
            //Log.d(TAG, String.valueOf(baseStations.size()));
        }

        @Override
        public void onAttitudeChanged(float roll, float pitch, float yaw) {
            currentAttitudeYaw = radToDeg(yaw);
            //Log.d(TAG, String.valueOf(currentAttitudeYaw));
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
     *
     * The drone is moved forward, until the coordinates of the drone matches the destination.
     *
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
     * Function to turn the drone around the z-Axis for a specific angle.
     *
     * lat/lng: (0.0,1.0) atan2:     90.0
     * lat/lng: (1.0,0.0) atan2:     0.0
     * lat/lng: (-1.0,0.0)atan2:    -180.0
     * lat/lng: (0.0,-1.0)atan2:    -90.0
     *
     *               90
     *               |
     *               |
     *  +-180 _______|_______ 0
     *               |
     *               |
     *               |
     *              -90
     *
     * Use modulo to get the correct degrees.
     *      degrees = degrees % 360.
     *          0 to 180    will return the same numbers you put in.
     *          -180 to -1  return values between 180 to 359 degrees.
     *
     * The function will find out the shortest way to turn the drone (left or right) to reach
     * the wanted bearing.
     *
     * This function should be started async.
     *
     * @param bearing The destination angle you want to turn the drone to.
     *                Value between 180 <= x <= -180.
     */
    private void droneYawTo(final double bearing) {
        if (mBebopDrone != null && droneIsFlying()) {
            double startAngle = this.currentAttitudeYaw;
            final double tolerance = 3.0;
            final double result = (bearing % 360) - (startAngle % 360);

            Log.d(TAG, "droneYawTo: startAngle: " + currentAttitudeYaw + " destBearing: " + bearing
                    + "result = " + result);

            // start turning.
            mBebopDrone.setYaw((byte) (30 * (result < 0 ? -1 : 1)));

            while(!(currentAttitudeYaw >= bearing - tolerance
                    && currentAttitudeYaw <= bearing + tolerance)) { }

            Log.d(TAG, "Drone yawed for: " + result);

            // end turning.
            mBebopDrone.setYaw((byte) 0);
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
            mBebopDrone.calibrateMagnetometer((byte)1);
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
     * @param cursor from Database.
     * @return List with all BaseStations from Database.
     */
    public ArrayList<BaseStation> getCursorData(Cursor cursor) {
        ArrayList<BaseStation> ret = new ArrayList<>();

        if (cursor != null && cursor.isClosed()) {
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
                } while (cursor.moveToFirst());

                cursor.close();
            }
        }
        return ret;
    }

    /**
     * Function to estimate the drones position with scanned wifi points.
     * @param baseStations scanned from the drone.
     */
    private void getPositionFromWifi(ArrayList<BaseStation> baseStations) {
        ArrayList<BaseStation> foundBaseSations = new ArrayList<>();

        // find base stations in database.
        for (BaseStation bs : baseStations) {
            Cursor c = db.sqlSelect(
                    DbTables.RadioMap.TABLE_NAME,
                    null,
                    DbTables.RadioMap.COL_SSID + " = ?",
                    new String[] { bs.getSsid() },
                    null, null, null
            );
            foundBaseSations.addAll(getCursorData(c));
        }

        // laterate Position.
        try {
            currentPosition = Lateration.calculatePosition(foundBaseSations);
            notifyPositionChanged(currentPosition, currentAttitudeYaw);
            Log.d("drone",
                    "\nwifi found: " + baseStations.size()
                            + "\nwifi in db found: " + foundBaseSations.size()
                            + "\ncalculated Position: " + currentPosition.toString());
        } catch (Exception e) {
            //Log.e(TAG, e.getMessage());
        }
    }

    /**
     * Function converts radians to degrees.
     * @param value in radiant.
     * @return value in degrees.
     */
    private float radToDeg(float value) {
        return (float) (value * (180 / Math.PI));
    }

    /**
     * Function to start an autonomous flight with the drone.
     * @param route the drone has to fly.
     */
    public void startAutopilot(ArrayList<Marker> route) {
        this.route = route;

        if (mBebopDrone != null && route.size() > 0) {
            if (!droneIsFlying()) {
                droneTakeOff();
            }
            autonomousFlight = true;
        }
    }

    private void startAutopilot() {
        if (autonomousFlight) {
            if (route.size() == 0) {
                autonomousFlight = false;
                Log.d(TAG, "moveDrone(): no more points left on route. Landing drone...");
                this.droneLand();
            } else {
                // get first marker
                Marker marker = route.get(0);
                Log.d(TAG, "moveDrone(): current Destination: " + marker.getPosition().toString());

                try {
                    autopilotTask.execute(marker);
                } catch (IllegalStateException e) {
                    Log.e(TAG, "moveDrone(): " + e.getMessage());
                }
            }
            Log.d(TAG, "start yaw");
            droneYawTo(0);
            mBebopDrone.calibrateMagnetometer((byte) 1);

        }
        // SphericalUtil.computeHeading
    }

    private AsyncTask autopilotTask = new AsyncTask() {
        @Override
        protected Object doInBackground(Object[] params) {
            try {
                Marker destMarker = (Marker) params[0];
                if (destMarker != null) {
                    droneYawTo(SphericalUtil.computeHeading(currentPosition, destMarker.getPosition()));
                }
                return destMarker;

            } catch (Exception e) {
                Log.e(TAG, "autopilotTask: " + e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            try {
                Marker destMarker = (Marker) o;

                // TODO start yaw observer task
                yawObserverTask.execute(destMarker);

                // TODO start move task
                moveDroneTask.execute(destMarker);
            } catch (Exception e) {
                Log.e(TAG, "autopilotTask post exec:\n" + e.getMessage());
            }
        }
    };

    /**
     * Task to prevent the drone from drifting away, while moving towards a destination.
     */
    private AsyncTask yawObserverTask = new AsyncTask() {
        @Override
        protected Object doInBackground(Object[] params) {
            try {
                Marker destMarker = (Marker) params[0];
                if (destMarker != null) {
                    while (true) {
                        droneYawTo(SphericalUtil.computeHeading(currentPosition, destMarker.getPosition()));
                        this.wait(100);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "yawObserverTask: " + e.getMessage());
            }
            return null;
        }
    };

    /**
     * Task to move the drone.
     * When finished the task will end the yawObserverTask.
     */
    private AsyncTask moveDroneTask = new AsyncTask() {
        @Override
        protected Object doInBackground(Object[] params) {
            try {
                Marker destMarker = (Marker) params[0];
                if (destMarker != null) {

                    droneMove(destMarker.getPosition());

                    return destMarker;
                }
            } catch (Exception e) {
                Log.e(TAG, "yawObserverTask: " + e.getMessage());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            try {
                Marker destMarker = (Marker) o;

                // stop observer.
                yawObserverTask.cancel(true);

                // move marker from route to routeDone
                route.remove(destMarker);
                routeDone.add(destMarker);
                notifyCheckPointReached(destMarker);
            } catch (Exception e) {
                Log.e(TAG, "autopilotTask post exec:\n" + e.getMessage());
            }
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

        droneDiscoverer.stopDiscovering();
        droneDiscoverer.cleanup();
        droneDiscoverer.removeListener(mDiscovererListener);

        db.close();
    }

    public void destroy() {
        try {

            if (mBebopDrone != null) {
                this.droneLand();
            }

            droneDiscoverer.stopDiscovering();
            droneDiscoverer.cleanup();
            droneDiscoverer.removeListener(mDiscovererListener);

            db.close();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }
}
