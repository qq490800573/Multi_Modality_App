package com.tvs.vitalsign.ui.history;

import androidx.annotation.StringRes;

import com.tvs.vitalsign.R;

public enum HistoryDataType {

    HEART_RATE(R.string.heart_rate),
    OXYGEN_SATURATION(R.string.oxygen_saturation),
    RESPIRATORY_RATE(R.string.respiratory_rate),
    STRESS(R.string.stress),
    HRV_SDNN(R.string.hrv_sdnn),
    BLOOD_PRESSURE(R.string.blood_pressure);

    public @StringRes int name;
    HistoryDataType(@StringRes int name) {
        this.name = name;
    }

}