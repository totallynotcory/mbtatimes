package com.corypotwin.mbtatimes;

import android.util.Log;

import com.corypotwin.mbtatimes.apidata.MbtaData;

import java.util.Objects;

import okhttp3.HttpUrl;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * Created by ctpotwin on 8/7/16.
 */
public class Utility {

    public void Utility(){

    }

    public Call<MbtaData> apiCall(String typeOfCallToMake, String route, String stop){
        final String TAG = "Utility API Call";

        HttpUrl BASE_URL = HttpUrl.parse("http://realtime.mbta.com/developer/api/v2/");

        HttpUrl url = BASE_URL.newBuilder()
                .addQueryParameter("api_key", SecretApiKeyFile.getKey())
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        MbtaApiEndpoint apiService =
                retrofit.create(MbtaApiEndpoint.class);

        Call<MbtaData> call = determineAndCallProperUrl(apiService, typeOfCallToMake, route, stop);

        return call;
        //  Code to use for response logic
//        call.enqueue(new Callback<MbtaData>() {
//            @Override
//            public void onResponse(Call<MbtaData> call, Response<MbtaData> response) {
//                int statusCode = response.code();
//                MbtaData returnData = response.body();
//
//            }
//
//            @Override
//            public void onFailure(Call<MbtaData> call, Throwable t) {
//                // Log error here since request failed
//                Log.e(TAG, "onFailure: stuff got messed up: " + t.toString());
//            }
//        });

    }

    private static Call<MbtaData> determineAndCallProperUrl(MbtaApiEndpoint apiService, String typeOfCall,
                                                     String route, String stop){
        if(Objects.equals("routes",typeOfCall)) {
            return apiService.getRoutes(SecretApiKeyFile.getKey());
        } else if(Objects.equals("scheduleByRoutes", typeOfCall)){
            return apiService.getSchedule(route);
        } else if(Objects.equals("predictionByRoute", typeOfCall)){
            return apiService.getPredictionByRoute(route);
        } else if(Objects.equals("predictionByRoutes", typeOfCall)){
            return apiService.getPredictionByRoutes(route);
        } else if(Objects.equals("scheduleByStop", typeOfCall)){
            return apiService.getTrainScheduleByStop(stop, route);
        } else {
            return null;
        }

    }

}


