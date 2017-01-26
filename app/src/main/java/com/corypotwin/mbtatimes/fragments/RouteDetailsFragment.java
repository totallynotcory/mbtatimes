package com.corypotwin.mbtatimes.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.corypotwin.mbtatimes.MbtaApiEndpoint;
import com.corypotwin.mbtatimes.R;
import com.corypotwin.mbtatimes.RouteDetailsRecyclerViewAdapter;
import com.corypotwin.mbtatimes.SecretApiKeyFile;
import com.corypotwin.mbtatimes.TripDetails;
import com.corypotwin.mbtatimes.apidata.CurrentLocationStops;
import com.corypotwin.mbtatimes.apidata.MbtaData;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

import okhttp3.HttpUrl;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * A placeholder fragment containing a simple view.
 */
public class RouteDetailsFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    final String TAG = "routedetailsfragment";

    private Location mLastLocation;
    private String mLatitudeText;
    private String mLongitudeText;

    private GoogleApiClient mGoogleApiClient;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private ArrayList<TripDetails> mTripDetailList = new ArrayList<>();

    private boolean locationSearch = false;

    private final String apiKey = "wX9NwuHnZU2ToO7GmGR9uw";

    private int cardNumber = 0;

    public RouteDetailsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Intent intent = getActivity().getIntent();
        final ArrayList<TripDetails> requestedTrips = intent.getParcelableArrayListExtra("tripDetails");

        View routeDetailsFragment = inflater.inflate(R.layout.fragment_route_details, container, false);
        mRecyclerView = (RecyclerView) routeDetailsFragment.findViewById(R.id.recycler_view);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        // mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        if(requestedTrips == null) {
            locationSearch = true;
        }

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED){
            Log.d(TAG, "onCreateView: booms");
        }

        if(requestedTrips != null){
            callRequestedTrips(requestedTrips);
        }

        return routeDetailsFragment;
    }

    private void callRequestedTrips(ArrayList<TripDetails> requestedTrips){
        for (int i = 0; i < requestedTrips.size(); i++) {
            TripDetails aRequestedTrip = requestedTrips.get(i);
            String stopId = aRequestedTrip.getStopId();
            String routeId = aRequestedTrip.getRouteId();
            String mode = aRequestedTrip.getMode();
            int directionId = aRequestedTrip.getDirectionId();

            HttpUrl BASE_URL = HttpUrl.parse("http://realtime.mbta.com/developer/api/v2/");

            HttpUrl url = BASE_URL.newBuilder()
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(url)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            MbtaApiEndpoint apiService = retrofit.create(MbtaApiEndpoint.class);

            Call<MbtaData> call = apiService.getPredictionsByStop(SecretApiKeyFile.getKey(), stopId);

            Callback<MbtaData> callback = new RouteDetailsCallback(stopId, routeId, mode, directionId, aRequestedTrip);

            call.enqueue(callback);
        }
    }

    private void callRequestedTrips(String stopId){

        HttpUrl BASE_URL = HttpUrl.parse("http://realtime.mbta.com/developer/api/v2/");

        HttpUrl url = BASE_URL.newBuilder()
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        MbtaApiEndpoint apiService = retrofit.create(MbtaApiEndpoint.class);

        Call<MbtaData> call = apiService.getPredictionsByStop(SecretApiKeyFile.getKey(), stopId);

        call.enqueue(new Callback<MbtaData>() {
            @Override
            public void onResponse(Call<MbtaData> call, Response<MbtaData> response) {
                Log.d(TAG, "onResponse: " + response.message());
                MbtaData stopTimePredictions = response.body();
                setupTimePredictions(stopTimePredictions);

                mAdapter = new RouteDetailsRecyclerViewAdapter(mTripDetailList);
                mRecyclerView.setAdapter(mAdapter);
            }

            @Override
            public void onFailure(Call<MbtaData> call, Throwable t) {
                // Log error here since request failed
                Log.e(TAG, "onFailure: stuff got messed up: " + t.toString());
            }
        });
    }

    private void setupTimePredictions(MbtaData stopTimePredictions, String mode, String routeId, int directionId, TripDetails aRequestedTrip) {

        List<Integer> timePredictions = new ArrayList<>();

        for (int i = 0; i < stopTimePredictions.getMode().size(); i++) {
            if (stopTimePredictions.getMode().get(i).getModeName().equals(mode)) {
                for (int j = 0; j < stopTimePredictions.getMode().get(i).getRoute().size(); j++) {
                    if (stopTimePredictions.getMode().get(i).getRoute().get(j).getRouteId().equals(routeId)) {
                        for (int k = 0; k < stopTimePredictions.getMode().get(i).getRoute().get(j).getDirection().size(); k++) {
                            if (stopTimePredictions.getMode().get(i).getRoute().get(j).getDirection().get(k).getDirectionId().equals((directionId + ""))) {
                                for (int l = 0; l < stopTimePredictions.getMode().get(i).getRoute().get(j).getDirection().get(k).getTrip().size(); l++) {
                                    timePredictions.add(stopTimePredictions.getMode().get(i).getRoute().get(j).getDirection().get(k).getTrip().get(l).getPreAway());
                                }
                            }
                        }
                    }
                }
            }
        }

        aRequestedTrip.setTimeEstimates(assembleHumanReadableTime(timePredictions));

        ArrayList<TripDetails> tripDetailList = new ArrayList<>();
        tripDetailList.add(aRequestedTrip);
        mAdapter = new RouteDetailsRecyclerViewAdapter(tripDetailList);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void setupTimePredictions(MbtaData stopTimePredictions){
        TripDetails detailsOfTrip = new TripDetails();

        detailsOfTrip.setStopId(stopTimePredictions.getStopId());
        detailsOfTrip.setStationName(stopTimePredictions.getStopName());

        if( stopTimePredictions.getMode().size() > 0 ){
            detailsOfTrip.setMode(stopTimePredictions.getMode().get(0).getModeName());
            detailsOfTrip.setRouteId(stopTimePredictions.getMode().get(0).getRoute().get(0).getRouteId());

            detailsOfTrip.setRouteAndDirection(stopTimePredictions.getMode().get(0).getRoute().get(0).getRouteName() + ": " +
                    stopTimePredictions.getMode().get(0).getRoute().get(0).getDirection().get(0).getDirectionName());

            Integer directionId = Integer.parseInt(stopTimePredictions.getMode().get(0).getRoute().get(0).getDirection().get(0).getDirectionId());
            detailsOfTrip.setDirectionId(directionId);}

            List<Integer> timePredictions = new ArrayList<>();
            for (int i = 0; i < stopTimePredictions.getMode().get(0).getRoute().get(0).getDirection().get(0).getTrip().size(); i++) {
                timePredictions.add(stopTimePredictions.getMode().get(0).getRoute().get(0).getDirection().get(0).getTrip().get(i).getPreAway());
            }

        detailsOfTrip.setTimeEstimates(assembleHumanReadableTime(timePredictions));
        mTripDetailList.add(detailsOfTrip);
    }

    private static String assembleHumanReadableTime(List<Integer> timePredictions){
        String readableTimePredictions = new String();
        for (int i = 0; i < timePredictions.size(); i++) {
            readableTimePredictions = readableTimePredictions + timeConversion(timePredictions.get(i));
        }
        return readableTimePredictions;
    }

    private static String timeConversion(int totalSeconds) {

        final int MINUTES_IN_AN_HOUR = 60;
        final int SECONDS_IN_A_MINUTE = 60;

        int seconds = totalSeconds % SECONDS_IN_A_MINUTE;
        int totalMinutes = totalSeconds / SECONDS_IN_A_MINUTE;
        int minutes = totalMinutes % MINUTES_IN_AN_HOUR;
        int hours = totalMinutes / MINUTES_IN_AN_HOUR;

        String returnString = new String();
        if(hours > 0){
            returnString = hours + "h, ";
        }
        if(minutes > 0){
            returnString = returnString + minutes + "m, ";
        }
        if(seconds > 0){
            returnString = returnString + seconds + "s";
        }

        return returnString + "; ";
    }

    class RouteDetailsCallback implements Callback<MbtaData> {

        private final String stopId;
        private final String routeId;
        private final String mode;
        private final int directionId;
        private final TripDetails aRequestedTrip;

        RouteDetailsCallback(String stopId, String routeId, String mode, int directionId, TripDetails aRequestedTrip) {
            this.stopId = stopId;
            this.routeId = routeId;
            this.mode = mode;
            this.directionId = directionId;
            this.aRequestedTrip = aRequestedTrip;
        }

        @Override
        public void onResponse(Call<MbtaData> call, Response<MbtaData> response) {
            String whatWentWrong = response.message();
            MbtaData stopTimePredictions = response.body();
            setupTimePredictions(stopTimePredictions, mode, routeId, directionId, aRequestedTrip);

        }

        @Override
        public void onFailure(Call<MbtaData> call, Throwable t) {
            // Log error here since request failed
            Log.e(TAG, "onFailure: stuff got messed up: " + t.toString());
        }

    }

    public void callLocationApi(){
        HttpUrl BASE_URL = HttpUrl.parse("http://realtime.mbta.com/developer/api/v2/");

        HttpUrl url = BASE_URL.newBuilder()
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        MbtaApiEndpoint apiService = retrofit.create(MbtaApiEndpoint.class);

        Call<CurrentLocationStops> call = apiService.getStopsByLocation(SecretApiKeyFile.getKey(), mLatitudeText, mLongitudeText);

        call.enqueue(new Callback<CurrentLocationStops>() {
            @Override
            public void onResponse(Call<CurrentLocationStops> call, Response<CurrentLocationStops> response) {
                Log.d(TAG, "onResponse: " + response.message());
                CurrentLocationStops nearbyRoutes = response.body();
                for (int i = 0; i < nearbyRoutes.getStop().size(); i++) {
                    callRequestedTrips(nearbyRoutes.getStop().get(i).getStopId());
                }
            }

            @Override
            public void onFailure(Call<CurrentLocationStops> call, Throwable t) {
                // Log error here since request failed
                Log.e(TAG, "onFailure: stuff got messed up: " + t.toString());
            }
        });
    }

    public void onConnected(Bundle connectionHint) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            mLatitudeText = String.valueOf(mLastLocation.getLatitude());
            mLongitudeText = String.valueOf(mLastLocation.getLongitude());
        } else {
            mLatitudeText = "42.346961";
            mLongitudeText = "-71.076640";
        }

        if(locationSearch) {
            callLocationApi();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }
    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

}
