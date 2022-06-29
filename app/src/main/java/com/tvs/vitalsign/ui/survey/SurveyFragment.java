package com.tvs.vitalsign.ui.survey;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.tvs.vitalsign.R;
import com.tvs.vitalsign.VitalSignApp;
import com.tvs.vitalsign.repository.util.WaveRecorder;
import com.tvs.vitalsign.ui.MainActivityViewModel;
import com.tvs.vitalsign.ui.analysis.VitalAnalysisViewModel;

import java.io.IOException;

import io.reactivex.rxjava3.disposables.CompositeDisposable;


public class SurveyFragment extends Fragment {

    private final CompositeDisposable disposable = new CompositeDisposable();
    private static String TAG = SurveyFragment.class.getSimpleName();

    private TextView surveyQuestionNumber;
    private TextView surveyQuestion;
    private ConstraintLayout surveyMultiChoiceButtonContainer;
    private ConstraintLayout surveyYesNoButtonContainer;
    private ConstraintLayout surveyCompleteContainer;
    private ConstraintLayout surveyContainer;
    private ConstraintLayout surveyFinishContainer;
    private ConstraintLayout surveySttProgressContainer;
    private Button surveyYes;
    private Button surveyNo;
    private Button surveyMultiChoice;
    private Button surveyStt;
    private ConstraintLayout surveySttStop;
    private Button surveyTtsReplay;
    private RecyclerView surveyQuestionExamplesGrid;
    private SurveyExamplesAdapter surveyExamplesAdapter;

    private SurveyViewModel viewModel;
    private SurveyModel currentSurveyModel;

    private VitalAnalysisViewModel vitalAnalysisViewModel;
    private MainActivityViewModel mainActivityViewModel;

    private MediaPlayer mediaPlayer = new MediaPlayer();
    private WaveRecorder waveRecorder;

