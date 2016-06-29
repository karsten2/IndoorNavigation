package com.indoornavigation.Adapter;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
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

    private ArrayList<BaseStation> selectedBaseStations = new ArrayList<>();


    public BsAdapter(Context context, ArrayList<BaseStation> baseStations) {
        super(context, 0, baseStations);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        BaseStation baseStation = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    //android.R.layout.simple_list_item_multiple_choice,
                    R.layout.dialog_radiomap_item,
                    parent, false);
        }

        TextView tv = (TextView) convertView.findViewById(R.id.txtRadiomapDialog);
        CheckBox cBox = (CheckBox) convertView.findViewById(R.id.checkBox);

        if (this.selectedBaseStations.contains(baseStation))
            cBox.setChecked(true);
        else
            cBox.setChecked(false);

        tv.setText(baseStation.getSsid());

        return convertView;
    }

    public void toggleSelection(BaseStation bs) {
        if (this.selectedBaseStations.contains(bs))
            this.selectedBaseStations.remove(bs);
        else
            this.selectedBaseStations.add(bs);

        this.notifyDataSetChanged();
    }

    public ArrayList<BaseStation> getCheckedItems() {
        return this.selectedBaseStations;
    }
}
