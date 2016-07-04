package com.indoornavigation.View;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.indoornavigation.Adapter.ApCursorAdapter;
import com.indoornavigation.Adapter.WifiAdapter;
import com.indoornavigation.Database.DbTables;
import com.indoornavigation.Database.SQLiteDBHelper;
import com.indoornavigation.Helper.Utils;
import com.indoor.navigation.indoornavigation.R;
import com.indoornavigation.Services.WifiScanner;

import java.util.ArrayList;
import java.util.Random;

public class SqliteFragment extends Fragment {

    private final String TAG = "sqlFragment";

    private OnFragmentInteractionListener mListener;

    private ApCursorAdapter apAdapter;
    private Cursor cursor;
    private SQLiteDBHelper dbHelper;

    private WifiAdapter wifiAdapter;
    private ArrayList<ScanResult> scanResults = new ArrayList<>();

    /**
     * Empty constructor. (required)
     */
    public SqliteFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dbHelper = new SQLiteDBHelper(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sqlite, container, false);

        ListView listView = (ListView) view.findViewById(R.id.listView);
        if (listView != null) {
            apAdapter = new ApCursorAdapter(getContext(), cursor);
            listView.setAdapter(apAdapter);
        }

        Button btnDel = (Button) view.findViewById(R.id.btnDel);
        btnDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int delRows = dbHelper.sqlDelete(DbTables.BaseStation.TABLE_NAME, "", new String[]{});
                Toast.makeText(
                        getContext(), "Deleted " + delRows + " rows.", Toast.LENGTH_SHORT).show();
                getTable();
            }
        });

        Button btnGet = (Button) view.findViewById(R.id.btnGet);
        btnGet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getTable();
            }
        });

        Button btnDrop = (Button) view.findViewById(R.id.btnDrop);
        btnDrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbHelper.dropTable(DbTables.BaseStation.SQL_DROP_TABLE);
                getTable();
            }
        });

        Button btnCreate = (Button) view.findViewById(R.id.btnCreate);
        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbHelper.createTable(DbTables.BaseStation.SQL_CREATE_ENTRIES);
                getTable();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long result = dbHelper.sqlInsert(
                        DbTables.BaseStation.TABLE_NAME,
                        null,
                        getDummy().toDbValues());
                Toast.makeText(getContext(), result + "rows inserted", Toast.LENGTH_SHORT).show();
            }
        });

        getActivity().registerReceiver(
                broadcastReceiver, new IntentFilter(WifiScanner.TAG));

        return view;
    }


    private void getTable() {
        if (dbHelper.tableExists(DbTables.BaseStation.TABLE_NAME)) {
            apAdapter.changeCursor(dbHelper.sqlSelect(DbTables.BaseStation.TABLE_NAME,
                    null, null, null, null, null, null));
        } else {
            apAdapter.changeCursor(null);
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

    private ArrayList<ScanResult> savedResults = new ArrayList<>();

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                //scanResults = (ArrayList<ScanResult>) intent.getExtras().get("scanResults");
                wifiAdapter.clear();
                wifiAdapter.addAll((ArrayList<ScanResult>) intent.getExtras().get("scanResults"));
                wifiAdapter.notifyDataSetChanged();
                Log.d(TAG, "received data: " + scanResults.size());
            } catch (ClassCastException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    };

    private com.indoornavigation.Model.BaseStation getDummy() {
        return new com.indoornavigation.Model.BaseStation(
                "Router_" + getRandom(),
                "00:00:00:00:00:00",
                "1.2.3.4.5",
                "00:00:00:00:00:00",
                1,
                new LatLng(0, 0));
    }

    private int getRandom() {
        return new Random().nextInt();
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(broadcastReceiver);

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
        dbHelper.close();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        Utils.stopService(WifiScanner.class, getActivity());

        dbHelper.close();
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
