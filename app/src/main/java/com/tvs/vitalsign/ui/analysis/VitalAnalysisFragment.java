package com.tvs.vitalsign.ui.analysis;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.github.psambit9791.jdsp.signal.Detrend;
import com.github.psambit9791.jdsp.signal.Smooth;
import com.paramsen.noise.Noise;
import com.robinhood.ticker.TickerView;
import com.tvs.vitalsign.R;
import com.tvs.vitalsign.VitalSignApp;
import com.tvs.vitalsign.analysis.Vital;
import com.tvs.vitalsign.ml.BlazeFace;
import com.tvs.vitalsign.ml.Recognizer;
import com.tvs.vitalsign.tracking.MultiBoxTracker;
import com.tvs.vitalsign.ui.MainActivityViewModel;
import com.tvs.vitalsign.ui.survey.SurveyFragment;
import com.tvs.vitalsign.ui.view.AutoFitTextureView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import static com.tvs.vitalsign.ui.MainActivity.BP;
import static com.tvs.vitalsign.ui.MainActivity.DBP;
import static com.tvs.vitalsign.ui.MainActivity.HR_result;
import static com.tvs.vitalsign.ui.MainActivity.LF_HF_ratio;
import static com.tvs.vitalsign.ui.MainActivity.RR_result;
import static com.tvs.vitalsign.ui.MainActivity.SBP;
import static com.tvs.vitalsign.ui.MainActivity.sdnn_result;
import static com.tvs.vitalsign.ui.MainActivity.spo2_result;
import com.tvs.vitalsign.ui.vital_sensor.Sensor;

public class VitalAnalysisFragment extends BaseCameraFragment {

    private final CompositeDisposable disposable = new CompositeDisposable();
    private final static String TAG = VitalAnalysisFragment.class.getSimpleName();



        private VitalAnalysisViewModel viewModel;
        private Sensor sensor;
        private MainActivityViewModel mainActivityViewModel;


    private final static int updateInterval = 4; // Second
    private final static double initDoubleValue = 0.0;

    private TextView vitalAnalysisWarningTextView;
    private ProgressBar analysisProgress;
    private final static int analysisTimeValue = 30_000;
    private final static int analysisTimeInterval = 100;
    private int analysisTimeStart = 0;
    private boolean analysisAvailable = false;

    private ConstraintLayout analysisContainer;
    private ConstraintLayout analysisCompleteContainer;

    private TickerView vitalHeartRateValue;
    private TickerView vitalHrvSdnnValue;
    private TickerView vitalRespiratoryRateValue;
    private TickerView vitalOxygenSaturationValue;
    private TickerView vitalHighestBloodPressureValue;
    private TickerView vitalLowestBloodPressureValue;
    private TickerView vitalStressValue;
    private ProgressBar vitalHeartRateProgress;
    private ProgressBar vitalHrvSdnnProgress;
    private ProgressBar vitalRespiratoryRateProgress;
    private ProgressBar vitalOxygenSaturationProgress;
    private ProgressBar vitalBloodPressureProgress;
    private ProgressBar vitalStressValueProgress;

    private ImageView back;

    private AutoFitTextureView textureView;

    private Handler analysisProgressHandler = new Handler();

