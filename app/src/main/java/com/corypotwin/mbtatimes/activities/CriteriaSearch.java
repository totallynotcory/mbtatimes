package com.corypotwin.mbtatimes.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.corypotwin.mbtatimes.MbtaApiEndpoint;
import com.corypotwin.mbtatimes.R;
import com.corypotwin.mbtatimes.SecretApiKeyFile;
import com.corypotwin.mbtatimes.TripDetails;
import com.corypotwin.mbtatimes.activities.RouteDetails;
import com.corypotwin.mbtatimes.apidata.MbtaData;
import com.corypotwin.mbtatimes.apidata.Stop;
import com.corypotwin.mbtatimes.apidata.StopsData;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.HttpUrl;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CriteriaSearch extends AppCompatActivity {

    final String TAG = "MyRoutes";

    @BindView(R.id.mode_spinner) Spinner modeSpinner;
    @BindView(R.id.route_spinner) Spinner routeSpinner;
    @BindView(R.id.direction_spinner) Spinner directionSpinner;
    @BindView(R.id.stop_spinner) Spinner stopSpinner;

    private GoogleApiClient client;
    private MbtaData dataFromApi;
    private StopsData stopsDataFromApi;
    String modeSpinnerValue;

    private HashMap<String, String> routeHashMap = new HashMap<>();
    private HashMap<String, Integer> directionsHashMap = new HashMap<>();
    private HashMap<String, String> stopHashMap = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_criteria_search);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        // modeSpinner
        modeSpinner = (Spinner) findViewById(R.id.mode_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.mode_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modeSpinner.setOnItemSelectedListener(new modeSpinnerSelect());
        modeSpinner.setAdapter(adapter);
        getModeResults();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "MyRoutes Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.corypotwin.mbtatimes/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "MyRoutes Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.corypotwin.mbtatimes/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    private MbtaApiEndpoint setupRetrofitForCall(){
        HttpUrl BASE_URL = HttpUrl.parse("http://realtime.mbta.com/developer/api/v2/");

        HttpUrl url = BASE_URL.newBuilder()
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit.create(MbtaApiEndpoint.class);
    }

    private void getModeResults() {
        //  TODO setup overloading instead of this junk with "unusued"
        MbtaApiEndpoint apiService = setupRetrofitForCall();
        Call<MbtaData> call = apiService.getRoutes(SecretApiKeyFile.getKey());
        call.enqueue(new Callback<MbtaData>() {
            @Override
            public void onResponse(Call<MbtaData> call, Response<MbtaData> response) {
                Log.d(TAG, "onResponse: " + response.message());
                MbtaData modeOfTransportData = response.body();
                setRouteSpinner(modeOfTransportData);
            }

            @Override
            public void onFailure(Call<MbtaData> call, Throwable t) {
                // Log error here since request failed
                Log.e(TAG, "onFailure: stuff got messed up: " + t.toString());
            }
        });
    }

    public class modeSpinnerSelect implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int pos, long id) {
            modeSpinnerValue = parent.getItemAtPosition(pos).toString();
            setRouteSpinner(dataFromApi);
        }

        public void onNothingSelected(AdapterView<?> parent) {
            // Another interface callback
        }
    }

    public void setRouteSpinner(MbtaData returnValueForRoutes){

        if(returnValueForRoutes != null){
            dataFromApi = returnValueForRoutes;
            ArrayList<String> routes = new ArrayList<>();

            for (int i = 0; i < returnValueForRoutes.getMode().size(); i++) {
                if(Objects.equals(modeSpinnerValue, returnValueForRoutes.getMode().get(i).getModeName())){
                    for (int j = 0; j < returnValueForRoutes.getMode().get(i).getRoute().size(); j++) {
                        String routeName = returnValueForRoutes.getMode().get(i).getRoute().get(j).getRouteName();
                        String routeId = returnValueForRoutes.getMode().get(i).getRoute().get(j).getRouteId();
                        routes.add(routeName);
                        routeHashMap.put(routeName,routeId);
                    }
                }
            }

            routeSpinner = (Spinner) findViewById(R.id.route_spinner);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, routes);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            routeSpinner.setVisibility(View.VISIBLE);
            routeSpinner.setOnItemSelectedListener(new routeSpinnerSelect());
            routeSpinner.setAdapter(adapter);

        }}

    public class routeSpinnerSelect implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int pos, long id) {
            String routeName = parent.getItemAtPosition(pos).toString();
            String routeId = routeHashMap.get(routeName);

            MbtaApiEndpoint apiService = setupRetrofitForCall();

            Call<StopsData> call = apiService.getStopsByRoute(SecretApiKeyFile.getKey(), routeId);

            call.enqueue(new Callback<StopsData>() {
                @Override
                public void onResponse(Call<StopsData> call, Response<StopsData> response) {
                    StopsData stopData = response.body();
                    setDirectionSpinner(stopData);
                    int one = 1;
                }

                @Override
                public void onFailure(Call<StopsData> call, Throwable t) {
                    // Log error here since request failed
                    Log.e(TAG, "onFailure: stuff got messed up: " + t.toString());
                }
            });
        }

        public void onNothingSelected(AdapterView<?> parent) {
            // Another interface callback
        }
    }

    public void setDirectionSpinner(StopsData returnValueForStop){

        if(returnValueForStop != null){
            stopsDataFromApi = returnValueForStop;
            ArrayList<String> directions = new ArrayList<>();

            for (int i = 0; i < stopsDataFromApi.getDirection().size(); i++) {
                String directionName = stopsDataFromApi.getDirection().get(i).getDirectionName();
                Integer directionId = i;
                directions.add(directionName);
                directionsHashMap.put(directionName, directionId);
            }

            directionSpinner = (Spinner) findViewById(R.id.direction_spinner);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, directions);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            directionSpinner.setVisibility(View.VISIBLE);
            directionSpinner.setOnItemSelectedListener(new directionSpinnerSelect());
            directionSpinner.setAdapter(adapter);
        }
    }

    public class directionSpinnerSelect implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int pos, long id) {
            ArrayList<String> stops = new ArrayList<>();

            String directionName = parent.getItemAtPosition(pos).toString();
            int directionId = directionsHashMap.get(directionName);

            List<Stop> stopsForThisDirection = stopsDataFromApi.getDirection().get(directionId).getStop();
            for (int i = 0; i < stopsForThisDirection.size(); i++) {
                String stopName = stopsForThisDirection.get(i).getStopName();
                String stopId = stopsForThisDirection.get(i).getStopId();
                stops.add(stopName);
                stopHashMap.put(stopName, stopId);
            }

            stopSpinner = (Spinner) findViewById(R.id.stop_spinner);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getBaseContext(),
                    android.R.layout.simple_spinner_item, stops);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            stopSpinner.setVisibility(View.VISIBLE);
            stopSpinner.setAdapter(adapter);
        }



        public void onNothingSelected(AdapterView<?> parent) {
            // Another interface callback
        }
    }

    public void callApi(View button) {
        ArrayList<TripDetails> tripRequestDataList = new ArrayList<>();
        TripDetails tripRequestData = new TripDetails();
        tripRequestData.setDirectionId(directionsHashMap.get(directionSpinner.getSelectedItem().toString()));
        tripRequestData.setMode(modeSpinner.getSelectedItem().toString());
        tripRequestData.setRouteId(routeHashMap.get(routeSpinner.getSelectedItem().toString()));
        tripRequestData.setStopId(stopHashMap.get(stopSpinner.getSelectedItem().toString()));
        tripRequestData.setRouteAndDirection(routeSpinner.getSelectedItem().toString() + ": "
                + directionSpinner.getSelectedItem().toString());
        tripRequestDataList.add(tripRequestData);

        Intent intent = new Intent(this, RouteDetails.class);
        intent.putParcelableArrayListExtra("tripDetails", tripRequestDataList);
        startActivity(intent);
    }
}
