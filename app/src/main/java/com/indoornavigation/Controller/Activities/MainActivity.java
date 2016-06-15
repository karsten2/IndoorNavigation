package com.indoornavigation.Controller.Activities;

import com.indoornavigation.Controller.Fragments.DroneFragment;
import com.indoornavigation.Controller.Fragments.MapFragment;
import com.indoornavigation.Controller.Fragments.SqliteFragment;
import com.indoornavigation.Controller.Fragments.WifiFragment;
import com.indoor.navigation.indoornavigation.R;
import com.python.python27.ScriptActivity;

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
        WifiFragment.OnFragmentInteractionListener,
        SqliteFragment.OnFragmentInteractionListener,
        DroneFragment.OnFragmentInteractionListener {

    private static final String TAG = "MainActivity";

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
            fragmentManager.beginTransaction().replace(
                    R.id.flContent, MapFragment.class.newInstance()).commit();
        } catch (InstantiationException | IllegalAccessException e) {
            Log.e(TAG, e.getMessage());
        }
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
                            MainActivity.super.onBackPressed();
                        }
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .show();
        }
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
        Fragment fragment = new Fragment();

        try {
            if (id == R.id.nav_map) {
                fragment = MapFragment.class.newInstance();
                if (actionBar != null) actionBar.setTitle(R.string.title_activity_map);
            } else if (id == R.id.nav_wifi) {
                fragment = WifiFragment.class.newInstance();
                if (actionBar != null) actionBar.setTitle(R.string.title_activity_wifi);
            } else if (id == R.id.nav_sqlite) {
                fragment = SqliteFragment.class.newInstance();
                if (actionBar != null) actionBar.setTitle(R.string.title_fragment_sqlite);
            } else if (id == R.id.nav_drone) {
                fragment = DroneFragment.class.newInstance();
            } else if (id == R.id.nav_python) {
                this.startActivity(new Intent(this, ScriptActivity.class));
                throw new Exception("dummy exception");
            }

            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();
        } catch (Exception e) {
            e.printStackTrace();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null) {
            drawer.closeDrawer(GravityCompat.START);
        }
        return true;
    }
}
