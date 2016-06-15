package com.indoornavigation.Adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.indoornavigation.Database.DbTables;
import com.indoor.navigation.indoornavigation.R;

/**
 * Custom Adapter between SQLite and the apps listview.
 */
public class ApCursorAdapter extends CursorAdapter {

    public ApCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.wifi_row, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView tvESSID = (TextView) view.findViewById(R.id.txtESSID);
        TextView tvInfo = (TextView) view.findViewById(R.id.txtInfo);

        tvESSID.setText(cursor.getString(
                cursor.getColumnIndexOrThrow(DbTables.RadioMap.COL_SSID)));
        tvInfo.setText(cursor.getString(
                cursor.getColumnIndexOrThrow(DbTables.RadioMap.COL_BSSID)));
    }
}
