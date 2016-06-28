package com.indoornavigation.Database;

import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.Arrays;


/**
 * Class that represents the database tables within nested abstract classes.
 */
public final class DbTables {

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

        public static final String REAL_TYPE    = " REAL";
        public static final String INTEGER_TYPE = " INTEGER";
        public static final String TEXT_TYPE    = " TEXT";

        public static final String COL_ID_MEASURING = "ID_MEASURING";
        public static final String COL_BEARING      = "BEARING";
        public static final String COL_AP1_ID       = "AP1_ID";
        public static final String COL_AP1_RSSI     = "AP1_RSSI";
        public static final String COL_AP2_ID       = "AP2_ID";
        public static final String COL_AP2_RSSI     = "AP2_RSSI";
        public static final String COL_AP3_ID       = "AP3_ID";
        public static final String COL_AP3_RSSI     = "AP3_RSSI";

        public static final String COMMA_SEP = ", ";

        public static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE IF NOT EXISTS " + RadioMap_3.TABLE_NAME + " (" +
                        RadioMap_3._ID + " INTEGER PRIMARY KEY," +
                        RadioMap_3.COL_ID_MEASURING + INTEGER_TYPE + COMMA_SEP +
                        RadioMap_3.COL_BEARING + TEXT_TYPE + COMMA_SEP +
                        RadioMap_3.COL_AP1_ID   + INTEGER_TYPE + COMMA_SEP +
                        RadioMap_3.COL_AP1_RSSI + REAL_TYPE + COMMA_SEP +
                        RadioMap_3.COL_AP2_ID   + INTEGER_TYPE + COMMA_SEP +
                        RadioMap_3.COL_AP2_RSSI + REAL_TYPE + COMMA_SEP +
                        RadioMap_3.COL_AP3_ID   + INTEGER_TYPE + COMMA_SEP +
                        RadioMap_3.COL_AP3_RSSI + REAL_TYPE + ")";

        public static final String SQL_DELETE_ENTRIES =
                "DELETE FROM " + RadioMap_3.TABLE_NAME;

        public static final String SQL_DROP_TABLE =
                "DROP TABLE IF EXISTS " + RadioMap_3.TABLE_NAME;

        public static final String SQL_SELECT_ALL =
                "SELECT * FROM " + RadioMap_3.TABLE_NAME;
    }

    public static abstract class RadioMapNormalized_3 implements BaseColumns {
        public static final String TABLE_NAME = "radiomap_normalized_3";

        public static final String REAL_TYPE    = " REAL";
        public static final String INTEGER_TYPE = " INTEGER";
        public static final String TEXT_TYPE    = " TEXT";

        public static final String COL_ID_MEASURING = "ID_MEASURING";
        public static final String COL_BEARING      = "BEARING";
        public static final String COL_AP1_ID       = "AP1_ID";
        public static final String COL_AP1_RSSI     = "AP1_RSSI";
        public static final String COL_AP2_ID       = "AP2_ID";
        public static final String COL_AP2_RSSI     = "AP2_RSSI";
        public static final String COL_AP3_ID       = "AP3_ID";
        public static final String COL_AP3_RSSI     = "AP3_RSSI";

        public static final String COMMA_SEP = ", ";

        public static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE IF NOT EXISTS " + RadioMapNormalized_3.TABLE_NAME + " (" +
                        RadioMapNormalized_3._ID + " INTEGER PRIMARY KEY," +
                        RadioMapNormalized_3.COL_ID_MEASURING + INTEGER_TYPE + COMMA_SEP +
                        RadioMapNormalized_3.COL_BEARING + TEXT_TYPE + COMMA_SEP +
                        RadioMapNormalized_3.COL_AP1_ID   + INTEGER_TYPE + COMMA_SEP +
                        RadioMapNormalized_3.COL_AP1_RSSI + REAL_TYPE + COMMA_SEP +
                        RadioMapNormalized_3.COL_AP2_ID   + INTEGER_TYPE + COMMA_SEP +
                        RadioMapNormalized_3.COL_AP2_RSSI + REAL_TYPE + COMMA_SEP +
                        RadioMapNormalized_3.COL_AP3_ID   + INTEGER_TYPE + COMMA_SEP +
                        RadioMapNormalized_3.COL_AP3_RSSI + REAL_TYPE + ")";

        public static final String SQL_DELETE_ENTRIES =
                "DELETE FROM " + RadioMapNormalized_3.TABLE_NAME;

        public static final String SQL_DROP_TABLE =
                "DROP TABLE IF EXISTS " + RadioMapNormalized_3.TABLE_NAME;

        public static final String SQL_SELECT_ALL =
                "SELECT * FROM " + RadioMapNormalized_3.TABLE_NAME;
    }

