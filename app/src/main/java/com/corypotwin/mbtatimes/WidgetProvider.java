package com.corypotwin.mbtatimes;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import java.util.Random;

import static android.content.ContentValues.TAG;
import static android.webkit.ConsoleMessage.MessageLevel.LOG;

/**
 * Created by ctpotwin on 6/25/17.
 */

public class WidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

            Log.w("paaah", "onUpdate method called");
            // Get all ids
            ComponentName thisWidget = new ComponentName(context,
                    WidgetProvider.class);
            int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

            // Build the intent to call the service
            Intent intent = new Intent(context.getApplicationContext(),
                    UpdateWidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);

            // Update the widgets via the service
            context.startService(intent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }

}
