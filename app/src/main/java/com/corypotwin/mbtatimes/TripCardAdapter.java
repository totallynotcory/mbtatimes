package com.corypotwin.mbtatimes;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.TextView;

import com.corypotwin.mbtatimes.apidata.MbtaData;

/**
 * Created by ctpotwin on 7/27/16.
 */
public class TripCardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView mTextView;
        public ViewHolder(TextView v) {
            super(v);
            mTextView = v;
        }
    }

    public MbtaData mMbtaData;

    public TripCardAdapter(MbtaData fullDataSet){
        mMbtaData = fullDataSet;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return mMbtaData.getMode().size();
    }
}
