package com.tvs.vitalsign.repository;

import com.tvs.vitalsign.repository.model.COVID19StatusModel;
import com.tvs.vitalsign.repository.response.COVID19Status;
import com.tvs.vitalsign.repository.service.API;
import com.tvs.vitalsign.repository.service.OpenApiService;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Single;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class Covid19Repository {
    private OpenApiService service;

    public Covid19Repository() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addNetworkInterceptor(interceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        service = new Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(API.openApiBaseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build()
                .create(OpenApiService.class);
    }

    public Single<COVID19StatusModel> getLatestCOVID19Status() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        c.add(Calendar.DAY_OF_MONTH, -1);
        String dayBefore = formatter.format(new Date(c.getTimeInMillis()));
        String today = formatter.format(new Date(System.currentTimeMillis()));

        return service.getLatestCOVID19Status(dayBefore, today)
                .onErrorResumeNext(throwable -> {
                    String twoDaysAgo = formatter.format(new Date(c.getTimeInMillis()));
                    c.add(Calendar.DAY_OF_MONTH, -1);
                    String theDayBefore = formatter.format(new Date(c.getTimeInMillis()));
                    return service.getLatestCOVID19Status(theDayBefore, twoDaysAgo);
                })
                .map(openApiResponse -> {
                    List<COVID19Status> statuses = openApiResponse.response.body.items.statuses;
                    COVID19Status todayStatus = statuses.get(0);
                    COVID19Status dayBeforeStatus = statuses.get(1);
                    return new COVID19StatusModel(
                            todayStatus.decideCnt,
                            (todayStatus.decideCnt - dayBeforeStatus.decideCnt),
                            todayStatus.clearCnt,
                            (todayStatus.clearCnt - dayBeforeStatus.clearCnt),
                            todayStatus.deathCnt,
                            (todayStatus.deathCnt - dayBeforeStatus.deathCnt)
                    );
                });
    }
}
