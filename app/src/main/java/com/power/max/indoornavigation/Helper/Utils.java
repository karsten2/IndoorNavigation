package com.power.max.indoornavigation.Helper;

import android.content.Context;
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
}
