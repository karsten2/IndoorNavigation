package com.indoornavigation.View;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
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
import com.indoor.navigation.indoornavigation.R;
import com.indoornavigation.Controller.DroneController;
import com.indoornavigation.Controller.MainActivity;
import com.indoornavigation.Database.SQLiteDBHelper;
import com.indoornavigation.Helper.MapUtils;
import com.indoornavigation.Model.BaseStation;
import com.indoornavigation.Model.CustomPolyLine;
import com.parrot.arsdk.arcontroller.ARControllerCodec;

import java.util.ArrayList;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private final String TAG = "Map Fragment";

    private FloatingActionButton fab;
    private FloatingActionButton fabStart;

    private MenuItem menuCancel;
    private MenuItem menuAccept;
    private MenuItem menuDelete;
    private MenuItem menuDroneConnectionState;

    private GoogleMap mMap;
    private Marker mMarkerDronePosition;
    private CustomPolyLine route;
    private boolean addRoute = false;
    private BitmapDescriptor iconStart, iconRoute, iconDone;

    private SQLiteDBHelper db;

    private DroneController droneController;

    private OnFragmentInteractionListener mListener;

    private final DroneController.Listener mDroneControllerListener = new DroneController.Listener() {
        float mBearing = 0.0f;

        @Override
        public void checkPointReachedListener(Marker marker) {
            //route.get(route.indexOf(marker)).setIcon(iconDone);
            route.setMarkerIcon(marker, iconDone);
            Log.d("Listener", "marker added" + marker.getId());
        }

        @Override
        public void videoReceivedListener(ARControllerCodec codec) { }

        @Override
        public void onDroneConnectionChangedListener(boolean connected) {
            if (menuDroneConnectionState != null) {
                if (connected) {
                    menuDroneConnectionState.setIcon(R.drawable.ic_drone_connected);
                } else {
                    menuDroneConnectionState.setIcon(R.drawable.ic_drone_disconnected);
                    droneController.removeListener(mDroneControllerListener);
                }
            }
        }

        @Override
        public void positionChangedListener(LatLng latLng) {
            Log.d(TAG, "Position changed: " + latLng.toString());
            // Prepare marker for drone position.
            BitmapDescriptor iconDrone = BitmapDescriptorFactory.fromResource(
                    R.drawable.ic_play_arrow_red_a700_24dp);
            if (mMarkerDronePosition != null)
                mMarkerDronePosition.remove();

            mMarkerDronePosition = mMap.addMarker(new MarkerOptions()
                    .icon(iconDrone)
                    .anchor(0.5f, 0.5f)
                    .position(latLng)
                    .rotation(mBearing + 180));
        }

        @Override
        public void onWifiScanlistChanged(ArrayList<BaseStation> baseStations) {
            Log.d(TAG, "wifi detected: " + baseStations.size());
        }

        @Override
        public void onBearingChangedListener(float bearing) {
            mBearing = bearing;
        }
    };

    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = new SQLiteDBHelper(getContext());

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        droneController = ((MainActivity) getActivity()).mDroneController;
        droneController.setListener(mDroneControllerListener);
        droneController.estimatePosition = true;

        View view = inflater.inflate(R.layout.fragment_map, container, false);

        iconStart = BitmapDescriptorFactory.fromResource(R.drawable.ic_action_location_lgreen);
        iconRoute = BitmapDescriptorFactory.fromResource(R.drawable.ic_action_location_2_black);
        iconDone = BitmapDescriptorFactory.fromResource(R.drawable.ic_action_location_2_green);

        fab = (FloatingActionButton) view.findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    db.getBaseStations();
                    addRoute();
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
                MapUtils.currentPosition = cameraPosition.target;
                MapUtils.currentZoom = cameraPosition.zoom;
                setStatus(cameraPosition.target.toString());
            }
        });


        mMap.setOnMapClickListener(onMapClickListener);

        // drawing the access points stored in the database.
        drawAccessPoints();

        //

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(MapUtils.getStartPosition())
                .zoom(mMap.getMaxZoomLevel())
                .build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(
                cameraPosition);
        mMap.moveCamera(cameraUpdate);

        if (ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        MapUtils.addGroundOverlay(mMap);

        mMap.setMyLocationEnabled(true);
    }

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
                            .anchor(0.5f, 0.5f)
                            .draggable(true)));
                } else {
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
     * Function to get all Access points from the database and draw them on the map.
     */
    private void drawAccessPoints() {
        for (BaseStation baseStation : db.getBaseStations()) {
            mMap.addMarker(new MarkerOptions()
                    .position(baseStation.getLatLng())
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_router))
                    .title(baseStation.getSsid())
                    .anchor(0.5f, 0.5f)
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

    private void addRoute() {

        setActionBarText(getString(R.string.edit_route));

        addRoute = true;
        fab.setVisibility(View.INVISIBLE);

        if (menuAccept != null) menuAccept.setVisible(true);
        if (menuCancel != null) menuCancel.setVisible(true);

        if (getView() != null) {
            Snackbar.make(
                    getView(), "Karte berühren, um Punkte zur Route hinzuzufügen.", Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
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
        MapUtils.addGroundOverlay(this.mMap);
    }

    private void setActionBarText(String text) {
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null)
            actionBar.setTitle(text);
    }


}
