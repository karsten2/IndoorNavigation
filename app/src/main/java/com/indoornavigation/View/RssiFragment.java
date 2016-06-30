package com.indoornavigation.View;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.indoornavigation.Adapter.WifiAdapter;
import com.indoornavigation.Database.DbTables;
import com.indoornavigation.Database.SQLiteDBHelper;
import com.indoornavigation.Helper.Utils;
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
    private int window1 = 65;
    private int window2 = 80;
    private int window3 = 100;
    private final String fileHeader = String.format(
            "DATE;SSID;DISTANCE;RAW;MEAN_%1$s;MEAN_%2$s;MEAN_%3$s;MEDIAN_%1$s;MEDIAN_%2$s;MEDIAN_%3$s"
                    + ";PREDICTION_RAW;PREDICTION_MEAN_%1$s;PREDICTION_MEAN_%2$s;PREDICTION_MEAN_%3$s"
                    + ";PREDICTION_MEDIAN_%1$s;PREDICTION_MEDIAN_%2$s;PREDICTION_MEDIAN_%3$s"
                    + ";PROX_CALC_RAW;PROX_CALC_MEAN_%1$s;PROX_CALC_MEAN_%2$s;PROX_CALC_MEAN_%3$s"
                    + ";PROX_CALC_MEDIAN_%1$s;PROX_CALC_MEDIAN_%2$s;PROX_CALC_MEDIAN_%3$s\n", window1, window2, window3);
    private BufferedWriter bw;
    private SQLiteDBHelper dbHelper;

    Statistics statistics_1, statistics_2, statistics_3;

    private boolean creatingStatistics = false;

    private Button btnAddFilter;
    private Button btnStart;
    private ProgressBar pbar;

    public RssiFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getContext().registerReceiver(
                broadcastReceiver, new IntentFilter(WifiScanner.TAG));

        dbHelper = new SQLiteDBHelper(getContext());
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
        public void onBearingChangedListener(float bearing) {

        }

        @Override
        public void positionChangedListener(LatLng latLng) {
        }

        @Override
        public void onWifiScanlistChanged(ArrayList<BaseStation> baseStations) {
            Log.d("Drone rssi", "wifi detected: " + baseStations.size());

            if (write && !creatingStatistics) {
                createStatistics(new ArrayList<BaseStation>(baseStations));
            }
        }
    };

    /**
     * Function to calculate the distance in meters from dbm rssi values.
     * http://rvmiller.com/2013/05/part-1-wifi-based-trilateration-on-android/
     * https://en.wikipedia.org/wiki/Free-space_path_loss
     * @param levelInDb RSSI value.
     * @param freqInMHz Frequency of the sending device.
     * @return Distance in meters.
     */
    private double calculateDistance(double levelInDb, double freqInMHz) {
        if (levelInDb == 0)
            return -1;

        double exp = (27.55 - (20 * Math.log10(freqInMHz)) + Math.abs(levelInDb)) / 20.0;
        return Math.pow(10.0, exp);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rssi, container, false);

        DroneController droneController = new DroneController(getContext());
        droneController.setListener(mDroneControllerListener);

        pbar = (ProgressBar) view.findViewById(R.id.pbar);

        btnAddFilter = (Button) view.findViewById(R.id.btnAddFilter);
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

        btnStart = (Button) view.findViewById(R.id.btnStart);
        if (btnStart != null) {
            btnStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (txtNumber != null)
                        distance = Double.valueOf(txtNumber.getText().toString());

                    statistics_1 = new Statistics(window1);
                    statistics_2 = new Statistics(window2);
                    statistics_3 = new Statistics(window3);

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
                TimeUnit.SECONDS.sleep(300);
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
     *
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

    private void createStatistics(ArrayList<BaseStation> baseStations) {
        try {
            creatingStatistics = true;
            for (BaseStation bs : baseStations) {
                if (bsFilter != null && bsFilter.getSsid() != null && bsFilter.getSsid().equals(bs.getSsid())) {
                    double rssiRaw = bs.getRssi();
                    double freqMhz = 2412;

                    SRegression sRegression = new SRegression(true);

                    statistics_1.add(bs.getRssi());
                    statistics_2.add(bs.getRssi());
                    statistics_3.add(bs.getRssi());

                    double mean_1 = statistics_1.getMean();
                    double mean_2 = statistics_2.getMean();
                    double mean_3 = statistics_3.getMean();

                    double median_1 = statistics_1.getMedian();
                    double median_2 = statistics_2.getMedian();
                    double median_3 = statistics_3.getMedian();

                    double predictionRaw = sRegression.getPrediction(rssiRaw);
                    double predictionMean_1 = sRegression.getPrediction(mean_1);
                    double predictionMean_2 = sRegression.getPrediction(mean_2);
                    double predictionMean_3 = sRegression.getPrediction(mean_3);
                    double predictionMedian_1 = sRegression.getPrediction(median_1);
                    double predictionMedian_2 = sRegression.getPrediction(median_2);
                    double predictionMedian_3 = sRegression.getPrediction(median_3);

                    double proxCalcRaw = calculateDistance(rssiRaw, freqMhz);
                    double proxCalcMean_1 = calculateDistance(mean_1, freqMhz);
                    double proxCalcMean_2 = calculateDistance(mean_2, freqMhz);
                    double proxCalcMean_3 = calculateDistance(mean_3, freqMhz);
                    double proxCalcMedian_1 = calculateDistance(median_1, freqMhz);
                    double proxCalcMedian_2 = calculateDistance(median_2, freqMhz);
                    double proxCalcMedian_3 = calculateDistance(median_3, freqMhz);


                    String writer = distance + ";" + rssiRaw
                            + ";" + mean_1 + ";" + mean_2 + ";" + mean_3
                            + ";" + median_1 + ";" + median_2 + ";" + median_3
                            + ";" + predictionRaw
                            + ";" + predictionMean_1 + ";" + predictionMean_2 + ";" + predictionMean_3
                            + ";" + predictionMedian_1 + ";" + predictionMedian_2 + ";" + predictionMedian_3
                            + ";" + proxCalcRaw
                            + ";" + proxCalcMean_1 + ";" + proxCalcMean_2 + ";" + proxCalcMean_3
                            + ";" + proxCalcMedian_1 + ";" + proxCalcMedian_2 + ";" + proxCalcMedian_3;

                    writeData(";" + bs.toString() + writer + "\n");
                }
            }
            creatingStatistics = false;
        } catch (IOException e) {
            Log.e("csv writing", e.getMessage());
            creatingStatistics = false;
        }
    }

    private double[][] getRegressionValuesFromDb(BaseStation baseStation) {

        if (dbHelper != null) {
            Cursor cursor = dbHelper.rawQuery(
                    "SELECT x, y " +
                    "FROM " + DbTables.ApRegressionValues.TABLE_NAME + " regr, " +
                    DbTables.RadioMap.TABLE_NAME + " rmap " +
                    "WHERE regr.ap_id = rmap._id " +
                    "AND rmap._id in (" +
                            "SELECT _id " +
                            "FROM " + DbTables.RadioMap.TABLE_NAME + " " +
                            "WHERE ssid = '" + baseStation.getSsid() + "')");

            double[][] regrValues = new double[cursor.getCount()][cursor.getCount()];
            int index = 0;

            if (cursor.moveToFirst()) {
                do {
                    regrValues[index][0] = cursor.getDouble(cursor.getColumnIndex("x"));
                    regrValues[index][1] = cursor.getDouble(cursor.getColumnIndex("y"));

                    index++;
                } while (cursor.moveToNext());
            }
            return regrValues;
        }
        return new double[][]{};
    }

    /**
     * Deactivates buttons and shows a progress bar.
     * @param disable true to disable, false to enable.
     */
    private void guiDisable(boolean disable) {
        if (btnStart != null)
            btnStart.setEnabled(!disable);
        if (btnAddFilter != null)
            btnAddFilter.setEnabled(!disable);
        if (pbar != null)
            pbar.setVisibility((disable ? View.VISIBLE : View.INVISIBLE));
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
