package com.indoornavigation.Controller.Fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.indoornavigation.Adapter.WifiAdapter;
import com.indoornavigation.Helper.ScanResultComparator;
import com.indoor.navigation.indoornavigation.R;

import java.util.ArrayList;
import java.util.Collections;

public class WifiFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    private WifiManager wifi;
    private WifiAdapter wifiAdapter;
    private ArrayList<ScanResult> scanResults = new ArrayList<>();
    private ActionBar actionBar;
    private Handler handler = new Handler();
    private Activity context;


    public WifiFragment() { }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment WifiFragment.
     */
    public static WifiFragment newInstance(String param1, String param2) {
        WifiFragment fragment = new WifiFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (wifi != null) {
                wifi.startScan();
                handler.postDelayed(runnable, 1000);
            }
        }
    };

    private BroadcastReceiver wifiReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context c, Intent intent)
        {
            scanResults.clear();
            scanResults.addAll(wifi.getScanResults());
            Collections.sort(scanResults, new ScanResultComparator());

            Log.d("wifi", "scanResults: " + scanResults.size());
            wifiAdapter.notifyDataSetChanged();
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getActivity().finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wifi, container, false);

        wifi = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);

        if (!wifi.isWifiEnabled()) {
            Toast.makeText(getActivity(), "Enabling wifi, please wait.", Toast.LENGTH_LONG).show();
            wifi.setWifiEnabled(true);
        }

        handler.post(runnable);

        ListView listView = (ListView) view.findViewById(R.id.listView);
        if (listView != null) {
            wifiAdapter = new WifiAdapter(getActivity(), scanResults);
            listView.setAdapter(wifiAdapter);

            getActivity().registerReceiver(wifiReceiver,
                    new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    ScanResult scanResult = scanResults.get(position);
                    String info = "";
                    info += "SSID: " + scanResult.SSID + "\n";
                    info += "BSSID: " + scanResult.BSSID + "\n";
                    info += "Strength: " + scanResult.level + "\n";
                    info += "Sec: " + scanResult.capabilities + "\n";
                    info += "Freq: " + scanResult.frequency;

                    Toast.makeText(getActivity(), info, Toast.LENGTH_LONG).show();
                }
            });
        }

        return view;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
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
