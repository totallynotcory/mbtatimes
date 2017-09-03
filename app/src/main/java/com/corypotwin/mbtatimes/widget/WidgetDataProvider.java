package com.corypotwin.mbtatimes.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.corypotwin.mbtatimes.MbtaApiEndpoint;
import com.corypotwin.mbtatimes.R;
import com.corypotwin.mbtatimes.SecretApiKeyFile;
import com.corypotwin.mbtatimes.TripDetails;
import com.corypotwin.mbtatimes.apidata.MbtaData;
import com.corypotwin.mbtatimes.database.MyRoutesContract;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.HttpUrl;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.content.ContentValues.TAG;


/**
 * Created by ctpotwin on 8/15/17.
 */

public class WidgetDataProvider implements RemoteViewsService.RemoteViewsFactory {

    private Context context;
    private Cursor cursor;
    private Intent intent;
    private ArrayList<TripDetails> listPositionToRouteData = new ArrayList<>();


    //For obtaining the activity's context and intent
    public WidgetDataProvider(Context context, Intent intent) {
        this.context = context;
        this.intent = intent;
    }

    @Override
    public void onCreate() {
        initCursor();
    }

    /**
     * open the cursor to our saved myRoutes data
     */
    private void initCursor(){
        if (cursor != null) {
            cursor.close();
        }

        final long identityToken = Binder.clearCallingIdentity();
        cursor = context.getContentResolver().query(MyRoutesContract.CONTENT_URI,
                null, null, null, MyRoutesContract.COLUMN_ROUTE);
        Binder.restoreCallingIdentity(identityToken);
    }

    @Override
    public void onDataSetChanged() {
        initCursor();
        setupCallToApi();
    }

    @Override
    public RemoteViews getViewAt(int i) {

        /** Populate the widget's single list item **/
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_row);

        TripDetails aRequestedTrip = listPositionToRouteData.get(i);

        remoteViews.setTextViewText(R.id.route_and_direction_text, aRequestedTrip.getRouteAndDirection());
        remoteViews.setTextViewText(R.id.times_text, aRequestedTrip.getTimeEstimates());
        remoteViews.setTextViewText(R.id.station_name, aRequestedTrip.getStationName());
        remoteViews.setImageViewResource(R.id.mode_image, extractModeImage(aRequestedTrip.getMode()));

        return remoteViews;
    }

    /**
     * if we have internet connection and data in our cursor, make a call to request data from server
     */
    private void setupCallToApi(){
        if (isNetWorkAvailable(context)) {
            cursor.moveToFirst();
            listPositionToRouteData.clear();

            while(!cursor.isAfterLast()) {
                Log.e(TAG, "we're setting up the routes!");
                TripDetails thisRequestedTrip = new TripDetails();
                thisRequestedTrip.setMyRouteId(cursor.getInt(cursor.getColumnIndex(MyRoutesContract.ID)));
                thisRequestedTrip.setMode(cursor.getString(cursor.getColumnIndex(MyRoutesContract.COLUMN_MODE)));
                thisRequestedTrip.setStopId(cursor.getString(cursor.getColumnIndex(MyRoutesContract.COLUMN_STOP)));
                thisRequestedTrip.setRouteId(cursor.getString(cursor.getColumnIndex(MyRoutesContract.COLUMN_ROUTE)));
                thisRequestedTrip.setDirectionId(cursor.getInt(cursor.getColumnIndex(MyRoutesContract.COLUMN_DIRECTION)));

                requestTripsByDetails(thisRequestedTrip);
                cursor.moveToNext();
            }
        }
    }

    /**
     * given the details of a trip, make a syncronous call to request time data.
     * @param aRequestedTrip TripDetails containing the trip we want timing for
     */
    private void requestTripsByDetails(TripDetails aRequestedTrip){
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

        MbtaData stopTimePredictions = new MbtaData();

        try {
            stopTimePredictions = call.execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if( stopTimePredictions != null ){
            setupTimePredictions(stopTimePredictions, mode, routeId, directionId,
                    aRequestedTrip);
        }

    }

    /**
     * with return data from the MBTA api and the trip details, find and store the external
     * times for use in display
     * @param stopTimePredictions data returned from server
     * @param mode requested mode
     * @param routeId requested routeId
     * @param directionId requested directionId
     * @param aRequestedTrip requested trip details
     */
    private void setupTimePredictions(MbtaData stopTimePredictions, String mode, String routeId,
                                      int directionId, TripDetails aRequestedTrip) {
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
        aRequestedTrip.setStationName(stopTimePredictions.getStopName());
        aRequestedTrip.setMode(mode);

        listPositionToRouteData.add(aRequestedTrip);
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
    public void onDestroy() {
        if (cursor != null) {
            cursor.close();
        }

    }

    @Override
    public int getCount() {
        if(listPositionToRouteData != null) {
            return listPositionToRouteData.size();
        } else {
            return 0;
        }
    }

    private int extractModeImage(String mode){
        int modeImage;

        switch (mode) {
            case "Bus":
                modeImage = R.drawable.ic_bus;
                break;
            case "Subway":
                modeImage = R.drawable.ic_subway;
                break;
            case "Commuter Rail":
                modeImage = R.drawable.ic_train;
                break;
            default:
                modeImage = R.drawable.ic_train;
        }

        return modeImage;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    public boolean isNetWorkAvailable(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }


}




