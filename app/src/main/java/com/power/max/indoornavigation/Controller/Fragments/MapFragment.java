package com.power.max.indoornavigation.Controller.Fragments;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.android.gms.maps.model.PolygonOptions;
import com.power.max.indoornavigation.Adapter.WifiAdapter;
import com.power.max.indoornavigation.Database.DbTables;
import com.power.max.indoornavigation.Database.SQLiteDBHelper;
import com.power.max.indoornavigation.Helper.Utils;
import com.power.max.indoornavigation.Model.BaseStation;
import com.power.max.indoornavigation.R;
import com.power.max.indoornavigation.Services.WifiScanner;

import java.util.ArrayList;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private final String TAG = "Map Fragment";

    private WifiAdapter wifiAdapter;
    private ArrayList<ScanResult> scanResults = new ArrayList<>();

    private PolygonOptions indoorMock = new PolygonOptions();
    private GoogleMap mMap;
    private ActionBar actionBar;
    private LatLng currentPosition;
    private ArrayList<Marker> mapMarkers = new ArrayList<>();

    private SQLiteDBHelper dbHelper;

    private OnFragmentInteractionListener mListener;

    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().registerReceiver(
                broadcastReceiver, new IntentFilter(WifiScanner.TAG));

        dbHelper = new SQLiteDBHelper(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_map, container, false);

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog();
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        return view;
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
        mMap = googleMap;
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                currentPosition = cameraPosition.target;
                setStatus(cameraPosition.target.toString());
            }
        });

        //final PolylineOptions polylineOptions = new PolylineOptions();

        mMap.setOnMarkerClickListener(onMarkerClickListener);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                //polylineOptions.add(latLng);
                //mMap.addPolyline(polylineOptions);
                indoorMock.add(latLng);
                mMap.addPolygon(indoorMock);
            }
        });

        addMarkersToMap();

        double START_LAT = 54.33901533;
        double START_LNG = 13.07454586;
        LatLng latLng = new LatLng(START_LAT, START_LNG);
        CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng).zoom(18.5f).build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        mMap.addMarker(new MarkerOptions().position(latLng).title("Marker"));
        mMap.moveCamera(cameraUpdate);

        if (ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mMap.setMyLocationEnabled(true);
    }

    void addMarkersToMap() {
        for (BaseStation baseStation : getBaseStations()) {
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(baseStation.getLatLng())
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_router))
                    .title(baseStation.getSsid()));

            mapMarkers.add(marker);
        }
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
                    marker.remove();
                }
            });
            builder.setNegativeButton("Cancel", null);
            builder.show();
            return false;
        }
    };

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

    private void addRoute() {}

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
                        addMarkersToMap();
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

        Utils.stopService(WifiScanner.class, getActivity());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        Utils.stopService(WifiScanner.class, getActivity());
    }
}
