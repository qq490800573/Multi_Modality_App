package com.tvs.vitalsign.ui.main;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.tvs.vitalsign.R;
import com.tvs.vitalsign.repository.AnalysisRepository;
import com.tvs.vitalsign.repository.Covid19Repository;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;

public class MainViewModel extends AndroidViewModel {

    private static final String TAG = MainViewModel.class.getSimpleName();

    private final CompositeDisposable disposable = new CompositeDisposable();
    private final Covid19Repository covid19Repository;
    private final AnalysisRepository analysisRepository;

    private final MutableLiveData<String> _elapsedDays = new MutableLiveData<>("-");
    public LiveData<String> elapsedDays = _elapsedDays;
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

    private final MutableLiveData<String> _latestCovid19StatusUpdateDate = new MutableLiveData<>("-");
    public LiveData<String> latestCovid19StatusUpdateDate = _latestCovid19StatusUpdateDate;
    private final MutableLiveData<String> _decideCnt = new MutableLiveData<>("-");
    public LiveData<String> decideCnt = _decideCnt;
    private final MutableLiveData<String> _decideCntDiff = new MutableLiveData<>("-");
    public LiveData<String> decideCntDiff = _decideCntDiff;
    private final MutableLiveData<String> _deathCnt = new MutableLiveData<>("-");
    public LiveData<String> deathCnt = _deathCnt;
    private final MutableLiveData<String> _deathCntDiff = new MutableLiveData<>("-");
    public LiveData<String> deathCntDiff = _deathCntDiff;
    private final MutableLiveData<String> _clearCnt = new MutableLiveData<>("-");
    public LiveData<String> clearCnt = _clearCnt;
    private final MutableLiveData<String> _clearCntDiff = new MutableLiveData<>("-");
    public LiveData<String> clearCntDiff = _clearCntDiff;

    private PublishSubject<String> _errorEvent = PublishSubject.create();
    public Observable<String> errorEvent = _errorEvent.hide();

    public MainViewModel(Application application) {
        super(application);
        covid19Repository = new Covid19Repository();
        analysisRepository = new AnalysisRepository(application.getApplicationContext());
    }

    public void loadCovid19Status() {
        disposable.add(covid19Repository.getLatestCOVID19Status()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(covid19StatusModel -> {
                    _decideCnt.setValue(covid19StatusModel.decideCnt);
                    _decideCntDiff.setValue(covid19StatusModel.decideCntDiff);
                    _deathCnt.setValue(covid19StatusModel.deathCnt);
                    _deathCntDiff.setValue(covid19StatusModel.deathCntDiff);
                    _clearCnt.setValue(covid19StatusModel.clearCnt);
                    _clearCntDiff.setValue(covid19StatusModel.clearCntDiff);

                    // update Date
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd a h:mm:ss", Locale.getDefault());
                    long latestUpdatedDate = System.currentTimeMillis();
                    String latestDate = formatter.format(new Date(latestUpdatedDate));
                    _latestCovid19StatusUpdateDate.setValue(latestDate);
                }, throwable -> {
                    _errorEvent.onNext(getApplication().getString(R.string.server_error));
                }));
    }

    public void loadLatestAnalysis() {
        disposable.add(analysisRepository.getLatestAnalysis()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(analysisEntity -> {
                    long elapsedDays = TimeUnit.DAYS.convert(
                            Instant.now().toEpochMilli() - analysisEntity.utcTime,
                            TimeUnit.MILLISECONDS
                    );
                    _elapsedDays.setValue(String.valueOf(elapsedDays));
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