    public static SurveyFragment newInstance() {
        return new SurveyFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_survey, container, false);
        initView(view);
        waveRecorder = new WaveRecorder(requireContext().getCacheDir().getAbsolutePath());
        return view;
    }

    private void initView(View view) {
        surveyQuestionNumber = view.findViewById(R.id.survey_question_number);
        surveyQuestion = view.findViewById(R.id.survey_question);
        surveyMultiChoiceButtonContainer = view.findViewById(R.id.survey_multi_choice_button_container);
        surveyYesNoButtonContainer = view.findViewById(R.id.survey_yes_no_button_container);
        surveyStt = view.findViewById(R.id.survey_stt);
        surveyStt.setOnClickListener(v -> {
            stopTTSPlay();
            startRecording();
            surveyStt.setVisibility(View.GONE);
            surveySttStop.setVisibility(View.VISIBLE);
        });
        surveySttStop = view.findViewById(R.id.survey_stt_stop);
        surveySttStop.setOnClickListener(v -> stopRecording());
        surveyTtsReplay = view.findViewById(R.id.survey_tts_replay);
        surveyTtsReplay.setOnClickListener(v -> TTSReplay());
        surveyYes = view.findViewById(R.id.survey_yes);
        surveyYes.setOnClickListener(v -> {
            if (currentSurveyModel instanceof YesNoSurveyModel) {
                ((YesNoSurveyModel) currentSurveyModel).answer = YesNoSurveyModel.YesNo.YES;
                viewModel.setHasSymptom(currentSurveyModel.isTriggered());
                viewModel.loadNextSurvey();
            }
        });
        surveyNo = view.findViewById(R.id.survey_no);
        surveyNo.setOnClickListener(v -> {
            if (currentSurveyModel instanceof YesNoSurveyModel) {
                ((YesNoSurveyModel) currentSurveyModel).answer = YesNoSurveyModel.YesNo.NO;
                viewModel.setHasSymptom(currentSurveyModel.isTriggered());
                viewModel.loadNextSurvey();
            }
        });
        surveyMultiChoice = view.findViewById(R.id.survey_multi_choice);
        surveyMultiChoice.setOnClickListener(v -> {
            if (currentSurveyModel instanceof MultiChoiceSurveyModel) {
                mainActivityViewModel.symptoms.addAll(((MultiChoiceSurveyModel) currentSurveyModel).getSelectedExamples());
                viewModel.setHasSymptom(currentSurveyModel.isTriggered());
                viewModel.loadNextSurvey();
            }
        });
        surveyQuestionExamplesGrid = view.findViewById(R.id.survey_question_examples_grid);
        surveyExamplesAdapter = new SurveyExamplesAdapter();
        surveyQuestionExamplesGrid.setAdapter(surveyExamplesAdapter);

        surveyCompleteContainer = view.findViewById(R.id.survey_complete_container);
        surveyCompleteContainer.setOnTouchListener((v, event) -> true);

        surveySttProgressContainer = view.findViewById(R.id.survey_stt_progress_container);
        surveySttProgressContainer.setOnTouchListener((v, event) -> true);

        surveyContainer = view.findViewById(R.id.survey_container);
        surveyFinishContainer = view.findViewById(R.id.survey_finish_container);
    }

    private void TTSReplay() {
        mediaPlayer.seekTo(0);
        mediaPlayer.start();
    }

    private void stopTTSPlay() {
        mediaPlayer.pause();
        mediaPlayer.seekTo(0);
    }

    private void startRecording() {
        waveRecorder.startRecording();
    }

    private void stopRecording() {
        waveRecorder.stopRecording(() -> {
            surveySttProgressContainer.setVisibility(View.VISIBLE);
            viewModel.getSTT(waveRecorder.getWavFilePath());
        });
        surveyStt.setVisibility(View.VISIBLE);
        surveySttStop.setVisibility(View.GONE);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this, VitalSignApp.factory).get(SurveyViewModel.class);
        vitalAnalysisViewModel = new ViewModelProvider(requireParentFragment(), VitalSignApp.factory).get(VitalAnalysisViewModel.class);
        mainActivityViewModel = new ViewModelProvider(requireActivity()).get(MainActivityViewModel.class);
        mainActivityViewModel.symptoms.clear();
        bind();
        view.post(() -> viewModel.requestTTS());
    }

    private void bind() {
        viewModel.surveyQuestion.observe(this, surveyQuestionPair -> {
            mediaPlayer.reset();
            surveyQuestionNumber.setText(String.format(getString(R.string.survey_question_number), surveyQuestionPair.first));
            currentSurveyModel = surveyQuestionPair.second;
            this.surveyQuestion.setText(currentSurveyModel.question);
            switch (currentSurveyModel.type) {
                case YES_OR_NO:
                    surveyMultiChoiceButtonContainer.setVisibility(View.GONE);
                    surveyYesNoButtonContainer.setVisibility(View.VISIBLE);
                    surveyQuestionExamplesGrid.setVisibility(View.GONE);
                    break;
                case MULTI_CHOICE:
                    surveyMultiChoiceButtonContainer.setVisibility(View.VISIBLE);
                    surveyYesNoButtonContainer.setVisibility(View.GONE);
                    surveyQuestionExamplesGrid.setVisibility(View.VISIBLE);
                    MultiChoiceSurveyModel model = (MultiChoiceSurveyModel) currentSurveyModel;
                    surveyExamplesAdapter.submitList(model.examples);
                    break;
            }
        });
        disposable.add(viewModel.surveyFinish.subscribe(hasSymptom -> {
            mediaPlayer.pause();
            vitalAnalysisViewModel.surveyFinish(hasSymptom);
            surveyCompleteContainer.setVisibility(View.VISIBLE);
            surveyCompleteContainer.postDelayed(() -> {
                surveyContainer.setVisibility(View.GONE);
                surveyFinishContainer.setVisibility(View.VISIBLE);
            }, 5000);
        }));
        viewModel.voiceWavFilePath.observe(this, s -> {
            try {
                if (s.isEmpty()) return;
                mediaPlayer.reset();
                mediaPlayer.setDataSource(s);
                mediaPlayer.prepare();
                mediaPlayer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        disposable.add(viewModel.sttEvent
                .subscribe(
                        text -> {
                            surveySttProgressContainer.setVisibility(View.GONE);
                            String joinedText = text.replaceAll(" ", "");
                            String commandText = voiceCommand(joinedText);
                            Toast.makeText(
                                    requireContext(),
                                    commandText.isEmpty() ? getString(R.string.survey_stt_error_2) : commandText,
                                    Toast.LENGTH_SHORT
                            ).show();
                        },
                        throwable -> {
                            surveySttProgressContainer.setVisibility(View.GONE);
                            Toast.makeText(requireContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                ));
        disposable.add(viewModel.errorEvent
                .subscribe(errorMessage -> {
                    surveySttProgressContainer.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
                }));
    }

    private String voiceCommand(String textCommand) {
        if (currentSurveyModel instanceof YesNoSurveyModel) {
            if (surveyYes.getText().toString()
                    .replaceAll(" ", "").equals(textCommand)) {
                surveyYes.performClick();
                return textCommand;
            } else if (surveyNo.getText().toString()
                    .replaceAll(" ", "").equals(textCommand)) {
                surveyNo.performClick();
                return textCommand;
            }
        } else if (currentSurveyModel instanceof MultiChoiceSurveyModel) {
            for (SurveyExampleModel model :
                    surveyExamplesAdapter.getCurrentList()) {
                if (model.text.replaceAll(" ", "").equals(textCommand)) {
                    selectExample(textCommand);
                    return textCommand;
                }
            }
            if (surveyMultiChoice.getText().toString()
                    .replaceAll(" ", "").equals(textCommand)) {
                surveyMultiChoice.performClick();
                return textCommand;
            }
        }
        return "";
    }

    private void selectExample(String exampleText) {
        surveyExamplesAdapter.selectExample(exampleText);
    }

    @Override
    public void onPause() {
        stopTTSPlay();
        stopRecording();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        disposable.clear();
        mediaPlayer.release();
        super.onDestroyView();
    }
}
