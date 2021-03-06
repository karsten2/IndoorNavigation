package com.indoornavigation.Adapter;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.indoor.navigation.indoornavigation.R;

import java.util.ArrayList;

/**
 * Class to handle data from wifi scanresults.
 */
public class WifiAdapter extends ArrayAdapter<ScanResult> {
    public WifiAdapter(Context context, ArrayList<ScanResult> scanResults) {
        super(context, 0, scanResults);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        ScanResult scanResult = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.wifi_row, parent, false);
        }
        // Lookup view for data population
        TextView tvSSID = (TextView) convertView.findViewById(R.id.txtSSID);
        TextView tvInfo = (TextView) convertView.findViewById(R.id.txtInfo);
        // Populate the data into the template view using the data object
        tvSSID.setText(scanResult.SSID);
        tvInfo.setText(String.format("Level: %d | + %s", scanResult.level, scanResult.BSSID));

        return convertView;
    }
}
