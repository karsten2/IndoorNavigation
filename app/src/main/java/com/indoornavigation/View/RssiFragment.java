package com.indoornavigation.View;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.BooleanResult;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.vision.text.Text;
import com.indoornavigation.Adapter.WifiAdapter;
import com.indoornavigation.Helper.Utils;
import com.indoornavigation.Math.DataSmoothing;
import com.indoornavigation.Services.WifiScanner;
import com.parrot.arsdk.arcontroller.ARControllerCodec;
import com.indoornavigation.Controller.DroneController;
import com.indoornavigation.Model.BaseStation;
import com.indoor.navigation.indoornavigation.R;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RssiFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private boolean write = false;
    private int distance = -1;
    private ArrayList<ArrayList<BaseStation>> allResults = new ArrayList<>();
    private Button btnStart;
    private WifiAdapter wifiAdapter;
    private ArrayList<ScanResult> scanResults = new ArrayList<>();
    private BaseStation bsFilter;
    private TextView txtFilter;
    private EditText txtNumber;

    public RssiFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment RssiFragment.
     */
    public static RssiFragment newInstance() {
        RssiFragment fragment = new RssiFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getContext().registerReceiver(
                broadcastReceiver, new IntentFilter(WifiScanner.TAG));
    }

    DroneController.Listener mDroneControllerListener = new DroneController.Listener() {
        @Override
        public void checkPointReachedListener(Marker marker) { }

        @Override
        public void videoReceivedListener(ARControllerCodec codec) { }

        @Override
        public void onDroneConnectionChangedListener(boolean connected) { }

        @Override
        public void positionChangedListener(LatLng latLng, float bearing) { }

        @Override
        public void onWifiScanlistChanged(ArrayList<BaseStation> baseStations) {
            try{
                if (write) {
                    DataSmoothing.SRegression sRegression = new DataSmoothing.SRegression();
                    DataSmoothing.MovingAverage movingAverage = new DataSmoothing.MovingAverage(3);
                    List<Double> dataRaw = new ArrayList<>();
                    List<Double> dataRegression = new ArrayList<>();
                    List<Double> dataMovingAverage = new ArrayList<>();

                    for (BaseStation bs : baseStations) {
                        if (bsFilter != null && bsFilter.getSsid().equals(bs.getSsid())) {
                            double bsDistance = bs.getDistance();
                            sRegression.addData(distance, bsDistance);
                            movingAverage.add(bsDistance);

                            dataRaw.add(bs.getDistance());
                            dataRegression.add(sRegression.getIntercept());
                            dataMovingAverage.add(movingAverage.getAverage());

                            String writer = ":" + sRegression.getIntercept() + ":"
                                    + movingAverage.getAverage();
                            writeData(bs.toString() + writer);
                        }
                    }

                    if (dataRaw.size() > 0) {
                        double rawMedian = DataSmoothing.getMedian(dataRaw.toArray());
                        dataRaw.
                    }

                }
            } catch (IOException e) {
                Log.e("csv writing", e.getMessage());
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        try{
            createCsvWriter();
        } catch (IOException e) {
            Log.e("csv writing", e.getMessage());
        }

        View view = inflater.inflate(R.layout.fragment_rssi, container, false);

        DroneController droneController = new DroneController(getContext());
        droneController.setListener(mDroneControllerListener);

        Button btnAddFilter = (Button) view.findViewById(R.id.btnAddFilter);
        if (btnAddFilter != null) {
            btnAddFilter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    wifiDialog();
                }
            });
        }

        txtFilter = (TextView) view.findViewById(R.id.txtFilter);

        txtNumber = (EditText) view.findViewById(R.id.txtNumber);

        btnStart = (Button) view.findViewById(R.id.btnStart);
        if (btnStart != null) {
            btnStart.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    if (txtNumber != null) {
                        distance = Integer.parseInt(txtNumber.getText().toString());
                    }

                    new AsyncTask() {
                        @Override
                        protected Object doInBackground(Object[] params) {
                            Toast.makeText(getContext(), "Starte Aufzeichnung für 10 Sekunden.",
                                    Toast.LENGTH_SHORT);

                            write = true;
                            try {
                                TimeUnit.SECONDS.sleep(10);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            write = false;

                            Toast.makeText(getContext(), "Aufzeichnung beendet.",
                                    Toast.LENGTH_SHORT);
                            return null;
                        }
                    };
                }
            });
        }

        return view;
    }

    FileWriter fileWriter;

    private void writeData(String data) throws IOException {
        if (fileWriter != null) {
            fileWriter.write(data);
        }
    }

    private void createCsvWriter() throws IOException {
        String baseDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        String fileName = "AnalysisData.csv";
        String filePath = baseDir + "/indoorNavigation" + File.separator + fileName;
        File f = new File(filePath );

        fileWriter = new FileWriter(f, true);
    }

    /**
     * Function to create a dialog box, that ist filled by all nearby wifi networks.
     * Selecting a wifi network will add the router to the radiomap database.
     */
    private void wifiDialog() {

        // start wifi service
        Utils.startService(WifiScanner.class, getActivity());
        wifiAdapter = new WifiAdapter(getActivity(), scanResults);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Select Access Point");

        builder.setNegativeButton("Abbrechen", null);

        builder.setAdapter(wifiAdapter,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        bsFilter = new BaseStation(wifiAdapter.getItem(which));
                        if (txtFilter != null)
                            txtFilter.setText(bsFilter.getSsid());
                    }
                });

        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                Utils.stopService(WifiScanner.class, getActivity());
            }
        });

        builder.create().show();
    }

    private String postCompute() {

        if (allResults != null) {


            for (ArrayList<BaseStation> arrBs : allResults) {
                for (BaseStation bs : arrBs) {

                }
            }
        }

        return "";
    }

    private final static String TAG = "RssiFragment";
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                wifiAdapter.clear();
                wifiAdapter.addAll((ArrayList<ScanResult>) intent.getExtras().get("scanResults"));
                wifiAdapter.notifyDataSetChanged();
                Log.d(TAG, "received data: " + scanResults.size());
            } catch (ClassCastException e) {
                Log.e(TAG, "wifi receiver onReceive" + e.getMessage());
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();

        try {
            getContext().unregisterReceiver(broadcastReceiver);
        } catch (Exception e) {
            Log.e(TAG, "Tried to unregister not registerd reciever");
        }

        try {
            if (fileWriter != null) {
                fileWriter.close();
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
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
}
