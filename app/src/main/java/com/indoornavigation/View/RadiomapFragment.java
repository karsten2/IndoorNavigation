package com.indoornavigation.View;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.indoornavigation.Adapter.BsAdapter;
import com.indoornavigation.Adapter.WifiAdapter;
import com.indoornavigation.Controller.DroneController;
import com.indoornavigation.Database.DbTables;
import com.indoornavigation.Database.SQLiteDBHelper;
import com.indoornavigation.Helper.ScanResultComparator;
import com.indoor.navigation.indoornavigation.R;
import com.indoornavigation.Helper.Utils;
import com.indoornavigation.Math.Statistics;
import com.indoornavigation.Model.BaseStation;
import com.parrot.arsdk.arcontroller.ARControllerCodec;

import org.apache.commons.math3.geometry.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RadiomapFragment extends Fragment implements OnMapReadyCallback {

    private OnFragmentInteractionListener mListener;

    private WifiManager wifi;
    private WifiAdapter wifiAdapter;
    private ArrayList<ScanResult> scanResults = new ArrayList<>();

    private GoogleMap mMap;
    private LatLng currentPosition;
    private ArrayList<BaseStation> baseStations = new ArrayList<>();
    private HashMap<String, LatLng> measuringPoints = new HashMap<>();

    private HashMap<String, Statistics> selectedStatistics = new HashMap<>();
    private LatLng selectedMeasuringPoint;
    private ArrayList<BaseStation> selectedBaseStations;
    private boolean write = false;

    private TextView tvBearing;
    private Spinner spinner;

    private SQLiteDBHelper db;

    private static final String TAG = "RadiomapFragment";

    public RadiomapFragment() {
    }

    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (wifi != null) {
                wifi.startScan();
                handler.postDelayed(runnable, 1000);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) { super.onCreate(savedInstanceState); }

    private BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {
            scanResults.clear();
            scanResults.addAll(wifi.getScanResults());
            Collections.sort(scanResults, new ScanResultComparator());
            if (wifiAdapter != null) wifiAdapter.notifyDataSetChanged();
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getActivity().finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private DroneController.Listener droneListener = new DroneController.Listener() {
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
            if (tvBearing != null) {
                tvBearing.setText(String.valueOf(bearing));
            }
        }

        @Override
        public void onWifiScanlistChanged(ArrayList<BaseStation> baseStations) {
            if (write) updateStatistics(baseStations);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        db = new SQLiteDBHelper(getContext());

        DroneController droneController = new DroneController(getContext());
        droneController.setListener(droneListener);

        View view = inflater.inflate(R.layout.fragment_radiomap, container, false);

        wifi = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
        getActivity().registerReceiver(wifiReceiver,
                new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        if (!wifi.isWifiEnabled()) {
            Toast.makeText(getActivity(), "Enabling wifi, please wait.", Toast.LENGTH_LONG).show();
            wifi.setWifiEnabled(true);
        }

        ListView listView = (ListView) view.findViewById(R.id.listView);
        if (listView != null) {
            wifiAdapter = new WifiAdapter(getActivity(), scanResults);
            listView.setAdapter(wifiAdapter);
        }

        FloatingActionButton fabAdd = (FloatingActionButton) view.findViewById(R.id.fabAdd);
        FloatingActionButton fabStart = (FloatingActionButton) view.findViewById(R.id.fabStart);

        if (fabAdd != null) {
            fabAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Show dialog to choose an action.
                    actionDialog();
                }
            });
        }

        if (fabStart != null) {
            fabStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Show dialog to start the measuring.
                    startMeasureDialog();
                }
            });
        }

        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }


        return view;
    }

    /**
     * Dialog to chose the users action.
     *  The user can add a base station to the map.
     *  The user can add a measuring point to the map.
     */
    private void actionDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Aktion auswählen");

        builder.setItems(R.array.spinnerRadiomap, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        // Add base station.
                        dialog.dismiss();
                        wifiDialog();
                        break;
                    case 1:
                        // Add measuring point.
                        measureDialog();
                        break;
                }

                String[] array = getResources().getStringArray(R.array.spinnerRadiomap);
                Log.d(TAG, "Item selected: " + array[which]);
            }
        });

        builder.setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    /**
     * Dialog to add a measurement point to the database.
     */
    private void measureDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Messpunkt hinzufügen");
        final EditText input = new EditText(getContext());
        input.setHint("Name des Messpunktes");

        builder.setView(input);

        builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        db.addMeasuringPoint(currentPosition, input.getText().toString());
                        drawMeasuringPoint(currentPosition, input.getText().toString());
                        notifyMeasurementPointsChanged();
                    }
                }
        );

        builder.setNegativeButton(
                "Abbrechen",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });


        builder.show();
    }

    /**
     * Function to create a dialog box, that ist filled by all nearby wifi networks.
     * Selecting a wifi network will add the router to the radiomap database.
     */
    private void wifiDialog() {

        // start wifi service
        //Utils.startService(WifiScanner.class, getActivity());
        handler.post(runnable);
        wifiAdapter = new WifiAdapter(getActivity(), scanResults);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Select Access Point");

        builder.setNegativeButton("Abbrechen", null);

        builder.setAdapter(wifiAdapter,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        BaseStation bs = new BaseStation(
                                wifiAdapter.getItem(which), currentPosition);
                        db.addBaseStation(bs);
                        baseStations = db.getBaseStations();
                        drawBaseStation(bs);
                    }
                });

        builder.create().show();
    }

    /**
     * Dialog to set up the measuring process.
     * The user can set:
     *      The measuring point
     *      The base stations
     *      The duration of the measuring.
     *
     * When clicking on okay a progress dialog is popping up while the app measures the data in
     * the background. After the measuring the statistics for every base station gives the moving
     * average for the measured time. The data will be written into the database in the following
     * form:
     *      _id | id_measuring | bearing | ap1_id | ap1_rssi | apn_id | apn_rssi | ...
     */
    private void startMeasureDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getLayoutInflater(null);
        View dialogView = inflater.inflate(R.layout.dialog_radiomap, null);
        builder.setView(dialogView);

        builder.setTitle("Messung starten");

        spinner = (Spinner) dialogView.findViewById(R.id.spinner);
        if (spinner != null) {
            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                    getContext(), android.R.layout.simple_spinner_item,
                    android.R.id.text1,
                    new ArrayList<>(measuringPoints.keySet()));
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(spinnerAdapter);
        }

        final BsAdapter bsAdapter = new BsAdapter(getContext(), baseStations);
        final ListView listView = (ListView) dialogView.findViewById(R.id.listView2);
        if (listView != null) {
            listView.setAdapter(bsAdapter);
        }

        final TextView tvDuration = (TextView) dialogView.findViewById(R.id.txtLength);

        builder.setPositiveButton("Start", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d("listview", String.valueOf(bsAdapter.getCheckedItems().size()));
                if (tvDuration != null && spinner != null) {
                    selectedBaseStations = bsAdapter.getCheckedItems();
                    selectedMeasuringPoint = measuringPoints.get(
                            spinner.getSelectedItem().toString());

                    launchBarDialog(Integer.valueOf(tvDuration.getText().toString()));
                }
            }
        });

        tvBearing = (TextView) dialogView.findViewById(R.id.tvBearing);
        if (tvBearing != null) tvBearing.setText("0");

        builder.setNegativeButton("Abbrechen", null);

        builder.create().show();
    }

    /**
     * Dialog to show the progress of the recording.
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
                    setupStatistics(selectedBaseStations, 80);
                    String mPoint = spinner.getSelectedItem().toString();
                    double bearing = Double.valueOf(tvBearing.getText().toString());

                    write = true;
                    while (progressDialog.getProgress() <= progressDialog.getMax()) {
                        Thread.sleep(1000);

                        progressDialog.incrementProgressBy(1);

                        if (progressDialog.getProgress() == duration) {
                            progressDialog.dismiss();
                        }
                    }
                    write = false;
                    writeStatisticsToDb(mPoint, bearing);
                } catch (Exception e) {
                    Log.e(TAG, "");
                }
            }
        }).start();
    }

    /**
     * For every by the user selected basestation, a statistic object is created.
     * @param selectedBaseStations by the user selected.
     * @param windowSize moving window size. Uses the last <windowSize> data to
     *                   create the statistics.
     */
    private void setupStatistics(ArrayList<BaseStation> selectedBaseStations, int windowSize) {
        selectedStatistics.clear();
        for (BaseStation bs : selectedBaseStations) {
            selectedStatistics.put(bs.getSsid(), new Statistics(windowSize));
        }
    }

    /**
     * Updating the statistics for every base station.
     *
     * @param scanResults from the drone wifi scan list.
     */
    private void updateStatistics(ArrayList<BaseStation> scanResults) {
        if (scanResults.containsAll(selectedBaseStations)) {
            for (BaseStation bs : selectedBaseStations) {
                // get the statistics from the hashmap and add the rssi value from the wifi scan.
                selectedStatistics.get(bs.getSsid()).add(
                        (scanResults.get(scanResults.indexOf(bs))).getRssi());
            }
        }
    }

    /**
     * Writes the collected data from the measuring into the database.
     * Creates a string from the statistics.
     * Automatically creates the normalized database table.
     *
     * @param measuringPoint Name of the measuringPoint.
     * @param bearing of the drone.
     */
    private void writeStatisticsToDb(String measuringPoint, double bearing) {
        ArrayList<BaseStation> results = new ArrayList<>();
        for (Map.Entry<String, Statistics> entry : selectedStatistics.entrySet()) {
            results.add(new BaseStation(entry.getKey(), entry.getValue().getMean()));
        }

        Collections.sort(results);

        String query = String.format("INSERT INTO radiomap_%s values (\n", results.size());
        String queryNormalized = String.format("INSERT INTO radiomap_normalized_%s " +
                "values (\n", results.size());

        query += String.format(
                "(SELECT _ID FROM %s WHERE NAME = %s), \n" +
                "%s, \n",
                DbTables.MeasuringPoints.TABLE_NAME,
                measuringPoint,
                bearing);

        queryNormalized += query;

        ArrayList<BaseStation> normalized = normalizeRSS(results);

        for (BaseStation bs : normalized) {
            queryNormalized += String.format(
                    "(SELECT _ID FROM radiomap WHERE SSID = '%s'), \n" +
                            "%s",
                    bs.getSsid(),
                    bs.getRssi());
        }

        for (BaseStation bs : results) {
            query += String.format(
                    "(SELECT _ID FROM radiomap WHERE SSID = '%s'), \n" +
                    "%s",
                    bs.getSsid(),
                    bs.getRssi());
        }

        query += ")";
        queryNormalized += ")";

        db.rawQuery(query);
        db.rawQuery(queryNormalized);
    }

    /**
     * Normalizes the rss values before they are written into the database.
     * Db row: ap1_ssid: test | ap1_rss: -43 | ap2_ssid: test2 | ap1_rss: -37 | ap3_ssid: test3 | ap3_rss: -40 |
     *                          |-43|                                           |0|
     *      gives the vector:   |-37| to normalize subtract the first value:    |6|
     *                          |-40|                                           |3|
     *
     * @param baseStations to normalize.
     * @return normalized baseStations.
     */
    private ArrayList<BaseStation> normalizeRSS(ArrayList<BaseStation> baseStations) {
        ArrayList<BaseStation> returnValues = new ArrayList<>();

        if (baseStations.size() > 0) {
            double first = returnValues.get(0).getRssi();

            for (BaseStation bs : baseStations) {
                BaseStation newBs = new BaseStation(bs.getSsid(), bs.getRssi() - first);
                returnValues.add(newBs);
            }
        }

        return returnValues;
    }

    /**
     * Draw all base stations.
     */
    private void drawBaseStation() {
        for (BaseStation baseStation : baseStations) {
            drawBaseStation(baseStation);
        }
    }

    /**
     * Draw a single base stations.
     *
     * @param bs The base station to draw.
     */
    private void drawBaseStation(BaseStation bs) {
        mMap.addMarker(new MarkerOptions()
                .position(bs.getLatLng())
                .anchor(0.5f, 0.5f)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_router))
                .title(bs.getSsid())
                .draggable(true));
    }

    /**
     * Draw all measuring points.
     */
    private void drawMeasuringPoint() {
        for (Map.Entry<String, LatLng> entry : measuringPoints.entrySet()) {
            drawMeasuringPoint(entry);
        }
    }

    /**
     * Draw a single measuring point.
     * @param measuringPoint The point to draw.
     */
    private void drawMeasuringPoint(Map.Entry<String, LatLng> measuringPoint) {
        drawMeasuringPoint(measuringPoint.getValue(), measuringPoint.getKey());
    }

    /**
     * Draw a single measuring Point.
     * @param p Location as LatLng.
     * @param name Name of the point.
     */
    private void drawMeasuringPoint(LatLng p, String name) {
        mMap.addMarker(new MarkerOptions()
                .position(p)
                .title(name)
                .anchor(0.5f, 0.5f)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_crop_square_black_24dp))
                .draggable(true));
    }

    private void notifyMeasurementPointsChanged() {
        this.measuringPoints = db.getMeasurementPoints();
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "map ready");
        mMap = googleMap;

        // Set GoogleMap listener.
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                currentPosition = cameraPosition.target;
            }
        });

        // setting camera position and and zoom level.
        LatLng latLng = new LatLng(54.33901533, 13.07454586);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng).zoom(mMap.getMaxZoomLevel()).build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        mMap.moveCamera(cameraUpdate);

        if (ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mMap.setOnMarkerClickListener(markerClickListener);

        mMap.setMyLocationEnabled(true);

        Utils.addGroundOverlay(mMap);

        // get basestations from database
        this.baseStations = db.getBaseStations();
        drawBaseStation();

        // get measuring points from database
        notifyMeasurementPointsChanged();
        drawMeasuringPoint();
    }

    GoogleMap.OnMarkerClickListener markerClickListener = new GoogleMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker) {
            return false;
        }
    };

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        try {
            getActivity().unregisterReceiver(wifiReceiver);
        } catch (IllegalArgumentException e) {
            Log.e("wifi", "Trying to unregister not registered receiver!");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        try {
            getActivity().unregisterReceiver(wifiReceiver);
        } catch (IllegalArgumentException e) {
            Log.e("wifi", "Trying to unregister not registered receiver!");
        }
    }
}
