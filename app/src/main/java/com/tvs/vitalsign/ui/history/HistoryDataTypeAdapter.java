package com.tvs.vitalsign.ui.history;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.tvs.vitalsign.R;

import java.util.Arrays;
import java.util.List;

class HistoryDataTypeAdapter extends ListAdapter<HistoryDataTypeModel, HistoryDataTypeAdapter.HistoryDataViewHolder> {

    private List<HistoryDataTypeModel> list = Arrays.asList(
            new HistoryDataTypeModel(HistoryDataType.HEART_RATE),
            new HistoryDataTypeModel(HistoryDataType.OXYGEN_SATURATION),
            new HistoryDataTypeModel(HistoryDataType.RESPIRATORY_RATE),
            new HistoryDataTypeModel(HistoryDataType.STRESS),
            new HistoryDataTypeModel(HistoryDataType.HRV_SDNN),
            new HistoryDataTypeModel(HistoryDataType.BLOOD_PRESSURE)
    );

    private final int maxItemCount = 6;
    private int selectedPosition = 0;
    private OnHistoryDataClickListener listener = null;

    public void setListener(OnHistoryDataClickListener listener) {
        this.listener = listener;
    }

    public HistoryDataTypeAdapter() {
        super(new DiffUtil.ItemCallback<HistoryDataTypeModel>() {
            @Override
            public boolean areItemsTheSame(@NonNull HistoryDataTypeModel oldItem, @NonNull HistoryDataTypeModel newItem) {
                return oldItem.type.equals(newItem.type);
            }

            @Override
            public boolean areContentsTheSame(@NonNull HistoryDataTypeModel oldItem, @NonNull HistoryDataTypeModel newItem) {
                return oldItem.type.equals(newItem.type);
            }
        });
        submitList(list);
    }

    @Override
    public int getItemCount() {
        return Math.min(super.getItemCount(), maxItemCount);
    }

    @NonNull
    @Override
    public HistoryDataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new HistoryDataViewHolder(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryDataViewHolder holder, int position) {
        holder.bind(getItem(position), position);
    }

    class HistoryDataViewHolder extends RecyclerView.ViewHolder {

        private TextView text;
        private ConstraintLayout textContainer;

        public HistoryDataViewHolder(@NonNull View parentView) {
            super(LayoutInflater.from(parentView.getContext()).inflate(R.layout.item_history_data, null, false));
            init();
        }

        private void init() {
            text = itemView.findViewById(R.id.text);
            textContainer = itemView.findViewById(R.id.text_container);
        }

        public void bind(HistoryDataTypeModel model, int position) {
            text.setText(model.type.name);
            text.setTextColor(text.getResources().getColor(position == selectedPosition ? R.color.white : R.color.brownishGrey, null));
            textContainer.setBackgroundColor(textContainer.getResources().getColor(position == selectedPosition ? R.color.lightish_blue : R.color.offWhite, null));
            textContainer.setOnClickListener(v -> updateSelectedPosition(position));
        }

    }

    public void updateSelectedPosition(int position) {
        int prevSelectedPosition = selectedPosition;
        selectedPosition = position;
        notifyItemChanged(prevSelectedPosition);
        notifyItemChanged(position);
        if (listener != null) listener.onClick(getCurrentList().get(position));
    }

    public interface OnHistoryDataClickListener {
        void onClick(HistoryDataTypeModel model);
    }

}
