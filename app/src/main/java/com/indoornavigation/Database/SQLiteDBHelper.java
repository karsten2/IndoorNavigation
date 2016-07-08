package com.indoornavigation.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.indoornavigation.Model.CustomVector;

import java.util.ArrayList;
import java.util.Map;

/**
 * Class for Database handling.
 * Creates database tables.
 * Provides functions for CRUD-Operations and raw sql queries.
 * Requires Context.
 */
public class SQLiteDBHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "radiomap.db";
    private final String TAG = "sqlite";

    /**
     * Constructor will create all database tables, if not exists.
     *
     * @param context that calls the dbHelper class.
     */
    public SQLiteDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        SQLiteDatabase db = this.getWritableDatabase();

        Log.d("sql", "Creating database tables.");
        db.execSQL(DbTables.BaseStation.SQL_CREATE_ENTRIES);
        db.execSQL(DbTables.Radiomap_3.SQL_CREATE_ENTRIES);
        db.execSQL(DbTables.Radiomap_4.SQL_CREATE_ENTRIES);
        db.execSQL(DbTables.Radiomap_5.SQL_CREATE_ENTRIES);
        db.execSQL(DbTables.Radiomap_6.SQL_CREATE_ENTRIES);
        db.execSQL(DbTables.MeasuringPoint.SQL_CREATE_ENTRIES);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DbTables.BaseStation.SQL_DELETE_ENTRIES);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onDowngrade(db, oldVersion, newVersion);
    }

    /**
     * ###########################
     * # CRUD database functions #
     * ###########################
     */

    /**
     * Function to insert data into a db table.
     *
     * @param tableName          Name of the table to insert data.
     * @param columnNameNullable // TODO
     * @param colValues          Key-Value pairs: Key(Column) -> Value
     * @return Number of inserted rows.
     */
    public long sqlInsert(String tableName,
                          String columnNameNullable,
                          ContentValues colValues) {
        long ret = -1;

        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ret = db.insertOrThrow(tableName, columnNameNullable, colValues);
        } catch (SQLiteException e) {
            Log.e(TAG, "Error while inserting:\n" + e.getMessage());
        }

        return ret;
    }

    /**
     * Function to update a table.
     *
     * @param tableName Name of the table to update.
     * @param colValues Key-Value pairs: Key(Column) -> Value
     * @param where     Columns for where. e.g "col1 LIKE ?, col2 = ?" (? for where args)
     * @param whereArgs Array of arguments for where part.
     * @return Number of updated rows.
     */
    public long sqlUpdate(String tableName,
                          Map<String, String> colValues,
                          String where,
                          String whereArgs[]) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        for (String key : colValues.keySet()) {
            values.put(key, colValues.get(key));
        }

        return db.update(tableName, values, where, whereArgs);
    }

    /**
     * @param tableName    Name of the table to query.
     * @param columns      Columns to query e.g. {"col1", "col2", ...}
     * @param whereColumns Columns for where. e.g "col1 LIKE ?, col2 = ?" (? for where args)
     * @param whereArgs    Array of arguments for where part.
     * @param groupBy      Columns to group.
     * @param filter       // TODO
     * @param sort         Columns to sort the result.
     * @return Cursor with query results.
     */
    public Cursor sqlSelect(String tableName,
                            String columns[],
                            String whereColumns,
                            String whereArgs[],
                            String groupBy,
                            String filter,
                            String sort) {

        SQLiteDatabase db = this.getReadableDatabase();

        return db.query(tableName,
                columns,
                whereColumns,
                whereArgs,
                groupBy,
                filter,
                sort);
    }

    /**
     * Function to delete specific rows of a table.
     *
     * @param tableName Name of the table.
     * @param where     Column + command e.g: "col1 LIKE ?" (? for where args)
     * @param whereArgs Array of Arguments for where columns
     * @return Number of rows deleted.
     */
    public int sqlDelete(String tableName, String where, String whereArgs[]) {
        int ret = -1;
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            ret = db.delete(tableName, where, whereArgs);
        } catch (SQLException e) {
            Log.e(TAG, "Error while deleting:\n" + e.getMessage());
        }
        return ret;
    }

    /**
     * Function to drop a table.
     *
     * @param query sql-String to drop a table. See SQL_DROP_TABLE in DbTables.Radiomap.
     */
    public void dropTable(String query) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            db.execSQL(query);
        } catch (SQLiteException e) {
            Log.e(TAG, "Error while dropping table:\n" + e.getMessage());
        }
    }

    /**
     * Function to create a table.
     *
     * @param query sql-String to create a table. See SQL_CREATE_ENTRIES in DbTables.Radiomap.
     */
    public void createTable(String query) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            Log.d("sql", " " + query);
            db.execSQL(query);
        } catch (SQLiteException e) {
            Log.e(TAG, "Error while creating table:\n" + e.getMessage());
        }
    }

    /**
     * Function to do a raw sql query.
     *
     * @param query sql-String.
     * @return Cursor with the query result.
     */
    public Cursor rawQuery(String query) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(query, null);
    }

    /**
     * Function to check if a table with a given name exists in the database.
     *
     * @param tableName String with the table name.
     * @return true if exists, else false.
     */
    public boolean tableExists(String tableName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name='" + tableName + "'",
                null);

        cursor.close();
        return cursor.getCount() > 0;

    }

    /**
     * Function to add a scanresult to the database, to create the radiomap.
     *
     * @param baseStation : Data to write to the database.
     * @return Database cursor code.
     */
    public long addBaseStation(com.indoornavigation.Model.BaseStation baseStation) {
        return this.sqlInsert(DbTables.BaseStation.TABLE_NAME,
                null, baseStation.toDbValues());
    }

    /**
     * Adding a measuring point to the database.
     *
     * @param p Point as LatLng.
     * @return Database cursor code.
     */
    public long addMeasuringPoint(LatLng p, String name) {
        ContentValues values = new ContentValues();

        values.put(DbTables.MeasuringPoint.COL_LAT, p.latitude);
        values.put(DbTables.MeasuringPoint.COL_LNG, p.longitude);
        values.put(DbTables.MeasuringPoint.COL_NAME, name);

        return this.sqlInsert(DbTables.MeasuringPoint.TABLE_NAME, null, values);
    }

    /**
     * Gets all measuring points from the database.
     * @return Hashmap with all measuring points key: name, value: latlng.
     */
    public ArrayList<com.indoornavigation.Model.MeasuringPoint> getMeasuringPoints() {
        ArrayList<com.indoornavigation.Model.MeasuringPoint> dbValues = new ArrayList<>();
        Cursor c = this.rawQuery(DbTables.MeasuringPoint.SQL_SELECT_ALL);

        if (c.moveToFirst()) {
            do {
                int id = c.getInt(c.getColumnIndexOrThrow("_id"));
                String name = c.getString(c.getColumnIndexOrThrow("NAME"));
                double lat = c.getDouble(c.getColumnIndexOrThrow("LAT"));
                double lng = c.getDouble(c.getColumnIndexOrThrow("LNG"));
                dbValues.add(new com.indoornavigation.Model.MeasuringPoint(name, new LatLng(lat, lng), id));
            } while (c.moveToNext());
        }

        c.close();

        return dbValues;
    }

    /**
     * Get all base stations from the database.
     * @return ArrayList with all found base stations.
     */
    public ArrayList<com.indoornavigation.Model.BaseStation> getBaseStations() {

        ArrayList<com.indoornavigation.Model.BaseStation> ret = new ArrayList<>();
        Cursor cursor = this.sqlSelect(DbTables.BaseStation.TABLE_NAME,
                null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                com.indoornavigation.Model.BaseStation baseStation = new com.indoornavigation.Model.BaseStation();
                baseStation.setSsid(cursor.getString(cursor.getColumnIndexOrThrow("SSID")));
                baseStation.setBssid(cursor.getString(cursor.getColumnIndexOrThrow("BSSID")));

                double lat = cursor.getDouble(cursor.getColumnIndexOrThrow(
                        DbTables.BaseStation.COL_LAT));
                double lng = cursor.getDouble(cursor.getColumnIndexOrThrow(
                        DbTables.BaseStation.COL_LNG));

                try {
                    double rssi_1m = cursor.getDouble(cursor.getColumnIndexOrThrow(
                            DbTables.BaseStation.COL_RSSI1M));
                    baseStation.setRss1_1m(rssi_1m);

                    double _const = cursor.getDouble(cursor.getColumnIndexOrThrow(
                            DbTables.BaseStation.COL_CONST));
                    baseStation.setLat_const(_const);
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, e.getMessage());
                }

                int id = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
                baseStation.setDbId(id);

                baseStation.setLatLng(new LatLng(lat, lng));

                ret.add(baseStation);

            } while (cursor.moveToNext());
        }

        cursor.close();

        return ret;
    }

    /**
     * Check if a query has data.
     * @param query Data base query as string.
     * @return true if cursor has data, else false.
     */
    public boolean hasData(String query) {
        try (Cursor c = this.rawQuery(query)) {
            return c != null && c.moveToFirst();
        } finally {
            close();
        }
    }

    /**
     * Creates a Hashmap that contains the LatLng as key and n rss values as normalized vector.
     *      12.12345 51.12345 -> {0, -2, 3}
     * @param size number of found base stations. Indicates what db table to use.
     * @return Hashmap with coordinates and vectors.
     */
    public ArrayList<CustomVector> getVectorTable(int size, double bearing) {
        ArrayList<CustomVector> returnValue = new ArrayList<>();

        try {
            Cursor c = this.rawQuery(String.format("SELECT * FROM radiomap_%s r " +
                    "WHERE r._id IN (" +
                    "SELECT _id FROM radiomap_%1$s r2 " +
                    "WHERE r.ID_MEASURING = r2.ID_MEASURING " +
                    "ORDER BY abs(r2.BEARING - %s) LIMIT  1)", size, bearing));

            if (c != null && c.moveToFirst()) {
                do {
                    LatLng latLng = getLatLng(c.getInt(c.getColumnIndexOrThrow("ID_MEASURING")));
                    ArrayList<Double> rss = new ArrayList<>();
                    for (int i = 1; i <= size; i++) {
                        rss.add(c.getDouble(c.getColumnIndexOrThrow(String.format("AP%s_RSSI", i))));
                    }
                    returnValue.add(new CustomVector(latLng, rss));

                } while (c.moveToNext());
            }

            if (c != null) {
                c.close();
            }
        } catch (IllegalArgumentException e) {
            Log.e(TAG, e.getMessage());
        }

        return returnValue;
    }

    /**
     * Get latlng from the measuring table.
     * @param id of the measuring.
     * @return LatLng from the measuring point.
     */
    public LatLng getLatLng (int id) {
        LatLng returnValue = new LatLng(0, 0);

        Cursor c = this.rawQuery(
                String.format("SELECT LAT, LNG FROM %s WHERE _id = %s",
                        DbTables.MeasuringPoint.TABLE_NAME, id));

        if (c != null && c.moveToFirst()) {
            double lat = c.getDouble(c.getColumnIndexOrThrow("LAT"));
            double lng = c.getDouble(c.getColumnIndexOrThrow("LNG"));
            returnValue = new LatLng(lat, lng);
        }

        if (c != null) {
            c.close();
        }

        return returnValue;
    }
}
