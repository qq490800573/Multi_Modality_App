package com.tvs.vitalsign.repository.service;

import com.tvs.vitalsign.repository.response.STTResponse;

import io.reactivex.rxjava3.core.Single;
import okhttp3.MultipartBody;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface STTApiService {

    @Multipart
    @POST("stt")
    Single<STTResponse> getSTT(@Part MultipartBody.Part filePart);

}