    public static VitalAnalysisFragment newInstance() {
        return new VitalAnalysisFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) VitalSignApp.factory).get(VitalAnalysisViewModel.class);
        mainActivityViewModel = new ViewModelProvider(requireActivity()).get(MainActivityViewModel.class);
        try {
            tracker = new MultiBoxTracker(requireContext());
            recognizer = Recognizer.getInstance(requireActivity().getAssets());
        } catch (Exception e) {
            Log.e(TAG, "Exception!");
            Log.e(TAG, "Exception initializing classifier!", e);
            requireFragmentManager().popBackStack();
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_vital_analysis, container, false);

        Log.d("fragment", "현재 : CameraConnectionFragment");

        back = view.findViewById(R.id.back);
        back.setOnClickListener(v -> requireActivity().onBackPressed());

        vitalAnalysisWarningTextView = view.findViewById(R.id.vital_analysis_warning);
        analysisProgress = view.findViewById(R.id.analysis_progress);
        analysisProgress.setMax(analysisTimeValue);

        analysisContainer = view.findViewById(R.id.analysis_container);
        analysisCompleteContainer = view.findViewById(R.id.analysis_complete_container);

        trackingOverlay = view.findViewById(R.id.tracking_overlay);
        trackingOverlay.addCallback(canvas -> tracker.draw(canvas));

        vitalHeartRateValue = view.findViewById(R.id.vital_heart_rate_value);
        vitalHrvSdnnValue = view.findViewById(R.id.vital_hrv_sdnn_value);
        vitalRespiratoryRateValue = view.findViewById(R.id.vital_respiratory_rate_value);
        vitalOxygenSaturationValue = view.findViewById(R.id.vital_oxygen_saturation_value);
        vitalHighestBloodPressureValue = view.findViewById(R.id.vital_highest_blood_pressure_value);
        vitalLowestBloodPressureValue = view.findViewById(R.id.vital_lowest_blood_pressure_value);
        vitalStressValue = view.findViewById(R.id.vital_stress_value);

        vitalHeartRateProgress = view.findViewById(R.id.vital_heart_rate_progress);
        vitalHrvSdnnProgress = view.findViewById(R.id.vital_hrv_sdnn_progress);
        vitalRespiratoryRateProgress = view.findViewById(R.id.vital_respiratory_rate_progress);
        vitalOxygenSaturationProgress = view.findViewById(R.id.vital_oxygen_saturation_progress);
        vitalBloodPressureProgress = view.findViewById(R.id.vital_blood_pressure_progress);
        vitalStressValueProgress = view.findViewById(R.id.vital_stress_value_progress);

        vitalHeartRateValue.setPreferredScrollingDirection(TickerView.ScrollingDirection.DOWN);
        vitalHrvSdnnValue.setPreferredScrollingDirection(TickerView.ScrollingDirection.DOWN);
        vitalRespiratoryRateValue.setPreferredScrollingDirection(TickerView.ScrollingDirection.DOWN);
        vitalOxygenSaturationValue.setPreferredScrollingDirection(TickerView.ScrollingDirection.DOWN);
        vitalHighestBloodPressureValue.setPreferredScrollingDirection(TickerView.ScrollingDirection.DOWN);
        vitalLowestBloodPressureValue.setPreferredScrollingDirection(TickerView.ScrollingDirection.DOWN);
        vitalStressValue.setPreferredScrollingDirection(TickerView.ScrollingDirection.DOWN);

        FragmentManager fm = getChildFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(R.id.survey_container, SurveyFragment.newInstance());
        ft.commit();

        bind();

        return view;
    }

    private void bind() {
        disposable.add(mainActivityViewModel.heartRateValue
                .throttleLast(updateInterval, TimeUnit.SECONDS, Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .startWithItem(initDoubleValue)
                .subscribe(heartRateValue -> {
                    vitalHeartRateProgress.setVisibility(heartRateValue == 0.0 ? View.VISIBLE : View.GONE);
                    vitalHeartRateValue.setVisibility(heartRateValue == 0.0 ? View.GONE : View.VISIBLE);
                    vitalHeartRateValue.setText(String.valueOf(heartRateValue.intValue()));
                }));

        disposable.add(mainActivityViewModel.oxygenSaturationValue
                .throttleLast(updateInterval, TimeUnit.SECONDS, Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .startWithItem(initDoubleValue)
                .subscribe(oxygenSaturationValue -> {
                    vitalOxygenSaturationProgress.setVisibility(oxygenSaturationValue == 0.0 ? View.VISIBLE : View.GONE);
                    vitalOxygenSaturationValue.setVisibility(oxygenSaturationValue == 0.0 ? View.GONE : View.VISIBLE);
                    vitalOxygenSaturationValue.setText(String.valueOf(oxygenSaturationValue.intValue()));
                }));

        disposable.add(mainActivityViewModel.respiratoryRateValue
                .throttleLast(updateInterval, TimeUnit.SECONDS, Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .startWithItem(initDoubleValue)
                .subscribe(respiratoryRateValue -> {
                    vitalRespiratoryRateProgress.setVisibility(respiratoryRateValue == 0.0 ? View.VISIBLE : View.GONE);
                    vitalRespiratoryRateValue.setVisibility(respiratoryRateValue == 0.0 ? View.GONE : View.VISIBLE);
                    vitalRespiratoryRateValue.setText(String.valueOf(respiratoryRateValue.intValue()));
                }));

        disposable.add(mainActivityViewModel.stressValue
                .throttleLast(updateInterval, TimeUnit.SECONDS, Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .startWithItem(getString(R.string.vital_stress_normal))
                .subscribe(stressValue -> {
                    vitalStressValueProgress.setVisibility(View.GONE);
                    vitalStressValue.setVisibility(View.VISIBLE);
                    vitalStressValue.setText(stressValue);
                }));

        disposable.add(mainActivityViewModel.hrvSdnnValue
                .throttleLast(updateInterval, TimeUnit.SECONDS, Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .startWithItem(initDoubleValue)
                .subscribe(hrvSdnnValue -> {
                    vitalHrvSdnnProgress.setVisibility(hrvSdnnValue == 0.0 ? View.VISIBLE : View.GONE);
                    vitalHrvSdnnValue.setVisibility(hrvSdnnValue == 0.0 ? View.GONE : View.VISIBLE);
                    vitalHrvSdnnValue.setText(String.valueOf(hrvSdnnValue.intValue()));
                }));

        disposable.add(
                Observable.zip(mainActivityViewModel.highestBloodPressureValue, mainActivityViewModel.lowestBloodPressureValue, Pair::new)
                        .throttleLast(updateInterval, TimeUnit.SECONDS, Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .startWithItem(new Pair<>(initDoubleValue, initDoubleValue))
                        .subscribe(bloodPressureValues -> {
                            vitalBloodPressureProgress.setVisibility(bloodPressureValues.first == 0.0 || bloodPressureValues.second == 0.0 ? View.VISIBLE : View.GONE);
                            int visibility = bloodPressureValues.first == 0.0 || bloodPressureValues.second == 0.0 ? View.GONE : View.VISIBLE;
                            vitalHighestBloodPressureValue.setVisibility(visibility);
                            vitalLowestBloodPressureValue.setVisibility(visibility);
                            vitalHighestBloodPressureValue.setText(bloodPressureValues.first.intValue() + " /");
                            vitalLowestBloodPressureValue.setText(String.valueOf(bloodPressureValues.second.intValue()));
                        })
        );
        disposable.add(viewModel.analysisResult.subscribe(
                hasSymptom -> mainActivityViewModel.analysisFinish(hasSymptom)
        ));
    }

    private void updateAnalysisProgress() {
        analysisTimeStart += analysisTimeInterval;
        analysisProgress.setProgress(analysisTimeStart);
    }

    private void analysisFinish() {
        setCameraLock(true);
        analysisProgressHandler.removeCallbacksAndMessages(null);
        vitalAnalysisWarningTextView.setVisibility(View.GONE);
        analysisCompleteContainer.setVisibility(View.VISIBLE);
        analysisCompleteContainer.postDelayed(() -> analysisContainer.setVisibility(View.GONE), 5000);
        viewModel.vitalAnalysisFinish();
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        textureView = view.findViewById(R.id.texture);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initValues();
    }

    @Override
    public void onResume() {
        super.onResume();
        analysisProgressHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (analysisAvailable) {
                    updateAnalysisProgress();
                }
                if (analysisTimeStart >= analysisTimeValue) {
                    analysisFinish();
                } else {
                    analysisProgressHandler.postDelayed(this, analysisTimeInterval);
                }
            }
        }, analysisTimeInterval);
    }

    @Override
    public void onPause() {
        analysisProgressHandler.removeCallbacksAndMessages(null);
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        disposable.clear();
        super.onDestroyView();
    }

    @Override
    protected AutoFitTextureView getTextureView() {
        return textureView;
    }

    @Override
    protected int getCropWidth() {
        return BlazeFace.INPUT_SIZE_WIDTH;
    }

    @Override
    protected int getCropHeight() {
        return BlazeFace.INPUT_SIZE_HEIGHT;
    }

    public List<Recognizer.Recognition> pre_mappedRecognitions = null;
    public List<Recognizer.Recognition> cur_mappedRecognitions;

    Noise noise;
    final float[] bpm = {0.0f};
    final float[] rr = {0.0f};
    final int[] cnt = {0};
    final float[] finalBpm = {0};
    final float[] finalRR = {0};
    final int[] finalCnt = {0};

    private long timestamp = 0;

    private MultiBoxTracker tracker;
    public static boolean computingDetection = false;
    private boolean training = false;
    private byte[] luminanceCopy;
    private Bitmap croppedfaceBitmap = null;
    private Bitmap test_cropped = null;

    private int BUFFER_SIZE = 100;
    private double levels = 2.0f;
    public int gaussian_w = 1;//(int) (200 / Math.pow(2.0, levels));
    //private int[] pixels_buffer = new int[gaussian_w * gaussian_w];
    private int pixels_buffer;
    private double[][]f_pixel_buff = new double[3][BUFFER_SIZE];
    private int bufferindex = 0;
    private int bpmCalculationFrequency = 15;
    private int bpCalculationFrequency = 100;
    private float[] fft_buffer = new float[BUFFER_SIZE + 2];
    private Vital vital = new Vital();
    private boolean[] hr_filter = new boolean[BUFFER_SIZE / 2];
    private boolean[] rr_filter = new boolean[BUFFER_SIZE / 2];
    private static int BPM_BUFFER_SIZE = 20;
    private float[] bpm_Buffer = new float[BPM_BUFFER_SIZE];
    private float[] rr_Buffer = new float[BPM_BUFFER_SIZE];
    private int bpm_buffer_index = 0;
    private float[] freq = new float[BUFFER_SIZE / 2];
    private int BP_FRAME = 100;
    private double[][] Vi = new double[3][BP_FRAME];
    private final int VIDEO_FRAME_RATE = 30;
    private double[] spo2_pixel_buff_R = new double[VIDEO_FRAME_RATE];
    private double[] spo2_pixel_buff_B = new double[VIDEO_FRAME_RATE];
    private int spo2_buff_index = 0;
    private int BP_bufferindex = 0;

    private List<Recognizer.Recognition> mappedRecognitions;
    private Recognizer recognizer;
    private Runnable postInferenceCallback;
    private OverlayView trackingOverlay;

    private void initValues() {
        noise = Noise.real(BUFFER_SIZE);
        float val = 0.3f;

        for (int i = 0; i < BUFFER_SIZE / 2; i++) {
            freq[i] = val * i;
            hr_filter[(i)] = Math.abs(freq[i]) >= 0.8 && Math.abs(freq[i]) <= 2.2;
            rr_filter[(i)] = Math.abs(freq[i]) >= 0.18 && Math.abs(freq[i]) <= 0.5;
        }
    }

    @Override
    protected void processImage(Image image) {
        trackingOverlay.postInvalidate();

        pre_mappedRecognitions = cur_mappedRecognitions;
        postInferenceCallback = image::close;

        ++timestamp;
        final long currTimestamp = timestamp;
        byte[] originalLuminance = getLuminance();
        tracker.onFrame(
                getPreviewWidth(),
                getPreviewHeight(),
                getLuminanceStride(),
                getSensorOrientation(),
                originalLuminance,
                timestamp);

        // No mutex needed as this method is not reentrant.
        if (computingDetection || training) {
            readyForNextImage();
            return;
        }
        computingDetection = true;

        if (luminanceCopy == null) {
            luminanceCopy = new byte[originalLuminance.length];
        }
        System.arraycopy(originalLuminance, 0, luminanceCopy, 0, originalLuminance.length);
        readyForNextImage();

        final Canvas canvas = new Canvas(getCroppedBitmap());
        canvas.drawBitmap(getRgbFrameBitmap(), getFrameToCropTransform(), null);

        runInBackground(
                () -> {
                    // inference
                    mappedRecognitions =
                            recognizer.recognizeImage(getCroppedBitmap(), getCropToFrameTransform(), finalBpm[0] / finalCnt[0]);

                    if (mappedRecognitions.size() == 1) { // 심장박동, SPO2 측정을 위해서 최소 1개이상의 얼굴 감지가 필요
                        Rect rect = new Rect();
                        mappedRecognitions.get(0).getLocation().round(rect); // mappedRecognition의 return이 float이기 때문에 int로 변환
                        if (rect.left <= 0 || rect.top <= 0 || rect.bottom >= getRgbFrameBitmap().getHeight() || rect.right >= getRgbFrameBitmap().getWidth()) { // 얼굴이 화면에 들어와 있는지 검사
                            vitalAnalysisWarningTextView.post(() -> vitalAnalysisWarningTextView.setVisibility(View.VISIBLE));
                            analysisAvailable = false;
                        } else {
                            vitalAnalysisWarningTextView.post(() -> vitalAnalysisWarningTextView.setVisibility(View.GONE));
                            analysisAvailable = true;

                            croppedfaceBitmap = Bitmap.createBitmap(getRgbFrameBitmap(), rect.left, rect.top, rect.width(), rect.height());
                            //rescale: resizez + gaussian
                            test_cropped = Bitmap.createScaledBitmap(croppedfaceBitmap, gaussian_w, gaussian_w, false); //Gaussian pyramid 2단계 50x50 resize
                            pixels_buffer = test_cropped.getPixel(0,0);

                            double sum_r = 0;
                            double sum_b = 0;
                            double sum_g = 0;

                            int r = (pixels_buffer& 0xFF0000) >> 16;
                            int g = (pixels_buffer & 0x00FF00) >> 8;
                            int b = (pixels_buffer & 0x0000FF);

                            f_pixel_buff[0][bufferindex]= Float.valueOf(String.valueOf(r));
                            f_pixel_buff[1][bufferindex]= Float.valueOf(String.valueOf(g));
                            f_pixel_buff[2][bufferindex]= Float.valueOf(String.valueOf(b));

                            if (bufferindex % bpmCalculationFrequency == 0) {
                                double[] pre_processed = vital.preprocessing(f_pixel_buff,false);
                                bpm_Buffer[bpm_buffer_index] = (vital.get_HR(pre_processed,BUFFER_SIZE));
                                rr_Buffer[bpm_buffer_index] = (vital.get_RR(pre_processed,BUFFER_SIZE));
                                bpm_buffer_index = (bpm_buffer_index + 1) % BPM_BUFFER_SIZE;
                                int count = 0;
                                int i;
                                for (i = 0, bpm[0] = 0, rr[0] = 0; i < BPM_BUFFER_SIZE; i++) {
                                    bpm[0] += bpm_Buffer[i];
                                    rr[0] += rr_Buffer[i];
                                    if (bpm_Buffer[i] == 0.0f && rr_Buffer[i] == 0.0f) {
                                        Log.d("BPM",""+i);
                                        break;
                                    }
                                }
                                Log.d("BPM", "" + bpm[0] /i + "cnt" + i);
                                Log.d("BPM", "RR" + rr[0] /i + "cnt" + i);

                                HR_result = Math.round(bpm[0] /(i));
                                RR_result = Math.round(rr[0] /(i));

                                //--------SDNN --------------------//
                                sdnn_result = vital.SDNN(bpm_Buffer, bpm_buffer_index);
                                sdnn_result = Math.round(sdnn_result * 100) / 100.0;
                                sdnn_result = sdnn_result * 100;
                                //--------SDNN --------------------//
                                LF_HF_ratio = vital.LF_HF_ratio(pre_processed,BUFFER_SIZE);

                                try {
                                    spo2_result = vital.spo2(f_pixel_buff[0], f_pixel_buff[2], VIDEO_FRAME_RATE);
                                } catch (Exception e) {
                                    spo2_result = 0;
                                }
                                spo2_result = Math.round(spo2_result);


                            }
                            if (bufferindex % bpCalculationFrequency == 0) {
                                double[] preprocessed_g = vital.get_normG(f_pixel_buff[1]);
                                double peak_avg = vital.get_peak_avg(preprocessed_g,true);
                                double valley_avg = vital.get_peak_avg(preprocessed_g,false);
                                Log.d("BP",""+peak_avg+":"+valley_avg);
                                double bmi = 20.7;

                                //원래는 23.7889
                                SBP = 23.7889 + (95.4335 * peak_avg) + (4.5958 * bmi) - (5.109 * peak_avg * bmi);
                                DBP = -17.3772 - (115.1747 * valley_avg) + (4.0251 * bmi) + (5.2825 * valley_avg * bmi);

                                BP = SBP * 0.33 + DBP * 0.66;
                            }
                            bufferindex = (bufferindex + 1) % BUFFER_SIZE;


                            VitalAnalysisModel model = new VitalAnalysisModel(
                                    HR_result, spo2_result,
                                    RR_result, LF_HF_ratio, sdnn_result,
                                    SBP, DBP
                            );
                            viewModel.setVitalAnalysisModel(model);
                            mainActivityViewModel.setVitalAnalysisData(model);

                        }
                    } else {
                        vitalAnalysisWarningTextView.post(() -> vitalAnalysisWarningTextView.setVisibility(View.VISIBLE));
                        analysisAvailable = false;
                    }

                    tracker.trackResults(mappedRecognitions, luminanceCopy, currTimestamp);
                    trackingOverlay.postInvalidate();
                    computingDetection = false;
                });
    }

    protected void readyForNextImage() {
        if (postInferenceCallback != null) {
            postInferenceCallback.run();
        }
    }

}