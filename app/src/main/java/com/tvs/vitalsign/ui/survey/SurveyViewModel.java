package com.tvs.vitalsign.ui.survey;

import android.app.Application;
import android.util.Pair;

import androidx.annotation.StringRes;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.tvs.vitalsign.R;
import com.tvs.vitalsign.repository.SaltLuxRepository;

import java.util.Arrays;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Notification;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.SingleSubject;

public class SurveyViewModel extends AndroidViewModel {

    private static String TAG = SurveyViewModel.class.getSimpleName();

    private final CompositeDisposable disposable = new CompositeDisposable();
    private List<SurveyModel> questions = Arrays.asList(
            new YesNoSurveyModel("s0", getStr(R.string.survey_question_0), YesNoSurveyModel.YesNo.YES),
            new YesNoSurveyModel("s1", getStr(R.string.survey_question_1), YesNoSurveyModel.YesNo.YES),
            new YesNoSurveyModel("s2", getStr(R.string.survey_question_2), YesNoSurveyModel.YesNo.YES),
            new YesNoSurveyModel("s3", getStr(R.string.survey_question_3), YesNoSurveyModel.YesNo.NONE),
            new YesNoSurveyModel("s4", getStr(R.string.survey_question_4), YesNoSurveyModel.YesNo.YES),
            new YesNoSurveyModel("s5", getStr(R.string.survey_question_5), YesNoSurveyModel.YesNo.YES),
            new MultiChoiceSurveyModel("s6", getStr(R.string.survey_question_6), Arrays.asList(getStr(R.string.survey_example_0), getStr(R.string.survey_example_1), getStr(R.string.survey_example_2), getStr(R.string.survey_example_3)), 1),
            new MultiChoiceSurveyModel("s7", getStr(R.string.survey_question_7), Arrays.asList(getStr(R.string.survey_example_4), getStr(R.string.survey_example_5), getStr(R.string.survey_example_6), getStr(R.string.survey_example_7)), 2),
            new MultiChoiceSurveyModel("s8", getStr(R.string.survey_question_8), Arrays.asList(getStr(R.string.survey_example_8), getStr(R.string.survey_example_9), getStr(R.string.survey_example_10), getStr(R.string.survey_example_11), getStr(R.string.survey_example_12)), 2)
    );

    private String getStr(@StringRes int id) {
        return getApplication().getString(id);
    }

    private MutableLiveData<Integer> activeIndex = new MutableLiveData<>(0);
    public MediatorLiveData<Pair<Integer, SurveyModel>> surveyQuestion = new MediatorLiveData<>();

    private boolean hasSymptom = false;

    public void setHasSymptom(boolean hasSymptom) {
        this.hasSymptom = this.hasSymptom || hasSymptom;
    }

    private SingleSubject<Boolean> _surveyFinish = SingleSubject.create();
    public Single<Boolean> surveyFinish = _surveyFinish.hide();

    private MutableLiveData<String> _voiceWavFilePath = new MutableLiveData<>("");
    public LiveData<String> voiceWavFilePath = _voiceWavFilePath;

    private PublishSubject<Integer> _requestTTSEvent = PublishSubject.create();

    private PublishSubject<String> _errorEvent = PublishSubject.create();
    public Observable<String> errorEvent = _errorEvent.hide();

    private SaltLuxRepository repository;

    public SurveyViewModel(Application application) {
        super(application);
        repository = new SaltLuxRepository(application);
        surveyQuestion.addSource(activeIndex, index -> surveyQuestion.setValue(new Pair<>(index + 1, questions.get(index))));
        initTTSRequest();
    }

    public void requestTTS() {
        _requestTTSEvent.onNext(activeIndex.getValue());
    }

    private void initTTSRequest() {
        disposable.add(_requestTTSEvent.concatMapSingle(integer -> {
            SurveyModel model = questions.get(integer);
            return repository.getTTS(model.id, model.question)
                    .observeOn(AndroidSchedulers.mainThread())
                    .onErrorReturn(throwable -> {
                        String errorMessage = getApplication().getString(R.string.server_error);
                        _errorEvent.onNext(errorMessage);
                        return Notification.createOnNext("");
                    });
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(noti -> {
                    if (noti.isOnNext()) {
                        _voiceWavFilePath.postValue(noti.getValue());
                    } else if (noti.isOnError()) {
                        noti.getError().printStackTrace();
                    }
                }));
    }

    public void loadNextSurvey() {
        int curIdx = activeIndex.getValue();
        if (curIdx + 1 < questions.size()) {
            activeIndex.setValue(curIdx + 1);
            requestTTS();
        } else {
            _surveyFinish.onSuccess(hasSymptom);
        }
    }

    private PublishSubject<String> _sttEvent = PublishSubject.create();
    public Observable<String> sttEvent = _sttEvent.hide();

    public void getSTT(String audioFilePath) {
        disposable.add(repository.getSTT(audioFilePath)
                .observeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(notification -> {
                    if (notification.isOnNext()) {
                        _sttEvent.onNext(notification.getValue());
                    } else if (notification.isOnError()) {
                        String errorMessage = getApplication().getString(R.string.survey_stt_error_1);
                        _errorEvent.onNext(errorMessage);
                    }
                }, throwable -> {
                    String errorMessage = getApplication().getString(R.string.server_error);
                    _errorEvent.onNext(errorMessage);
                }));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposable.clear();
    }
}
