package com.indoornavigation.Helper;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Write stuff to phones storage.
 */
public abstract class Utils {

    private static String path = Environment
            .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
    private static String filename = "data.dat";

    public static void Serialize(Context context, String fileName, ArrayList<ScanResult> object) {
        try {

            File file = new File(path + filename);
            if (file.exists())
                file.delete();
            file.createNewFile();

            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(object);
            os.close();
            fos.close();
            Log.d("Global Serialize", "Serialized");
        } catch (IOException e) {
            Log.e("Global Serialization", e.toString());
        }
    }

    public static ArrayList<ScanResult> Deserialize (Context context, String fileName) {
        try {
            //File file = context.getFileStreamPath(fileName);
            File file = new File(path + filename);
            if (file == null || !file.exists()) {
                Log.d("Global Serialization", "File not found");
            } else {
                //FileInputStream fis = context.openFileInput(fileName);
                FileInputStream fis = new FileInputStream(file);
                ObjectInputStream is = new ObjectInputStream(fis);
                ArrayList<ScanResult> retObj = (ArrayList) is.readObject();
                is.close();
                Log.d("Global Deserialize", "Deserialized");
                return retObj;

            }
        } catch (IOException e) {
            Log.e("Global Deserialization", e.getMessage());
        } catch (ClassNotFoundException e) {
            Log.e("Global Deserialization", e.getMessage());
        }

        return new ArrayList<>();
    }

    /**
     * Function to start a service.
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
     * @param milliSeconds Date in milliseconds
     * @param dateFormat Date format
     * @return String representing date in specified format
     */
    public static String getDate(long milliSeconds, String dateFormat)
    {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    /**
     * Function to calculate the distance in meters from dbm rssi values.
     * http://rvmiller.com/2013/05/part-1-wifi-based-trilateration-on-android/
     *
     * @param levelInDb RSSI value.
     * @param freqInMHz Frequency of the sending device.
     * @return Distance in meters.
     */
    public static double calculateDistance(double levelInDb, double freqInMHz) {
        double exp = (27.55 - (20 * Math.log10(freqInMHz)) + Math.abs(levelInDb)) / 20.0;
        return Math.pow(10.0, exp);
    }

    public static ArrayList<Double> normalizeVector(ArrayList<Double> vector) {
        ArrayList<Double> returnValue = new ArrayList<>();

        if (vector.size() > 0) {
            Double first = vector.get(0);
            for (Double d : vector) {
                returnValue.add(d - first);
            }
        }

        return returnValue;
    }

    /**
     * Subtract to vectors.
     * The size of v1 and v2 must be equal.
     *
     * @param v1 ArrayList with double values.
     * @param v2 ArrayList with double values.
     * @return new vector form v1 and v2.
     */
    public static ArrayList<Double> subtractVector(ArrayList<Double> v1, ArrayList<Double> v2) {
        ArrayList<Double> returnValue  = new ArrayList<>();

        if (v1.size() == v2.size()) {
            for (int i = 0; i < v1.size(); i ++) {
                returnValue.add(v1.get(i) - v2.get(i));
            }
        }

        return returnValue;
    }

    /**
     * Get the vectors magnitude.
     *      v {1, 2, 3}
     *      |v| 3.74...
     *
     * @param v values of vector as arraylist.
     * @return magnitude
     */
    public static double magnitudeVector(ArrayList<Double> v) {

        double vectorContent = 0;

        for (Double d : v) {
            vectorContent += Math.pow(d, 2);
        }

        return Math.sqrt(vectorContent);
    }



}
