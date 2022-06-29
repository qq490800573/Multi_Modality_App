package com.tvs.vitalsign.ui.result;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.robinhood.ticker.TickerView;
import com.tvs.vitalsign.R;
import com.tvs.vitalsign.VitalSignApp;
import com.tvs.vitalsign.ui.MainActivity;
import com.tvs.vitalsign.ui.MainActivityViewModel;

import java.util.List;

import io.reactivex.rxjava3.disposables.CompositeDisposable;


public class ResultFragment extends Fragment {

    private final CompositeDisposable disposable = new CompositeDisposable();

    private final static String HAS_SYMPTOM = "hasSymptom";
    private final static String TAG = ResultFragment.class.getSimpleName();

    private boolean hasSymptom = false;

    private MainActivityViewModel mainActivityViewModel;
    private ResultViewModel resultViewModel;

    public static ResultFragment newInstance(boolean hasSymptom) {
        ResultFragment fragment = new ResultFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(HAS_SYMPTOM, hasSymptom);
        fragment.setArguments(bundle);
        return fragment;
    }

    private TickerView heartRateValue;
    private TickerView hrvSdnnValue;
    private TickerView respiratoryRateValue;
    private TickerView oxygenSaturationValue;
    private TickerView highestBloodPressureValue;
    private TickerView lowestBloodPressureValue;
    private TickerView stressValue;
    private ConstraintLayout resultSymptomContainer;
    private ConstraintLayout resultNormalContainer;
    private Button resultSearchHospital;
    private Button resultBackToMain;
    private TextView resultSymptomCount;
    private TextView resultSymptomList;
    private ImageView back;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            hasSymptom = getArguments().getBoolean(HAS_SYMPTOM, false);
        }
        mainActivityViewModel = new ViewModelProvider(requireActivity()).get(MainActivityViewModel.class);
        resultViewModel = new ViewModelProvider(this, VitalSignApp.factory).get(ResultViewModel.class);
        resultViewModel.loadLatestAnalysis();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_result, container, false);

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

        resultSymptomContainer = view.findViewById(R.id.result_symptom_container);
        resultNormalContainer = view.findViewById(R.id.result_normal_container);
        resultSearchHospital = view.findViewById(R.id.result_search_hospital);
        resultBackToMain = view.findViewById(R.id.result_back_to_main);
        resultSymptomCount = view.findViewById(R.id.result_symptom_count);
        resultSymptomList = view.findViewById(R.id.result_symptom_list);

        resultSymptomContainer.setVisibility(hasSymptom ? View.VISIBLE : View.GONE);
        resultSearchHospital.setVisibility(hasSymptom ? View.VISIBLE : View.GONE);
        resultNormalContainer.setVisibility(hasSymptom ? View.GONE : View.VISIBLE);
        resultBackToMain.setVisibility(hasSymptom ? View.GONE : View.VISIBLE);

        if (hasSymptom) {
            List<String> symptoms = mainActivityViewModel.symptoms;
            resultSymptomCount.setText(String.valueOf(symptoms.size()));
            resultSymptomList.setText(String.join(", ", mainActivityViewModel.symptoms));
        }

        resultBackToMain.setOnClickListener(v -> requireActivity().onBackPressed());
        resultSearchHospital.setOnClickListener(v -> ((MainActivity) getActivity()).openCOVID19ScreeningStationKaKaoMapWeb());

        back = view.findViewById(R.id.back);
        back.setOnClickListener(v -> requireActivity().onBackPressed());

        bind();
        return view;
    }

    private void bind() {
        resultViewModel.heartRateValue.observe(this, value ->
                heartRateValue.setText(value));
        resultViewModel.oxygenSaturationValue.observe(this, value ->
                oxygenSaturationValue.setText(value));
        resultViewModel.respiratoryRateValue.observe(this, value ->
                respiratoryRateValue.setText(value));
        resultViewModel.stressValue.observe(this, value ->
                stressValue.setText(value));
        resultViewModel.hrvSdnnValue.observe(this, value ->
                hrvSdnnValue.setText(value));
        resultViewModel.highestBloodPressureValue.observe(this, value ->
                highestBloodPressureValue.setText(value));
        resultViewModel.lowestBloodPressureValue.observe(this, value ->
                lowestBloodPressureValue.setText(value));
    }

    @Override
    public void onDestroyView() {
        disposable.clear();
        super.onDestroyView();
    }

}
