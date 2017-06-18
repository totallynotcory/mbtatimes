package com.corypotwin.mbtatimes.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.corypotwin.mbtatimes.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Create an instance of GoogleAPIClient.
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    public void goToMyRoutes(View touchedView){
        Intent intent = new Intent(this, MyRoutes.class);
        startActivity(intent);
    }

    public void goToNearbyStations(View touchedView){
        Intent intent = new Intent(this, NearbyStations.class);
        startActivity(intent);
    }

    public void goToMyCriteriaSelect(View touchedView){
        Intent intent = new Intent(this, CriteriaSearch.class);
        startActivity(intent);
    }
}
