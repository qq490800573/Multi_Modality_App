package com.tvs.vitalsign.ui.survey;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.tvs.vitalsign.R;

import java.util.List;

class SurveyExamplesAdapter extends ListAdapter<SurveyExampleModel, SurveyExamplesAdapter.SurveyExampleViewHolder> {

    private final int maxItemCount = 6;

    public SurveyExamplesAdapter() {
        super(new DiffUtil.ItemCallback<SurveyExampleModel>() {
            @Override
            public boolean areItemsTheSame(@NonNull SurveyExampleModel oldItem, @NonNull SurveyExampleModel newItem) {
                return oldItem.text.equals(newItem.text);
            }

            @Override
            public boolean areContentsTheSame(@NonNull SurveyExampleModel oldItem, @NonNull SurveyExampleModel newItem) {
                return oldItem.text.equals(newItem.text) && oldItem.selected == newItem.selected;
            }
        });
    }

    @Override
    public int getItemCount() {
        return Math.min(super.getItemCount(), maxItemCount);
    }

    @NonNull
    @Override
    public SurveyExampleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SurveyExampleViewHolder(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull SurveyExampleViewHolder holder, int position) {
        holder.bind(getItem(position), position);
    }

    public void selectExample(String example) {
        for (int i = 0; i < getCurrentList().size(); i++) {
            SurveyExampleModel model = getCurrentList().get(i);
            if (model.text.replaceAll(" ", "").equals(example)) {
                model.selected = !model.selected;
                notifyItemChanged(i);
            }
        }
    }

    class SurveyExampleViewHolder extends RecyclerView.ViewHolder {

        private TextView text;
        private CardView surveyExampleContainer;

        public SurveyExampleViewHolder(@NonNull View parentView) {
            super(LayoutInflater.from(parentView.getContext()).inflate(R.layout.item_survey_example, null, false));
            init();
        }

        private void init() {
            text = itemView.findViewById(R.id.example_text);
            surveyExampleContainer = itemView.findViewById(R.id.survey_example_container);
        }

        public void bind(SurveyExampleModel model, int position) {
            text.setText(model.text);
            Resources resources = itemView.getResources();
            int bgColor = resources.getColor(model.selected ? R.color.lightish_blue : R.color.offWhite, null);
            surveyExampleContainer.setBackgroundColor(bgColor);
            int textColor = resources.getColor(model.selected ? R.color.offWhite : R.color.black, null);
            text.setTextColor(textColor);
            surveyExampleContainer.setOnClickListener(v -> {
                boolean toggle = !getItem(position).selected;
                List<SurveyExampleModel> list = getCurrentList();
                list.get(position).selected = toggle;
                notifyItemChanged(position);
            });
        }

    }
}
