package com.corypotwin.mbtatimes.widget;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import com.corypotwin.mbtatimes.R;
import com.corypotwin.mbtatimes.activities.CriteriaSearch;
import com.corypotwin.mbtatimes.activities.MainActivity;

import java.util.Random;

/**
 * Created by ctpotwin on 6/27/17.
 */

public class UpdateWidgetService extends Service {
    private static final String LOG = "de.vogella.android.widget.example";

    @Override
    public void onStart(Intent intent, int startId) {
//        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this
//                .getApplicationContext());
//
//        int[] allWidgetIds = intent
//                .getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
//
//        for (int widgetId : allWidgetIds) {
//            // create some random data
//            int number = (new Random().nextInt(100));
//
//            RemoteViews remoteViews = new RemoteViews(this
//                    .getApplicationContext().getPackageName(),
//                    R.layout.app_widget);
//
//            // Set the text
//            remoteViews.setTextViewText(R.id.textView,
//                    "Random: " + String.valueOf(number));
//
//            Intent inet = new Intent(this, MainActivity.class);
//            inet.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            PendingIntent pIntentNetworkInfo = PendingIntent.getActivity(this, 0,
//                    inet, 0);
//            remoteViews.setOnClickPendingIntent(R.id.actionButton, pIntentNetworkInfo);

            // Register an onClickListener
//            Intent clickIntent = new Intent(this, CriteriaSearch.class);
//            Intent clickIntent = new Intent(this.getApplicationContext(),
//                    WidgetProvider.class);

//            clickIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
//            clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
//                    allWidgetIds);

//            PendingIntent pendingIntent = PendingIntent.getBroadcast(
//                    getApplicationContext(), 0, clickIntent,
//                    PendingIntent.FLAG_UPDATE_CURRENT);
//            remoteViews.setOnClickPendingIntent(R.id.actionButton, pendingIntent);
//            appWidgetManager.updateAppWidget(widgetId, remoteViews);
//        }
//        stopSelf();

        super.onStart(intent, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}