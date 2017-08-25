package com.corypotwin.mbtatimes.widget;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.corypotwin.mbtatimes.R;
import com.corypotwin.mbtatimes.activities.CriteriaSearch;
import com.corypotwin.mbtatimes.activities.MainActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.Random;

/**
 * Created by ctpotwin on 6/27/17.
 */

public class UpdateWidgetService extends RemoteViewsService {
    private static final String LOG = "de.vogella.android.widget.example";

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new WidgetDataProvider(this,intent);
    }
}