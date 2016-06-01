package com.power.max.indoornavigation.Database;

import android.database.Cursor;
import android.provider.BaseColumns;

import com.google.android.gms.maps.model.LatLng;
import com.power.max.indoornavigation.Model.BaseStation;

import java.util.ArrayList;

/**
 * Class that represents the database tables within nested abstract classes.
 */
public final class DbTables {

    public DbTables() {}

    /**
     * Class that represents the table 'radiomap'.
     *  ______________________________________________________________________________
     * |   _ID   |   ENTRYID   |   TITLE   |   SSID   |   BSSID   |   LAT   |   LNG   |
     * |------------------------------------------------------------------------------|
     * |unique/pk|TEXT         |TEXT       |TEXT      |TEXT       |REAL     |REAL     |
     *  ------------------------------------------------------------------------------
     * |        1|             |           |   eduroam|11:22::::66|54.123456|13.123456|
     *  ...
     */
    public static abstract class RadioMap implements BaseColumns {
        public static final String TABLE_NAME = "radiomap";

        public static final String TEXT_TYPE = " TEXT";
        public static final String REAL_TYPE = " REAL";

        public static final String COLUMN_NAME_ENTRY_ID = "entryid";
        public static final String COLUMN_NAME_TITLE = "title";

        public static final String COL_SSID     = "SSID";
        public static final String COL_BSSID    = "BSSID";
        public static final String COL_LAT      = "LAT";
        public static final String COL_LNG      = "LNG";

        public static final String COMMA_SEP = ",";

        public static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE IF NOT EXISTS " + RadioMap.TABLE_NAME + " (" +
                        RadioMap._ID + " INTEGER PRIMARY KEY," +
                        RadioMap.COLUMN_NAME_ENTRY_ID + TEXT_TYPE + COMMA_SEP +
                        RadioMap.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
                        RadioMap.COL_SSID + TEXT_TYPE + COMMA_SEP +
                        RadioMap.COL_BSSID + TEXT_TYPE + " UNIQUE" + COMMA_SEP +
                        RadioMap.COL_LAT + REAL_TYPE + COMMA_SEP +
                        RadioMap.COL_LNG + REAL_TYPE + ")";

        public static final String SQL_DELETE_ENTRIES =
                "DELETE FROM " + RadioMap.TABLE_NAME;

        public static final String SQL_DROP_TABLE =
                "DROP TABLE IF EXISTS " + RadioMap.TABLE_NAME;
    }
}
