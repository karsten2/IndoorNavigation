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
import com.google.common.math.DoubleMath;
import com.indoornavigation.Adapter.WifiAdapter;
import com.indoornavigation.Helper.Utils;
import com.indoornavigation.Math.DataSmoothing;
import com.indoornavigation.Math.SRegression;
import com.indoornavigation.Math.Statistics;
import com.indoornavigation.Services.WifiScanner;
import com.parrot.arsdk.arcontroller.ARControllerCodec;
import com.indoornavigation.Controller.DroneController;
import com.indoornavigation.Model.BaseStation;
import com.indoor.navigation.indoornavigation.R;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RssiFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private boolean write = false;
    private double distance = -1;
    private ArrayList<ArrayList<BaseStation>> allResults = new ArrayList<>();
    private WifiAdapter wifiAdapter;
    private ArrayList<ScanResult> scanResults = new ArrayList<>();
    private BaseStation bsFilter;
    private TextView txtFilter;
    private final String fileHeader = "DATE;SSID;DISTANCE;RAW;MEAN;MEDIAN\n";
    private BufferedWriter bw;

    private boolean creatingStatistics = false;

    public RssiFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
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
        public void checkPointReachedListener(Marker marker) {
        }

        @Override
        public void videoReceivedListener(ARControllerCodec codec) {
        }

        @Override
        public void onDroneConnectionChangedListener(boolean connected) {
        }

        @Override
        public void positionChangedListener(LatLng latLng, float bearing) {
        }

        @Override
        public void onWifiScanlistChanged(ArrayList<BaseStation> baseStations) {
            Log.d("Drone rssi", "wifi detected: " + baseStations.size()
                    + "\n" + write + " : " + creatingStatistics);
            if (!test && bsFilter != null && bsFilter.getSsid() != null)
                test(new ArrayList<BaseStation>(baseStations));
            if (write && !creatingStatistics) {
                createStatistics(new ArrayList<BaseStation>(baseStations));
            }
        }
    };

    boolean test = false;
    private void test(ArrayList<BaseStation> baseStations) {
        test = true;
        for (BaseStation bs : baseStations) {
            if (bs.getSsid() != null && bs.getSsid().equals(bsFilter.getSsid())) {
                SRegression sRegression = new SRegression();
                double prediction = sRegression.getPrediction(bs.getDistance());
                double calcPrecision = calculateDistance(bs.getDistance(), 2457);
                Log.d("SRegression", "RSSI: " + bs.getDistance()
                        + "\nPrediction: " + prediction
                        + "\nCalculated Distance: " + calcPrecision);
            }
        }
        test = false;
    }

    /**
     * http://rvmiller.com/2013/05/part-1-wifi-based-trilateration-on-android/
     * @param levelInDb
     * @param freqInMHz
     * @return
     */
    private double calculateDistance(double levelInDb, double freqInMHz)    {
        double exp = (27.55 - (20 * Math.log10(freqInMHz)) + Math.abs(levelInDb)) / 20.0;
        return Math.pow(10.0, exp);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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

        final EditText txtNumber = (EditText) view.findViewById(R.id.txtNumber);

        Button btnStart = (Button) view.findViewById(R.id.btnStart);
        if (btnStart != null) {
            btnStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (txtNumber != null)
                        distance = Double.valueOf(txtNumber.getText().toString());

                    Statistics.clear();
                    Statistics.setWindowSize(20);
                    MyTask myTask = new MyTask();

                    try {
                        bw = getCsvWriter(String.valueOf(distance));
                    } catch (IOException e) {
                        Log.e("CSV Error", e.getMessage());
                    }
                    myTask.execute();

                }
            });
        }

        return view;
    }

    private class MyTask extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] params) {
            Log.d("CSV", "Starte Aufzeichnung");

            write = true;
            try {
                TimeUnit.SECONDS.sleep(60);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            write = false;

            try {
                if (bw != null) bw.close();
            } catch (IOException e) {
                Log.e("CSV closing", e.getMessage());
            }

            Log.d("CSV", "Aufzeichnung beendet");
            return null;
        }
    }

    private void writeData(String data) throws IOException {
        if (bw != null) {
            bw.write(data);
        }
    }

    /**
     * Function that creates a buffered writer to write to the smartphones download directory.
     * @param fileTag Addition to the filename (Name is always: AnalysisData->fileTag<-.csv
     * @return buffered writer.
     * @throws IOException
     */
    private BufferedWriter getCsvWriter(String fileTag) throws IOException {
        String baseDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        String fileName = "AnalysisData_" + fileTag + ".csv";
        String filePath = baseDir + "/" + File.separator + fileName;
        File f = new File(filePath);

        FileWriter fw = new FileWriter(f, true);
        bw = new BufferedWriter(fw);
        bw.write(fileHeader);

        return bw;
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

        builder.create().show();
    }

    private final static String TAG = "RssiFragment";
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                wifiAdapter.clear();
                wifiAdapter.addAll((ArrayList<ScanResult>) intent.getExtras().get("scanResults"));
                wifiAdapter.notifyDataSetChanged();
            } catch (ClassCastException e) {
                Log.e(TAG, "wifi receiver onReceive" + e.getMessage());
            }
        }
    };

    /*private void testCreateStatistics(ArrayList<ScanResult> scanResults) {
        if (write) {
            ArrayList<BaseStation> baseStations = new ArrayList<>();

            for (ScanResult sr : scanResults) {
                BaseStation bs = new BaseStation();
                bs.setSsid(sr.SSID);
                bs.setDistance(sr.level);
                baseStations.add(bs);
            }
            createStatistics(baseStations);
        }
    }*/

    private void createStatistics(ArrayList<BaseStation> baseStations) {
        try {
            creatingStatistics = true;
            for (BaseStation bs : baseStations) {
                if (bsFilter != null && bsFilter.getSsid().equals(bs.getSsid())) {
                    double bsDistance = bs.getDistance();
                    Statistics.add(bs.getDistance());

                    String writer = distance
                            + ";" + Statistics.getMean()
                            + ";" + Statistics.getMedian() + "\n";

                    writeData(bs.toString() + writer);
                    Log.d("CSV", bs.toString() + writer);
                }
            }
            creatingStatistics = false;
        } catch (IOException e) {
            Log.e("csv writing", e.getMessage());
            creatingStatistics = false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //Utils.stopService(WifiScanner.class, getActivity());

        try {
            getContext().unregisterReceiver(broadcastReceiver);
        } catch (Exception e) {
            Log.e(TAG, "Tried to unregister not registerd reciever");
        }

        try {
            if (bw != null) {
                bw.close();
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
