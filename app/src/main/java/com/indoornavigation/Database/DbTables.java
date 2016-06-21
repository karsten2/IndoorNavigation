package com.indoornavigation.Database;

import android.provider.BaseColumns;

import java.security.PublicKey;

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
                        RadioMap.COL_SSID + TEXT_TYPE + " UNIQUE" + COMMA_SEP +
                        RadioMap.COL_BSSID + TEXT_TYPE + COMMA_SEP +
                        RadioMap.COL_LAT + REAL_TYPE + COMMA_SEP +
                        RadioMap.COL_LNG + REAL_TYPE + ")";

        public static final String SQL_DELETE_ENTRIES =
                "DELETE FROM " + RadioMap.TABLE_NAME;

        public static final String SQL_DROP_TABLE =
                "DROP TABLE IF EXISTS " + RadioMap.TABLE_NAME;

        public static final String SQL_SELECT_ALL =
                "SELECT * FROM " + RadioMap.TABLE_NAME;
    }

    public static abstract class ApRegressionValues implements BaseColumns {
        public static final String TABLE_NAME = "apRegressionValues";

        public static final String REAL_TYPE = " REAL";
        public static final String INTEGER_TYPE = " INTEGER";

        public static final String COL_AP_ID = "AP_ID";
        public static final String COL_X = "X";
        public static final String COL_Y = "Y";

        public static final String COMMA_SEP = ", ";

        public static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE IF NOT EXISTS " + ApRegressionValues.TABLE_NAME + " (" +
                        ApRegressionValues._ID + INTEGER_TYPE + " PRIMARY KEY, " +
                        ApRegressionValues.COL_AP_ID + INTEGER_TYPE + COMMA_SEP +
                        ApRegressionValues.COL_X + REAL_TYPE + COMMA_SEP +
                        ApRegressionValues.COL_Y + REAL_TYPE + ")";

        public static final String SQL_DELETE_ENTRIES =
                "DELETE FROM " + ApRegressionValues.TABLE_NAME;

        public static final String SQL_DROP_TABLE =
                "DROP TABLE IF EXISTS " + ApRegressionValues.TABLE_NAME;

        public static final String SQL_SELECT_ALL =
                "SELECT * FROM " + ApRegressionValues.TABLE_NAME;

    }
}
