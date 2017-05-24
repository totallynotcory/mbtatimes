package com.corypotwin.mbtatimes;

import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ctpotwin on 8/27/16.
 */
public class TripDetails implements Parcelable {

    String stopId;
    String routeId;
    String mode;
    int directionId;
    String routeAndDirection;
    String stationName;
    Boolean checkboxState = false;
    int myRouteId;

    Drawable modeImage;

    String timeEstimates;

    public TripDetails() {
    }

    public TripDetails(Parcel in) {
        stopId = in.readString();
        routeId = in.readString();
        mode = in.readString();
        directionId = in.readInt();
        routeAndDirection = in.readString();
        stationName = in.readString();
    }

    public static final Creator<TripDetails> CREATOR = new Creator<TripDetails>() {
        @Override
        public TripDetails createFromParcel(Parcel in) {
            return new TripDetails(in);
        }

        @Override
        public TripDetails[] newArray(int size) {
            return new TripDetails[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(stopId);
        dest.writeString(routeId);
        dest.writeString(mode);
        dest.writeInt(directionId);
        dest.writeString(routeAndDirection);
    }

    public String getStopId() {
        return stopId;
    }

    public void setStopId(String stopId) {
        this.stopId = stopId;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public int getDirectionId() {
        return directionId;
    }

    public void setDirectionId(int directionId) {
        this.directionId = directionId;
    }

    public String getRouteAndDirection() {
        return routeAndDirection;
    }

    public void setRouteAndDirection(String routeAndDirection) {
        this.routeAndDirection = routeAndDirection;
    }

    public String getTimeEstimates() {
        return timeEstimates;
    }

    public void setTimeEstimates(String timeEstimates) {
        this.timeEstimates = timeEstimates;
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public Drawable getModeImage() {
        return modeImage;
    }

    public void setModeImage(Drawable modeImage) {
        this.modeImage = modeImage;
    }

    public Boolean getCheckboxState() {
        return checkboxState;
    }

    public void setCheckboxState(Boolean checkboxState) {
        this.checkboxState = checkboxState;
    }

    public int getMyRouteId() {
        return myRouteId;
    }

    public void setMyRouteId(int myRouteId) {
        this.myRouteId = myRouteId;
    }

}
