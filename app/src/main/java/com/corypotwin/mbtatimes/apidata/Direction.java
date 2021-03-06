
package com.corypotwin.mbtatimes.apidata;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class Direction {


    @SerializedName("direction_id")
    @Expose
    private String directionId;
    @SerializedName("direction_name")
    @Expose
    private String directionName;
    @SerializedName("trip")
    @Expose
    private List<Trip> trip = new ArrayList<Trip>();
    @SerializedName("stop")
    @Expose
    private List<Stop> stop = new ArrayList<Stop>();

    /**
     * 
     * @return
     *     The directionId
     */
    public String getDirectionId() {
        return directionId;
    }

    /**
     * 
     * @param directionId
     *     The direction_id
     */
    public void setDirectionId(String directionId) {
        this.directionId = directionId;
    }

    /**
     * 
     * @return
     *     The directionName
     */
    public String getDirectionName() {
        return directionName;
    }

    /**
     * 
     * @param directionName
     *     The direction_name
     */
    public void setDirectionName(String directionName) {
        this.directionName = directionName;
    }

    /**
     * 
     * @return
     *     The trip
     */
    public List<Trip> getTrip() {
        return trip;
    }

    /**
     * 
     * @param trip
     *     The trip
     */
    public void setTrip(List<Trip> trip) {
        this.trip = trip;
    }

    /**
     *
     * @return
     * The stop
     */
    public List<Stop> getStop() {
        return stop;
    }

    /**
     *
     * @param stop
     * The stop
     */
    public void setStop(List<Stop> stop) {
        this.stop = stop;
    }

}
