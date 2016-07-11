package com.indoornavigation.View;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;

import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.indoornavigation.Adapter.BsAdapter;
import com.indoornavigation.Controller.MainActivity;
import com.indoornavigation.Database.DbTables;
import com.indoornavigation.Database.SQLiteDBHelper;
import com.indoornavigation.Math.MathUtils;
import com.indoornavigation.Math.Statistics;
import com.parrot.arsdk.arcontroller.ARControllerCodec;
import com.indoornavigation.Controller.DroneController;
import com.indoor.navigation.indoornavigation.R;
import com.indoornavigation.Model.BaseStation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class RssiFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private boolean write = false;

    private BsAdapter bsAdapter;
    private BaseStation bsFilter;
    private TextView txtFilter;
    private ArrayList<BaseStation> baseStations;

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
    private SQLiteDBHelper db;
    private MenuItem menuDroneConnectionState;

    private double distance = -1.0;

    Statistics statistics_1, statistics_2, statistics_3;

    private boolean creatingStatistics = false;

    private Button btnAddFilter;
    private Button btnStart;
    private ProgressBar pbar;

    public static final String TAG = "RssiFragment";

    public RssiFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        db = new SQLiteDBHelper(getContext());
        baseStations = db.getBaseStations();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        menuDroneConnectionState = menu.findItem(R.id.action_connectionState);
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
            if (menuDroneConnectionState != null) {
                if (connected) {
                    menuDroneConnectionState.setIcon(R.drawable.ic_drone_connected);
                } else {
                    menuDroneConnectionState.setIcon(R.drawable.ic_drone_disconnected);
                }
            }
        }

        @Override
        public void onBearingChangedListener(float bearing) {

        }

        @Override
        public void positionChangedListener(LatLng latLng) {
        }

        @Override
        public void onWifiScanlistChanged(ArrayList<BaseStation> baseStations) {
            //Log.d(TAG, "wifi detected: " + baseStations.size());


            if (write && !creatingStatistics) {
                createStatistics(new ArrayList<>(baseStations));
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rssi, container, false);

        DroneController droneController = ((MainActivity) getActivity()).mDroneController;
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

        final EditText txtDistance = (EditText) view.findViewById(R.id.txtDistance);
        if (txtDistance != null) {

            txtDistance.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (!txtDistance.getText().toString().equals(""))
                        distance = Double.valueOf(txtDistance.getText().toString());

                    txtDistance.setGravity(Gravity.CENTER);
                }
            });
        }

        btnStart = (Button) view.findViewById(R.id.btnStart);
        if (btnStart != null) {
            btnStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (distance >= 0) {
                        statistics_1 = new Statistics(window1);
                        statistics_2 = new Statistics(window2);
                        statistics_3 = new Statistics(window3);

                        try {
                            bw = getCsvWriter(String.valueOf(distance));
                        } catch (IOException e) {
                            Log.e("CSV Error", e.getMessage());
                        }
                        launchBarDialog(30);
                    } else if (distance < 0) {
                        Toast.makeText(getContext(), "Bitte eine Distanz eingeben",
                                Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

        return view;
    }

    /**
     * Dialog to show the progress of the recording.
     *
     * @param duration of the recording.
     */
    private void launchBarDialog(final int duration) {
        final ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Datenaufzeichnung");
        progressDialog.setMessage("Daten werden aufgezeichnet...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setProgress(0);
        progressDialog.setMax(duration);
        progressDialog.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    write = true;
                    int counter = 1;
                    while (counter <= duration) {
                        Thread.sleep(1000);
                        progressDialog.incrementProgressBy(1);
                        counter++;
                        if (progressDialog.getProgress() == progressDialog.getMax()) {
                            progressDialog.dismiss();
                        }
                    }
                    write = false;
                    writeToDb(distance);
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }).start();
    }

    private void writeToDb(double mode) {
        HashMap<String, String> values = new HashMap<>();

        if (mode == 1) {
            Log.d(TAG, "write rssi_1m to db: " + statistics_3.getMean());
            values.put(DbTables.BaseStation.COL_RSSI1M,
                    String.valueOf(statistics_3.getMean()));
        } else {
            BaseStation bs = new BaseStation();
            for (BaseStation baseStation : baseStations) {
                if (baseStation.getSsid().equals(txtFilter.getText().toString())) {
                    bs = baseStation;
                    break;
                }
            }
            double rssi_1m = bs.getRss1_1m();
            values.put(DbTables.BaseStation.COL_CONST,
                    String.valueOf(MathUtils.distancePropConst(
                            distance, statistics_3.getMean(), rssi_1m)));
            Log.d(TAG, "write const to db: " + values.get(DbTables.BaseStation.COL_CONST));
        }

        db.sqlUpdate(
                DbTables.BaseStation.TABLE_NAME,
                values,
                DbTables.BaseStation.COL_SSID + " = ?",
                new String[]{txtFilter.getText().toString()}
        );

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

        bsAdapter = new BsAdapter(getActivity(), db.getBaseStations());

        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Select Access Point");

        builder.setNegativeButton("Abbrechen", null);

        builder.setAdapter(bsAdapter,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        bsFilter = bsAdapter.getItem(which);
                        if (txtFilter != null) {
                            txtFilter.setText(bsFilter.getSsid());
                            txtFilter.setGravity(Gravity.CENTER);
                        }
                    }
                });

        builder.create().show();
    }

    private void createStatistics(ArrayList<BaseStation> baseStations) {
        creatingStatistics = true;
        for (BaseStation bs : baseStations) {
            if (bsFilter != null && bsFilter.getSsid() != null && bsFilter.getSsid().equals(bs.getSsid())) {
                statistics_1.add(bs.getRssi());
                statistics_2.add(bs.getRssi());
                statistics_3.add(bs.getRssi());
            }
        }
        creatingStatistics = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();


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
