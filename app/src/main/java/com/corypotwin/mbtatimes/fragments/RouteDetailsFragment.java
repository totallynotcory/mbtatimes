package com.corypotwin.mbtatimes.fragments;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Toast;

import com.corypotwin.mbtatimes.MbtaApiEndpoint;
import com.corypotwin.mbtatimes.R;
import com.corypotwin.mbtatimes.RouteDetailsRecyclerViewAdapter;
import com.corypotwin.mbtatimes.SecretApiKeyFile;
import com.corypotwin.mbtatimes.TripDetails;
import com.corypotwin.mbtatimes.apidata.CurrentLocationStops;
import com.corypotwin.mbtatimes.apidata.MbtaData;
import com.corypotwin.mbtatimes.apidata.Mode;
import com.corypotwin.mbtatimes.database.MyRoutesProvider;
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

    public boolean showClickboxes = false;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private ArrayList<TripDetails> mTripDetailList = new ArrayList<>();

    private boolean locationSearch = false;

    private final String apiKey = "wX9NwuHnZU2ToO7GmGR9uw";

    private int cardNumber = 0;

    public RouteDetailsFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View routeDetailsFragment = inflater.inflate(R.layout.fragment_route_details, container, false);
        mRecyclerView = (RecyclerView) routeDetailsFragment.findViewById(R.id.recycler_view);

        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        String callingAct = getActivity().getLocalClassName();

        if(callingAct.equals("activities.RouteDetails")){
            Intent intent = getActivity().getIntent();
            final ArrayList<TripDetails> requestedTrips = intent.getParcelableArrayListExtra("tripDetails");
            if(requestedTrips != null){
                requestTripsByDetails(requestedTrips);
            }

        } else if (callingAct.equals("activities.NearbyStations")){
            locationSearch = true;

        } else if (callingAct.equals("activities.MyRoutes")){
            requestTripsByDetails(loadUserRoutesData());

        } else {
            Toast.makeText(getActivity(), "How did you get here??  Get out, get out, get out!!", Toast.LENGTH_LONG).show();

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

        return routeDetailsFragment;
    }

    public ArrayList<TripDetails> loadUserRoutesData() {
        Uri routes = MyRoutesProvider.CONTENT_URI;
        ArrayList<TripDetails> allTripDetails = new ArrayList<>();
        Cursor c = getActivity().getContentResolver().query(routes, null, null, null, MyRoutesProvider.COLUMN_ROUTE);
        String result = "Results:";

        if (!c.moveToFirst()) {
            Toast.makeText(getActivity(), result + " no content yet!", Toast.LENGTH_LONG).show();
            return null;
        }else{
            do{
                TripDetails aSingleTrip = new TripDetails();
                aSingleTrip.setMyRouteId(c.getInt(c.getColumnIndex(MyRoutesProvider.ID)));
                aSingleTrip.setMode(c.getString(c.getColumnIndex(MyRoutesProvider.COLUMN_MODE)));
                aSingleTrip.setStopId(c.getString(c.getColumnIndex(MyRoutesProvider.COLUMN_STOP)));
                aSingleTrip.setRouteId(c.getString(c.getColumnIndex(MyRoutesProvider.COLUMN_ROUTE)));
                aSingleTrip.setDirectionId(c.getInt(c.getColumnIndex(MyRoutesProvider.COLUMN_DIRECTION)));

                aSingleTrip.setRouteAndDirection(aSingleTrip.getRouteId() + ": " + aSingleTrip.getDirectionId());

                allTripDetails.add(aSingleTrip);

            } while (c.moveToNext());
            return allTripDetails;
        }
    }

    private void requestTripsByDetails(ArrayList<TripDetails> requestedTrips){

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

    private void requestTripsByLocation(String stopId){

        HttpUrl BASE_URL = HttpUrl.parse("http://realtime.mbta.com/developer/api/v2/");

        HttpUrl url = BASE_URL.newBuilder()
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        MbtaApiEndpoint apiService = retrofit.create(MbtaApiEndpoint.class);

        Call<MbtaData> call = apiService.getPredictionsByStop(SecretApiKeyFile.getKey(), stopId);

        mAdapter = new RouteDetailsRecyclerViewAdapter(mTripDetailList, this);

        call.enqueue(new Callback<MbtaData>() {
            @Override
            public void onResponse(Call<MbtaData> call, Response<MbtaData> response) {
                Log.d(TAG, "onResponse: " + response.message());
                MbtaData stopTimePredictions = response.body();
                setupTimePredictionsForLocations(stopTimePredictions);
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
                        String routeName = stopTimePredictions.getMode().get(i).getRoute().get(j).getRouteName();

                        for (int k = 0; k < stopTimePredictions.getMode().get(i).getRoute().get(j).getDirection().size(); k++) {
                            if (stopTimePredictions.getMode().get(i).getRoute().get(j).getDirection().get(k).getDirectionId().equals((directionId + ""))) {
                                String directionName = stopTimePredictions.getMode().get(i).getRoute().get(j).getDirection().get(k).getDirectionName();
                                aRequestedTrip.setRouteAndDirection(routeName + ": " + directionName);

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

        aRequestedTrip.setModeImage(extractModeImage(mode));
        aRequestedTrip.setStationName(stopTimePredictions.getStopName());

        mTripDetailList.add(aRequestedTrip);
        mAdapter = new RouteDetailsRecyclerViewAdapter(mTripDetailList, this);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void setupTimePredictionsForLocations(MbtaData stopTimePredictions){
        TripDetails detailsOfTrip = new TripDetails();

        detailsOfTrip.setStopId(stopTimePredictions.getStopId());
        detailsOfTrip.setStationName(stopTimePredictions.getStopName());

        if( stopTimePredictions.getMode().size() > 0 ) {
            Mode currentMode = stopTimePredictions.getMode().get(0);
            detailsOfTrip.setMode(currentMode.getModeName());
            detailsOfTrip.setRouteId(currentMode.getRoute().get(0).getRouteId());

            detailsOfTrip.setModeImage(extractModeImage(currentMode.getModeName()));

            detailsOfTrip.setRouteAndDirection(currentMode.getRoute().get(0).getRouteName() + ": " +
                    currentMode.getRoute().get(0).getDirection().get(0).getDirectionName());

            Integer directionId = Integer.parseInt(currentMode.getRoute().get(0).getDirection().get(0).getDirectionId());
            detailsOfTrip.setDirectionId(directionId);

            if (currentMode.getRoute().size() > 0) {
                List<Integer> timePredictions = new ArrayList<>();

                for (int i = 0; i < currentMode.getRoute().get(0).getDirection().get(0).getTrip().size(); i++) {
                    timePredictions.add(currentMode.getRoute().get(0).getDirection().get(0).getTrip().get(i).getPreAway());
                }

                detailsOfTrip.setTimeEstimates(assembleHumanReadableTime(timePredictions));
            }
        }

        mTripDetailList.add(detailsOfTrip);
    }

    private Drawable extractModeImage(String mode){
        Drawable modeImage;

        switch (mode) {
            case "Bus":
                modeImage = getResources().getDrawable(R.drawable.ic_bus);
                break;
            case "Subway":
                modeImage = getResources().getDrawable(R.drawable.ic_subway);
                break;
            case "Commuter Rail":
                modeImage = getResources().getDrawable(R.drawable.ic_train);
                break;
            default:
                modeImage = getResources().getDrawable(R.drawable.ic_train);
        }

        return modeImage;
    }

    private static String assembleHumanReadableTime(List<Integer> timePredictions){
        String readableTimePredictions = new String();
        if (timePredictions.size() == 0) {
            return "No current predictions!";
        }

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
                    requestTripsByLocation(nearbyRoutes.getStop().get(i).getStopId());
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

    public void fabOnClick(String intention){
        if(locationSearch){
            Log.d(TAG, "fabOnClick: we shouldn't be seeing a fab because we're in location");
        } else if (intention.equals("deleteSavedRoutes")) {
            CardView cards = (CardView) mRecyclerView.findViewById(R.id.card_view);
            CheckBox checkboxCharlie = (CheckBox) cards.findViewById(R.id.delete_checkbox);

            if(checkboxCharlie.getVisibility() == View.VISIBLE){
                showClickboxes = false;
                for (int i = 0; i < mTripDetailList.size(); i++) {
                    TripDetails oneTripDetail = mTripDetailList.get(i);
                    if(oneTripDetail.getCheckboxState()){
                        mTripDetailList.remove(oneTripDetail);

                        ContentValues values = new ContentValues();
                        ContentResolver contentResolver = getContext().getContentResolver();

                        int idToDelete = oneTripDetail.getMyRouteId();

                        Uri thisUri = MyRoutesProvider.CONTENT_URI.buildUpon().appendPath(String.valueOf(idToDelete)).build();

                        int deletedIds = contentResolver.delete(thisUri, null, null);
                        oneTripDetail.getMyRouteId();

                    }
                }
                mAdapter.notifyDataSetChanged();
            } else if (checkboxCharlie.getVisibility() == View.GONE) {
                showClickboxes = true;
                mAdapter.notifyDataSetChanged();
            }
        } else if (intention.equals("addToTable")){
                addRouteToUserMyRoutes(mTripDetailList.get(0));
            }
        }


        private void addRouteToUserMyRoutes(TripDetails aRequestedTrip){
            ContentValues values = new ContentValues();
            ContentResolver contentResolver = getContext().getContentResolver();

            values.put(MyRoutesProvider.COLUMN_STOP, aRequestedTrip.getStopId());
            values.put(MyRoutesProvider.COLUMN_ROUTE, aRequestedTrip.getRouteId());
            values.put(MyRoutesProvider.COLUMN_MODE, aRequestedTrip.getMode());
            values.put(MyRoutesProvider.COLUMN_DIRECTION, aRequestedTrip.getDirectionId());

            contentResolver.insert(MyRoutesProvider.CONTENT_URI, values);

            Toast.makeText(getContext(),
                    getResources().getText(R.string.route_added_toast), Toast.LENGTH_LONG).show();
        }

}
