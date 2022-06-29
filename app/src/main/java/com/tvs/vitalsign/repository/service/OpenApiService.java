package com.tvs.vitalsign.repository.service;

import com.tvs.vitalsign.repository.response.OpenApiResponse;

import io.reactivex.rxjava3.core.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface OpenApiService {

    String serviceKey = "4ValebMwmcYb%2BQxaf%2BWzMDQI%2FPvOCB8Kt%2FLVtlU3c3AlFXR8%2FBaBJppd%2Fn0GFI%2Bykp1x3YJQzqkQ%2FansNPLE9A%3D%3D";
    String apiURL = "Covid19/getCovid19InfStateJson?serviceKey="
            + serviceKey
            + "&pageNo=1&numOfRows=10&_type=json";

    @GET(apiURL)
    Single<OpenApiResponse> getLatestCOVID19Status(
            @Query("startCreateDt") String startCreateDt,
            @Query("endCreateDt") String endCreateDt
    );
}
