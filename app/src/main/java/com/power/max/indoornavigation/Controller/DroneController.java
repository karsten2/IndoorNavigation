package com.power.max.indoornavigation.Controller;

import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
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

    // Load native libraries, mandatory!
    static {
        ARSDK.loadSDKLibs();
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
                }

                Log.d(TAG, "drone found" + dronesList.get(0).getName());
            }
        }
    };

    private final BebopDrone.Listener mBebopListener = new BebopDrone.Listener() {
        @Override
        public void onDroneConnectionChanged(ARCONTROLLER_DEVICE_STATE_ENUM state) {
            Log.d(TAG, state.toString());
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
                    droneLand();
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
            //getPositionFromWifi(baseStations);
            //Log.d(TAG, String.valueOf(baseStations.size()));
        }

        @Override
        public void onAttitudeChanged(float roll, float pitch, float yaw) {
            currentAttitudeYaw = radToDeg(yaw);
            Log.d(TAG, String.valueOf(currentAttitudeYaw));
        }

        @Override
        public void onPositionChanged(double latitude, double longitude, double altitude) {
            currentPositionGPS = new LatLng(latitude, longitude);
            Log.d(TAG, currentPositionGPS.toString());
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
    private void droneMove(byte pitch, byte flag) {
        if (mBebopDrone != null) {
            mBebopDrone.setPitch(pitch);
            mBebopDrone.setFlag(flag);
        }
    }

    /**
     * Function to turn the drown around the y-Axis for a specific angle.
     * Positive values = right, negative values = left.
     * -
     * yaw value in percentage -100 to 100.
     * @param angle the angle you want to turn the drone.
     *              positive = right turn, negative = left turn.
     */
    private void droneYaw(double angle) {
        if (mBebopDrone != null && droneIsFlying()) {
            double startAngle = this.currentAttitudeYaw;
            double tolerance = 5.0;

            // start turning.
            mBebopDrone.setYaw((byte) 25);

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

    public void EmergencyLand() {
        if (mBebopDrone != null) {
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
            currentPosition = Lateration.calculatePosition(foundBaseSations);
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
     * Function to get the angle between two given points.
     *      Usage:  Drone calculates own Position and knows the next point to fly to.
     *              The drone also knows the direction of north, east, etc. By calculating the angle
     *              between the two points, the drone can be adjusted, to point to the next
     *              destination, and than just have to be moved forward.
     * http://www.igismap.com/formula-to-find-bearing-or-heading-angle-between-two-points-latitude-longitude/
     * @param p1 Position of the drone.
     * @param p2 Next target.
     * @return Angle between p1 and p2.
     */
    private double pointsToAngle(LatLng p1, LatLng p2) {
        double deltaLng = p2.longitude - p1.longitude;
        double x = Math.cos(p1.latitude) * Math.sin(deltaLng);
        double y = Math.cos(p2.latitude) * Math.sin(p1.latitude) - Math.sin(p2.latitude) * Math.cos(p1.latitude) * Math.cos(deltaLng);

        return Math.atan2(x, y);
    }

    /**
     * Function to start an autonomous flight with the drone.
     * @param route the drone has to fly.
     */
    public void startAutonomousFlight(ArrayList<Marker> route) {
        ArrayList<LatLng> test = new ArrayList<>();
        /*test.add(new LatLng(54.338524, 13.074356));
        test.add(new LatLng(54.338538, 13.074436));*/

        test.add(new LatLng(0, 0));
        test.add(new LatLng(0, 1));
        test.add(new LatLng(0, 0));
        test.add(new LatLng(1, 0));
        test.add(new LatLng(0, 0));
        test.add(new LatLng(0, -1));
        test.add(new LatLng(0, 0));
        test.add(new LatLng(-1, 0));
        test.add(new LatLng(54.33848680468227, 13.074342012405396));
        test.add(new LatLng(54.33850244183943, 13.07449221611023));

        if (mBebopDrone != null /*&& route != null && route.size() > 1*/) {
            switch (mBebopDrone.getFlyingState()) {
                case ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_HOVERING:
                case ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_FLYING:
                    break;
                case ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_LANDED:
                    // start drone.
                    //droneTakeOff();

                    for (int i = 0; i < test.size(); i ++) {
                        Log.d("atan", "punkte: " + test.get(i) + "; "
                                + test.get(i + 1) + "atan2: "
                                + pointsToAngle(test.get(i), test.get(i + 1)));
                        i ++;
                    }
                    Log.d("atan", "current bearing: " + this.currentAttitudeYaw);


                    /*double turnAngle = pointsToAngle(test.get(0), test.get(1));
                    turnAngle = radToDeg((float) turnAngle);
                    Log.d(TAG, " TURN ANGLE #############: " + turnAngle);*/
                    break;
            }
        }
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

        droneDiscoverer.stopDiscovering();
        droneDiscoverer.cleanup();
        droneDiscoverer.removeListener(mDiscovererListener);

        db.close();
    }
}