    public static abstract class RadioMap_4 implements BaseColumns {
        public static final String TABLE_NAME = "radiomap_4";

        public static final String REAL_TYPE    = " REAL";
        public static final String INTEGER_TYPE = " INTEGER";
        public static final String TEXT_TYPE    = " TEXT";

        public static final String COL_ID_MEASURING = "ID_MEASURING";
        public static final String COL_BEARING      = "BEARING";
        public static final String COL_AP1_ID       = "AP1_ID";
        public static final String COL_AP1_RSSI     = "AP1_RSSI";
        public static final String COL_AP2_ID       = "AP2_ID";
        public static final String COL_AP2_RSSI     = "AP2_RSSI";
        public static final String COL_AP3_ID       = "AP3_ID";
        public static final String COL_AP3_RSSI     = "AP3_RSSI";
        public static final String COL_AP4_ID       = "AP4_ID";
        public static final String COL_AP4_RSSI     = "AP4_RSSI";

        public static final String COMMA_SEP = ", ";

        public static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE IF NOT EXISTS " + RadioMap_4.TABLE_NAME + " (" +
                        RadioMap_4._ID + " INTEGER PRIMARY KEY," +
                        RadioMap_4.COL_ID_MEASURING + INTEGER_TYPE + COMMA_SEP +
                        RadioMap_4.COL_BEARING + TEXT_TYPE + COMMA_SEP +
                        RadioMap_4.COL_AP1_ID   + INTEGER_TYPE + COMMA_SEP +
                        RadioMap_4.COL_AP1_RSSI + REAL_TYPE + COMMA_SEP +
                        RadioMap_4.COL_AP2_ID   + INTEGER_TYPE + COMMA_SEP +
                        RadioMap_4.COL_AP2_RSSI + REAL_TYPE + COMMA_SEP +
                        RadioMap_4.COL_AP3_ID   + INTEGER_TYPE + COMMA_SEP +
                        RadioMap_4.COL_AP3_RSSI + REAL_TYPE + COMMA_SEP +
                        RadioMap_4.COL_AP4_ID   + INTEGER_TYPE + COMMA_SEP +
                        RadioMap_4.COL_AP4_RSSI + REAL_TYPE + ")";

        public static final String SQL_DELETE_ENTRIES =
                "DELETE FROM " + RadioMap_4.TABLE_NAME;

        public static final String SQL_DROP_TABLE =
                "DROP TABLE IF EXISTS " + RadioMap_4.TABLE_NAME;

        public static final String SQL_SELECT_ALL =
                "SELECT * FROM " + RadioMap_4.TABLE_NAME;
    }

    public static abstract class RadioMapNormalized_4 implements BaseColumns {
        public static final String TABLE_NAME = "radiomap_normalized_4";

        public static final String REAL_TYPE    = " REAL";
        public static final String INTEGER_TYPE = " INTEGER";
        public static final String TEXT_TYPE    = " TEXT";

        public static final String COL_ID_MEASURING = "ID_MEASURING";
        public static final String COL_BEARING      = "BEARING";
        public static final String COL_AP1_ID       = "AP1_ID";
        public static final String COL_AP1_RSSI     = "AP1_RSSI";
        public static final String COL_AP2_ID       = "AP2_ID";
        public static final String COL_AP2_RSSI     = "AP2_RSSI";
        public static final String COL_AP3_ID       = "AP3_ID";
        public static final String COL_AP3_RSSI     = "AP3_RSSI";
        public static final String COL_AP4_ID       = "AP4_ID";
        public static final String COL_AP4_RSSI     = "AP4_RSSI";

        public static final String COMMA_SEP = ", ";

