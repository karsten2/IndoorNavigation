package com.indoornavigation.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.indoor.navigation.indoornavigation.R;
import com.indoornavigation.Model.BaseStation;

import java.util.ArrayList;

/**
 * Class to handle data from wifi scanresults.
 */
public class BsAdapter extends ArrayAdapter<BaseStation> {
    public BsAdapter(Context context, ArrayList<BaseStation> baseStations) {
        super(context, 0, baseStations);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        BaseStation bs = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.wifi_row, parent, false);
        }
        // Lookup view for data population
        TextView tvSSID = (TextView) convertView.findViewById(R.id.txtSSID);
        // Populate the data into the template view using the data object
        tvSSID.setText(bs.getSsid());

        return convertView;
    }
}
