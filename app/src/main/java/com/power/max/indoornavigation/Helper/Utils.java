package com.power.max.indoornavigation.Helper;

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
import java.util.ArrayList;

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
}
