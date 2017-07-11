package com.corypotwin.mbtatimes.fragments;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.corypotwin.mbtatimes.MbtaApiEndpoint;
import com.corypotwin.mbtatimes.R;
import com.corypotwin.mbtatimes.RouteDetailsRecyclerViewAdapter;
import com.corypotwin.mbtatimes.SecretApiKeyFile;
import com.corypotwin.mbtatimes.TripDetails;
import com.corypotwin.mbtatimes.apidata.CurrentLocationStops;
import com.corypotwin.mbtatimes.apidata.MbtaData;
import com.corypotwin.mbtatimes.apidata.Mode;
import com.corypotwin.mbtatimes.database.MyRoutesContract;
import com.corypotwin.mbtatimes.database.MyRoutesProvider;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
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
        GoogleApiClient.OnConnectionFailedListener, LoaderManager.LoaderCallbacks<Cursor>{

    final String TAG = "routedetailsfragment";

    private GoogleApiClient mGoogleApiClient;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    public boolean showClickboxes = false;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private ArrayList<TripDetails> mTripDetailList = new ArrayList<>();

    private TextView mErrorBox;

    SimpleCursorAdapter mCursorAdapter;
    String mCurFilter;

    private boolean locationSearch;
    private Location mLastLocation;
    private String mLatitudeText;
    private String mLongitudeText;

    private final String TRIP_ARRAY = "Trip Array";
    private final String POSITION = "position";
    private int mPosition;

    final String[] STRING_PROJECTION = new String[] {
            MyRoutesContract.ID,
            MyRoutesContract.COLUMN_MODE,
            MyRoutesContract.COLUMN_STOP,
            MyRoutesContract.COLUMN_ROUTE,
            MyRoutesContract.COLUMN_DIRECTION
    };

    public RouteDetailsFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (savedInstanceState != null && savedInstanceState.containsKey(POSITION)) {
            mPosition = savedInstanceState.getInt(POSITION);
        }

        boolean tripDetailsExist = false;
        if (savedInstanceState != null && savedInstanceState.containsKey(TRIP_ARRAY)) {
            mTripDetailList = savedInstanceState.getParcelableArrayList(TRIP_ARRAY);
            if(mTripDetailList.size() > 0) tripDetailsExist = true;
        }

        View routeDetailsFragment = inflater.inflate(R.layout.fragment_route_details, container, false);
        mErrorBox = (TextView) routeDetailsFragment.findViewById(R.id.error_box);
        mRecyclerView = (RecyclerView) routeDetailsFragment.findViewById(R.id.recycler_view);

        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        String callingAct = getActivity().getLocalClassName();

        if (!isNetWorkAvailable(getContext())){
            mErrorBox.setText(getResources().getText(R.string.no_internet_connection));
            mErrorBox.setVisibility(View.VISIBLE);
        } else if( tripDetailsExist ){
            mAdapter = new RouteDetailsRecyclerViewAdapter(mTripDetailList, this);
            //  We've got a good thing going on.
            mRecyclerView.setAdapter(mAdapter);

        } else if(callingAct.equals("activities.RouteDetails")){
            Intent intent = getActivity().getIntent();
            final ArrayList<TripDetails> requestedTrips = intent.getParcelableArrayListExtra("tripDetails");
            if(requestedTrips != null){
                requestTripsByDetails(requestedTrips);
            }

        } else if (callingAct.equals("activities.NearbyStations")){
            locationSearch = true;

        } else if (callingAct.equals("activities.MyRoutes")){

            getLoaderManager().initLoader(0, null, this);

//            mCursorAdapter = new SimpleCursorAdapter(getActivity(),
//                    R.layout.activity_route_details, null, STRING_PROJECTION, null, 0);

        } else {
            Log.e(TAG, "onCreateView: the routedetail fragment has been reached from an unknown view.");
        }

        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    0);
            buildGoogleApiClient();
        } else {
            buildGoogleApiClient();
        }

        return routeDetailsFragment;
    }

    /**
     * if not GoogleApiClient exists, connect and create ApiClient
     */
    protected synchronized void buildGoogleApiClient(){
        if (mGoogleApiClient == null && checkPlayServices()) {
            mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(getContext());
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, getActivity(),
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getContext(),
                        getResources().getText(R.string.device_not_supported), Toast.LENGTH_LONG)
                        .show();
            }
            return false;
        }
        return true;
    }
    /**
     * retrieve all of a user's MyRoutes from MyRoutes database
     * @return ArrayList of all MyRoutes for current user
     */
    public ArrayList<TripDetails> loadUserRoutesData(Cursor c) {
        ArrayList<TripDetails> allTripDetails = new ArrayList<>();

        if (!c.moveToFirst()) {
            mErrorBox.setText(getResources().getText(R.string.no_saved_routes));
            mErrorBox.setVisibility(View.VISIBLE);
            return null;
        }else{
            do{
                TripDetails aSingleTrip = new TripDetails();
                aSingleTrip.setMyRouteId(c.getInt(c.getColumnIndex(MyRoutesContract.ID)));
                aSingleTrip.setMode(c.getString(c.getColumnIndex(MyRoutesContract.COLUMN_MODE)));
                aSingleTrip.setStopId(c.getString(c.getColumnIndex(MyRoutesContract.COLUMN_STOP)));
                aSingleTrip.setRouteId(c.getString(c.getColumnIndex(MyRoutesContract.COLUMN_ROUTE)));
                aSingleTrip.setDirectionId(c.getInt(c.getColumnIndex(MyRoutesContract.COLUMN_DIRECTION)));

                aSingleTrip.setRouteAndDirection(aSingleTrip.getRouteId() + ": " + aSingleTrip.getDirectionId());

                allTripDetails.add(aSingleTrip);

            } while (c.moveToNext());
            return allTripDetails;
        }
    }

    /**
     * Given the details of the desired routes, make a call to retrieve times for the routes
     * @param requestedTrips ArrayList of the requested troops
     */
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

    /**
     * Given the details of the desired stop, make a call to retrieve times for the stop
     * @param stopId MBTA stop id
     */
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

    /**
     * given data returned from MBTA api, setup
     * @param stopTimePredictions time predictions containing data from api
     * @param mode requested mode of transport
     * @param routeId requested route id
     * @param directionId requested direction id
     * @param aRequestedTrip data contained the requested trip
     */
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

    /**
     * given details of trip from location, extract time predictions
     * @param stopTimePredictions data returned from mbta api
     */
    private void setupTimePredictionsForLocations(MbtaData stopTimePredictions){

        if( stopTimePredictions.getMode().size() > 0 ) {
            TripDetails detailsOfTrip = new TripDetails();


            detailsOfTrip.setStopId(stopTimePredictions.getStopId());
            detailsOfTrip.setStationName(stopTimePredictions.getStopName());
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

            mTripDetailList.add(detailsOfTrip);
        }
    }

    /**
     * given a mode of transportation, return the corresponding drawable id
     * @param mode mode of transport
     * @return image id
     */
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

    /**
     * given multiple times, return them in a human readable string
     * @param timePredictions list of int time predictions
     * @return human readable string in the form of XXh, XXm, XXs; XXh, XXm, XXs; ...
     *  or No current predictions!
     */
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

    /**
     * given a number of seconds, convert it to hours, minutes, and seconds
     * @param totalSeconds # of seconds
     * @return XXh, XXm, XXs
     */
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

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri baseUri;
        if (mCurFilter != null) {
            baseUri = Uri.withAppendedPath(MyRoutesContract.CONTENT_URI,
                    Uri.encode(mCurFilter));
        } else {
            baseUri = MyRoutesContract.CONTENT_URI;
        }

        String select = "";
        return new CursorLoader(getActivity(), baseUri,
                STRING_PROJECTION, select, null,
                MyRoutesContract.ID + " COLLATE LOCALIZED ASC");
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {

        if(data != null && !data.equals(null)) {
            final ArrayList<TripDetails> requestedTrips = loadUserRoutesData(data);
            if(requestedTrips != null){
                requestTripsByDetails(requestedTrips);
            }
        }
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
    }


    /**
     * when getting data back from the MBTA, this makes calls to deal with it
     */
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

    /**
     * for location based search, make call to api and deal with data returned
     */
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
                if(nearbyRoutes.getStop().size() != 0) {
                    for (int i = 0; i < nearbyRoutes.getStop().size(); i++) {
                        requestTripsByLocation(nearbyRoutes.getStop().get(i).getStopId());
                    }
                } else {
                    mErrorBox.setText(getResources().getText(R.string.no_nearby_stations_error));
                    mErrorBox.setVisibility(View.VISIBLE);
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
            //  If we don't have a location, shut it down
            locationSearch = false;
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

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelableArrayList(TRIP_ARRAY, mTripDetailList);
        savedInstanceState.putInt(POSITION, mPosition);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Service destroyed!");
        mGoogleApiClient.disconnect();
        super.onDestroy();
    }

    /**
     * Logic for dealing with Route Details fab click
     * @param intention what the fab should do, can be "deleteSavedRoutes" or "addToTable"
     * @param view the view id for when the fab needs to change
     */
    public void fabOnClick(String intention, @Nullable View view){
        if(locationSearch){
            Log.d(TAG, "fabOnClick: we shouldn't be seeing a fab because we're in location");
        } else if (intention.equals("deleteSavedRoutes")) {
            CardView cards = (CardView) mRecyclerView.findViewById(R.id.card_view);
            CheckBox checkboxCharlie = (CheckBox) cards.findViewById(R.id.delete_checkbox);
            FloatingActionButton thisButton = (FloatingActionButton) view;

            if(checkboxCharlie.getVisibility() == View.VISIBLE){
                showClickboxes = false;
                thisButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_delete_white_24dp));
                thisButton.setContentDescription(getResources().getText(R.string.delete_selection));
                getActivity().setTitle(getResources().getText(R.string.my_routes));
                deleteCheckedMyRoutes();
                mAdapter.notifyDataSetChanged();
            } else if (checkboxCharlie.getVisibility() == View.GONE) {
                showClickboxes = true;
                thisButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_delete_forever_white_24dp));
                thisButton.setContentDescription(getResources().getText(R.string.delete_selected));
                getActivity().setTitle(getResources().getText(R.string.delete_my_routes));
                mAdapter.notifyDataSetChanged();
            }
        } else if (intention.equals("addToTable")){
                addRouteToUserMyRoutes(mTripDetailList.get(0));
            }
    }

    /**
     * deletes check routes from MyRoutes database.
     */
    private void deleteCheckedMyRoutes(){
        for (int i = 0; i < mTripDetailList.size(); i++) {
            TripDetails oneTripDetail = mTripDetailList.get(i);
            if(oneTripDetail.getCheckboxState()){
                mTripDetailList.remove(oneTripDetail);
                ContentResolver contentResolver = getContext().getContentResolver();

                int idToDelete = oneTripDetail.getMyRouteId();

                Uri uriOfTripToDelete = MyRoutesContract.CONTENT_URI.buildUpon().
                        appendPath(String.valueOf(idToDelete)).build();
                contentResolver.delete(uriOfTripToDelete, null, null);
            }
        }
    }

    /**
     * adds a TripDetails to the user's MyRoutes database
     * @param aRequestedTrip - TripDetails of what needs to be added to the MyRoutes database
     */
    private void addRouteToUserMyRoutes(TripDetails aRequestedTrip){
        ContentResolver contentResolver = getContext().getContentResolver();

        ContentValues values = new ContentValues();

        values.put(MyRoutesContract.COLUMN_STOP, aRequestedTrip.getStopId());
        values.put(MyRoutesContract.COLUMN_ROUTE, aRequestedTrip.getRouteId());
        values.put(MyRoutesContract.COLUMN_MODE, aRequestedTrip.getMode());
        values.put(MyRoutesContract.COLUMN_DIRECTION, aRequestedTrip.getDirectionId());

        contentResolver.insert(MyRoutesContract.CONTENT_URI, values);

        Toast.makeText(getContext(),
                getResources().getText(R.string.route_added_toast), Toast.LENGTH_LONG).show();
    }

    public boolean isNetWorkAvailable(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }


}