        public static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE IF NOT EXISTS " + RadioMapNormalized_4.TABLE_NAME + " (" +
                        RadioMapNormalized_4._ID + " INTEGER PRIMARY KEY," +
                        RadioMapNormalized_4.COL_ID_MEASURING + INTEGER_TYPE + COMMA_SEP +
                        RadioMapNormalized_4.COL_BEARING + TEXT_TYPE + COMMA_SEP +
                        RadioMapNormalized_4.COL_AP1_ID   + INTEGER_TYPE + COMMA_SEP +
                        RadioMapNormalized_4.COL_AP1_RSSI + REAL_TYPE + COMMA_SEP +
                        RadioMapNormalized_4.COL_AP2_ID   + INTEGER_TYPE + COMMA_SEP +
                        RadioMapNormalized_4.COL_AP2_RSSI + REAL_TYPE + COMMA_SEP +
                        RadioMapNormalized_4.COL_AP3_ID   + INTEGER_TYPE + COMMA_SEP +
                        RadioMapNormalized_4.COL_AP3_RSSI + REAL_TYPE + COMMA_SEP +
                        RadioMapNormalized_4.COL_AP4_ID   + INTEGER_TYPE + COMMA_SEP +
                        RadioMapNormalized_4.COL_AP4_RSSI + REAL_TYPE + ")";

        public static final String SQL_DELETE_ENTRIES =
                "DELETE FROM " + RadioMapNormalized_4.TABLE_NAME;

        public static final String SQL_DROP_TABLE =
                "DROP TABLE IF EXISTS " + RadioMapNormalized_4.TABLE_NAME;

        public static final String SQL_SELECT_ALL =
                "SELECT * FROM " + RadioMapNormalized_4.TABLE_NAME;
    }

    public static abstract class RadioMap_5 implements BaseColumns {
        public static final String TABLE_NAME = "radiomap_5";

        public static final String REAL_TYPE    = " REAL";
        public static final String INTEGER_TYPE = " INTEGER";
        public static final String TEXT_TYPE    = " TEXT";

        public static final String COL_ID_MEASURING = "ID_MEASURING";
        public static final String COL_BEARING      = "BEARING";
        public static final String COL_AP1_ID       = "AP1_ID";
        public static final String COL_AP1_RSSI     = "AP1_RSSI";
        public static final String COL_AP2_ID       = "AP2_ID";
        public static final String COL_AP2_RSSI     = "AP2_RSSI";
        public static final String COL_AP3_ID       = "AP3_ID";
        public static final String COL_AP3_RSSI     = "AP3_RSSI";
        public static final String COL_AP4_ID       = "AP4_ID";
        public static final String COL_AP4_RSSI     = "AP4_RSSI";
        public static final String COL_AP5_ID       = "AP5_ID";
        public static final String COL_AP5_RSSI     = "AP5_RSSI";

        public static final String COMMA_SEP = ", ";

        public static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE IF NOT EXISTS " + RadioMap_5.TABLE_NAME + " (" +
                        RadioMap_5._ID + " INTEGER PRIMARY KEY," +
                        RadioMap_5.COL_ID_MEASURING + INTEGER_TYPE + COMMA_SEP +
                        RadioMap_5.COL_BEARING + TEXT_TYPE + COMMA_SEP +
                        RadioMap_5.COL_AP1_ID   + INTEGER_TYPE + COMMA_SEP +
                        RadioMap_5.COL_AP1_RSSI + REAL_TYPE + COMMA_SEP +
                        RadioMap_5.COL_AP2_ID   + INTEGER_TYPE + COMMA_SEP +
                        RadioMap_5.COL_AP2_RSSI + REAL_TYPE + COMMA_SEP +
                        RadioMap_5.COL_AP3_ID   + INTEGER_TYPE + COMMA_SEP +
                        RadioMap_5.COL_AP3_RSSI + REAL_TYPE + COMMA_SEP +
                        RadioMap_5.COL_AP4_ID   + INTEGER_TYPE + COMMA_SEP +
                        RadioMap_5.COL_AP4_RSSI + REAL_TYPE + COMMA_SEP +
                        RadioMap_5.COL_AP5_ID   + INTEGER_TYPE + COMMA_SEP +
                        RadioMap_5.COL_AP5_RSSI + REAL_TYPE + ")";

        public static final String SQL_DELETE_ENTRIES =
                "DELETE FROM " + RadioMap_5.TABLE_NAME;

        public static final String SQL_DROP_TABLE =
                "DROP TABLE IF EXISTS " + RadioMap_5.TABLE_NAME;

        public static final String SQL_SELECT_ALL =
                "SELECT * FROM " + RadioMap_5.TABLE_NAME;
    }

