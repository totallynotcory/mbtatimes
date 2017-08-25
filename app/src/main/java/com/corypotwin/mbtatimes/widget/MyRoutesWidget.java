package com.corypotwin.mbtatimes.widget;

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.RemoteViews;

import com.corypotwin.mbtatimes.MbtaApiEndpoint;
import com.corypotwin.mbtatimes.R;
import com.corypotwin.mbtatimes.SecretApiKeyFile;
import com.corypotwin.mbtatimes.TripDetails;
import com.corypotwin.mbtatimes.apidata.MbtaData;
import com.corypotwin.mbtatimes.database.MyRoutesContract;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.HttpUrl;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.content.ContentValues.TAG;

/**
 * Implementation of App Widget functionality.
 */
public class MyRoutesWidget extends AppWidgetProvider {

    private Cursor cursor;
    private Intent intent;
    private ArrayList<TripDetails> listPositionToRouteData = new ArrayList<>();

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId, ArrayList<TripDetails> displayData) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.my_routes_widget);
        views.setTextViewText(R.id.last_update_text, "Last Update: " +
                DateFormat.getDateTimeInstance().format(new Date()));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            setRemoteAdapter(context, views, displayData);
        } else {
            setRemoteAdapterV11(context, views, displayData);
        }

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_list_view);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        initCursor(context);
        Log.e(TAG, "onUpdate: The cursor is loaded");
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            // Instruct the widget manager to update the widget
            setupCallToApi(context, appWidgetManager, appWidgetId);
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    private void initCursor(Context context){
        if (cursor != null) {
            cursor.close();
        }

        final long identityToken = Binder.clearCallingIdentity();
        /**This is done because the widget runs as a separate thread
         when compared to the current app and hence the app's data won't be accessible to it
         because I'm using a content provided **/
        cursor = context.getContentResolver().query(MyRoutesContract.CONTENT_URI,
                null, null, null, MyRoutesContract.COLUMN_ROUTE);
        Binder.restoreCallingIdentity(identityToken);
    }

    private void setupCallToApi(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId){
        if (isNetWorkAvailable(context)) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                Log.e(TAG, "setupCallToApi: this is how many we have in the cursor");
                TripDetails thisRequestedTrip = new TripDetails();
                thisRequestedTrip.setMyRouteId(cursor.getInt(cursor.getColumnIndex(MyRoutesContract.ID)));
                thisRequestedTrip.setMode(cursor.getString(cursor.getColumnIndex(MyRoutesContract.COLUMN_MODE)));
                thisRequestedTrip.setStopId(cursor.getString(cursor.getColumnIndex(MyRoutesContract.COLUMN_STOP)));
                thisRequestedTrip.setRouteId(cursor.getString(cursor.getColumnIndex(MyRoutesContract.COLUMN_ROUTE)));
                thisRequestedTrip.setDirectionId(cursor.getInt(cursor.getColumnIndex(MyRoutesContract.COLUMN_DIRECTION)));

                requestTripsByDetails(thisRequestedTrip, appWidgetManager, appWidgetId, context);

                cursor.moveToNext();
            }


        }
    }


    private void requestTripsByDetails(TripDetails aRequestedTrip, AppWidgetManager appWidgetManager,
                                       int appWidgetId, Context context){
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

        Callback<MbtaData> callback = new RouteDetailsCallback(stopId, routeId, mode, directionId,
                aRequestedTrip, appWidgetManager, appWidgetId, context);

        call.enqueue(callback);

    }

    class RouteDetailsCallback implements Callback<MbtaData> {

        private final String stopId;
        private final String routeId;
        private final String mode;
        private final int directionId;
        private final TripDetails aRequestedTrip;
        private final AppWidgetManager appWidgetManager;
        private final int appWidgetId;
        private final Context context;

        RouteDetailsCallback(String stopId, String routeId, String mode, int directionId,
                             TripDetails aRequestedTrip, AppWidgetManager appWidgetManager,
                             int appWidgetId, Context context) {
            this.stopId = stopId;
            this.routeId = routeId;
            this.mode = mode;
            this.directionId = directionId;
            this.aRequestedTrip = aRequestedTrip;
            this.appWidgetManager = appWidgetManager;
            this.appWidgetId = appWidgetId;
            this.context = context;
            Log.e(TAG, "RouteDetailsCallback: we init the callback");
        }

        @Override
        public void onResponse(Call<MbtaData> call, Response<MbtaData> response) {
            String whatWentWrong = response.message();
            MbtaData stopTimePredictions = response.body();
            setupTimePredictions(stopTimePredictions, mode, routeId, directionId,
                    aRequestedTrip);

            if(listPositionToRouteData.size() == cursor.getCount()) {
                updateAppWidget(context, appWidgetManager, appWidgetId, listPositionToRouteData);
            }


        }

        @Override
        public void onFailure(Call<MbtaData> call, Throwable t) {
            // Log error here since request failed
            Log.e(TAG, "onFailure: stuff got messed up: " + t.toString());
        }

    }

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


    /** Set the Adapter for out widget **/

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private static void setRemoteAdapter(Context context, @NonNull final RemoteViews views,
                                         ArrayList<TripDetails> displayData) {

        Intent remoteViewsIntent = new Intent(context, UpdateWidgetService.class);
        Bundle b = new Bundle();
        b.putParcelableArrayList("myRoutesData", displayData);
        b.putString("stringhere", "stringbuddy");
        Log.e(TAG, "setRemoteAdapter: " + displayData.size() );
        remoteViewsIntent.putExtra("bundle", b);
        views.setRemoteAdapter(R.id.widget_list_view, remoteViewsIntent);
    }


    /** Deprecated method, don't create this if you are not planning to support devices below 4.0 **/
    @SuppressWarnings("deprecation")
    private static void setRemoteAdapterV11(Context context, @NonNull final RemoteViews views,
                                            ArrayList<TripDetails> displayData) {
        views.setRemoteAdapter(0, R.id.widget_list_view,
                new Intent(context, UpdateWidgetService.class));
    }

    public boolean isNetWorkAvailable(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }
}

