package com.corypotwin.mbtatimes.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.corypotwin.mbtatimes.R;
import com.corypotwin.mbtatimes.TripDetails;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;


/**
 * Created by ctpotwin on 8/15/17.
 */

public class WidgetDataProvider implements RemoteViewsService.RemoteViewsFactory {

    private Context context;
    private Cursor cursor;
    private Intent intent;
    private ArrayList<TripDetails> listPositionToRouteData;

    //For obtaining the activity's context and intent
    public WidgetDataProvider(Context context, Intent intent) {
        this.context = context;
        this.intent = intent;
        Bundle b = intent.getBundleExtra("bundle");
        listPositionToRouteData = b.getParcelableArrayList("myRoutesData");
        Log.e(TAG, "WidgetDataProvider: get on wid cha bad self" + listPositionToRouteData.size());
    }


    @Override
    public void onCreate() {
    }

    @Override
    public void onDataSetChanged() {
    }


    @Override
    public void onDestroy() {
    }


    @Override
    public int getCount() {
        if(listPositionToRouteData != null) {
            return listPositionToRouteData.size();
        } else {
            return 0;
        }
    }

    @Override
    public RemoteViews getViewAt(int i) {
        /** Populate your widget's single list item **/
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_row);

        TripDetails aRequestedTrip = listPositionToRouteData.get(i);

        remoteViews.setTextViewText(R.id.route_and_direction_text, aRequestedTrip.getRouteAndDirection());
        remoteViews.setTextViewText(R.id.times_text, aRequestedTrip.getTimeEstimates());
        remoteViews.setTextViewText(R.id.station_name, aRequestedTrip.getStationName());
        remoteViews.setImageViewResource(R.id.mode_image, extractModeImage(aRequestedTrip.getMode()));

        return remoteViews;
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

}


