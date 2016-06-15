package com.indoornavigation.Controller.Fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.parrot.arsdk.ARSDK;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_DEVICE_STATE_ENUM;
import com.parrot.arsdk.arcontroller.ARControllerCodec;
import com.parrot.arsdk.arcontroller.ARFrame;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.indoornavigation.Database.SQLiteDBHelper;
import com.indoornavigation.Drone.BebopDrone;
import com.indoornavigation.Drone.BebopDrone.Listener;
import com.indoornavigation.Drone.DroneDiscoverer;
import com.indoornavigation.Model.BaseStation;
import com.indoor.navigation.indoornavigation.R;

import java.util.ArrayList;
import java.util.List;

public class DroneFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private DroneDiscoverer droneDiscoverer;
    private BebopDrone mBebopDrone;

    private BebopVideoView mVideoView;

    private TextView mBatteryLabel;
    private Button mTakeOffLandBt;
    private Button mDownloadBt;

    private SQLiteDBHelper db;

    private Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            mBebopDrone.scanWifi();
            handler.postDelayed(runnable, 5000);
        }
    };

    public final String TAG = "DroneFragment";

    private final DroneDiscoverer.Listener mDiscovererListener = new DroneDiscoverer.Listener() {
        @Override
        public void onDronesListUpdated(List<ARDiscoveryDeviceService> dronesList) {
            if (dronesList.size() > 0) {
                ARDiscoveryDeviceService service = new ARDiscoveryDeviceService(
                        dronesList.get(0).getName(),
                        dronesList.get(0).getDevice(),
                        dronesList.get(0).getProductID()
                );

                mBebopDrone = new BebopDrone(getContext(), service);
                mBebopDrone.addListener(mBebopListener);
                if (mBebopDrone.connect()) {
                    runnable.run();
                }

                Log.d("drone", "drone found" + dronesList.get(0).getName());
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();

        db.close();
    }

    @Override
    public void onPause() {
        super.onPause();

        droneDiscoverer.stopDiscovering();
        droneDiscoverer.cleanup();
        droneDiscoverer.removeListener(mDiscovererListener);
    }

    // this block loads the native libraries
    // it is mandatory
    static {
        ARSDK.loadSDKLibs();
    }

    public DroneFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_drone, container, false);

        db = new SQLiteDBHelper(getContext());

        droneDiscoverer = new DroneDiscoverer(getContext());
        droneDiscoverer.setup();
        droneDiscoverer.addListener(mDiscovererListener);
        droneDiscoverer.startDiscovering();

        initIHM(view);

        return view;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    private void initIHM(View view) {
        mVideoView = (BebopVideoView) view.findViewById(R.id.videoView);

        view.findViewById(R.id.emergencyBt).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mBebopDrone.emergency();
            }
        });

        mTakeOffLandBt = (Button) view.findViewById(R.id.takeOffOrLandBt);
        mTakeOffLandBt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                switch (mBebopDrone.getFlyingState()) {
                    case ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_LANDED:
                        mBebopDrone.takeOff();
                        mBebopDrone.calibrateMagnetometer((byte)1);
                        break;
                    case ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_FLYING:
                    case ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_HOVERING:
                        mBebopDrone.land();
                        break;
                    default:
                }
            }
        });

        view.findViewById(R.id.takePictureBt).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mBebopDrone.takePicture();
            }
        });

        view.findViewById(R.id.yawLeftBt).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        mBebopDrone.setYaw((byte) -50);
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        mBebopDrone.setYaw((byte) 0);
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        view.findViewById(R.id.yawRightBt).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        mBebopDrone.setYaw((byte) 50);
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        mBebopDrone.setYaw((byte) 0);
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        view.findViewById(R.id.forwardBt).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        mBebopDrone.setPitch((byte) 50);
                        mBebopDrone.setFlag((byte) 1);
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        mBebopDrone.setPitch((byte) 0);
                        mBebopDrone.setFlag((byte) 0);
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        view.findViewById(R.id.backBt).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        mBebopDrone.setPitch((byte) -50);
                        mBebopDrone.setFlag((byte) 1);
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        mBebopDrone.setPitch((byte) 0);
                        mBebopDrone.setFlag((byte) 0);
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        view.findViewById(R.id.rollLeftBt).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        mBebopDrone.setRoll((byte) -50);
                        mBebopDrone.setFlag((byte) 1);
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        mBebopDrone.setRoll((byte) 0);
                        mBebopDrone.setFlag((byte) 0);
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        view.findViewById(R.id.rollRightBt).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        mBebopDrone.setRoll((byte) 50);
                        mBebopDrone.setFlag((byte) 1);
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        mBebopDrone.setRoll((byte) 0);
                        mBebopDrone.setFlag((byte) 0);
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        mBatteryLabel = (TextView) view.findViewById(R.id.batteryLabel);
    }

    private final Listener mBebopListener = new Listener() {
        @Override
        public void onAttitudeChanged(float roll, float pitch, float yaw) {

        }

        @Override
        public void onDroneConnectionChanged(ARCONTROLLER_DEVICE_STATE_ENUM state) {
            switch (state) {
                case ARCONTROLLER_DEVICE_STATE_RUNNING:
                    Log.d("drone", "connected");
                    break;

                case ARCONTROLLER_DEVICE_STATE_STOPPED:
                    // if the deviceController is stopped, go back to the previous activity
                    //mConnectionProgressDialog.dismiss();
                    Log.d("drone", "connected");
                    getActivity().finish();
                    break;

                default:
                    break;
            }
        }

        @Override
        public void onBatteryChargeChanged(int batteryPercentage) {
            Log.d("drone", "Battery: " + batteryPercentage);
            mBatteryLabel.setText(String.format("%d%%", batteryPercentage));
        }

        @Override
        public void onPilotingStateChanged(ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM state) {
            switch (state) {
                case ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_LANDED:
                    mTakeOffLandBt.setText("Take off");
                    mTakeOffLandBt.setEnabled(true);
                    break;
                case ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_FLYING:
                case ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_HOVERING:
                    mTakeOffLandBt.setText("Land");
                    mTakeOffLandBt.setEnabled(true);
                    break;
                default:
                    mTakeOffLandBt.setEnabled(false);
            }
        }

        @Override
        public void onPictureTaken(
                ARCOMMANDS_ARDRONE3_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR_ENUM error) {
            Log.i("Drone Fragment", "Picture has been taken");
        }

        @Override
        public void configureDecoder(ARControllerCodec codec) {
            mVideoView.configureDecoder(codec);
        }

        @Override
        public void onFrameReceived(ARFrame frame) {
            mVideoView.displayFrame(frame);
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

        }

        @Override
        public void onPositionChanged(double latitude, double longitude, double altitude) {

        }

    };
}
