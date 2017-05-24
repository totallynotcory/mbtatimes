package com.corypotwin.mbtatimes;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.corypotwin.mbtatimes.fragments.RouteDetailsFragment;

import java.util.ArrayList;

import static java.security.AccessController.getContext;

/**
 * Created by ctpotwin on 8/28/16.
 */
public class RouteDetailsRecyclerViewAdapter extends RecyclerView.Adapter<RouteDetailsRecyclerViewAdapter.ViewHolder> {
    private static String LOG_TAG = "MyRecyclerViewAdapter";
    private ArrayList<TripDetails> mDataset;
    private RouteDetailsFragment mCallingFragment;

    public static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView modeImage;
        TextView routeAndDirectionText;
        TextView timesText;
        TextView stationText;
        CheckBox deleteCheckbox;

        public ViewHolder(View itemView) {
            super(itemView);

            modeImage = (ImageView) itemView.findViewById(R.id.mode_image);
            routeAndDirectionText = (TextView) itemView.findViewById(R.id.route_and_direction_text);
            timesText = (TextView) itemView.findViewById(R.id.times_text);
            stationText = (TextView) itemView.findViewById(R.id.station_name);
            deleteCheckbox = (CheckBox) itemView.findViewById(R.id.delete_checkbox);
        }

    }

    public RouteDetailsRecyclerViewAdapter(ArrayList<TripDetails> myDataset,
                                           RouteDetailsFragment callingFragment) {
        mDataset = myDataset;
        mCallingFragment = callingFragment;
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
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final TripDetails thisTripDetails = mDataset.get(position);
        String routeAndDirectionString = thisTripDetails.getRouteAndDirection();
        String timesString = thisTripDetails.getTimeEstimates();
        String stationString = thisTripDetails.getStationName();
        Drawable transportationType = thisTripDetails.getModeImage();
        Boolean isSelected = thisTripDetails.getCheckboxState();

        holder.routeAndDirectionText.setText(routeAndDirectionString);
        holder.timesText.setText(timesString);
        holder.modeImage.setImageDrawable(transportationType);
        holder.stationText.setText(stationString);

        CheckBox checkbox = holder.deleteCheckbox;

        if(mCallingFragment.showClickboxes) {
            checkbox.setVisibility(View.VISIBLE);
        } else {
            checkbox.setVisibility(View.GONE);
        }

        //in some cases, it will prevent unwanted situations
        holder.deleteCheckbox.setOnCheckedChangeListener(null);

        //if true, your checkbox will be selected, else unselected
        holder.deleteCheckbox.setChecked(isSelected);

        holder.deleteCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //set your object's last status
                thisTripDetails.setCheckboxState(isChecked);
            }
        });

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
