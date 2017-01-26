
package com.corypotwin.mbtatimes.apidata;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class Mode {

    @SerializedName("route_type")
    @Expose
    private String routeType;
    @SerializedName("mode_name")
    @Expose
    private String modeName;
    @SerializedName("route")
    @Expose
    private List<Route> route = new ArrayList<Route>();

    /**
     * 
     * @return
     *     The routeType
     */
    public String getRouteType() {
        return routeType;
    }

    /**
     * 
     * @param routeType
     *     The route_type
     */
    public void setRouteType(String routeType) {
        this.routeType = routeType;
    }

    /**
     * 
     * @return
     *     The modeName
     */
    public String getModeName() {
        return modeName;
    }

    /**
     * 
     * @param modeName
     *     The mode_name
     */
    public void setModeName(String modeName) {
        this.modeName = modeName;
    }

    /**
     * 
     * @return
     *     The route
     */
    public List<Route> getRoute() {
        return route;
    }

    /**
     * 
     * @param route
     *     The route
     */
    public void setRoute(List<Route> route) {
        this.route = route;
    }

}
