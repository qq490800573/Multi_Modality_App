package com.tvs.vitalsign;

import android.app.Application;
import android.content.Context;

import androidx.lifecycle.ViewModelProvider;

public class VitalSignApp extends Application {
    public static ViewModelProvider.AndroidViewModelFactory factory = null;
    public static Context context;

    public VitalSignApp() {
        factory = new ViewModelProvider.AndroidViewModelFactory(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this.getApplicationContext();
    }
}
