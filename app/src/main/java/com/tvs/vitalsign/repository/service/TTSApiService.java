package com.tvs.vitalsign.repository.service;

import io.reactivex.rxjava3.core.Single;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface TTSApiService {

    @GET("ttsstream?voice=signage&cache=True")
    Single<ResponseBody> getTTS(
            @Query("text") String text
    );
}
