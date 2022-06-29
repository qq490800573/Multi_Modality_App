package com.tvs.vitalsign.ui.result;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.tvs.vitalsign.R;
import com.tvs.vitalsign.repository.AnalysisRepository;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ResultViewModel extends AndroidViewModel {

    private static final String TAG = ResultViewModel.class.getSimpleName();

    private final CompositeDisposable disposable = new CompositeDisposable();
    private final AnalysisRepository repository;

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

    public ResultViewModel(Application application) {
        super(application);
        repository = new AnalysisRepository(application.getApplicationContext());
    }

    public void loadLatestAnalysis() {
        disposable.add(repository.getLatestAnalysis()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(analysisEntity -> {
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
