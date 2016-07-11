package com.indoornavigation.View;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.SphericalUtil;
import com.indoornavigation.Adapter.BsAdapterCheckbox;
import com.indoornavigation.Adapter.WifiAdapter;
import com.indoornavigation.Controller.DroneController;
import com.indoornavigation.Controller.MainActivity;
import com.indoornavigation.Database.DbTables;
import com.indoornavigation.Database.SQLiteDBHelper;
import com.indoornavigation.Helper.MapUtils;
import com.indoornavigation.Helper.ScanResultComparator;
import com.indoor.navigation.indoornavigation.R;
import com.indoornavigation.Helper.Utils;
import com.indoornavigation.Math.Statistics;
import com.indoornavigation.Model.BaseStation;
import com.indoornavigation.Model.MeasuringPoint;
import com.parrot.arsdk.arcontroller.ARControllerCodec;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
    private ArrayList<Marker> bsMarker = new ArrayList<>();
    private ArrayList<Marker> mpMarkers = new ArrayList<>();

    private double _bearing = 0.0;

    private ArrayList<BaseStation> baseStations = new ArrayList<>();
    private ArrayList<com.indoornavigation.Model.MeasuringPoint> measuringPoints = new ArrayList<>();

    private HashMap<String, Statistics> selectedStatistics = new HashMap<>();
    private ArrayList<BaseStation> selectedBaseStations;
    private boolean write = false;
    private boolean write2 = false;

    private TextView tvBearing;
    private Spinner spinner;
    private BsAdapterCheckbox bsAdapterCheckbox;
    private SQLiteDBHelper db;

    private Marker mMarkerDronePosition;

    private String lastSelectedLength;
    private int lastSelectedItem = -1;

    private MenuItem menuDroneConnectionState;

    private static final String TAG = "RadiomapFragment";

    public RadiomapFragment() { }

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

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
        public void checkPointReachedListener(Marker marker) { }

        @Override
        public void videoReceivedListener(ARControllerCodec codec) { }

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
            _bearing = bearing;
            if (tvBearing != null) {
                tvBearing.setText(String.valueOf(bearing));
            }
        }

        @Override
        public void positionChangedListener(LatLng latLng) {
            BitmapDescriptor iconDrone = BitmapDescriptorFactory.fromResource(
                    R.drawable.ic_play_arrow_red_a700_24dp);
            if (mMarkerDronePosition != null)
                mMarkerDronePosition.remove();

            mMarkerDronePosition = mMap.addMarker(new MarkerOptions()
                    .icon(iconDrone)
                    .anchor(0.5f, 0.5f)
                    .position(latLng));


            if (write2) {
                String data = "";
                data += String.valueOf(Utils.getPrefKnn(getContext()));
                data += ";" + testPoints.get(temp2).getLatLng();
                data += ";" + latLng;
                data += ";" + SphericalUtil.computeDistanceBetween(
                        testPoints.get(temp2).getLatLng(), latLng);
                data += "\n";
                try {
                    Log.d(TAG, "Point: " + testPoints.get(temp2).getName());
                    writeData(data);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onWifiScanlistChanged(ArrayList<BaseStation> baseStations) {
            Log.d(TAG, "wifi found: " + baseStations.size());
            if (write && !busy) {
                //Log.d(TAG, "writing: " + baseStations.size());
                updateStatistics(new ArrayList<>(baseStations));
            }
        }
    };

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menuDroneConnectionState = menu.findItem(R.id.action_connectionState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        db = new SQLiteDBHelper(getContext());
        notifyMeasurementPointsChanged();
        prepareTest();

        Log.d(TAG, "create view radiomap");

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
        FloatingActionButton fabData = (FloatingActionButton) view.findViewById(R.id.fabData);

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

        if (fabData != null) {
            fabData.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (temp2 == -1) {
                        Toast.makeText(getContext(), "Starte test", Toast.LENGTH_SHORT).show();
                        try {
                            bw = getCsvWriter(String.valueOf(
                                    Utils.getPrefKnn(getContext())) + "_route_" + String.valueOf(_bearing).substring(0, 4));
                        } catch (IOException e) {
                            Log.e(TAG, e.getMessage());
                        }
                        temp2 ++;
                        write2 = true;
                    } else {
                        temp2 ++;
                        Toast.makeText(getContext(), "Point: " + temp2, Toast.LENGTH_SHORT).show();

                        if (temp2 == testPoints.size()) {
                            Toast.makeText(getContext(), "Test beendet", Toast.LENGTH_SHORT).show();
                            write2 = false;
                            temp2 = -1;
                            try {
                                bw.close();
                            } catch (IOException e) {
                                Log.e(TAG, e.getMessage());
                            }
                        }
                    }


                    //launchBarDialog2(150);
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

    @Override
    public void onResume() {
        super.onResume();

        DroneController droneController = ((MainActivity) getActivity()).mDroneController;
        droneController.setListener(droneListener);
        droneController.estimatePosition = true;
    }

    @Override
    public void onPause() {
        super.onPause();

        DroneController droneController = ((MainActivity) getActivity()).mDroneController;
        droneController.removeListener(droneListener);
    }

    private int temp2 = -1;

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
                        db.addMeasuringPoint(MapUtils.currentPosition, input.getText().toString());
                        drawMeasuringPoint(MapUtils.currentPosition, input.getText().toString());
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
                                wifiAdapter.getItem(which), MapUtils.currentPosition);
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
            Collections.sort(measuringPoints);
            ArrayList<String> adapterList = new ArrayList<>();
            for (com.indoornavigation.Model.MeasuringPoint mp : measuringPoints) {
                adapterList.add(mp.toString());
            }

            ArrayAdapter spinnerAdapter = new ArrayAdapter<>(
                    getContext(), android.R.layout.simple_spinner_item,
                    android.R.id.text1,
                    adapterList);
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(spinnerAdapter);
            if (lastSelectedItem != -1)
                spinner.setSelection(lastSelectedItem);
        }

        if (bsAdapterCheckbox == null)
            bsAdapterCheckbox = new BsAdapterCheckbox(getContext(), baseStations, baseStations);
        final ListView listView = (ListView) dialogView.findViewById(R.id.listView2);
        if (listView != null) {
            listView.setAdapter(bsAdapterCheckbox);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String bsSSId = ((TextView) view.findViewById(R.id.txtRadiomapDialog))
                            .getText().toString();
                    for (BaseStation bs : baseStations) {
                        if (bs.getSsid().equals(bsSSId)) {
                            bsAdapterCheckbox.toggleSelection(bs);
                            break;
                        }
                    }
                }
            });
        }

        final TextView tvDuration = (TextView) dialogView.findViewById(R.id.txtLength);
        if (tvDuration != null) tvDuration.setText(lastSelectedLength);

        builder.setPositiveButton("Start", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (tvDuration != null && spinner != null) {
                    selectedBaseStations = bsAdapterCheckbox.getCheckedItems();
                    for (com.indoornavigation.Model.MeasuringPoint mp : measuringPoints) {
                        if (mp.getName().equals(spinner.getSelectedItem().toString())) {
                            break;
                        }
                    }
                    lastSelectedLength = tvDuration.getText().toString();
                    lastSelectedItem = spinner.getSelectedItemPosition();

                    int duration = 60;

                    try {
                        duration = Integer.valueOf(tvDuration.getText().toString());
                    } catch (NumberFormatException e) {
                        Log.d(TAG, "No duration entered: " + e.getMessage());
                    }

                    launchBarDialog(duration);
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
                    setupStatistics(selectedBaseStations);
                    String mPoint = spinner.getSelectedItem().toString();
                    double bearing = Double.valueOf(tvBearing.getText().toString());

                    write = true;
                    Log.d(TAG, "Start Writing");
                    int counter = 1;
                    while (counter <= duration) {
                        Thread.sleep(1000);
                        progressDialog.incrementProgressBy(1);
                        counter ++;
                        if (progressDialog.getProgress() == progressDialog.getMax()) {
                            progressDialog.dismiss();
                        }
                    }
                    write = false;
                    Log.d(TAG, "stopped writing");
                    writeStatisticsToDb(mPoint, bearing);
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }).start();
    }

    /**
     * For every by the user selected basestation, a statistic object is created.
     * @param selectedBaseStations by the user selected.
     */
    private void setupStatistics(ArrayList<BaseStation> selectedBaseStations) {
        selectedStatistics.clear();
        for (BaseStation bs : selectedBaseStations) {
            selectedStatistics.put(bs.getSsid(), new Statistics());
        }
    }

    private boolean busy = false;
    /**
     * Updating the statistics for every base station.
     *
     * @param scanResults from the drone wifi scan list.
     */
    private void updateStatistics(ArrayList<BaseStation> scanResults) {
        if (scanResults.containsAll(selectedBaseStations)) {
            busy = true;
            for (BaseStation bs : selectedBaseStations) {
                // get the statistics from the hashmap and add the rssi value from the wifi scan.
                selectedStatistics.get(bs.getSsid()).add(
                        (scanResults.get(scanResults.indexOf(bs))).getRssi());
            }
            busy = false;
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

        for (BaseStation bs : results) {
            if (baseStations.contains(bs)) {
                bs.setDbId(baseStations.get(baseStations.indexOf(bs)).getDbId());
            }
        }

        // sort after db id.
        Collections.sort(results);

        ContentValues values = new ContentValues();

        Cursor c = db.rawQuery(String.format("SELECT _ID FROM %s WHERE NAME = '%s'",
                DbTables.MeasuringPoint.TABLE_NAME, measuringPoint));
        if (c != null && c.moveToFirst()) {
            values.put("id_measuring", c.getInt(c.getColumnIndexOrThrow("_id")));
        }
        values.put("bearing", bearing + 180);

        ArrayList<BaseStation> normalized = normalizeRSS(results);

        for (int i = 1; i <= results.size(); i++) {
            String currentAp = String.format("ap%s_id", i);
            String currentRss = String.format("ap%s_rssi", i);

            Cursor c2 = db.rawQuery(String.format("SELECT _ID FROM %s WHERE SSID = '%s'",
                    DbTables.BaseStation.TABLE_NAME,
                    results.get(i - 1).getSsid()));
            int currentId = -1;
            if (c2 != null && c2.moveToFirst())
                currentId = c2.getInt(c2.getColumnIndexOrThrow("_id"));

            values.put(currentAp, currentId);
            values.put(currentRss, normalized.get(i - 1).getRssi());

            switch (i) {
                case 3:
                case 4:
                case 5:
                case 6:
                    db.sqlInsert("radiomap_" + i, null, values);
                    Log.d(TAG, "Values: \n" + values.toString());
                    break;
            }
        }
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
            double first = baseStations.get(0).getRssi();

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
        bsMarker.add(mMap.addMarker(new MarkerOptions()
                .position(bs.getLatLng())
                .anchor(0.5f, 0.5f)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_router))
                .title(bs.getSsid())
                .draggable(true)));
    }

    /**
     * Draw all measuring points.
     */
    private void drawMeasuringPoint() {
        for (com.indoornavigation.Model.MeasuringPoint mp : measuringPoints) {
            drawMeasuringPoint(mp);
        }
    }

    /**
     * Draw a single measuring point.
     * @param mp The point to draw.
     */
    private void drawMeasuringPoint(com.indoornavigation.Model.MeasuringPoint mp) {
        drawMeasuringPoint(mp.getLatLng(), mp.getName());
    }

    /**
     * Draw a single measuring Point.
     * @param p Location as LatLng.
     * @param name Name of the point.
     */
    private void drawMeasuringPoint(LatLng p, String name) {
        mpMarkers.add(mMap.addMarker(new MarkerOptions()
                .position(p)
                .title(name)
                .anchor(0.5f, 0.5f)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_crop_square_black_24dp))
                .draggable(true)));
    }

    private void notifyMeasurementPointsChanged() {
        this.measuringPoints = db.getMeasuringPoints();
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
                MapUtils.currentPosition = cameraPosition.target;
                MapUtils.currentZoom = cameraPosition.zoom;
            }
        });

        // setting camera position and and zoom level.
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(MapUtils.getStartPosition())
                .zoom(MapUtils.currentZoom)
                .build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        mMap.moveCamera(cameraUpdate);

        if (ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mMap.setOnMarkerClickListener(markerClickListener);
        mMap.setOnMarkerDragListener(markerDragListener);

        mMap.setMyLocationEnabled(true);

        MapUtils.addGroundOverlay(mMap);

        // get basestations from database
        this.baseStations = db.getBaseStations();
        drawBaseStation();

        // get measuring points from database
        notifyMeasurementPointsChanged();
        drawMeasuringPoint();
    }

    private String selectedMarker = "";

    GoogleMap.OnMarkerClickListener markerClickListener = new GoogleMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(final Marker marker) {
            if (marker.getTitle().equals(selectedMarker)) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                dialog.setTitle("Marker Löschen");
                dialog.setMessage("Möchten Sie den ausgewählten Marker löschen?");
                dialog.setNegativeButton("Abbrechen", null);
                dialog.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            db.sqlDelete(DbTables.BaseStation.TABLE_NAME,
                                    DbTables.BaseStation.COL_SSID + " = ?",
                                    new String[]{marker.getTitle()});
                            db.sqlDelete(DbTables.MeasuringPoint.TABLE_NAME,
                                    DbTables.MeasuringPoint.COL_NAME + " = ?",
                                    new String[]{marker.getTitle()});

                            redrawMap();
                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                });

                dialog.show();
            }

            selectedMarker = marker.getTitle();

            return false;
        }
    };

    GoogleMap.OnMarkerDragListener markerDragListener = new GoogleMap.OnMarkerDragListener() {
        @Override
        public void onMarkerDragStart(Marker marker) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Marker löschen");
            builder.setMessage("Möchten Sie den Marker wirklich löschen?");
            builder.setPositiveButton("Löschen", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            builder.setNegativeButton("Abbrechen", null);
            builder.show();
        }

        @Override
        public void onMarkerDrag(Marker marker) {

        }

        @Override
        public void onMarkerDragEnd(Marker marker) {

        }
    };

    private void redrawMap() {
        mMap.clear();
        measuringPoints = db.getMeasuringPoints();
        drawMeasuringPoint();

        baseStations = db.getBaseStations();
        drawBaseStation();

        MapUtils.addGroundOverlay(mMap);
    }


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



    private BufferedWriter bw;

    /**
     * Function that creates a buffered writer to write to the smartphones download directory.
     *
     * @param fileTag Addition to the filename (Name is always: AnalysisData->fileTag<-.csv
     * @return buffered writer.
     * @throws IOException
     */
    private BufferedWriter getCsvWriter(String fileTag) throws IOException {
        String baseDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        String fileName = "AnalysisData_knn_" + fileTag + ".csv";
        String filePath = baseDir + "/" + File.separator + fileName;
        File f = new File(filePath);

        FileWriter fw = new FileWriter(f, true);
        bw = new BufferedWriter(fw);
        String fileHeader = "KNN;tatsächliche Position;berechnete Position;Distanz\n";
        bw.write(fileHeader);

        return bw;
    }

    /**
     * Dialog to show the progress of the recording.
     * @param duration of the recording.
     */
    private void launchBarDialog2(final int duration) {
        final ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Datenaufzeichnung");
        progressDialog.setMessage("Daten werden aufgezeichnet...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setProgress(0);
        progressDialog.setMax(duration);

        progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                write2 = false;
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
                Toast.makeText(getContext(), "Aufzeichnung abgebrochen", Toast.LENGTH_SHORT);
            }
        });

        progressDialog.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    write2 = true;
                    int counter = 1;
                    while (counter <= duration) {
                        Thread.sleep(1000);
                        progressDialog.incrementProgressBy(1);
                        counter ++;
                        if (progressDialog.getProgress() == progressDialog.getMax()) {
                            progressDialog.dismiss();
                        }
                    }
                    write2 = false;
                    bw.close();
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }).start();
    }

    private void writeData(String data) throws IOException {
        if (bw != null) {
            bw.write(data);
        }
    }

    private ArrayList<MeasuringPoint> testPoints = new ArrayList<>();
    private void prepareTest() {
        String[] points = new String[]{
                "p16", "p17", "p18", "p19", "p20",
                "p7", "p30", "p29", "p28", "p27", "p26"
        };

        for (String point : points) {
            for (MeasuringPoint mp : measuringPoints) {
                if (mp.getName().equals(point)) {
                    testPoints.add(mp);
                    break;
                }
            }
        }
    }
}
