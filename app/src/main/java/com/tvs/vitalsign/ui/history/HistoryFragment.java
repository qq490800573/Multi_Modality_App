package com.tvs.vitalsign.ui.history;

import android.app.DatePickerDialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.robinhood.ticker.TickerView;
import com.tvs.vitalsign.R;
import com.tvs.vitalsign.VitalSignApp;
import com.tvs.vitalsign.ui.view.GridSpacingItemDecoration;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HistoryFragment extends Fragment {

    private static final String TAG = HistoryFragment.class.getSimpleName();

    private HistoryViewModel viewModel;
    private Button datePickerButton;
    private ImageView back;
    private DatePickerDialog dialog;
    private RecyclerView dataRecyclerView;
    private HistoryDataTypeAdapter adapter;
    private LineChart chart;

    private TextView historyTestDate;
    private TickerView heartRateValue;
    private TickerView hrvSdnnValue;
    private TickerView respiratoryRateValue;
    private TickerView oxygenSaturationValue;
    private TickerView highestBloodPressureValue;
    private TickerView lowestBloodPressureValue;
    private TickerView stressValue;

    public static HistoryFragment newInstance() {
        return new HistoryFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this, VitalSignApp.factory).get(HistoryViewModel.class);
        bind();
        initView(view);
    }

    private void bind() {
        viewModel.heartRateValues.observe(this, values -> setChartData(HistoryDataType.HEART_RATE, values));
        viewModel.oxygenSaturationValues.observe(this, values -> setChartData(HistoryDataType.OXYGEN_SATURATION, values));
        viewModel.respiratoryRateValues.observe(this, values -> setChartData(HistoryDataType.RESPIRATORY_RATE, values));
        viewModel.stressValues.observe(this, values -> setChartData(HistoryDataType.STRESS, values));
        viewModel.hrvSdnnValues.observe(this, values -> setChartData(HistoryDataType.HRV_SDNN, values));
        viewModel.highestBloodPressureValues.observe(this, values -> setChartData(HistoryDataType.BLOOD_PRESSURE, values, viewModel.lowestBloodPressureValues.getValue()));

        viewModel.latestTestDate.observe(this, value ->
                historyTestDate.setText(String.format(getString(R.string.history_test_date), value)));
        viewModel.heartRateValue.observe(this, value ->
                heartRateValue.setText(value));
        viewModel.oxygenSaturationValue.observe(this, value ->
                oxygenSaturationValue.setText(value));
        viewModel.respiratoryRateValue.observe(this, value ->
                respiratoryRateValue.setText(value));
        viewModel.stressValue.observe(this, value ->
                stressValue.setText(value));
        viewModel.hrvSdnnValue.observe(this, value ->
                hrvSdnnValue.setText(value));
        viewModel.highestBloodPressureValue.observe(this, value ->
                highestBloodPressureValue.setText(value));
        viewModel.lowestBloodPressureValue.observe(this, value ->
                lowestBloodPressureValue.setText(value));
    }

    private void initView(View view) {
        back = view.findViewById(R.id.back);
        back.setOnClickListener(v -> requireActivity().onBackPressed());

        historyTestDate = view.findViewById(R.id.history_test_date);
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

        datePickerButton = view.findViewById(R.id.date_picker_button);
        Calendar c = Calendar.getInstance(Locale.getDefault());
        c.setTimeInMillis(System.currentTimeMillis());
        dialog = new DatePickerDialog(requireContext(), R.style.MySpinnerDatePickerStyle, this::updateDate,
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        dialog.getDatePicker().findViewById(Resources.getSystem()
                .getIdentifier("day", "id", "android"))
                .setVisibility(View.GONE);
        dialog.getDatePicker().setCalendarViewShown(false);
        datePickerButton.setOnClickListener(v -> dialog.show());

        dataRecyclerView = view.findViewById(R.id.data_recycler);
        int spanCount = ((GridLayoutManager) dataRecyclerView.getLayoutManager()).getSpanCount();
        dataRecyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, 20, false));
        adapter = new HistoryDataTypeAdapter();
        adapter.setListener(this::updateGraph);
        dataRecyclerView.setAdapter(adapter);

        initChart(view);

        updateDate(null, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
    }

    private void updateDate(View view, int year, int month, int dayOfMonth) {
        String selectedDate = String.format(Locale.getDefault(), "%04d/%02d", year, month + 1);
        datePickerButton.setText(selectedDate);
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month + 1);
        calendar.set(Calendar.DAY_OF_MONTH, 0);
        long utcDateFrom = calendar.getTimeInMillis();
        calendar.add(Calendar.MONTH, 1);
        long utcDateTo = calendar.getTimeInMillis();
        viewModel.setDate(utcDateFrom, utcDateTo);
        adapter.updateSelectedPosition(0);
    }

    private void updateGraph(HistoryDataTypeModel model) {
        viewModel.loadAnalysis(model.type);
    }

    private void initChart(View view) {
        chart = view.findViewById(R.id.chart);
        chart.setBackgroundColor(view.getResources().getColor(R.color.pale_sky_blue_two, null));
        chart.setTouchEnabled(true);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setDoubleTapToZoomEnabled(false);
        chart.setPinchZoom(false);
        chart.setMinOffset(getResources().getDimension(R.dimen.history_chart_min_offset));
        chart.setAutoScaleMinMaxEnabled(true);
        chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                viewModel.loadAnalysisOnTime(((VitalDataEntry) e).utcTime);
            }

            @Override
            public void onNothingSelected() {

            }
        });
        XAxis x = chart.getXAxis();
        x.setEnabled(false);
        YAxis yLeft = chart.getAxisLeft();
        YAxis yRight = chart.getAxisRight();
        yLeft.setEnabled(false);
        yRight.setEnabled(false);

        // create marker to display box when values are selected
        HighlightMarkerView mv = new HighlightMarkerView(requireContext(), R.layout.custom_marker_view);

        // Set the marker to the chart
        mv.setChartView(chart);
        chart.setMarker(mv);
    }

    private void setChartData(HistoryDataType type, List<HistoryDataModel> rawValues) {
        setChartData(type, rawValues, null);
    }

    private void setChartData(HistoryDataType type, List<HistoryDataModel> rawValues, List<HistoryDataModel> rawValues2) {

        if (rawValues.isEmpty()) {
            chart.setData(null);
            chart.invalidate();
            viewModel.loadAnalysisOnTime(0L);
            return;
        }

        ArrayList<Entry> values = new ArrayList<>();

        int unitTextRes = R.string.empty;
        switch (type) {
            case HEART_RATE:
                unitTextRes = R.string.heart_rate_unit;
                break;
            case OXYGEN_SATURATION:
                unitTextRes = R.string.oxygen_saturation_unit;
                break;
            case RESPIRATORY_RATE:
                unitTextRes = R.string.respiratory_rate_unit;
                break;
            case STRESS:
                unitTextRes = R.string.empty;
                break;
            case HRV_SDNN:
                unitTextRes = R.string.hrv_sdnn_unit;
                break;
            case BLOOD_PRESSURE:
                unitTextRes = R.string.blood_pressure_unit;
                break;
        }

        for (int i = 0; i < rawValues.size(); i++) {
            HistoryDataModel model = rawValues.get(i);
            values.add(new VitalDataEntry(i, model.value.floatValue(), unitTextRes, model.utcTime));
        }
        LineDataSet set1;

        set1 = buildLineDataSet(values, "DataSet 1");
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1); // add the data sets

        if (rawValues2 != null) {
            ArrayList<Entry> values1 = new ArrayList<>();
            for (int i = 0; i < rawValues2.size(); i++) {
                HistoryDataModel model = rawValues2.get(i);
                values1.add(new VitalDataEntry(i, model.value.floatValue(), unitTextRes, model.utcTime));
            }
            dataSets.add(buildLineDataSet(values1, "DataSet 2"));
        }

        // create a data object with the data sets
        LineData data = new LineData(dataSets);

        // set data
        chart.setData(data);
        chart.invalidate();

        if (!values.isEmpty()) {
            Entry lastEntry = values.get(values.size() - 1);
            Highlight highlight = new Highlight(lastEntry.getX(), lastEntry.getY(), 0);
            highlight.setDataIndex(0);
            chart.highlightValue(highlight);
            chart.animateX(1000);
            viewModel.loadAnalysisOnTime(((VitalDataEntry) lastEntry).utcTime);
        }
    }

    private LineDataSet buildLineDataSet(List<Entry> values, String dateSetName) {
        LineDataSet set = new LineDataSet(values, "DataSet 1");

        set.setColor(ResourcesCompat.getColor(getResources(), R.color.brownishGrey, null));
        set.setCircleColor(ResourcesCompat.getColor(getResources(), R.color.brownishGrey, null));

        set.setDrawValues(false);
        set.setHighLightColor(ResourcesCompat.getColor(getResources(), R.color.darkish_blue, null));

        // line thickness and point size
        set.setLineWidth(getResources().getDimension(R.dimen.history_chart_line_width));
        set.setCircleRadius(getResources().getDimension(R.dimen.history_chart_circle_radius));

        // draw points as solid circles
        set.setDrawCircleHole(false);

        set.setHighlightLineWidth(getResources().getDimension(R.dimen.history_chart_highlight_line_width));
        return set;
    }

}
