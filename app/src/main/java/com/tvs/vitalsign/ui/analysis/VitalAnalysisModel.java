package com.tvs.vitalsign.ui.analysis;

public class VitalAnalysisModel {
    public double heartRate;
    public double oxygenSaturation;
    public double respiratoryRate;
    public double stress = 1.0; // normal is default
    public double hrvSdnn;
    public double highestBloodPressure;
    public double lowestBloodPressure;

    public VitalAnalysisModel() {

    }

    public VitalAnalysisModel(double heartRate, double oxygenSaturation,
                              double respiratoryRate, double stress, double hrvSdnn,
                              double highestBloodPressure, double lowestBloodPressure) {
        this.heartRate = heartRate;
        this.oxygenSaturation = oxygenSaturation;
        this.respiratoryRate = respiratoryRate;
        this.stress = stress;
        this.hrvSdnn = hrvSdnn;
        this.highestBloodPressure = highestBloodPressure;
        this.lowestBloodPressure = lowestBloodPressure;
    }
}
