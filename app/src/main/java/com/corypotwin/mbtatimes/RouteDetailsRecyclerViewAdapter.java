package com.corypotwin.mbtatimes;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by ctpotwin on 8/28/16.
 */
public class RouteDetailsRecyclerViewAdapter extends RecyclerView.Adapter<RouteDetailsRecyclerViewAdapter.ViewHolder> {
    private static String LOG_TAG = "MyRecyclerViewAdapter";
    private ArrayList<TripDetails> mDataset;

    public static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView modeImage;
        TextView routeAndDirectionText;
        TextView timesText;
        TextView stationText;

        public ViewHolder(View itemView) {
            super(itemView);
            modeImage = (ImageView) itemView.findViewById(R.id.mode_image);
            routeAndDirectionText = (TextView) itemView.findViewById(R.id.route_and_direction_text);
            timesText = (TextView) itemView.findViewById(R.id.times_text);
            stationText = (TextView) itemView.findViewById(R.id.station_name);
        }

    }

    public RouteDetailsRecyclerViewAdapter(ArrayList<TripDetails> myDataset) {
        mDataset = myDataset;
    }

    @Override
    public RouteDetailsRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                               int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_route_details_card, parent, false);

        ViewHolder dataObjectHolder = new ViewHolder(view);
        return dataObjectHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String routeAndDirectionString = mDataset.get(position).getRouteAndDirection();
        String timesString = mDataset.get(position).getTimeEstimates();
        String stationString = mDataset.get(position).getStationName();

        holder.routeAndDirectionText.setText(routeAndDirectionString);
        holder.timesText.setText(timesString);
//      holder.modeImage.setImage();
        holder.stationText.setText(stationString);
    }

    public void addItem(TripDetails dataObj, int index) {
        mDataset.add(index, dataObj);
        notifyItemInserted(index);
    }

    public void deleteItem(int index) {
        mDataset.remove(index);
        notifyItemRemoved(index);
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

}
