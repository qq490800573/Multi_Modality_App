package com.tvs.vitalsign.ui.history;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.tvs.vitalsign.R;
import com.tvs.vitalsign.repository.AnalysisRepository;
import com.tvs.vitalsign.repository.database.entity.AnalysisEntity;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class HistoryViewModel extends AndroidViewModel {

    private static final String TAG = HistoryViewModel.class.getSimpleName();

    private final CompositeDisposable disposable = new CompositeDisposable();
    private final AnalysisRepository analysisRepository;

    private final MutableLiveData<String> _latestTestDate = new MutableLiveData<>("-");
    public LiveData<String> latestTestDate = _latestTestDate;
    private final MutableLiveData<String> _heartRateValue = new MutableLiveData<>("-");
    public LiveData<String> heartRateValue = _heartRateValue;
    private final MutableLiveData<String> _oxygenSaturationValue = new MutableLiveData<>("-");
    public LiveData<String> oxygenSaturationValue = _oxygenSaturationValue;
    private final MutableLiveData<String> _respiratoryRateValue = new MutableLiveData<>("-");
    public LiveData<String> respiratoryRateValue = _respiratoryRateValue;
    private final MutableLiveData<String> _stressValue = new MutableLiveData<>("-");
    public LiveData<String> stressValue = _stressValue;
    private final MutableLiveData<String> _hrvSdnnValue = new MutableLiveData<>("-");
    public LiveData<String> hrvSdnnValue = _hrvSdnnValue;
    private final MutableLiveData<String> _highestBloodPressureValue = new MutableLiveData<>("-");
    public LiveData<String> highestBloodPressureValue = _highestBloodPressureValue;
    private final MutableLiveData<String> _lowestBloodPressureValue = new MutableLiveData<>("-");
    public LiveData<String> lowestBloodPressureValue = _lowestBloodPressureValue;

    public HistoryViewModel(Application application) {
        super(application);
        analysisRepository = new AnalysisRepository(application.getApplicationContext());
    }

    private Long utcDateFrom = 0L;
    private Long utcDateTo = 0L;

    public void setDate(Long utcDateFrom, Long utcDateTo) {
        this.utcDateFrom = utcDateFrom;
        this.utcDateTo = utcDateTo;
    }

    private final MutableLiveData<List<HistoryDataModel>> _heartRateValues = new MutableLiveData<>(Collections.emptyList());
    public LiveData<List<HistoryDataModel>> heartRateValues = _heartRateValues;
    private final MutableLiveData<List<HistoryDataModel>> _oxygenSaturationValues = new MutableLiveData<>(Collections.emptyList());
    public LiveData<List<HistoryDataModel>> oxygenSaturationValues = _oxygenSaturationValues;
    private final MutableLiveData<List<HistoryDataModel>> _respiratoryRateValues = new MutableLiveData<>(Collections.emptyList());
    public LiveData<List<HistoryDataModel>> respiratoryRateValues = _respiratoryRateValues;
    private final MutableLiveData<List<HistoryDataModel>> _stressValues = new MutableLiveData<>(Collections.emptyList());
    public LiveData<List<HistoryDataModel>> stressValues = _stressValues;
    private final MutableLiveData<List<HistoryDataModel>> _hrvSdnnValues = new MutableLiveData<>(Collections.emptyList());
    public LiveData<List<HistoryDataModel>> hrvSdnnValues = _hrvSdnnValues;
    private final MutableLiveData<List<HistoryDataModel>> _highestBloodPressureValues = new MutableLiveData<>(Collections.emptyList());
    public LiveData<List<HistoryDataModel>> highestBloodPressureValues = _highestBloodPressureValues;
    private final MutableLiveData<List<HistoryDataModel>> _lowestBloodPressureValues = new MutableLiveData<>(Collections.emptyList());
    public LiveData<List<HistoryDataModel>> lowestBloodPressureValues = _lowestBloodPressureValues;

    public void loadAnalysis(HistoryDataType type) {
        Log.d(TAG, "getAnalysis utcDateFrom: " + utcDateFrom + " utcDateTo: " + utcDateTo);
        disposable.add(analysisRepository.getAnalysis(utcDateFrom, utcDateTo)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(analysisEntities -> {
                    Log.d(TAG, "getAnalysis type:" + type.name);
                    switch (type) {
                        case HEART_RATE:
                            _heartRateValues.setValue(analysisEntities.stream()
                                    .map(item -> new HistoryDataModel(item.heartRate, item.utcTime))
                                    .collect(Collectors.toList()));
                            break;
                        case OXYGEN_SATURATION:
                            _oxygenSaturationValues.setValue(analysisEntities.stream()
                                    .map(item -> new HistoryDataModel(item.oxygenSaturation, item.utcTime))
                                    .collect(Collectors.toList()));
                            break;
                        case RESPIRATORY_RATE:
                            _respiratoryRateValues.setValue(analysisEntities.stream()
                                    .map(item -> new HistoryDataModel(item.respiratoryRate, item.utcTime))
                                    .collect(Collectors.toList()));
                            break;
                        case STRESS:
                            _stressValues.setValue(analysisEntities.stream()
                                    .map(item -> new HistoryDataModel(item.stress, item.utcTime))
                                    .collect(Collectors.toList()));
                            break;
                        case HRV_SDNN:
                            _hrvSdnnValues.setValue(analysisEntities.stream()
                                    .map(item -> new HistoryDataModel(item.hrvSdnn, item.utcTime))
                                    .collect(Collectors.toList()));
                            break;
                        case BLOOD_PRESSURE:
                            _lowestBloodPressureValues.setValue(analysisEntities.stream()
                                    .map(item -> new HistoryDataModel(item.lowestBloodPressure, item.utcTime))
                                    .collect(Collectors.toList()));
                            _highestBloodPressureValues.setValue(analysisEntities.stream()
                                    .map(item -> new HistoryDataModel(item.highestBloodPressure, item.utcTime))
                                    .collect(Collectors.toList()));
                            break;
                    }
                }, throwable -> Log.d(TAG, throwable.getMessage())));
    }

    public void loadAnalysisOnTime(long utcTime) {
        Log.d(TAG, "loadAnalysisOnTime utcTime: " + utcTime);
        if (utcTime == 0L) {
            _latestTestDate.setValue("");
            _heartRateValue.setValue("-");
            _oxygenSaturationValue.setValue("-");
            _respiratoryRateValue.setValue("-");
            _stressValue.setValue("-");
            _hrvSdnnValue.setValue("-");
            _highestBloodPressureValue.setValue("-");
            _lowestBloodPressureValue.setValue("-");
            return;
        }
        disposable.add(analysisRepository.getAnalysis(utcTime)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(analysisEntity -> {
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd a h:mm:ss", Locale.getDefault());
                    String latestTestDate = formatter.format(new Date(analysisEntity.utcTime));
                    _latestTestDate.setValue(latestTestDate);
                    _heartRateValue.setValue(analysisEntity.heartRate == 0.0 ?
                            "-" : String.valueOf(analysisEntity.heartRate.intValue()));
                    _oxygenSaturationValue.setValue(analysisEntity.oxygenSaturation == 0.0 ?
                            "-" : String.valueOf(analysisEntity.oxygenSaturation.intValue()));
                    _respiratoryRateValue.setValue(analysisEntity.respiratoryRate == 0.0 ?
                            "-" : String.valueOf(analysisEntity.respiratoryRate.intValue()));
                    _stressValue.setValue(0.5 <= analysisEntity.stress && analysisEntity.stress <= 2.0 ?
                            getApplication().getString(R.string.vital_stress_normal) :
                            getApplication().getString(R.string.vital_stress_danger));
                    _hrvSdnnValue.setValue(analysisEntity.hrvSdnn == 0.0 ?
                            "-" : String.valueOf(analysisEntity.hrvSdnn.intValue()));
                    _highestBloodPressureValue.setValue(analysisEntity.highestBloodPressure == 0.0 ?
                            "-" : String.valueOf(analysisEntity.highestBloodPressure.intValue()));
                    _lowestBloodPressureValue.setValue(analysisEntity.lowestBloodPressure == 0.0 ?
                            "-" : String.valueOf(analysisEntity.lowestBloodPressure.intValue()));
                }, throwable -> Log.d(TAG, throwable.getMessage())));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposable.clear();
    }
}