    public static abstract class RadioMapNormalized_5 implements BaseColumns {
        public static final String TABLE_NAME = "radiomap_normalized_5";

        public static final String REAL_TYPE    = " REAL";
        public static final String INTEGER_TYPE = " INTEGER";
        public static final String TEXT_TYPE    = " TEXT";

        public static final String COL_ID_MEASURING = "ID_MEASURING";
        public static final String COL_BEARING      = "BEARING";
        public static final String COL_AP1_ID       = "AP1_ID";
        public static final String COL_AP1_RSSI     = "AP1_RSSI";
        public static final String COL_AP2_ID       = "AP2_ID";
        public static final String COL_AP2_RSSI     = "AP2_RSSI";
        public static final String COL_AP3_ID       = "AP3_ID";
        public static final String COL_AP3_RSSI     = "AP3_RSSI";
        public static final String COL_AP4_ID       = "AP4_ID";
        public static final String COL_AP4_RSSI     = "AP4_RSSI";
        public static final String COL_AP5_ID       = "AP5_ID";
        public static final String COL_AP5_RSSI     = "AP5_RSSI";

        public static final String COMMA_SEP = ", ";

        public static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE IF NOT EXISTS " + RadioMapNormalized_5.TABLE_NAME + " (" +
                        RadioMapNormalized_5._ID + " INTEGER PRIMARY KEY," +
                        RadioMapNormalized_5.COL_ID_MEASURING + INTEGER_TYPE + COMMA_SEP +
                        RadioMapNormalized_5.COL_BEARING + TEXT_TYPE + COMMA_SEP +
                        RadioMapNormalized_5.COL_AP1_ID   + INTEGER_TYPE + COMMA_SEP +
                        RadioMapNormalized_5.COL_AP1_RSSI + REAL_TYPE + COMMA_SEP +
                        RadioMapNormalized_5.COL_AP2_ID   + INTEGER_TYPE + COMMA_SEP +
                        RadioMapNormalized_5.COL_AP2_RSSI + REAL_TYPE + COMMA_SEP +
                        RadioMapNormalized_5.COL_AP3_ID   + INTEGER_TYPE + COMMA_SEP +
                        RadioMapNormalized_5.COL_AP3_RSSI + REAL_TYPE + COMMA_SEP +
                        RadioMapNormalized_5.COL_AP4_ID   + INTEGER_TYPE + COMMA_SEP +
                        RadioMapNormalized_5.COL_AP4_RSSI + REAL_TYPE + COMMA_SEP +
                        RadioMapNormalized_5.COL_AP5_ID   + INTEGER_TYPE + COMMA_SEP +
                        RadioMapNormalized_5.COL_AP5_RSSI + REAL_TYPE + ")";

        public static final String SQL_DELETE_ENTRIES =
                "DELETE FROM " + RadioMapNormalized_5.TABLE_NAME;

        public static final String SQL_DROP_TABLE =
                "DROP TABLE IF EXISTS " + RadioMapNormalized_5.TABLE_NAME;

        public static final String SQL_SELECT_ALL =
                "SELECT * FROM " + RadioMapNormalized_5.TABLE_NAME;
    }

    public static abstract class RadioMap_6 implements BaseColumns {
        public static final String TABLE_NAME = "radiomap_6";

        public static final String REAL_TYPE    = " REAL";
        public static final String INTEGER_TYPE = " INTEGER";
        public static final String TEXT_TYPE    = " TEXT";

