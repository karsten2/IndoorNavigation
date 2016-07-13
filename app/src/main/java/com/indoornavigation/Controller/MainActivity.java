package com.indoornavigation.Controller;

import com.indoor.navigation.indoornavigation.R;
import com.indoornavigation.View.MapFragment;
import com.indoornavigation.View.RssiFragment;
import com.indoornavigation.View.SettingsActivity;
import com.indoornavigation.View.RadiomapFragment;

import android.content.DialogInterface;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        MapFragment.OnFragmentInteractionListener,
        RadiomapFragment.OnFragmentInteractionListener,
        RssiFragment.OnFragmentInteractionListener {

    private static final String TAG = "MainActivity";
    public DroneController mDroneController;
    private Fragment lastFragment;

    @Override
    public void onFragmentInteraction(Uri uri) { }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        if (drawer != null) drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) navigationView.setNavigationItemSelectedListener(this);

        // Open Map.
        try {
            FragmentManager fragmentManager = getSupportFragmentManager();
            lastFragment = MapFragment.class.newInstance();
            fragmentManager.beginTransaction().replace(
                    R.id.flContent, lastFragment).commit();
        } catch (InstantiationException | IllegalAccessException e) {
            Log.e(TAG, e.getMessage());
        }

        this.mDroneController = new DroneController(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            // Dialog to make sure, the user wants to close the app.
            new AlertDialog.Builder(this)
                    .setTitle("App beenden")
                    .setMessage("Sind Sie sicher, dass Sie die App beenden möchten?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            mDroneController.destroy();
                            MainActivity.super.onBackPressed();
                            destroyActivity();
                        }
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .show();
        }
    }

    private void destroyActivity() {
        this.finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Handle navigation view item clicks here.
     *
     * @param item Menu item.
     * @return true false.
     */
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        ActionBar actionBar = getSupportActionBar();
        Fragment fragment = null;

        try {
            if (id == R.id.nav_map) {
                fragment = new MapFragment();
                if (actionBar != null) actionBar.setTitle(R.string.title_activity_map);
            } else if (id == R.id.nav_radiomap) {
                Log.d(TAG, "loading radiomap");
                fragment = new RadiomapFragment();
                if (actionBar != null) actionBar.setTitle(R.string.title_fragment_radiomap);
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            }

            if (fragment != null) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null) {
            drawer.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        if (mDroneController != null) mDroneController.destroy();
        Log.d("drone", "main destroyed.");
        super.onDestroy();
    }




}
