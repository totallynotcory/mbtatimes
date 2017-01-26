package com.corypotwin.mbtatimes.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.corypotwin.mbtatimes.MbtaApiEndpoint;
import com.corypotwin.mbtatimes.R;
import com.corypotwin.mbtatimes.SecretApiKeyFile;
import com.corypotwin.mbtatimes.TripDetails;
import com.corypotwin.mbtatimes.apidata.MbtaData;
import com.corypotwin.mbtatimes.apidata.StopsData;
import com.corypotwin.mbtatimes.apidata.Trip;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;

import okhttp3.HttpUrl;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RouteDetails extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }
}
