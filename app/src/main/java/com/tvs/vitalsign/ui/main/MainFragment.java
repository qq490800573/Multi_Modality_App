package com.tvs.vitalsign.ui.main;


import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
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

import com.robinhood.ticker.TickerView;
import com.tvs.vitalsign.R;
import com.tvs.vitalsign.VitalSignApp;
import com.tvs.vitalsign.ui.MainActivity;
import com.tvs.vitalsign.ui.history.HistoryFragment;
import com.tvs.vitalsign.ui.loading.LoadingAnalysisFragment;

import io.reactivex.rxjava3.disposables.CompositeDisposable;


public class MainFragment extends Fragment {

    private final CompositeDisposable disposable = new CompositeDisposable();

    private MainViewModel viewModel;

    private TextView mainHelloUserTextView;
    private TextView mainCurrentSelfTestTextView;
    private TextView mainCovid19LatestUpdate;
    private TickerView mainCovid19DecideCnt;
    private TickerView mainCovid19DecideCntDiff;
    private TickerView mainCovid19DeathCnt;
    private TickerView mainCovid19DeathCntDiff;
    private TickerView mainCovid19ClearCnt;
    private TickerView mainCovid19CleatCntDiff;
    private ConstraintLayout mainSearchClinic;

    private TextView mainLatestTestDate;
    private TickerView heartRateValue;
    private TickerView hrvSdnnValue;
    private TickerView respiratoryRateValue;
    private TickerView oxygenSaturationValue;
    private TickerView highestBloodPressureValue;
    private TickerView lowestBloodPressureValue;
    private TickerView stressValue;

