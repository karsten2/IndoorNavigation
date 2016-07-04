package com.indoornavigation.Database;

import android.provider.BaseColumns;
import android.text.TextUtils;

import java.util.ArrayList;


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
    public static abstract class BaseStation implements BaseColumns {
        public static final String TABLE_NAME   = "basestation";

        public static final String TEXT_TYPE    = " TEXT";
        public static final String REAL_TYPE    = " REAL";

        public static final String COL_SSID     = "SSID";
        public static final String COL_BSSID    = "BSSID";
        public static final String COL_LAT      = "LAT";
        public static final String COL_LNG      = "LNG";

        public static final String COMMA_SEP = ",";

        public static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE IF NOT EXISTS " + BaseStation.TABLE_NAME + " (" +
                        BaseStation._ID + " INTEGER PRIMARY KEY," +
                        BaseStation.COL_SSID    + TEXT_TYPE + " UNIQUE" + COMMA_SEP +
                        BaseStation.COL_BSSID   + TEXT_TYPE + COMMA_SEP +
                        BaseStation.COL_LAT     + REAL_TYPE + " NOT NULL " + COMMA_SEP +
                        BaseStation.COL_LNG     + REAL_TYPE + " NOT NULL " + ")";

        public static final String SQL_DELETE_ENTRIES =
                "DELETE FROM " + BaseStation.TABLE_NAME;

        public static final String SQL_DROP_TABLE =
                "DROP TABLE IF EXISTS " + BaseStation.TABLE_NAME;

        public static final String SQL_SELECT_ALL =
                "SELECT * FROM " + BaseStation.TABLE_NAME;
    }

    public static abstract class Radiomap_3 implements BaseColumns {
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
                "CREATE TABLE IF NOT EXISTS " + Radiomap_3.TABLE_NAME + " (" +
                        Radiomap_3._ID + " INTEGER PRIMARY KEY," +
                        Radiomap_3.COL_ID_MEASURING + INTEGER_TYPE + COMMA_SEP +
                        Radiomap_3.COL_BEARING + TEXT_TYPE + COMMA_SEP +
                        Radiomap_3.COL_AP1_ID   + INTEGER_TYPE + COMMA_SEP +
                        Radiomap_3.COL_AP1_RSSI + REAL_TYPE + COMMA_SEP +
                        Radiomap_3.COL_AP2_ID   + INTEGER_TYPE + COMMA_SEP +
                        Radiomap_3.COL_AP2_RSSI + REAL_TYPE + COMMA_SEP +
                        Radiomap_3.COL_AP3_ID   + INTEGER_TYPE + COMMA_SEP +
                        Radiomap_3.COL_AP3_RSSI + REAL_TYPE + ")";

        public static final String SQL_DELETE_ENTRIES =
                "DELETE FROM " + Radiomap_3.TABLE_NAME;

        public static final String SQL_DROP_TABLE =
                "DROP TABLE IF EXISTS " + Radiomap_3.TABLE_NAME;

        public static final String SQL_SELECT_ALL =
                "SELECT * FROM " + Radiomap_3.TABLE_NAME;
    }

    public static abstract class Radiomap_4 implements BaseColumns {
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
                "CREATE TABLE IF NOT EXISTS " + Radiomap_4.TABLE_NAME + " (" +
                        Radiomap_4._ID + " INTEGER PRIMARY KEY," +
                        Radiomap_4.COL_ID_MEASURING + INTEGER_TYPE + COMMA_SEP +
                        Radiomap_4.COL_BEARING + TEXT_TYPE + COMMA_SEP +
                        Radiomap_4.COL_AP1_ID   + INTEGER_TYPE + COMMA_SEP +
                        Radiomap_4.COL_AP1_RSSI + REAL_TYPE + COMMA_SEP +
                        Radiomap_4.COL_AP2_ID   + INTEGER_TYPE + COMMA_SEP +
                        Radiomap_4.COL_AP2_RSSI + REAL_TYPE + COMMA_SEP +
                        Radiomap_4.COL_AP3_ID   + INTEGER_TYPE + COMMA_SEP +
                        Radiomap_4.COL_AP3_RSSI + REAL_TYPE + COMMA_SEP +
                        Radiomap_4.COL_AP4_ID   + INTEGER_TYPE + COMMA_SEP +
                        Radiomap_4.COL_AP4_RSSI + REAL_TYPE + ")";

        public static final String SQL_DELETE_ENTRIES =
                "DELETE FROM " + Radiomap_4.TABLE_NAME;

        public static final String SQL_DROP_TABLE =
                "DROP TABLE IF EXISTS " + Radiomap_4.TABLE_NAME;

        public static final String SQL_SELECT_ALL =
                "SELECT * FROM " + Radiomap_4.TABLE_NAME;
    }

    public static abstract class Radiomap_5 implements BaseColumns {
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
                "CREATE TABLE IF NOT EXISTS " + Radiomap_5.TABLE_NAME + " (" +
                        Radiomap_5._ID + " INTEGER PRIMARY KEY," +
                        Radiomap_5.COL_ID_MEASURING + INTEGER_TYPE + COMMA_SEP +
                        Radiomap_5.COL_BEARING + TEXT_TYPE + COMMA_SEP +
                        Radiomap_5.COL_AP1_ID   + INTEGER_TYPE + COMMA_SEP +
                        Radiomap_5.COL_AP1_RSSI + REAL_TYPE + COMMA_SEP +
                        Radiomap_5.COL_AP2_ID   + INTEGER_TYPE + COMMA_SEP +
                        Radiomap_5.COL_AP2_RSSI + REAL_TYPE + COMMA_SEP +
                        Radiomap_5.COL_AP3_ID   + INTEGER_TYPE + COMMA_SEP +
                        Radiomap_5.COL_AP3_RSSI + REAL_TYPE + COMMA_SEP +
                        Radiomap_5.COL_AP4_ID   + INTEGER_TYPE + COMMA_SEP +
                        Radiomap_5.COL_AP4_RSSI + REAL_TYPE + COMMA_SEP +
                        Radiomap_5.COL_AP5_ID   + INTEGER_TYPE + COMMA_SEP +
                        Radiomap_5.COL_AP5_RSSI + REAL_TYPE + ")";

        public static final String SQL_DELETE_ENTRIES =
                "DELETE FROM " + Radiomap_5.TABLE_NAME;

        public static final String SQL_DROP_TABLE =
                "DROP TABLE IF EXISTS " + Radiomap_5.TABLE_NAME;

        public static final String SQL_SELECT_ALL =
                "SELECT * FROM " + Radiomap_5.TABLE_NAME;
    }

    public static abstract class Radiomap_6 implements BaseColumns {
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
                "CREATE TABLE IF NOT EXISTS " + Radiomap_6.TABLE_NAME + " (" +
                        Radiomap_6._ID + " INTEGER PRIMARY KEY," +
                        Radiomap_6.COL_ID_MEASURING + INTEGER_TYPE + COMMA_SEP +
                        Radiomap_6.COL_BEARING + TEXT_TYPE + COMMA_SEP +
                        Radiomap_6.COL_AP1_ID   + INTEGER_TYPE + COMMA_SEP +
                        Radiomap_6.COL_AP1_RSSI + REAL_TYPE + COMMA_SEP +
                        Radiomap_6.COL_AP2_ID   + INTEGER_TYPE + COMMA_SEP +
                        Radiomap_6.COL_AP2_RSSI + REAL_TYPE + COMMA_SEP +
                        Radiomap_6.COL_AP3_ID   + INTEGER_TYPE + COMMA_SEP +
                        Radiomap_6.COL_AP3_RSSI + REAL_TYPE + COMMA_SEP +
                        Radiomap_6.COL_AP4_ID   + INTEGER_TYPE + COMMA_SEP +
                        Radiomap_6.COL_AP4_RSSI + REAL_TYPE + COMMA_SEP +
                        Radiomap_6.COL_AP5_ID   + INTEGER_TYPE + COMMA_SEP +
                        Radiomap_6.COL_AP5_RSSI + REAL_TYPE + COMMA_SEP +
                        Radiomap_6.COL_AP6_ID   + INTEGER_TYPE + COMMA_SEP +
                        Radiomap_6.COL_AP6_RSSI + REAL_TYPE + ")";

        public static final String SQL_DELETE_ENTRIES =
                "DELETE FROM " + Radiomap_6.TABLE_NAME;

        public static final String SQL_DROP_TABLE =
                "DROP TABLE IF EXISTS " + Radiomap_6.TABLE_NAME;

        public static final String SQL_SELECT_ALL =
                "SELECT * FROM " + Radiomap_6.TABLE_NAME;
    }

    public static abstract class MeasuringPoint implements BaseColumns {
        public static final String TABLE_NAME   = "measuringpoint";

        public static final String REAL_TYPE    = " REAL";
        public static final String TEXT_TYPE    = " TEXT";

        public static final String COL_LAT      = "LAT";
        public static final String COL_LNG      = "LNG";
        public static final String COL_NAME     = "NAME";

        public static final String COMMA_SEP    = ", ";

        public static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE IF NOT EXISTS " + MeasuringPoint.TABLE_NAME + " (" +
                        MeasuringPoint._ID          + " INTEGER PRIMARY KEY," +
                        MeasuringPoint.COL_NAME     + TEXT_TYPE + COMMA_SEP +
                        MeasuringPoint.COL_LAT      + REAL_TYPE + " NOT NULL " + COMMA_SEP +
                        MeasuringPoint.COL_LNG      + REAL_TYPE + " NOT NULL " + ")";

        public static final String SQL_DELETE_ENTRIES =
                "DELETE FROM " + MeasuringPoint.TABLE_NAME;

        public static final String SQL_DROP_TABLE =
                "DROP TABLE IF EXISTS " + MeasuringPoint.TABLE_NAME;

        public static final String SQL_SELECT_ALL =
                "SELECT * FROM " + MeasuringPoint.TABLE_NAME;
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
        String sIds = TextUtils.join(", ", ids);
        String returnValues = String.format("SELECT _id FROM %s \n" +
                "WHERE ", tableName);

        for (int i = 1; i <= ids.size(); i++) {
            returnValues += String.format("AP%s_ID in (%s)", i, sIds);
            if (i != ids.size())
                returnValues += " AND ";
        }

        return returnValues;
    }
}
