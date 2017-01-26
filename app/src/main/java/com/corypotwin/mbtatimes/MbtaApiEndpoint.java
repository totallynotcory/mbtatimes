package com.corypotwin.mbtatimes;

import com.corypotwin.mbtatimes.apidata.CurrentLocationStops;
import com.corypotwin.mbtatimes.apidata.MbtaData;
import com.corypotwin.mbtatimes.apidata.StopsData;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by ctpotwin on 6/27/16.
 */
public interface MbtaApiEndpoint {
    // Request method and URL specified in the annotation
    // Callback for the parsed response is the last parameter

    // bus schedule by route #
    @GET("schedulebyroutes?format=json")
    Call<MbtaData> getSchedule(@Query("routes") String route);

    //  predictions by route
    @GET("predictionsbyroute?format=json")
    Call<MbtaData> getPredictionByRoute(@Query("routes") String route);

    //  predictions by route
    @GET("predictionsbyroutes?format=json")
    Call<MbtaData> getPredictionByRoutes(@Query("routes") String routes);

    // train schedule by stop and route
    @GET("schedulebystop?format=json")
    Call<MbtaData> getTrainScheduleByStop(@Query("stop") String stop,
                                          @Query("route") String route);



    // train schedule by stop and route
    @GET("stopsbylocation?format=json")
    Call<CurrentLocationStops> getStopsByLocation(@Query("api_key") String apiKey,
                                                  @Query("lat") String latitude,
                                                  @Query("lon") String longitude);

    //  predictions by route
    @GET("predictionsbystop?format=json")
    Call<MbtaData> getPredictionsByStop(@Query("api_key") String apiKey,
                                       @Query("stop") String stops);

    @GET("routes?format=json")
    Call<MbtaData> getRoutes(@Query("api_key") String apiKey);

    @GET("stopsbyroute?format=json")
    Call<StopsData> getStopsByRoute(@Query("api_key") String apiKey,
                                    @Query("route") String routeName);

}
