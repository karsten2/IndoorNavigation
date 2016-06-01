package com.power.max.indoornavigation.Controller.Fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.power.max.indoornavigation.Adapter.ApCursorAdapter;
import com.power.max.indoornavigation.Adapter.WifiAdapter;
import com.power.max.indoornavigation.Database.DbTables;
import com.power.max.indoornavigation.Database.SQLiteDBHelper;
import com.power.max.indoornavigation.Helper.Utils;
import com.power.max.indoornavigation.Math.Lateration;
import com.power.max.indoornavigation.Model.BaseStation;
import com.power.max.indoornavigation.R;
import com.power.max.indoornavigation.Services.WifiScanner;

import java.util.ArrayList;

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
                int delRows = dbHelper.sqlDelete(DbTables.RadioMap.TABLE_NAME, "", new String[]{});
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
                dbHelper.dropTable(DbTables.RadioMap.SQL_DROP_TABLE);
                getTable();
            }
        });

        Button btnCreate = (Button) view.findViewById(R.id.btnCreate);
        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbHelper.createTable(DbTables.RadioMap.SQL_CREATE_ENTRIES);
                getTable();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //dialog();
                Lateration.calculatePosition(new ArrayList<BaseStation>());
            }
        });

        getActivity().registerReceiver(
                broadcastReceiver, new IntentFilter(WifiScanner.TAG));

        return view;
    }


    private void getTable() {
        if (dbHelper.tableExists(DbTables.RadioMap.TABLE_NAME)) {
            apAdapter.changeCursor(dbHelper.sqlSelect(DbTables.RadioMap.TABLE_NAME,
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

    private void dialog() {

        // start wifi service
        Utils.startService(WifiScanner.class, getActivity());
        wifiAdapter = new WifiAdapter(getActivity(), scanResults);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Select Access Point");

        builder.setPositiveButton("Okay",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Utils.Serialize(getContext(), "file", savedResults);
                    }
                });

        builder.setAdapter(wifiAdapter,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ScanResult scanResult = wifiAdapter.getItem(which);
                        savedResults.add(scanResult);
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
