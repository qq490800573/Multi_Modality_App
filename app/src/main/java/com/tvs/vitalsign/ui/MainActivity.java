package com.tvs.vitalsign.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.tvs.vitalsign.R;
import com.tvs.vitalsign.VitalSignApp;
import com.tvs.vitalsign.ui.analysis.VitalAnalysisFragment;
import com.tvs.vitalsign.ui.main.MainFragment;
import com.tvs.vitalsign.ui.result.ResultFragment;

public class MainActivity extends AppCompatActivity {

    private MainActivityViewModel viewModel;
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;

    public static float LF_HF_ratio = 0;
    public static double HR_result = 0;
    public static double RR_result = 0;
    public static double spo2_result = 0;
    public static double sdnn_result = 0;
    public static double BP = 0;
    public static double SBP = 0;
    public static double DBP = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewModel = new ViewModelProvider(this, VitalSignApp.factory).get(MainActivityViewModel.class);
        bind();

        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.container, MainFragment.newInstance());
        fragmentTransaction.commit();
    }

    private void bind() {
        viewModel.analysisFinish.subscribe(hasSymptom -> {
                    getSupportFragmentManager().popBackStack();
                    replaceFragment(ResultFragment.newInstance(hasSymptom));
                });
    }

    public void setCameraFragment() {
        VitalAnalysisFragment vitalAnalysisFragment = VitalAnalysisFragment.newInstance();
        getSupportFragmentManager().popBackStack();
        getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(null)
                .replace(R.id.container, vitalAnalysisFragment)
                .commit();
    }

    public void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(null)
                .replace(R.id.container, fragment)
                .commit();
    }

    public void openCOVID19ScreeningStationKaKaoMapWeb() {
        Uri webpage = Uri.parse("https://m.map.kakao.com/actions/searchView?q=%EC%84%A0%EB%B3%84%EC%A7%84%EB%A3%8C%EC%86%8C");
        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
}
