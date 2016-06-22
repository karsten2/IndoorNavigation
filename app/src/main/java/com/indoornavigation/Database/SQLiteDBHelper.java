package com.indoornavigation.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

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
     * @param context that calls the dbHelper class.
     */
    public SQLiteDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        SQLiteDatabase db = this.getWritableDatabase();

        Log.d("sql", "Creating database tables.");
        db.execSQL(DbTables.RadioMap.SQL_CREATE_ENTRIES);
        db.execSQL(DbTables.ApRegressionValues.SQL_CREATE_ENTRIES);
        db.execSQL(DbTables.RadioMap_3.SQL_CREATE_ENTRIES);
        db.execSQL(DbTables.RadioMap_4.SQL_CREATE_ENTRIES);
        db.execSQL(DbTables.RadioMap_5.SQL_CREATE_ENTRIES);
        db.execSQL(DbTables.RadioMap_6.SQL_CREATE_ENTRIES);
    }

    @Override
    public void onCreate(SQLiteDatabase db) { }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DbTables.RadioMap.SQL_DELETE_ENTRIES);
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
     * @param tableName             Name of the table to insert data.
     * @param columnNameNullable    // TODO
     * @param colValues             Key-Value pairs: Key(Column) -> Value
     * @return                      Number of inserted rows.
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
     * @param tableName         Name of the table to update.
     * @param colValues         Key-Value pairs: Key(Column) -> Value
     * @param where             Columns for where. e.g "col1 LIKE ?, col2 = ?" (? for where args)
     * @param whereArgs         Array of arguments for where part.
     * @return                  Number of updated rows.
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
     *
     * @param tableName         Name of the table to query.
     * @param columns           Columns to query e.g. {"col1", "col2", ...}
     * @param whereColumns      Columns for where. e.g "col1 LIKE ?, col2 = ?" (? for where args)
     * @param whereArgs         Array of arguments for where part.
     * @param groupBy           Columns to group.
     * @param filter            // TODO
     * @param sort              Columns to sort the result.
     * @return                  Cursor with query results.
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
     * @param tableName     Name of the table.
     * @param where         Column + command e.g: "col1 LIKE ?" (? for where args)
     * @param whereArgs     Array of Arguments for where columns
     * @return              Number of rows deleted.
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
     * @param query     sql-String to drop a table. See SQL_DROP_TABLE in DbTables.Radiomap.
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
     * @param query     sql-String to create a table. See SQL_CREATE_ENTRIES in DbTables.Radiomap.
     */
    public void createTable(String query) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            Log.d("sql", query);
            db.execSQL(query);
        } catch (SQLiteException e) {
            Log.e(TAG, "Error while creating table:\n" + e.getMessage());
        }
    }

    /**
     * Function to do a raw sql query.
     * @param query     sql-String.
     * @return          Cursor with the query result.
     */
    public Cursor rawQuery(String query) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(query, null);
    }

    /**
     * Function to check if a table with a given name exists in the database.
     * @param tableName     String with the table name.
     * @return              true if exists, else false.
     */
    public boolean tableExists(String tableName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name='" + tableName + "'",
                null);

        if (cursor.getCount() <= 0)
            return false;

        return true;

    }


}