        public static final String COL_ID_MEASURING = "ID_MEASURING";
        public static final String COL_BEARING      = "BEARING";
        public static final String COL_AP1_ID       = "AP1_ID";
        public static final String COL_AP1_RSSI     = "AP1_RSSI";
        public static final String COL_AP2_ID       = "AP2_ID";
        public static final String COL_AP2_RSSI     = "AP2_RSSI";
        public static final String COL_AP3_ID       = "AP3_ID";
        public static final String COL_AP3_RSSI     = "AP3_RSSI";
        public static final String COL_AP4_ID       = "AP4_ID";
        public static final String COL_AP4_RSSI     = "AP4_RSSI";
        public static final String COL_AP5_ID       = "AP5_ID";
        public static final String COL_AP5_RSSI     = "AP5_RSSI";
        public static final String COL_AP6_ID       = "AP6_ID";
        public static final String COL_AP6_RSSI     = "AP6_RSSI";

        public static final String COMMA_SEP = ", ";

        public static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE IF NOT EXISTS " + RadioMap_6.TABLE_NAME + " (" +
                        RadioMap_6._ID + " INTEGER PRIMARY KEY," +
                        RadioMap_6.COL_ID_MEASURING + INTEGER_TYPE + COMMA_SEP +
                        RadioMap_6.COL_BEARING + TEXT_TYPE + COMMA_SEP +
                        RadioMap_6.COL_AP1_ID   + INTEGER_TYPE + COMMA_SEP +
                        RadioMap_6.COL_AP1_RSSI + REAL_TYPE + COMMA_SEP +
                        RadioMap_6.COL_AP2_ID   + INTEGER_TYPE + COMMA_SEP +
                        RadioMap_6.COL_AP2_RSSI + REAL_TYPE + COMMA_SEP +
                        RadioMap_6.COL_AP3_ID   + INTEGER_TYPE + COMMA_SEP +
                        RadioMap_6.COL_AP3_RSSI + REAL_TYPE + COMMA_SEP +
                        RadioMap_6.COL_AP4_ID   + INTEGER_TYPE + COMMA_SEP +
                        RadioMap_6.COL_AP4_RSSI + REAL_TYPE + COMMA_SEP +
                        RadioMap_6.COL_AP5_ID   + INTEGER_TYPE + COMMA_SEP +
                        RadioMap_6.COL_AP5_RSSI + REAL_TYPE + COMMA_SEP +
                        RadioMap_6.COL_AP6_ID   + INTEGER_TYPE + COMMA_SEP +
                        RadioMap_6.COL_AP6_RSSI + REAL_TYPE + ")";

        public static final String SQL_DELETE_ENTRIES =
                "DELETE FROM " + RadioMap_6.TABLE_NAME;

        public static final String SQL_DROP_TABLE =
                "DROP TABLE IF EXISTS " + RadioMap_6.TABLE_NAME;

        public static final String SQL_SELECT_ALL =
                "SELECT * FROM " + RadioMap_6.TABLE_NAME;
    }

    public static abstract class RadioMapNormalized_6 implements BaseColumns {
        public static final String TABLE_NAME = "radiomap_normalized_6";

        public static final String REAL_TYPE    = " REAL";
        public static final String INTEGER_TYPE = " INTEGER";
        public static final String TEXT_TYPE    = " TEXT";

        public static final String COL_ID_MEASURING = "ID_MEASURING";
        public static final String COL_BEARING      = "BEARING";
        public static final String COL_AP1_ID       = "AP1_ID";
        public static final String COL_AP1_RSSI     = "AP1_RSSI";
        public static final String COL_AP2_ID       = "AP2_ID";
        public static final String COL_AP2_RSSI     = "AP2_RSSI";
        public static final String COL_AP3_ID       = "AP3_ID";
        public static final String COL_AP3_RSSI     = "AP3_RSSI";
        public static final String COL_AP4_ID       = "AP4_ID";
        public static final String COL_AP4_RSSI     = "AP4_RSSI";
        public static final String COL_AP5_ID       = "AP5_ID";
        public static final String COL_AP5_RSSI     = "AP5_RSSI";
        public static final String COL_AP6_ID       = "AP6_ID";
        public static final String COL_AP6_RSSI     = "AP6_RSSI";

        public static final String COMMA_SEP = ", ";

