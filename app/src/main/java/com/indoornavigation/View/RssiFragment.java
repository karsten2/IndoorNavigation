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
    private final String fileHeader =
            "DATE;SSID;DISTANCE;RAW;MEAN_5;MEAN_10;MEAN_20;MEDIAN_5;MEDIAN_10;MEDIAN_20"
                    + ";PREDICTION_RAW;PREDICTION_MEAN_5;PREDICTION_MEAN_10;PREDICTION_MEAN_20"
                    + ";PREDICTION_MEDIAN_5;PREDICTION_MEDIAN_10;PREDICTION_MEDIAN_20"
                    + ";PROX_CALC_RAW;PROX_CALC_MEAN_5;PROX_CALC_MEAN_10;PROX_CALC_MEAN_20"
                    + ";PROX_CALC_MEDIAN_5;PROX_CALC_MEDIAN_10;PROX_CALC_MEDIAN_20";
    private BufferedWriter bw;
    private SQLiteDBHelper dbHelper;

    Statistics statistics_5, statistics_10, statistics_20;

    private boolean creatingStatistics = false;

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
        public void positionChangedListener(LatLng latLng, float bearing) {
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
     *
     * @param levelInDb RSSI value.
     * @param freqInMHz Frequency of the sending device.
     * @return Distance in meters.
     */
    private double calculateDistance(double levelInDb, double freqInMHz) {
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

                    statistics_5 = new Statistics(5);
                    statistics_10 = new Statistics(10);
                    statistics_20 = new Statistics(20);

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

    /*
    "DATE;SSID;DISTANCE;RAW;MEAN_5;MEAN_10;MEAN_20;MEDIAN_5;MEDIAN_10;MEDIAN_20"
            + ";PREDICTION_RAW;PREDICTION_MEAN_5;PREDICTION_MEAN_10;PREDICTION_MEAN_20"
            + ";PREDICTION_MEDIAN_5;PREDICTION_MEDIAN_10;PREDICTION_MEDIAN_20"
            + ";PROX_CALC_RAW;PROX_CALC_MEAN_5;PROX_CALC_MEAN_10;PROX_CALC_MEAN_20"
            + ";PROX_CALC_MEDIAN_5;PROX_CALC_MEDIAN_10;PROX_CALC_MEDIAN_20";

    */
    private void createStatistics(ArrayList<BaseStation> baseStations) {
        try {
            creatingStatistics = true;
            for (BaseStation bs : baseStations) {
                if (bsFilter != null && bsFilter.getSsid().equals(bs.getSsid())) {
                    double rssiRaw = bs.getDistance();
                    double freqMhz = 2457;

                    SRegression sRegression = new SRegression(true);

                    statistics_5.add(bs.getDistance());
                    statistics_10.add(bs.getDistance());
                    statistics_20.add(bs.getDistance());

                    double mean_5 = statistics_5.getMean();
                    double mean_10 = statistics_10.getMean();
                    double mean_20 = statistics_20.getMean();

                    double median_5 = statistics_5.getMedian();
                    double median_10 = statistics_10.getMedian();
                    double median_20 = statistics_20.getMedian();

                    double predictionRaw = sRegression.getPrediction(rssiRaw);
                    double predictionMean_5 = sRegression.getPrediction(mean_5);
                    double predictionMean_10 = sRegression.getPrediction(mean_10);
                    double predictionMean_20 = sRegression.getPrediction(mean_20);
                    double predictionMedian_5 = sRegression.getPrediction(median_5);
                    double predictionMedian_10 = sRegression.getPrediction(median_10);
                    double predictionMedian_20 = sRegression.getPrediction(median_20);

                    double proxCalcRaw = calculateDistance(rssiRaw, freqMhz);
                    double proxCalcMean_5 = calculateDistance(mean_5, freqMhz);
                    double proxCalcMean_10 = calculateDistance(mean_10, freqMhz);
                    double proxCalcMean_20 = calculateDistance(mean_20, freqMhz);
                    double proxCalcMedian_5 = calculateDistance(median_5, freqMhz);
                    double proxCalcMedian_10 = calculateDistance(median_10, freqMhz);
                    double proxCalcMedian_20 = calculateDistance(median_20, freqMhz);


                    String writer = distance + ";" + rssiRaw
                            + ";" + mean_5 + ";" + mean_10 + ";" + mean_20
                            + ";" + median_5 + ";" + median_10 + ";" + median_20
                            + ";" + predictionRaw
                            + ";" + predictionMean_5 + ";" + predictionMean_10 + ";" + predictionMean_20
                            + ";" + predictionMedian_5 + ";" + predictionMedian_10 + ";" + predictionMedian_20
                            + ";" + proxCalcRaw
                            + ";" + proxCalcMean_5 + ";" + proxCalcMean_10 + ";" + proxCalcMean_20
                            + ";" + proxCalcMedian_5 + ";" + proxCalcMedian_10 + ";" + proxCalcMedian_20;

                    writeData(bs.toString() + writer);
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
