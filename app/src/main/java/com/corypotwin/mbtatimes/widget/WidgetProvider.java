package com.corypotwin.mbtatimes.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Button;
import android.widget.RemoteViews;

import com.corypotwin.mbtatimes.R;
import com.corypotwin.mbtatimes.activities.CriteriaSearch;
import com.corypotwin.mbtatimes.activities.MainActivity;
import com.corypotwin.mbtatimes.activities.MyRoutes;
import com.corypotwin.mbtatimes.activities.NearbyStations;

import static android.content.ContentValues.TAG;
import static java.security.AccessController.getContext;

/**
 * Created by ctpotwin on 6/25/17.
 */

public class WidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                R.layout.app_widget);

        Intent myRoutes = new Intent(context, MyRoutes.class);
        PendingIntent myRoutesPendingIntent = PendingIntent.getActivity(context, 0, myRoutes, 0);
        remoteViews.setOnClickPendingIntent(R.id.my_routes_button, myRoutesPendingIntent);

        Intent currentLocation = new Intent(context, NearbyStations.class);
        PendingIntent currentLocationPendingIntent = PendingIntent.getActivity(context, 0, currentLocation, 0);
        remoteViews.setOnClickPendingIntent(R.id.location_button, currentLocationPendingIntent);

        Intent searchStations = new Intent(context, CriteriaSearch.class);
        PendingIntent searchStationsPendingIntent = PendingIntent.getActivity(context, 0, searchStations, 0);
        remoteViews.setOnClickPendingIntent(R.id.search_button, searchStationsPendingIntent);

        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);

    }

}
