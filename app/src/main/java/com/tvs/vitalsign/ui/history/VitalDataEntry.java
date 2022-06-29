package com.tvs.vitalsign.ui.history;

import androidx.annotation.StringRes;

import com.github.mikephil.charting.data.Entry;

class VitalDataEntry extends Entry {
    public @StringRes int unitTextRes;
    public long utcTime;

    public VitalDataEntry(float x, float y, @StringRes int unitTextRes, long utcTime) {
        super(x, y);
        this.unitTextRes = unitTextRes;
        this.utcTime = utcTime;
    }
}
