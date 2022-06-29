package com.tvs.vitalsign.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.tvs.vitalsign.R;
import com.tvs.vitalsign.ui.analysis.VitalAnalysisModel;

import java.util.ArrayList;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import io.reactivex.rxjava3.subjects.PublishSubject;

public class MainActivityViewModel extends AndroidViewModel {

    private final static double initDoubleValue = 0.0;

    public MainActivityViewModel(@NonNull Application application) {
        super(application);
    }

    private final BehaviorSubject<Double> _heartRateValue = BehaviorSubject.createDefault(initDoubleValue);

    public Observable<Double> heartRateValue = _heartRateValue.hide();

    private final BehaviorSubject<Double> _oxygenSaturationValue = BehaviorSubject.createDefault(initDoubleValue);

    public Observable<Double> oxygenSaturationValue = _oxygenSaturationValue.hide();

    private final BehaviorSubject<Double> _respiratoryRateValue = BehaviorSubject.createDefault(initDoubleValue);

    public Observable<Double> respiratoryRateValue = _respiratoryRateValue.hide();

    private final BehaviorSubject<Double> _stressValue = BehaviorSubject.createDefault(1.0);

    public Observable<String> stressValue = _stressValue.hide()
            .map(_stressValue -> 0.5 <= _stressValue && _stressValue <= 2.0 ?
                    getApplication().getString(R.string.vital_stress_normal) :
                    getApplication().getString(R.string.vital_stress_danger));

    private final BehaviorSubject<Double> _hrvSdnnValue = BehaviorSubject.createDefault(initDoubleValue);

    public Observable<Double> hrvSdnnValue = _hrvSdnnValue.hide();

    private final BehaviorSubject<Double> _highestBloodPressureValue = BehaviorSubject.createDefault(initDoubleValue);

    public Observable<Double> highestBloodPressureValue = _highestBloodPressureValue.hide();

    private final BehaviorSubject<Double> _lowestBloodPressureValue = BehaviorSubject.createDefault(initDoubleValue);

    public Observable<Double> lowestBloodPressureValue = _lowestBloodPressureValue.hide();

    public void setVitalAnalysisData(VitalAnalysisModel model) {
        _heartRateValue.onNext(model.heartRate);
        _oxygenSaturationValue.onNext(model.oxygenSaturation);
        _respiratoryRateValue.onNext(model.respiratoryRate);
        _stressValue.onNext(model.stress);
        _hrvSdnnValue.onNext(model.hrvSdnn);
        _highestBloodPressureValue.onNext(model.highestBloodPressure);
        _lowestBloodPressureValue.onNext(model.lowestBloodPressure);
    }

    private PublishSubject<Boolean> _analysisFinish = PublishSubject.create();
    public Observable<Boolean> analysisFinish = _analysisFinish.hide();

    public void analysisFinish(boolean hasSymptom) {
        _analysisFinish.onNext(hasSymptom);
    }

    public ArrayList<String> symptoms = new ArrayList<>();

}