        public static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE IF NOT EXISTS " + RadioMapNormalized_6.TABLE_NAME + " (" +
                        RadioMapNormalized_6._ID + " INTEGER PRIMARY KEY," +
                        RadioMapNormalized_6.COL_ID_MEASURING + INTEGER_TYPE + COMMA_SEP +
                        RadioMapNormalized_6.COL_BEARING + TEXT_TYPE + COMMA_SEP +
                        RadioMapNormalized_6.COL_AP1_ID   + INTEGER_TYPE + COMMA_SEP +
                        RadioMapNormalized_6.COL_AP1_RSSI + REAL_TYPE + COMMA_SEP +
                        RadioMapNormalized_6.COL_AP2_ID   + INTEGER_TYPE + COMMA_SEP +
                        RadioMapNormalized_6.COL_AP2_RSSI + REAL_TYPE + COMMA_SEP +
                        RadioMapNormalized_6.COL_AP3_ID   + INTEGER_TYPE + COMMA_SEP +
                        RadioMapNormalized_6.COL_AP3_RSSI + REAL_TYPE + COMMA_SEP +
                        RadioMapNormalized_6.COL_AP4_ID   + INTEGER_TYPE + COMMA_SEP +
                        RadioMapNormalized_6.COL_AP4_RSSI + REAL_TYPE + COMMA_SEP +
                        RadioMapNormalized_6.COL_AP5_ID   + INTEGER_TYPE + COMMA_SEP +
                        RadioMapNormalized_6.COL_AP5_RSSI + REAL_TYPE + COMMA_SEP +
                        RadioMapNormalized_6.COL_AP6_ID   + INTEGER_TYPE + COMMA_SEP +
                        RadioMapNormalized_6.COL_AP6_RSSI + REAL_TYPE + ")";

        public static final String SQL_DELETE_ENTRIES =
                "DELETE FROM " + RadioMapNormalized_6.TABLE_NAME;

        public static final String SQL_DROP_TABLE =
                "DROP TABLE IF EXISTS " + RadioMapNormalized_6.TABLE_NAME;

        public static final String SQL_SELECT_ALL =
                "SELECT * FROM " + RadioMapNormalized_6.TABLE_NAME;
    }

    public static abstract class MeasuringPoints implements BaseColumns {
        public static final String TABLE_NAME = "MeasuringPoints";

        public static final String REAL_TYPE    = " REAL";
        public static final String TEXT_TYPE    = " TEXT";

        public static final String COL_LAT      = "LAT";
        public static final String COL_LNG      = "LNG";
        public static final String COL_NAME     = "NAME";

        public static final String COMMA_SEP    = ", ";

        public static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE IF NOT EXISTS " + MeasuringPoints.TABLE_NAME + " (" +
                        MeasuringPoints._ID + " INTEGER PRIMARY KEY," +
                        MeasuringPoints.COL_NAME + TEXT_TYPE + COMMA_SEP +
                        MeasuringPoints.COL_LAT   + REAL_TYPE + COMMA_SEP +
                        MeasuringPoints.COL_LNG + REAL_TYPE + ")";

        public static final String SQL_DELETE_ENTRIES =
                "DELETE FROM " + MeasuringPoints.TABLE_NAME;

        public static final String SQL_DROP_TABLE =
                "DROP TABLE IF EXISTS " + MeasuringPoints.TABLE_NAME;

        public static final String SQL_SELECT_ALL =
                "SELECT * FROM " + MeasuringPoints.TABLE_NAME;
    }

    /**
     * Creates a db query to check if a table contains all aps.
     *      row: ap1_id 1, ap2_id 2, ap3_id 3
     *      ids: 2, 1, 3
     *      query:  SELECT COUNT(*) FROM radiomap_3
     *              WHERE ap1_id in (2, 1, 3) AND
     *                  ap2_id in (2, 1, 3) AND
     *                  ap3_id in (2, 1, 3)
     *
     * @param ids of base stations to check.
     * @param tableName to look in.
     * @return sql query string.
     */
    public static String tableContainsAps(String tableName, ArrayList<Integer> ids) {
        String sIds = Arrays.toString(ids.toArray(new Integer[ids.size()]));
        String returnValues = String.format("SELECT _id FROM %s \n" +
                "WHERE ", tableName);

        for (int i = 0; i < ids.size(); i++) {
            returnValues += String.format("AP%s_id in (%s)", i, sIds);
            if (i != ids.size() - 1)
                returnValues += " AND ";
        }

        return returnValues;
    }
}
