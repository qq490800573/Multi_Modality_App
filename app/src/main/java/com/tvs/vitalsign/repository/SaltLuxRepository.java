package com.tvs.vitalsign.repository;

import android.content.Context;

import com.tvs.vitalsign.repository.service.API;
import com.tvs.vitalsign.repository.service.STTApiService;
import com.tvs.vitalsign.repository.service.TTSApiService;
import com.tvs.vitalsign.repository.util.VoiceFileUtils;

import java.io.File;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Notification;
import io.reactivex.rxjava3.core.Single;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class SaltLuxRepository {
    private TTSApiService ttsApiService;
    private STTApiService sttApiService;
    private Context context;

    public SaltLuxRepository(Context context) {
        this.context = context;
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addNetworkInterceptor(interceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        ttsApiService = new Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(API.smartZinyBaseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build()
                .create(TTSApiService.class);
        sttApiService = new Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(API.smartZinySTTBaseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build()
                .create(STTApiService.class);
    }

    public Single<Notification<String>> getTTS(String id, String text) {
        File cachedWavFile = VoiceFileUtils.getVoiceFile(context, id);
        if (cachedWavFile.exists()) {
            return Single.just(Notification.createOnNext(cachedWavFile.getAbsolutePath()));
        }
        return ttsApiService.getTTS(text).map(body -> {
            if (VoiceFileUtils.writeWavFileIntoCacheDir(cachedWavFile, body.byteStream(), body.contentLength())) {
                return Notification.createOnNext(cachedWavFile.getAbsolutePath());
            } else {
                return Notification.createOnError(new Throwable("wav file write fail!"));
            }
        });
    }

    public Single<Notification<String>> getSTT(String filePath) {
        File myVoiceFile = new File(filePath);
        if (!myVoiceFile.exists())
            return Single.just(Notification.createOnError(new Throwable("voice file no exist")));
        MultipartBody.Part part = MultipartBody.Part.createFormData("audioData", myVoiceFile.getName(), RequestBody.create(myVoiceFile, MediaType.parse("audio/wav")));
        return sttApiService.getSTT(part).map(response -> {
            if (response.errorCode == -1) {
                return Notification.createOnError(new Throwable("Wav file invalid or unknown server error"));
            } else {
                return Notification.createOnNext(response.response.text);
            }
        });
    }

}
