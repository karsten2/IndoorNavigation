package com.indoornavigation.Database;

import android.provider.BaseColumns;


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
     * |------------------------------------------------------------------------------|
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

    public static abstract class RadioMap_3 implements BaseColumns {
        public static final String TABLE_NAME = "radiomap_3";

        public static final String REAL_TYPE = " REAL";
        public static final String INTEGER_TYPE = " INTEGER";

        public static final String COL_AP1_ID       = "AP1_ID";
        public static final String COL_AP1_RSSI     = "AP1_RSSI";
        public static final String COL_AP1_LAT      = "AP1_LAT";
        public static final String COL_AP1_LNG      = "AP1_LNG";

        public static final String COL_AP2_ID       = "AP2_ID";
        public static final String COL_AP2_RSSI     = "AP2_RSSI";
        public static final String COL_AP2_LAT      = "AP2_LAT";
        public static final String COL_AP2_LNG      = "AP2_LNG";

        public static final String COL_AP3_ID       = "AP3_ID";
        public static final String COL_AP3_RSSI     = "AP3_RSSI";
        public static final String COL_AP3_LAT      = "AP3_LAT";
        public static final String COL_AP3_LNG      = "AP3_LNG";

        public static final String COMMA_SEP = ", ";

        public static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE IF NOT EXISTS " + RadioMap_3.TABLE_NAME + " (" +
                        RadioMap_3._ID + " INTEGER PRIMARY KEY," +
                        RadioMap_3.COL_AP1_ID   + INTEGER_TYPE + COMMA_SEP +
                        RadioMap_3.COL_AP1_RSSI + REAL_TYPE + COMMA_SEP +
                        RadioMap_3.COL_AP1_LAT  + REAL_TYPE + COMMA_SEP +
                        RadioMap_3.COL_AP1_LNG  + REAL_TYPE +
                        RadioMap_3.COL_AP2_ID   + INTEGER_TYPE + COMMA_SEP +
                        RadioMap_3.COL_AP2_RSSI + REAL_TYPE + COMMA_SEP +
                        RadioMap_3.COL_AP2_LAT  + REAL_TYPE + COMMA_SEP +
                        RadioMap_3.COL_AP2_LNG  + REAL_TYPE +
                        RadioMap_3.COL_AP3_ID   + INTEGER_TYPE + COMMA_SEP +
                        RadioMap_3.COL_AP3_RSSI + REAL_TYPE + COMMA_SEP +
                        RadioMap_3.COL_AP3_LAT  + REAL_TYPE + COMMA_SEP +
                        RadioMap_3.COL_AP3_LNG  + REAL_TYPE + ")";

        public static final String SQL_DELETE_ENTRIES =
                "DELETE FROM " + RadioMap_3.TABLE_NAME;

        public static final String SQL_DROP_TABLE =
                "DROP TABLE IF EXISTS " + RadioMap_3.TABLE_NAME;

        public static final String SQL_SELECT_ALL =
                "SELECT * FROM " + RadioMap_3.TABLE_NAME;
    }

    public static abstract class RadioMap_4 implements BaseColumns {
        public static final String TABLE_NAME = "radiomap_4";

        public static final String REAL_TYPE = " REAL";
        public static final String INTEGER_TYPE = " INTEGER";

        public static final String COL_AP1_ID       = "AP1_ID";
        public static final String COL_AP1_RSSI     = "AP1_RSSI";
        public static final String COL_AP1_LAT      = "AP1_LAT";
        public static final String COL_AP1_LNG      = "AP1_LNG";

        public static final String COL_AP2_ID       = "AP2_ID";
        public static final String COL_AP2_RSSI     = "AP2_RSSI";
        public static final String COL_AP2_LAT      = "AP2_LAT";
        public static final String COL_AP2_LNG      = "AP2_LNG";

        public static final String COL_AP3_ID       = "AP3_ID";
        public static final String COL_AP3_RSSI     = "AP3_RSSI";
        public static final String COL_AP3_LAT      = "AP3_LAT";
        public static final String COL_AP3_LNG      = "AP3_LNG";

        public static final String COL_AP4_ID       = "AP4_ID";
        public static final String COL_AP4_RSSI     = "AP4_RSSI";
        public static final String COL_AP4_LAT      = "AP4_LAT";
        public static final String COL_AP4_LNG      = "AP4_LNG";

        public static final String COMMA_SEP = ", ";

        public static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE IF NOT EXISTS " + RadioMap_4.TABLE_NAME + " (" +
                        RadioMap_4._ID + " INTEGER PRIMARY KEY," +
                        RadioMap_4.COL_AP1_ID   + INTEGER_TYPE + COMMA_SEP +
                        RadioMap_4.COL_AP1_RSSI + REAL_TYPE + COMMA_SEP +
                        RadioMap_4.COL_AP1_LAT  + REAL_TYPE + COMMA_SEP +
                        RadioMap_4.COL_AP1_LNG  + REAL_TYPE +
                        RadioMap_4.COL_AP2_ID   + INTEGER_TYPE + COMMA_SEP +
                        RadioMap_4.COL_AP2_RSSI + REAL_TYPE + COMMA_SEP +
                        RadioMap_4.COL_AP2_LAT  + REAL_TYPE + COMMA_SEP +
                        RadioMap_4.COL_AP2_LNG  + REAL_TYPE +
                        RadioMap_4.COL_AP3_ID   + INTEGER_TYPE + COMMA_SEP +
                        RadioMap_4.COL_AP3_RSSI + REAL_TYPE + COMMA_SEP +
                        RadioMap_4.COL_AP3_LAT  + REAL_TYPE + COMMA_SEP +
                        RadioMap_4.COL_AP3_LNG  + REAL_TYPE +
                        RadioMap_4.COL_AP4_ID   + INTEGER_TYPE + COMMA_SEP +
                        RadioMap_4.COL_AP4_RSSI + REAL_TYPE + COMMA_SEP +
                        RadioMap_4.COL_AP4_LAT  + REAL_TYPE + COMMA_SEP +
                        RadioMap_4.COL_AP4_LNG  + REAL_TYPE + ")";

        public static final String SQL_DELETE_ENTRIES =
                "DELETE FROM " + RadioMap_4.TABLE_NAME;

        public static final String SQL_DROP_TABLE =
                "DROP TABLE IF EXISTS " + RadioMap_4.TABLE_NAME;

        public static final String SQL_SELECT_ALL =
                "SELECT * FROM " + RadioMap_4.TABLE_NAME;
    }

    public static abstract class RadioMap_5 implements BaseColumns {
        public static final String TABLE_NAME = "radiomap_5";

        public static final String REAL_TYPE = " REAL";
        public static final String INTEGER_TYPE = " INTEGER";

        public static final String COL_AP1_ID       = "AP1_ID";
        public static final String COL_AP1_RSSI     = "AP1_RSSI";
        public static final String COL_AP1_LAT      = "AP1_LAT";
        public static final String COL_AP1_LNG      = "AP1_LNG";

        public static final String COL_AP2_ID       = "AP2_ID";
        public static final String COL_AP2_RSSI     = "AP2_RSSI";
        public static final String COL_AP2_LAT      = "AP2_LAT";
        public static final String COL_AP2_LNG      = "AP2_LNG";

        public static final String COL_AP3_ID       = "AP3_ID";
        public static final String COL_AP3_RSSI     = "AP3_RSSI";
        public static final String COL_AP3_LAT      = "AP3_LAT";
        public static final String COL_AP3_LNG      = "AP3_LNG";

        public static final String COL_AP4_ID       = "AP4_ID";
        public static final String COL_AP4_RSSI     = "AP4_RSSI";
        public static final String COL_AP4_LAT      = "AP4_LAT";
        public static final String COL_AP4_LNG      = "AP4_LNG";

        public static final String COL_AP5_ID       = "AP5_ID";
        public static final String COL_AP5_RSSI     = "AP5_RSSI";
        public static final String COL_AP5_LAT      = "AP5_LAT";
        public static final String COL_AP5_LNG      = "AP5_LNG";

        public static final String COMMA_SEP = ", ";

        public static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE IF NOT EXISTS " + RadioMap_5.TABLE_NAME + " (" +
                        RadioMap_5._ID + " INTEGER PRIMARY KEY," +
                        RadioMap_5.COL_AP1_ID   + INTEGER_TYPE + COMMA_SEP +
                        RadioMap_5.COL_AP1_RSSI + REAL_TYPE + COMMA_SEP +
                        RadioMap_5.COL_AP1_LAT  + REAL_TYPE + COMMA_SEP +
                        RadioMap_5.COL_AP1_LNG  + REAL_TYPE +
                        RadioMap_5.COL_AP2_ID   + INTEGER_TYPE + COMMA_SEP +
                        RadioMap_5.COL_AP2_RSSI + REAL_TYPE + COMMA_SEP +
                        RadioMap_5.COL_AP2_LAT  + REAL_TYPE + COMMA_SEP +
                        RadioMap_5.COL_AP2_LNG  + REAL_TYPE +
                        RadioMap_5.COL_AP3_ID   + INTEGER_TYPE + COMMA_SEP +
                        RadioMap_5.COL_AP3_RSSI + REAL_TYPE + COMMA_SEP +
                        RadioMap_5.COL_AP3_LAT  + REAL_TYPE + COMMA_SEP +
                        RadioMap_5.COL_AP3_LNG  + REAL_TYPE +
                        RadioMap_5.COL_AP4_ID   + INTEGER_TYPE + COMMA_SEP +
                        RadioMap_5.COL_AP4_RSSI + REAL_TYPE + COMMA_SEP +
                        RadioMap_5.COL_AP4_LAT  + REAL_TYPE + COMMA_SEP +
                        RadioMap_5.COL_AP4_LNG  + REAL_TYPE +
                        RadioMap_5.COL_AP5_ID   + INTEGER_TYPE + COMMA_SEP +
                        RadioMap_5.COL_AP5_RSSI + REAL_TYPE + COMMA_SEP +
                        RadioMap_5.COL_AP5_LAT  + REAL_TYPE + COMMA_SEP +
                        RadioMap_5.COL_AP5_LNG  + REAL_TYPE + ")";

        public static final String SQL_DELETE_ENTRIES =
                "DELETE FROM " + RadioMap_5.TABLE_NAME;

        public static final String SQL_DROP_TABLE =
                "DROP TABLE IF EXISTS " + RadioMap_5.TABLE_NAME;

        public static final String SQL_SELECT_ALL =
                "SELECT * FROM " + RadioMap_5.TABLE_NAME;
    }

    public static abstract class RadioMap_6 implements BaseColumns {
        public static final String TABLE_NAME = "radiomap_5";

        public static final String REAL_TYPE = " REAL";
        public static final String INTEGER_TYPE = " INTEGER";

        public static final String COL_AP1_ID       = "AP1_ID";
        public static final String COL_AP1_RSSI     = "AP1_RSSI";
        public static final String COL_AP1_LAT      = "AP1_LAT";
        public static final String COL_AP1_LNG      = "AP1_LNG";

        public static final String COL_AP2_ID       = "AP2_ID";
        public static final String COL_AP2_RSSI     = "AP2_RSSI";
        public static final String COL_AP2_LAT      = "AP2_LAT";
        public static final String COL_AP2_LNG      = "AP2_LNG";

        public static final String COL_AP3_ID       = "AP3_ID";
        public static final String COL_AP3_RSSI     = "AP3_RSSI";
        public static final String COL_AP3_LAT      = "AP3_LAT";
        public static final String COL_AP3_LNG      = "AP3_LNG";

        public static final String COL_AP4_ID       = "AP4_ID";
        public static final String COL_AP4_RSSI     = "AP4_RSSI";
        public static final String COL_AP4_LAT      = "AP4_LAT";
        public static final String COL_AP4_LNG      = "AP4_LNG";

        public static final String COL_AP5_ID       = "AP5_ID";
        public static final String COL_AP5_RSSI     = "AP5_RSSI";
        public static final String COL_AP5_LAT      = "AP5_LAT";
        public static final String COL_AP5_LNG      = "AP5_LNG";

        public static final String COL_AP6_ID       = "AP6_ID";
        public static final String COL_AP6_RSSI     = "AP6_RSSI";
        public static final String COL_AP6_LAT      = "AP6_LAT";
        public static final String COL_AP6_LNG      = "AP6_LNG";

        public static final String COMMA_SEP = ", ";

        public static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE IF NOT EXISTS " + RadioMap_6.TABLE_NAME + " (" +
                        RadioMap_6._ID + " INTEGER PRIMARY KEY," +
                        RadioMap_6.COL_AP1_ID   + INTEGER_TYPE + COMMA_SEP +
                        RadioMap_6.COL_AP1_RSSI + REAL_TYPE + COMMA_SEP +
                        RadioMap_6.COL_AP1_LAT  + REAL_TYPE + COMMA_SEP +
                        RadioMap_6.COL_AP1_LNG  + REAL_TYPE +
                        RadioMap_6.COL_AP2_ID   + INTEGER_TYPE + COMMA_SEP +
                        RadioMap_6.COL_AP2_RSSI + REAL_TYPE + COMMA_SEP +
                        RadioMap_6.COL_AP2_LAT  + REAL_TYPE + COMMA_SEP +
                        RadioMap_6.COL_AP2_LNG  + REAL_TYPE +
                        RadioMap_6.COL_AP3_ID   + INTEGER_TYPE + COMMA_SEP +
                        RadioMap_6.COL_AP3_RSSI + REAL_TYPE + COMMA_SEP +
                        RadioMap_6.COL_AP3_LAT  + REAL_TYPE + COMMA_SEP +
                        RadioMap_6.COL_AP3_LNG  + REAL_TYPE +
                        RadioMap_6.COL_AP4_ID   + INTEGER_TYPE + COMMA_SEP +
                        RadioMap_6.COL_AP4_RSSI + REAL_TYPE + COMMA_SEP +
                        RadioMap_6.COL_AP4_LAT  + REAL_TYPE + COMMA_SEP +
                        RadioMap_6.COL_AP4_LNG  + REAL_TYPE +
                        RadioMap_6.COL_AP5_ID   + INTEGER_TYPE + COMMA_SEP +
                        RadioMap_6.COL_AP5_RSSI + REAL_TYPE + COMMA_SEP +
                        RadioMap_6.COL_AP5_LAT  + REAL_TYPE + COMMA_SEP +
                        RadioMap_6.COL_AP5_LNG  + REAL_TYPE +
                        RadioMap_6.COL_AP6_ID   + INTEGER_TYPE + COMMA_SEP +
                        RadioMap_6.COL_AP6_RSSI + REAL_TYPE + COMMA_SEP +
                        RadioMap_6.COL_AP6_LAT  + REAL_TYPE + COMMA_SEP +
                        RadioMap_6.COL_AP6_LNG  + REAL_TYPE + ")";

        public static final String SQL_DELETE_ENTRIES =
                "DELETE FROM " + RadioMap_6.TABLE_NAME;

        public static final String SQL_DROP_TABLE =
                "DROP TABLE IF EXISTS " + RadioMap_6.TABLE_NAME;

        public static final String SQL_SELECT_ALL =
                "SELECT * FROM " + RadioMap_6.TABLE_NAME;
    }

}
