package com.power.max.indoornavigation.Services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;

import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.power.max.indoornavigation.Helper.ScanResultComparator;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Class to scan for wifi in the background.
 */
public class WifiScanner extends Service {

    public static final String TAG = "wifiService";
    private WifiManager wifi;
    private ArrayList<ScanResult> scanResults = new ArrayList<>();

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

    private BroadcastReceiver wifiReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context c, Intent intent)
        {
            scanResults.clear();
            scanResults.addAll(wifi.getScanResults());
            Collections.sort(scanResults, new ScanResultComparator());

            // Send broadcast with scan-results to receiver in fragment.
            Intent intentService = new Intent();
            intentService.setAction(TAG);
            intentService.putExtra("scanResults", scanResults);
            getApplication().sendBroadcast(intentService);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        wifi = (WifiManager) this.getSystemService(WIFI_SERVICE);

        if (!wifi.isWifiEnabled()) {
            wifi.setWifiEnabled(true);
        }

        this.registerReceiver(wifiReceiver,
                new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        handler.post(runnable);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        try {
            this.unregisterReceiver(wifiReceiver);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Trying to unregister not registered receiver!");
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
