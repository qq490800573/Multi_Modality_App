package com.tvs.vitalsign.ui.history;

class HistoryDataModel {
    public Double value;
    public long utcTime;

    public HistoryDataModel(
            Double value,
            long utcTime
    ) {
        this.value = value;
        this.utcTime = utcTime;
    }
}
