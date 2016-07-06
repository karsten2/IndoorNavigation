package com.indoornavigation.Helper;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Class with helper functions.
 */
public abstract class Utils {

    /**
     * Function to start a service.
     *
     * @param cls The service's class.
     */
    public static void startService(Class<?> cls, Activity activity) {
        if (!serviceIsRunning(cls, activity)) {
            activity.startService(new Intent(activity, cls));
            Log.d("Utils Service", "Service Started");
        }
    }

    /**
     * Function to stop a service.
     *
     * @param cls The service's class.
     */
    public static void stopService(Class<?> cls, Activity activity) {
        if (serviceIsRunning(cls, activity)) {
            activity.stopService(new Intent(activity, cls));
            Log.d("Utils Service", "Service Stopped");
        }
    }

    /**
     * Function to check if a Service is running.
     *
     * @param serviceClass The service's class.
     * @return true if running, else false.
     */
    private static boolean serviceIsRunning(Class<?> serviceClass, Activity activity) {
        ActivityManager manager =
                (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return date in specified format.
     *
     * @param milliSeconds Date in milliseconds
     * @param dateFormat   Date format
     * @return String representing date in specified format
     */
    public static String getDate(long milliSeconds, String dateFormat) {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    /**
     * Create a backup of the database.
     *
     * @param context who calls the function.
     * @param dbName Name of the database to backup. (If empty, radiomap.db is used).
     */
    public static void exportDb(Context context, String dbName) {
        try {
            if (dbName.equals(""))
                dbName = "radiomap.db";

            File sd = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String currentDBPath = "//data//" + context.getApplicationContext().getPackageName() + "//databases//"
                        + dbName;
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, dbName);

                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();

                Toast.makeText(context, "Datenbank nach Downloads exportiert!", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e("Utils", e.getMessage());
            Toast.makeText(context, "Exportieren fehlgeschlagen!", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Create a backup of the database.
     *
     * @param context who calls the function.
     * @param dbName Name of the database to backup. (If empty, radiomap.db is used).
     */
    public static void importDb(Context context, String dbName) {
        try {
            if (dbName.equals(""))
                dbName = "radiomap.db";

            File sd = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String currentDBPath = "//data//" + context.getApplicationContext().getPackageName() + "//databases//"
                        + dbName;
                File backupDB = new File(data, currentDBPath);
                File currentDB = new File(sd, dbName);

                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();

                Toast.makeText(context, "Datenbank von Downloads importiert!", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e("Utils", e.getMessage());
            Toast.makeText(context, "Import fehlgeschlagen!", Toast.LENGTH_LONG).show();
        }
    }
}
