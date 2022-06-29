package com.tvs.vitalsign.ui.analysis;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;

import com.tvs.vitalsign.repository.AnalysisRepository;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.SingleSubject;

public class VitalAnalysisViewModel extends AndroidViewModel {

    private final boolean hasSymptom = true;
    private final boolean normal = false;

    private AnalysisRepository analysisRepository;

    private VitalAnalysisModel vitalAnalysisModel;
    private SingleSubject<Boolean> vitalAnalysisFinish = SingleSubject.create();
    private SingleSubject<Boolean> surveyFinish = SingleSubject.create();
    public Single<Boolean> analysisResult = Single.zip(vitalAnalysisFinish, surveyFinish,
            (vitalAnalysisFinish, surveyFinish) -> vitalAnalysisFinish || surveyFinish);

    public VitalAnalysisViewModel(Application application) {
        super(application);
        analysisRepository = new AnalysisRepository(application.getApplicationContext());
    }

    public void setVitalAnalysisModel(VitalAnalysisModel model) {
        vitalAnalysisModel = model;
    }

    public void vitalAnalysisFinish() {
        if (vitalAnalysisModel != null) {
            Completable.fromAction(() -> analysisRepository.saveAnalysisEntity(vitalAnalysisModel))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> {
                        if (vitalAnalysisModel.heartRate < 60 ||
                                vitalAnalysisModel.heartRate > 100 ||
                                vitalAnalysisModel.oxygenSaturation < 95) {
                            vitalAnalysisFinish.onSuccess(hasSymptom);
                        } else {
                            vitalAnalysisFinish.onSuccess(normal);
                        }
                    }, Throwable::printStackTrace);
        }
    }

    public void surveyFinish(boolean hasSymptom) {
        surveyFinish.onSuccess(hasSymptom);
    }

}
