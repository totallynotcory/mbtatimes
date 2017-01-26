package com.corypotwin.mbtatimes.apidata;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CurrentLocationStops {

    @SerializedName("stop")
    @Expose
    private List<Stop> stop = null;

    public List<Stop> getStop() {
        return stop;
    }

    public void setStop(List<Stop> stop) {
        this.stop = stop;
    }

}