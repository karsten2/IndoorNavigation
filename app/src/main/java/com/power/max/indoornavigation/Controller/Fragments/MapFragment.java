package com.power.max.indoornavigation.Controller.Fragments;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
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
import android.widget.VideoView;

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
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.parrot.arsdk.arcontroller.ARControllerCodec;
import com.power.max.indoornavigation.Adapter.WifiAdapter;
import com.power.max.indoornavigation.Controller.DroneController;
import com.power.max.indoornavigation.Database.DbTables;
import com.power.max.indoornavigation.Database.SQLiteDBHelper;
import com.power.max.indoornavigation.Helper.Utils;
import com.power.max.indoornavigation.Model.BaseStation;
import com.power.max.indoornavigation.R;
import com.power.max.indoornavigation.Services.WifiScanner;

import java.util.ArrayList;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private final String TAG = "Map Fragment";

    private FloatingActionButton fab;
    private FloatingActionButton fabStart;
    private BebopVideoView bebopVideoView;

    private MenuItem menuCancel;
    private MenuItem menuAccept;
    private MenuItem menuDelete;

    private WifiAdapter wifiAdapter;
    private ArrayList<ScanResult> scanResults = new ArrayList<>();

    private PolygonOptions indoorMock = new PolygonOptions();
    private GoogleMap mMap;
    private LatLng currentPosition;

    private ArrayList<Marker> route = new ArrayList<>();
    private ArrayList<Polyline> polylines = new ArrayList<>();
    private boolean addRoute = false;

    private BitmapDescriptor iconStart, iconRoute, iconDone;

    private SQLiteDBHelper dbHelper;

    private DroneController droneController;

    private OnFragmentInteractionListener mListener;

    private final DroneController.Listener mDroneControllerListener = new DroneController.Listener() {
        @Override
        public void checkPointReachedListener(Marker marker) {
            route.get(route.indexOf(marker)).setIcon(iconDone);
            Log.d("Listener", "marker added" + marker.getId());
        }

        @Override
        public void videoReceivedListener(ARControllerCodec codec) {
            Log.d("Listener", "video received");
            if (bebopVideoView != null) {
                bebopVideoView.configureDecoder(codec);
            }
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
                    fabEmergency.setVisibility(View.INVISIBLE);
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
                        fabEmergency.setVisibility(View.VISIBLE);
                        droneController.startAutonomousFlight(new ArrayList<Marker>(route));
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

        drawAccessPoints();

        double START_LAT = 54.33901533;
        double START_LNG = 13.07454586;
        LatLng latLng = new LatLng(START_LAT, START_LNG);
        CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng).zoom(18.5f).build();
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
                    deleteMarker(marker);
                }
            });
            builder.setNegativeButton("Cancel", null);
            builder.show();
            return false;
        }
    };

    GoogleMap.OnMapClickListener onMapClickListener = new GoogleMap.OnMapClickListener() {
        @Override
        public void onMapClick(LatLng latLng) {
            if (addRoute) {
                // add marker to array and draw marker on map.
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
                    polylines.add(mMap.addPolyline(new PolylineOptions()
                            .add(old, latLng)
                            .color(Color.BLACK)
                            .width(4.0f)));
                }
            }
        }
    };

    GoogleMap.OnMarkerDragListener onMarkerDragListener = new GoogleMap.OnMarkerDragListener() {
        @Override
        public void onMarkerDragStart(Marker marker) { }

        @Override
        public void onMarkerDrag(Marker marker) { updateRoute(marker); }

        @Override
        public void onMarkerDragEnd(Marker marker) { }
    };

    private void deleteMarker(Marker marker) {
        marker.remove();
        dbHelper.sqlDelete(DbTables.RadioMap.TABLE_NAME,
                DbTables.RadioMap.COL_SSID + " = ?", new String[] {marker.getTitle()});

        // replace two polylines with one new.
        if (route.contains(marker)) {

            if (route.indexOf(marker) == 0 && polylines.size() > 0) {
                polylines.get(0).remove();
                polylines.remove(polylines.get(0));
            } else if (route.indexOf(marker) == route.size() - 1  && polylines.size() > 0) {
                polylines.get(polylines.size() - 1).remove();
                polylines.remove(polylines.size() - 1);
            } else if (polylines.size() > 0) {
                int firstLineIndex = route.indexOf(marker) - 1;
                int secondLineIndex = firstLineIndex + 1;

                if (firstLineIndex >= 0 && secondLineIndex >= 0
                        && firstLineIndex < polylines.size() && secondLineIndex < polylines.size()) {

                    Polyline firstLine = polylines.get(firstLineIndex);
                    Polyline secondLine = polylines.get(secondLineIndex);

                    // remove from map.
                    firstLine.remove();
                    secondLine.remove();

                    // replace first line with new line.
                    polylines.set(firstLineIndex, mMap.addPolyline(new PolylineOptions()
                            .add(firstLine.getPoints().get(0), secondLine.getPoints().get(1))
                            .width(4.0f)));

                    // remove seconde line from array.
                    polylines.remove(secondLine);
                }
            }

            // remove marker from route.
            route.remove(marker);
        }
    }

    /**
     * Draws all items in array route onto the map.
     * The markers in the array have to be replaced by the new ones, since google map does only
     * accept MarkerOptions to add.
     */
    private void drawRoute() {
        for (Marker marker : route) {
            marker.remove();
            route.set(route.indexOf(marker),
                    mMap.addMarker(new MarkerOptions()
                            .position(marker.getPosition())
                            .icon(route.indexOf(marker) == 0 ? iconStart : iconRoute)
                            .anchor(0.5f, 0.5f)
                            .draggable(true)));
        }
    }

    private void drawAccessPoints() {
        for (BaseStation baseStation : getBaseStations()) {
            mMap.addMarker(new MarkerOptions()
                    .position(baseStation.getLatLng())
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_router))
                    .title(baseStation.getSsid()));
        }
    }

    private void updateRoute(Marker marker) {
        int markerPosition = route.indexOf(marker);

        if (markerPosition == 0) {
            Polyline polyline = polylines.get(0);
            if (polyline != null) {
                polyline.remove();
                polylines.set(0, mMap.addPolyline(new PolylineOptions()
                    .add(marker.getPosition(), polyline.getPoints().get(1))
                    .width(4.0f)));
            }
        } else if (markerPosition == route.size() - 1) {
            Polyline polyline = polylines.get(polylines.size() - 1);
            if (polyline != null) {
                polyline.remove();
                polylines.set(polylines.size() - 1, mMap.addPolyline(new PolylineOptions()
                        .add(polyline.getPoints().get(0), marker.getPosition())
                        .width(4.0f)));
            }
        } else {
            int firstLineIndex = route.indexOf(marker) - 1;
            int secondLineIndex = firstLineIndex + 1;

            if (firstLineIndex >= 0 && secondLineIndex >= 0
                    && firstLineIndex < polylines.size() && secondLineIndex < polylines.size()) {
                Polyline firstLine = polylines.get(firstLineIndex);
                firstLine.remove();
                polylines.set(firstLineIndex, mMap.addPolyline(new PolylineOptions()
                        .add(firstLine.getPoints().get(0), marker.getPosition())
                        .width(4.0f)));

                Polyline secondLine = polylines.get(secondLineIndex);
                secondLine.remove();
                polylines.set(secondLineIndex, mMap.addPolyline(new PolylineOptions()
                        .add(marker.getPosition(), secondLine.getPoints().get(1))
                        .width(4.0f)));
            }
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
        cursor.moveToFirst();

        if (cursor.getCount() > 0) {

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
                //scanResults = (ArrayList<ScanResult>) intent.getExtras().get("scanResults");
                wifiAdapter.clear();
                wifiAdapter.addAll((ArrayList<ScanResult>) intent.getExtras().get("scanResults"));
                wifiAdapter.notifyDataSetChanged();
                Log.d("Map", "received data: " + scanResults.size());
            } catch (ClassCastException e) {
                Log.e("Map", e.getMessage());
            }
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        try {
            getActivity().unregisterReceiver(broadcastReceiver);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        Utils.stopService(WifiScanner.class, getActivity());
    }

    @Override
    public void onStop() {
        super.onStop();

        Utils.stopService(WifiScanner.class, getActivity());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        droneController.destroy();
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
        polylines.clear();
        mMap.clear();
    }

    private void setActionBarText(String text) {
        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null)
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(text);
    }
}
