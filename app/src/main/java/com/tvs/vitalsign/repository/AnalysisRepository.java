package com.tvs.vitalsign.repository;

import android.content.Context;

import com.tvs.vitalsign.repository.database.AppDatabase;
import com.tvs.vitalsign.repository.database.entity.AnalysisEntity;
import com.tvs.vitalsign.ui.analysis.VitalAnalysisModel;

import java.util.List;

import io.reactivex.rxjava3.core.Single;

public class AnalysisRepository {
    private AppDatabase db;

    public AnalysisRepository(Context applicationContext) {
        db = AppDatabase.getInstance(applicationContext);
    }

    public void saveAnalysisEntity(VitalAnalysisModel vitalAnalysisModel) {
        AnalysisEntity analysisEntity = new AnalysisEntity(
                vitalAnalysisModel.heartRate,
                vitalAnalysisModel.oxygenSaturation,
                vitalAnalysisModel.respiratoryRate,
                vitalAnalysisModel.stress,
                vitalAnalysisModel.hrvSdnn,
                vitalAnalysisModel.highestBloodPressure,
                vitalAnalysisModel.lowestBloodPressure
        );
        db.analysisDao().insertAnalysisEntity(analysisEntity);
    }

    public Single<AnalysisEntity> getLatestAnalysis() {
        return db.analysisDao().getLatestAnalysis();
    }

    public Single<List<AnalysisEntity>> getAnalysis(Long utcTimeFrom, Long utcTimeTo) {
        return db.analysisDao().getAnalysis(utcTimeFrom, utcTimeTo);
    }

    public Single<AnalysisEntity> getAnalysis(Long utcTime) {
        return db.analysisDao().getAnalysis(utcTime);
    }


}
