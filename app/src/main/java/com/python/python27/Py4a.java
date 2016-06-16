package com.python.python27;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.Log;

import com.googlecode.android_scripting.FileUtils;
import com.indoor.navigation.indoornavigation.R;
import com.python.python27.config.GlobalConstants;
import com.python.python27.support.Utils;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;

public class Py4a {

    private Context context;
    private Intent intentScriptService;
    private Intent intentBackgroundScriptService;
    private static final String TAG = "Py4a";

    public Py4a(Context context) {
        this.context = context;

        intentScriptService = new Intent(context, ScriptService.class);
        intentBackgroundScriptService = new Intent(context, BackgroundScriptService.class);
    }

    public void launchScript(Object... params) {

        if (isInstallNeeded()) {
            new InstallAsyncTask().execute();
        } else {
            new UpdateAsyncTask().execute();
        }
    }

    /**
     * Dirty check if main python file is in directory.
     * @return true if file is missing; else false.
     */
    private boolean isInstallNeeded() {
        File testedFile = new File(
                context.getFilesDir().getAbsolutePath() + "/"
                        + GlobalConstants.PYTHON_MAIN_SCRIPT_NAME);
        if (!testedFile.exists()) {
            return true;
        }
        return true;
    }

    public class InstallAsyncTask extends AsyncTask<Void, Integer, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            Log.i(GlobalConstants.LOG_TAG, "Installing...");

            createExternalStorageRootDir();

            // Copy all resources
            copyResourcesToLocal();

            // TODO
            return true;
        }

        @Override
        protected void onPostExecute(Boolean installStatus) {
            runScriptService();
        }
    }

    public class UpdateAsyncTask extends AsyncTask<Void, Integer, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            Log.i(GlobalConstants.LOG_TAG, "Updating...");

            updatePython();

            // TODO
            return true;
        }

        @Override
        protected void onPostExecute(Boolean installStatus) {
            runScriptService();
        }
    }

    private void createExternalStorageRootDir() {
        Utils.createDirectoryOnExternalStorage(context.getPackageName());
    }

    /**
     * Function to unpack the python files in the root directory.
     */
    private void copyResourcesToLocal() {
        String name, sFileName;
        InputStream content;

        R.raw a = new R.raw();
        java.lang.reflect.Field[] t = R.raw.class.getFields();
        Resources resources = context.getResources();

        boolean succeed = true;

        for (int i = 0; i < t.length; i++) {
            try {
                name = resources.getText(t[i].getInt(a)).toString();
                sFileName = name.substring(name.lastIndexOf('/') + 1, name.length());
                content = context.getResources().openRawResource(t[i].getInt(a));
                content.reset();

                if (sFileName.endsWith(GlobalConstants.PYTHON_PROJECT_ZIP_NAME)) {
                    succeed &= Utils.unzip(content, context.getFilesDir().getAbsolutePath() + "/", true, true);
                } else if (sFileName.endsWith(GlobalConstants.PYTHON_ZIP_NAME)) {
                    succeed &= Utils.unzip(content, context.getFilesDir().getAbsolutePath() + "/", true, true);
                    FileUtils.chmod(new File(context.getFilesDir().getAbsolutePath() + "/python/bin/python"), 0755);
                } else if (sFileName.endsWith(GlobalConstants.PYTHON_EXTRAS_ZIP_NAME)) {
                    succeed &= Utils.unzip(content, context.getFilesDir().getAbsolutePath() + "/", true, true);
                    FileUtils.chmod(new File(context.getFilesDir().getAbsolutePath() + "/packages"), 0755);
                }

            } catch (Exception e) {
                Log.e(GlobalConstants.LOG_TAG, "Failed to copyResourcesToLocal", e);
                succeed = false;
            }
        }
    }

    /**
     * Function to update the python files in the root directory.
     */
    private void updatePython() {
        String name, sFileName;
        InputStream content;

        R.raw a = new R.raw();
        java.lang.reflect.Field[] t = R.raw.class.getFields();
        Resources resources = context.getResources();

        boolean succeed = true;

        for (int i = 0; i < t.length; i++) {
            try {
                name = resources.getText(t[i].getInt(a)).toString();
                sFileName = name.substring(name.lastIndexOf('/') + 1, name.length());
                content = context.getResources().openRawResource(t[i].getInt(a));
                content.reset();

                if (sFileName.endsWith(GlobalConstants.PYTHON_PROJECT_ZIP_NAME)) {
                    succeed &= Utils.unzip(content, context.getFilesDir().getAbsolutePath() + "/", true, true);
                }
            } catch (Exception e) {
                Log.e(GlobalConstants.LOG_TAG, "Failed to update resources", e);
                succeed = false;
            }
        }
    }

    private void runScriptService() {
        if (GlobalConstants.IS_FOREGROUND_SERVICE) {
            context.startService(intentScriptService);
        } else {
            context.startService(intentBackgroundScriptService);
        }
    }

    private void killScriptService() {
        if (GlobalConstants.IS_FOREGROUND_SERVICE) {
            context.stopService(intentScriptService);
        } else {
            context.stopService(intentBackgroundScriptService);
        }
    }

    /**
     * Function to stop the running services.
     */
    public void destroy() {
        killScriptService();
    }
}
