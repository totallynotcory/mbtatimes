package com.corypotwin.mbtatimes.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.corypotwin.mbtatimes.R;
import com.corypotwin.mbtatimes.fragments.RouteDetailsFragment;

public class NearbyStations extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    RouteDetailsFragment aRouteDetailsFragment;
    final String FRAGMENT_TAG = "NearbyStationsFragmentDetail";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            //Restore the fragment's instance
            aRouteDetailsFragment = (RouteDetailsFragment)  getSupportFragmentManager().getFragment(savedInstanceState, FRAGMENT_TAG);
        }

        setContentView(R.layout.activity_nearby_stations);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(1).setChecked(true);

        aRouteDetailsFragment = (RouteDetailsFragment) getSupportFragmentManager().findFragmentById(R.id.content_nearby_stations);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //Save the fragment's instance
        getSupportFragmentManager().putFragment(outState, FRAGMENT_TAG, aRouteDetailsFragment);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_nearby_stations) {
            // Nothing will happen as it's the current action
        } else if (id == R.id.nav_search_stations) {
            this.finish();
            Intent intent = new Intent(this, CriteriaSearch.class);
            startActivity(intent);
        } else if (id == R.id.nav_my_routes) {
            this.finish();
            Intent intent = new Intent(this, MyRoutes.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
