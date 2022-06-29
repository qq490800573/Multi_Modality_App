package com.tvs.vitalsign.ui.loading;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.tvs.vitalsign.R;
import com.tvs.vitalsign.ui.analysis.VitalAnalysisFragment;
import com.tvs.vitalsign.ui.MainActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class LoadingAnalysisFragment extends Fragment {

    private CompositeDisposable disposable = new CompositeDisposable();
    private ProgressBar loadingProgress;
    private TextView loadingCounter;
    private boolean isProgressComplete = false;

    public static int total_yes = 0;
    public static int total_no = 0;
    public static int Q1_3_yes = 0;
    public static boolean asthma = false;
    public static boolean fever = false;
    public static boolean cough = false;
    public static int Q7_yes = 0;
    public static int Q8_yes = 0;
    public static int Q9_yes = 0;
    public static List<String> Symptom;

    public static LoadingAnalysisFragment newInstance() {
        return new LoadingAnalysisFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_loading_analysis, container, false);
        init(view);
        initValues();
        return view;
    }

    private void init(View view) {
        loadingProgress = view.findViewById(R.id.loading_progress);
        startProgressAnimation();

        loadingCounter = view.findViewById(R.id.loading_counter);
        disposable.add(Observable.interval(1L, TimeUnit.SECONDS, Schedulers.computation())
                .map(tic -> tic + 1)
                .startWithItem(0L)
                .take(4)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(tic -> {
                    if (tic == 3L) {
                        isProgressComplete = true;
                        VitalAnalysisFragment.computingDetection = false;
                        ((MainActivity) requireActivity()).setCameraFragment();
                    } else {
                        loadingCounter.setText(String.format(getString(R.string.loading_counter), 3 - tic));
                    }
                }));
    }

    private void startProgressAnimation() {
        ValueAnimator animator = ValueAnimator.ofInt(0, loadingProgress.getMax());
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(3000);
        animator.addUpdateListener(valueAnimator -> {
            int value = (int) valueAnimator.getAnimatedValue();
            loadingProgress.setProgress(value);
        });
        animator.start();
    }

    private void initValues() {
        total_yes = 0;
        total_no = 0;
        Q1_3_yes = 0;
        asthma = false;
        fever = false;
        cough = false;
        Q7_yes = 0;
        Q8_yes = 0;
        Q9_yes = 0;
        Symptom = new ArrayList<>();
    }

    @Override
    public void onPause() {
        disposable.clear();
        if (!isProgressComplete) {
            requireFragmentManager().popBackStack();
        }
        super.onPause();
    }

}