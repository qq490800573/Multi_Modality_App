package com.tvs.vitalsign.ui.history;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import com.tvs.vitalsign.R;

import java.util.Locale;

@SuppressLint("ViewConstructor")
public class HighlightMarkerView extends MarkerView {

    private final TextView tvContent;

    public HighlightMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);

        tvContent = findViewById(R.id.tvContent);
    }

    // runs every time the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        VitalDataEntry vde = ((VitalDataEntry) e);
        tvContent.setText(
                String.format(Locale.getDefault(),
                        "%d%s", (int) e.getY(), tvContent.getContext().getText(vde.unitTextRes))
        );
        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth() / 2), -getHeight());
    }
}