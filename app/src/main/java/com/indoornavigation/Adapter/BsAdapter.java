package com.indoornavigation.Adapter;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

import com.indoor.navigation.indoornavigation.R;
import com.indoornavigation.Model.BaseStation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Class to handle data from wifi scanresults.
 */
public class BsAdapter extends ArrayAdapter<BaseStation> {

    ArrayList<BaseStation> baseStations = new ArrayList<>();
    ArrayList<Boolean> checkState = new ArrayList<>();

    public BsAdapter(Context context, ArrayList<BaseStation> baseStations) {
        super(context, 0, baseStations);
        this.baseStations = baseStations;
        checkState = new ArrayList<>(Collections.nCopies(baseStations.size(), Boolean.FALSE));
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        final BaseStation baseStation = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    android.R.layout.simple_list_item_multiple_choice,
                    parent, false);
        }

        final CheckedTextView ctv = (CheckedTextView) convertView.findViewById(
                android.R.id.text1);
        ctv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ctv.setChecked(!ctv.isChecked());
                checkState.set(position, ctv.isChecked());
            }
        });

        ctv.setChecked(checkState.get(position));
        ctv.setText(baseStation.getSsid());

        return convertView;
    }

    public ArrayList<BaseStation> getCheckedItems() {
        ArrayList<BaseStation> returnValues = new ArrayList<>();

        for(int i = 0; i < checkState.size(); i++) {
            if (checkState.get(i))
                returnValues.add(baseStations.get(i));
        }

        return returnValues;
    }
}
