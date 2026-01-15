package com.mobl.clothingmarket.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.mobl.clothingmarket.R;

import java.util.ArrayList;
import java.util.List;

public class FilterChipAdapter extends RecyclerView.Adapter<FilterChipAdapter.FilterChipViewHolder> {
    private List<FilterItem> filters;
    private OnFilterRemoveListener listener;

    public interface OnFilterRemoveListener {
        void onFilterRemove(FilterItem filter);
    }

    public static class FilterItem {
        private String label;
        private FilterType type;

        public FilterItem(String label, FilterType type) {
            this.label = label;
            this.type = type;
        }

        public String getLabel() {
            return label;
        }

        public FilterType getType() {
            return type;
        }
    }

    public enum FilterType {
        CATEGORY, PRICE, STOCK, SIZE
    }

    public FilterChipAdapter(OnFilterRemoveListener listener) {
        this.filters = new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public FilterChipViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_filter_chip, parent, false);
        return new FilterChipViewHolder((Chip) view);
    }

    @Override
    public void onBindViewHolder(@NonNull FilterChipViewHolder holder, int position) {
        FilterItem item = filters.get(position);
        holder.chip.setText(item.getLabel());
        holder.chip.setOnCloseIconClickListener(v -> {
            if (listener != null) {
                listener.onFilterRemove(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return filters.size();
    }

    public void updateFilters(List<FilterItem> newFilters) {
        this.filters = newFilters;
        notifyDataSetChanged();
    }

    static class FilterChipViewHolder extends RecyclerView.ViewHolder {
        Chip chip;

        public FilterChipViewHolder(@NonNull Chip chip) {
            super(chip);
            this.chip = chip;
        }
    }
}