    private Button bt_vital;
    private Button mainSurveyButton;

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        init(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) VitalSignApp.factory).get(MainViewModel.class);
        viewModel.loadCovid19Status();
        viewModel.loadLatestAnalysis();
        bind();
    }

    private void init(View view) {
        mainHelloUserTextView = view.findViewById(R.id.main_hello_user);
        mainCurrentSelfTestTextView = view.findViewById(R.id.main_current_self_test);
        mainCovid19LatestUpdate = view.findViewById(R.id.main_covid19_latest_update);
        mainCovid19DecideCnt = view.findViewById(R.id.main_covid19_decide_cnt);
        mainCovid19DecideCntDiff = view.findViewById(R.id.main_covid19_decide_cnt_diff);
        mainCovid19DeathCnt = view.findViewById(R.id.main_covid19_death_cnt);
        mainCovid19DeathCntDiff = view.findViewById(R.id.main_covid19_death_cnt_diff);
        mainCovid19ClearCnt = view.findViewById(R.id.main_covid19_clear_cnt);
        mainCovid19CleatCntDiff = view.findViewById(R.id.main_covid19_clear_cnt_diff);

        mainCovid19DecideCnt.setPreferredScrollingDirection(TickerView.ScrollingDirection.ANY);
        mainCovid19DecideCntDiff.setPreferredScrollingDirection(TickerView.ScrollingDirection.ANY);
        mainCovid19DeathCnt.setPreferredScrollingDirection(TickerView.ScrollingDirection.ANY);
        mainCovid19DeathCntDiff.setPreferredScrollingDirection(TickerView.ScrollingDirection.ANY);
        mainCovid19ClearCnt.setPreferredScrollingDirection(TickerView.ScrollingDirection.ANY);
        mainCovid19CleatCntDiff.setPreferredScrollingDirection(TickerView.ScrollingDirection.ANY);

        mainSearchClinic = view.findViewById(R.id.search_clinic);
        mainSearchClinic.setOnClickListener(v -> ((MainActivity) getActivity()).openCOVID19ScreeningStationKaKaoMapWeb());

        mainLatestTestDate = view.findViewById(R.id.main_latest_test_date);
        bt_vital = view.findViewById(R.id.bt_vital);
        mainSurveyButton = view.findViewById(R.id.main_survey);
        heartRateValue = view.findViewById(R.id.heart_rate_value);
        hrvSdnnValue = view.findViewById(R.id.hrv_sdnn_value);
        respiratoryRateValue = view.findViewById(R.id.respiratory_rate_value);
        oxygenSaturationValue = view.findViewById(R.id.oxygen_saturation_value);
        highestBloodPressureValue = view.findViewById(R.id.highest_blood_pressure_value);
        lowestBloodPressureValue = view.findViewById(R.id.lowest_blood_pressure_value);
        stressValue = view.findViewById(R.id.stress_value);

        heartRateValue.setPreferredScrollingDirection(TickerView.ScrollingDirection.ANY);
        hrvSdnnValue.setPreferredScrollingDirection(TickerView.ScrollingDirection.ANY);
        respiratoryRateValue.setPreferredScrollingDirection(TickerView.ScrollingDirection.ANY);
        oxygenSaturationValue.setPreferredScrollingDirection(TickerView.ScrollingDirection.ANY);
        highestBloodPressureValue.setPreferredScrollingDirection(TickerView.ScrollingDirection.ANY);
        lowestBloodPressureValue.setPreferredScrollingDirection(TickerView.ScrollingDirection.ANY);
        stressValue.setPreferredScrollingDirection(TickerView.ScrollingDirection.ANY);

        bt_vital.setOnClickListener(v ->
                ((MainActivity) getActivity()).replaceFragment(HistoryFragment.newInstance()));
        mainSurveyButton.setOnClickListener(v ->
                ((MainActivity) getActivity()).replaceFragment(LoadingAnalysisFragment.newInstance()));
    }

    private void bind() {
        viewModel.elapsedDays.observe(getViewLifecycleOwner(), elapsedDays -> {
            // 최근 검사 이후 날짜 표기
            String rawText = getString(R.string.main_current_self_test);
            int startIndex = rawText.indexOf("%");
            String unitWord = rawText.substring(startIndex).split(" ")[1];
            int endIndex = startIndex + elapsedDays.length() + unitWord.length() + 1;
            String formattedDayText = String.format(rawText, elapsedDays);
            SpannableStringBuilder span = new SpannableStringBuilder(formattedDayText);
            int highlightColor = getResources().getColor(R.color.red, null);
            span.setSpan(new ForegroundColorSpan(highlightColor), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            span.setSpan(new StyleSpan(Typeface.BOLD), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            mainCurrentSelfTestTextView.setText("");
            mainCurrentSelfTestTextView.append(span);
        });
        viewModel.latestTestDate.observe(getViewLifecycleOwner(), value ->
                mainLatestTestDate.setText(String.format(getString(R.string.main_latest_test_date), value)));
        viewModel.heartRateValue.observe(getViewLifecycleOwner(), value ->
                heartRateValue.setText(value));
        viewModel.oxygenSaturationValue.observe(getViewLifecycleOwner(), value ->
                oxygenSaturationValue.setText(value));
        viewModel.respiratoryRateValue.observe(getViewLifecycleOwner(), value ->
                respiratoryRateValue.setText(value));
        viewModel.stressValue.observe(getViewLifecycleOwner(), value ->
                stressValue.setText(value));
        viewModel.hrvSdnnValue.observe(getViewLifecycleOwner(), value ->
                hrvSdnnValue.setText(value));
        viewModel.highestBloodPressureValue.observe(getViewLifecycleOwner(), value ->
                highestBloodPressureValue.setText(value));
        viewModel.lowestBloodPressureValue.observe(getViewLifecycleOwner(), value ->
                lowestBloodPressureValue.setText(value));

        // 환영인사 및 유저 이름
        String helloText = String.format(getString(R.string.main_hello_user), "User");
        mainHelloUserTextView.setText(helloText);

        // 코로나 현황 최신 업데이트 시간 표기
        viewModel.latestCovid19StatusUpdateDate.observe(getViewLifecycleOwner(), latestDate -> {
            String updateDateFormat = getString(R.string.main_covid_19_status_latest_update);
            mainCovid19LatestUpdate.setText(String.format(updateDateFormat, latestDate));
        });
        viewModel.decideCnt.observe(getViewLifecycleOwner(), decideCnt ->
                mainCovid19DecideCnt.setText(decideCnt));
        viewModel.decideCntDiff.observe(getViewLifecycleOwner(), decideCntDiff ->
                mainCovid19DecideCntDiff.setText(decideCntDiff));
        viewModel.deathCnt.observe(getViewLifecycleOwner(), deathCnt ->
                mainCovid19DeathCnt.setText(deathCnt));
        viewModel.deathCntDiff.observe(getViewLifecycleOwner(), deathCntDiff ->
                mainCovid19DeathCntDiff.setText(deathCntDiff));
        viewModel.clearCnt.observe(getViewLifecycleOwner(), clearCnt ->
                mainCovid19ClearCnt.setText(clearCnt));
        viewModel.clearCntDiff.observe(getViewLifecycleOwner(), clearCntDiff ->
                mainCovid19CleatCntDiff.setText(clearCntDiff));

        disposable.add(viewModel.errorEvent
                .subscribe(errorMessage -> {
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
                }));
    }

    @Override
    public void onDestroyView() {
        disposable.clear();
        super.onDestroyView();
    }
}
