package com.power.max.indoornavigation.Controller;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
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
    private float currentAttitude;

    // Load native libraries, mandatory!
    static {
        ARSDK.loadSDKLibs();
    }

    public DroneController(Context context) {
        this.context = context;

        droneDiscoverer = new DroneDiscoverer(context);
        droneDiscoverer.setup();
        droneDiscoverer.addListener(mDiscovererListener);
        droneDiscoverer.startDiscovering();
    }

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
                    //runnable.run();
                }

                Log.d(TAG, "drone found" + dronesList.get(0).getName());
            }
        }
    };

    private final BebopDrone.Listener mBebopListener = new BebopDrone.Listener() {
        @Override
        public void onDroneConnectionChanged(ARCONTROLLER_DEVICE_STATE_ENUM state) {

        }

        @Override
        public void onBatteryChargeChanged(int batteryPercentage) {
            switch (batteryPercentage) {
                case 75:
                case 50:
                    Toast.makeText(context,
                            "Battery has " + batteryPercentage + "% left.",
                            Toast.LENGTH_SHORT).show();
                    break;
                case 25:
                case 15:
                    Toast.makeText(context,
                            "Battery low! " + batteryPercentage + "% left",
                            Toast.LENGTH_SHORT).show();
                    break;
                case 5:
                    Toast.makeText(context,
                            "Battery low, landing now...",
                            Toast.LENGTH_LONG).show();
                    land();
            }
        }

        @Override
        public void onPilotingStateChanged(
                ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM state) {

        }

        @Override
        public void onPictureTaken(
                ARCOMMANDS_ARDRONE3_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR_ENUM error) {

        }

        @Override
        public void configureDecoder(ARControllerCodec codec) {

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
            getPositionFromWifi(baseStations);
        }

        @Override
        public void onAttitudeChanged(float roll, float pitch, float yaw) {
            currentAttitude = radToDeg(yaw);
        }

        @Override
        public void onPositionChanged(double latitude, double longitude, double altitude) {
            currentPositionGPS = new LatLng(latitude, longitude);
        }
    };

    public BebopDrone.Listener getBebopListener() {
        return this.mBebopListener;
    }

    /**
     * Function to move the connected drone.
     * @param pitch Forward/ backward angle of the drone. Value in percentage from -100 to 100.
     * @param flag 1 if the pitch and roll values should be used, 0 otherwise.
     */
    private void move(byte pitch, byte flag) {
        if (mBebopDrone != null) {
            mBebopDrone.setPitch(pitch);
            mBebopDrone.setFlag(flag);
        }
    }

    /**
     * Function to turn the drown around the y-Axis.
     * @param yaw value in percentage -100 to 100.
     */
    private void yaw(byte yaw) {
        if (mBebopDrone != null) {
            mBebopDrone.setYaw(yaw);
        }
    }

    /**
     * Function to start the drone.
     */
    private void takeOff() {
        if (mBebopDrone != null
                && mBebopDrone.getFlyingState() ==
                ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM
                        .ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_LANDED) {
            mBebopDrone.takeOff();
        }
    }

    /**
     * Function to land the drone.
     */
    private void land() {
        if (mBebopDrone != null
                && (mBebopDrone.getFlyingState() ==
                        ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM
                                .ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_FLYING
                || mBebopDrone.getFlyingState() ==
                ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM
                        .ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_HOVERING)) {
            mBebopDrone.land();
        }
    }

    /**
     * Function to get data from database cursor.
     * @param cursor from Database.
     * @return List with all BaseStations from Database.
     */
    public ArrayList<BaseStation> getCursorData(Cursor cursor) {
        ArrayList<BaseStation> ret = new ArrayList<>();

        if (cursor != null) {
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
            LatLng calcPosition = Lateration.calculatePosition(foundBaseSations);
            Log.d("drone",
                    "\nwifi found: " + baseStations.size()
                            + "\nwifi in db found: " + foundBaseSations.size()
                            + "\ncalculated Position: " + calcPosition.toString());
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
     * Function to get the angle between two given points.
     *      Usage:  Drone calculates own Position and knows the next point to fly to.
     *              The drone also knows the direction of north, east, etc. By calculating the angle
     *              between the two points, the drone can be adjusted, to point to the next
     *              destination, and than just have to be moved forward.
     * @param p1 Position of the drone.
     * @param p2 Next target.
     * @return Angle between p1 and p2.
     */
    private double pointsToAngle(LatLng p1, LatLng p2) {
        double deltaLat = p2.latitude - p1.latitude;
        double deltaLng = p2.longitude - p1.longitude;

        return Math.atan2(deltaLat, deltaLng) * 180 / Math.PI;
    }

    /**
     * Function to start an autonomous flight with the drone.
     * @param route the drone has to fly.
     */
    public void startAutonomousFlight(ArrayList<LatLng> route) {
        if (mBebopDrone != null && route != null && route.size() > 1) {
            switch (mBebopDrone.getFlyingState()) {
                case ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_HOVERING:
                case ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_FLYING:
                    break;
                case ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_LANDED:
                    break;
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();

        droneDiscoverer.stopDiscovering();
        droneDiscoverer.cleanup();
        droneDiscoverer.removeListener(mDiscovererListener);

        db.close();
    }
}
