package com.indoornavigation.View;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
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
import com.google.android.gms.maps.model.PolylineOptions;
import com.indoornavigation.Math.Knn;
import com.parrot.arsdk.arcontroller.ARControllerCodec;
import com.indoornavigation.Adapter.WifiAdapter;
import com.indoornavigation.Controller.DroneController;
import com.indoornavigation.Database.DbTables;
import com.indoornavigation.Database.SQLiteDBHelper;
import com.indoornavigation.Helper.Utils;
import com.indoornavigation.Model.BaseStation;
import com.indoornavigation.Model.CustomPolyLine;
import com.indoor.navigation.indoornavigation.R;
import com.indoornavigation.Services.WifiScanner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private final String TAG = "Map Fragment";

    private FloatingActionButton fab;
    private FloatingActionButton fabStart;
    private BebopVideoView bebopVideoView;

    private MenuItem menuCancel;
    private MenuItem menuAccept;
    private MenuItem menuDelete;
    private MenuItem menuDroneConnectionState;

    private WifiAdapter wifiAdapter;
    private ArrayList<ScanResult> scanResults = new ArrayList<>();

    private GoogleMap mMap;
    private LatLng currentPosition;

    private MarkerOptions mMarkerOptDronePosition;
    private Marker mMarkerDronePosition;

    private CustomPolyLine route;
    private boolean addRoute = false;

    private BitmapDescriptor iconStart, iconRoute, iconDone;

    private SQLiteDBHelper dbHelper;

    private DroneController droneController;

    private OnFragmentInteractionListener mListener;

    private final DroneController.Listener mDroneControllerListener = new DroneController.Listener() {
        @Override
        public void checkPointReachedListener(Marker marker) {
            //route.get(route.indexOf(marker)).setIcon(iconDone);
            route.setMarkerIcon(marker, iconDone);
            Log.d("Listener", "marker added" + marker.getId());
        }

        @Override
        public void videoReceivedListener(ARControllerCodec codec) {
            Log.d("Listener", "video received");
            if (bebopVideoView != null) {
                bebopVideoView.configureDecoder(codec);
            }
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
        public void positionChangedListener(LatLng latLng, float bearing) {
            if (mMarkerDronePosition != null) {

                mMarkerDronePosition = mMap.addMarker(mMarkerOptDronePosition
                        .visible(true)
                        .position(latLng)
                        .rotation(bearing));
            }
        }

        @Override
        public void onWifiScanlistChanged(ArrayList<BaseStation> baseStations) {

        }
    };

    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().registerReceiver(
                broadcastReceiver, new IntentFilter(WifiScanner.TAG));

        dbHelper = new SQLiteDBHelper(getContext());

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        droneController = new DroneController(getContext());
        droneController.setListener(mDroneControllerListener);

        View view = inflater.inflate(R.layout.fragment_map, container, false);

        iconStart = BitmapDescriptorFactory.fromResource(R.drawable.ic_action_location_lgreen);
        iconRoute = BitmapDescriptorFactory.fromResource(R.drawable.ic_action_location_2_black);
        iconDone = BitmapDescriptorFactory.fromResource(R.drawable.ic_action_location_2_green);

        bebopVideoView = (BebopVideoView) view.findViewById(R.id.videoView);

        fab = (FloatingActionButton) view.findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getBaseStations();
                    dialog();
                }
            });
        }

        final FloatingActionButton fabEmergency = (FloatingActionButton) view.findViewById(R.id.fabEmergency);
        if (fabEmergency != null) {
            fabEmergency.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    droneController.EmergencyLand();
                    fabStart.setVisibility(View.VISIBLE);

                    Knn.main();
                }
            });
        }

        fabStart = (FloatingActionButton) view.findViewById(R.id.fabStart);
        if (fabStart != null) {
            fabStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //droneController.startAutonomousFlight(route);
                    fabStart.setVisibility(View.INVISIBLE);

                    if (fabEmergency != null) {
                        droneController.startAutopilot(route.getMarkers());
                    }
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menuAccept = menu.findItem(R.id.action_accept);
        menuCancel = menu.findItem(R.id.action_cancel);
        menuDelete = menu.findItem(R.id.action_delete);
        menuDroneConnectionState = menu.findItem(R.id.action_connectionState);
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

        // make progress bar invisible
        ProgressBar progressBar = (ProgressBar) getActivity().findViewById(R.id.progressBar);
        if (progressBar != null) {
            progressBar.setVisibility(View.INVISIBLE);
        }

        mMap = googleMap;

        // create object to mock the room

        // create object to be the route
        // create marker options
        // create polyline options
        MarkerOptions routeMarkerOptions = new MarkerOptions().draggable(true);
        PolylineOptions routePLineOptions = new PolylineOptions().width(4);
        route = new CustomPolyLine(mMap, iconRoute, iconStart,
                routeMarkerOptions, routePLineOptions);

        // Set GoogleMap listener.
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                currentPosition = cameraPosition.target;
                setStatus(cameraPosition.target.toString());
            }
        });

        mMap.setOnMarkerClickListener(onMarkerClickListener);
        mMap.setOnMapClickListener(onMapClickListener);
        mMap.setOnMarkerDragListener(onMarkerDragListener);

        // Prepare marker for drone position.
        BitmapDescriptor iconDrone = BitmapDescriptorFactory.fromResource(R.drawable.ic_play_arrow_red_a700_24dp);
        mMarkerOptDronePosition = new MarkerOptions().icon(iconDrone);

        // drawing the access points stored in the database.
        drawAccessPoints();

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

        mMap.setMyLocationEnabled(true);
    }

    GoogleMap.OnMarkerClickListener onMarkerClickListener = new GoogleMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(final Marker marker) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Delete marker");
            builder.setMessage("Are you sure you want to delete this marker?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    route.removeLineMarker(marker);
                }
            });
            builder.setNegativeButton("Cancel", null);
            builder.show();
            return false;
        }
    };

    /**
     * Event for adding a route when clicking on the map.
     */
    GoogleMap.OnMapClickListener onMapClickListener = new GoogleMap.OnMapClickListener() {
        @Override
        public void onMapClick(LatLng latLng) {
            if (addRoute) {
                // add route marker to array and draw marker on map.
                if (route.size() == 0) {
                    route.add(mMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .icon(iconStart)
                            .draggable(true)));
                } else {
                    LatLng old = route.get(route.size() - 1).getPosition();

                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(latLng)
                            .icon(iconRoute)
                            .anchor(0.5f, 0.5f)
                            .draggable(true);

                    route.add(mMap.addMarker(markerOptions));
                }
            }
        }
    };

    /**
     * Event for dragging a marker on the map, when the marker is long clicked.
     */
    GoogleMap.OnMarkerDragListener onMarkerDragListener = new GoogleMap.OnMarkerDragListener() {
        @Override
        public void onMarkerDragStart(Marker marker) { }

        @Override
        public void onMarkerDrag(Marker marker) {
            if (route.contains(marker)) {
                route.updateLineMarker(marker);
            } else {
                // update base station in database.
                // create hashmap with columns and new values to update.
                Map<String, String> update = new HashMap<>();
                update.put(DbTables.RadioMap.COL_LAT, String.valueOf(marker.getPosition().latitude));
                update.put(DbTables.RadioMap.COL_LNG, String.valueOf(marker.getPosition().longitude));

                dbHelper.sqlUpdate(
                        DbTables.RadioMap.TABLE_NAME,
                        update,
                        DbTables.RadioMap.COL_SSID + " = ?",
                        new String[] {marker.getTitle()}
                );
            }
        }

        @Override
        public void onMarkerDragEnd(Marker marker) { }
    };

    /**
     * Function to get all Access points from the database and draw them on the map.
     */
    private void drawAccessPoints() {
        for (BaseStation baseStation : getBaseStations()) {
            mMap.addMarker(new MarkerOptions()
                    .position(baseStation.getLatLng())
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_router))
                    .title(baseStation.getSsid())
                    .draggable(true));
        }
    }

    void setStatus(String text) {
        if (getView() != null) {
            TextView statusView = (TextView) getView().findViewById(R.id.statusText);
            statusView.setText(text);
        }
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

    private void dialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("Add access points or create route.");
        builder.setPositiveButton("Add Access Point", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialogRadiomap();
            }
        });
        builder.setNegativeButton("Add Route", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                addRoute();
            }
        });
        builder.show();
    }

    private void addRoute() {

        setActionBarText(getString(R.string.edit_route));

        addRoute = true;
        fab.setVisibility(View.INVISIBLE);

        if (menuAccept != null) menuAccept.setVisible(true);
        if (menuCancel != null) menuCancel.setVisible(true);

        if (getView() != null) {
            Snackbar.make(
                    getView(), "Tap on the map to create a route.", Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * Function to create a dialog box, that ist filled by all nearby wifi networks.
     * Selecting a wifi network will add the router to the radiomap database.
     */
    private void dialogRadiomap() {

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
                        addToDb(new BaseStation(wifiAdapter.getItem(which), currentPosition));
                        drawAccessPoints();
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

    /**
     * Function to get all base stations from the database.
     * @return List with all base stations.
     */
    private ArrayList<BaseStation> getBaseStations() {
        return getData(dbHelper.sqlSelect(
                DbTables.RadioMap.TABLE_NAME,
                null, null, null, null, null, null));
    }

    /**
     * Function to convert the data from the data base into {@see BaseStation}.
     * @param cursor with Data from the database.
     * @return List with all base stations.
     */
    private ArrayList<BaseStation> getData(Cursor cursor) {

        ArrayList<BaseStation> ret = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                BaseStation baseStation = new BaseStation();
                baseStation.setSsid(cursor.getString(cursor.getColumnIndexOrThrow("SSID")));
                baseStation.setBssid(cursor.getString(cursor.getColumnIndexOrThrow("BSSID")));

                double lat = cursor.getDouble(cursor.getColumnIndexOrThrow("LAT"));
                double lng = cursor.getDouble(cursor.getColumnIndexOrThrow("LNG"));

                baseStation.setLatLng(new LatLng(lat, lng));

                ret.add(baseStation);

            } while (cursor.moveToNext());
        }

        return ret;
    }

    /**
     * Function to add a scanresult to the database, to create the radiomap.
     * @param baseStation : Data to write to the database.
     */
    private void addToDb(BaseStation baseStation) {
        long result = dbHelper.sqlInsert(DbTables.RadioMap.TABLE_NAME,
                null, baseStation.toDbValues());

        Toast.makeText(getContext(), String.valueOf(result) + " rows added.",
                Toast.LENGTH_SHORT).show();
    }

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
    public void onPause() {
        super.onPause();
        try {
            getActivity().unregisterReceiver(broadcastReceiver);
        } catch (Exception e) {
            Log.e(TAG, "Try to unregister reciever" + e.getMessage());
        }

        Utils.stopService(WifiScanner.class, getActivity());
    }

    @Override
    public void onStop() {
        super.onStop();

        try {
            getActivity().unregisterReceiver(broadcastReceiver);
        } catch (Exception e) {
            Log.e(TAG, "Try to unregister reciever" + e.getMessage());
        }

        Utils.stopService(WifiScanner.class, getActivity());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        try {
            getActivity().unregisterReceiver(broadcastReceiver);
        } catch (Exception e) {
            Log.e(TAG, "Try to unregister reciever" + e.getMessage());
        }

        if (droneController!= null) droneController.destroy();
        Utils.stopService(WifiScanner.class, getActivity());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        Utils.stopService(WifiScanner.class, getActivity());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_cancel) {

            if (menuAccept != null) menuAccept.setVisible(false);
            if (menuCancel != null) menuCancel.setVisible(false);

            fabStart.setVisibility(View.INVISIBLE);
            fab.setVisibility(View.VISIBLE);

            addRoute = false;

            clearMap();

        } else if (item.getItemId() == R.id.action_accept) {

            if (menuAccept != null) menuAccept.setVisible(false);
            if (menuCancel != null) menuCancel.setVisible(false);
            if (menuDelete != null) menuDelete.setVisible(true);

            fabStart.setVisibility(View.VISIBLE);
            fab.setVisibility(View.INVISIBLE);

            addRoute = false;

        } else if (item.getItemId() == R.id.action_delete) {

            menuDelete.setVisible(false);

            fabStart.setVisibility(View.INVISIBLE);
            fab.setVisibility(View.VISIBLE);

            setActionBarText(getString(R.string.title_activity_main));

            addRoute = false;

            clearMap();
        }

        return super.onOptionsItemSelected(item);
    }

    private void clearMap() {
        route.clear();
        drawAccessPoints();
    }

    private void setActionBarText(String text) {
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null)
            actionBar.setTitle(text);
    }
}
