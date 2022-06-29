package com.tvs.vitalsign.ui.vital_sensor

import com.google.gson.annotations.SerializedName

data class SensorData(
    @SerializedName("headder")
    val header: String,
    @SerializedName("Time")
    val timestamp: String,
    @SerializedName("ID")
    val id: String,
    @SerializedName("Sensortype")
    val sensorType: String,
    @SerializedName("BodyTemp")
    val bodyTemp: Double,
    @SerializedName("HeartRate")
    val heartRate: Double,
    @SerializedName("BreathRate")
    val breath: Double,
    @SerializedName("Motion")
    val motion: Int,
    @SerializedName("xRangePlot")
    val xRange: List<Double>,
    @SerializedName("yRangePlot")
    val yValue: List<Double>,
) {
    constructor() : this(
        "",
        "",
        "",
        "",
        0.0,
        0.0,
        0.0,
        0,
        emptyList<Double>(),
        emptyList<Double>()
    )
}
