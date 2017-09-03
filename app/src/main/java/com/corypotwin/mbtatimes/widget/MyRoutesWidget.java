package com.corypotwin.mbtatimes.widget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.NonNull;
import android.widget.RemoteViews;

import com.corypotwin.mbtatimes.R;

import java.text.DateFormat;
import java.util.Date;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Implementation of App Widget functionality.
 */
public class MyRoutesWidget extends AppWidgetProvider {

    public static String WIDGET_BUTTON = "MY_ROUTES_WIDGET.WIDGET_BUTTON";
    public static String APP_REFRESH_ID = "REFRESH_ID";


    @Override
    public void onReceive(Context context, Intent intent) {
        if (WIDGET_BUTTON.equals(intent.getAction())) {
            int[] appId = new int[] {intent.getIntExtra(APP_REFRESH_ID, 0)};
            onUpdate(context, AppWidgetManager.getInstance(context), appId);
        }
        super.onReceive(context, intent);
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.my_routes_widget);

        // Create the Click Intent
        Intent clickIntent = new Intent(WIDGET_BUTTON);
        clickIntent.putExtra(APP_REFRESH_ID, appWidgetId);
        PendingIntent clickPendingIntent = PendingIntent.getBroadcast(context, 0, clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.refresh_button, clickPendingIntent);

        // Setup Last Update: text
        views.setTextViewText(R.id.last_update_text, "Last Update: " +
                DateFormat.getDateTimeInstance().format(new Date()));

        views.setEmptyView(R.id.widget_list_view, R.id.no_data_to_display);

        if(isNetWorkAvailable(context)) {
//            views.setViewVisibility(R.id.widget_list_view, VISIBLE);
//            views.setViewVisibility(R.id.no_data_to_display, GONE);
            views.setTextViewText(R.id.no_data_to_display,
                    context.getResources().getString(R.string.no_saved_routes));
            // Setup the remote adapter
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                setRemoteAdapter(context, views);
            } else {
                setRemoteAdapterV11(context, views);
            }

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_list_view);
        } else {
            views.setTextViewText(R.id.no_data_to_display,
                    context.getResources().getString(R.string.no_internet_connection));

//            views.setViewVisibility(R.id.widget_list_view, GONE);
//            views.setViewVisibility(R.id.no_data_to_display, VISIBLE);

        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            // Instruct the widget manager to update the widget
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    /** Set the Adapter for out widget **/

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private static void setRemoteAdapter(Context context, @NonNull final RemoteViews views) {

        Intent remoteViewsIntent = new Intent(context, UpdateWidgetService.class);
        views.setRemoteAdapter(R.id.widget_list_view, remoteViewsIntent);

    }


    /** Deprecated method, don't create this if you are not planning to support devices below 4.0 **/
    @SuppressWarnings("deprecation")
    private static void setRemoteAdapterV11(Context context, @NonNull final RemoteViews views) {
        views.setRemoteAdapter(0, R.id.widget_list_view,
                new Intent(context, UpdateWidgetService.class));
    }


    public static boolean isNetWorkAvailable(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }


}

