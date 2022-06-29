package com.tvs.vitalsign.repository.database.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.time.Instant;
import java.util.Calendar;

@Entity(tableName = "analysis")
public class AnalysisEntity {
    @PrimaryKey
    @ColumnInfo(name = "utc_yyyy_MM_dd_date")
    @NonNull
    public String utc_yyyyMMddDate;

    @ColumnInfo(name = "utc_time")
    public Long utcTime;

    @ColumnInfo(name = "heart_rate")
    public Double heartRate;

    @ColumnInfo(name = "oxygen_saturation")
    public Double oxygenSaturation;

    @ColumnInfo(name = "respiratory_rate")
    public Double respiratoryRate;

    @ColumnInfo(name = "stress")
    public Double stress;

    @ColumnInfo(name = "hrv_sdnn")
    public Double hrvSdnn;

    @ColumnInfo(name = "highest_blood_pressure")
    public Double highestBloodPressure;

    @ColumnInfo(name = "lowest_blood_pressure")
    public Double lowestBloodPressure;

    public AnalysisEntity(Double heartRate, Double oxygenSaturation,
                          Double respiratoryRate, Double stress, Double hrvSdnn,
                          Double highestBloodPressure, Double lowestBloodPressure) {
        long utc = Instant.now().toEpochMilli();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(utc);
        this.utc_yyyyMMddDate = String.format("%04d%02d%02d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        this.utcTime = utc;
        this.heartRate = heartRate;
        this.oxygenSaturation = oxygenSaturation;
        this.respiratoryRate = respiratoryRate;
        this.stress = stress;
        this.hrvSdnn = hrvSdnn;
        this.highestBloodPressure = highestBloodPressure;
        this.lowestBloodPressure = lowestBloodPressure;
    }
}
