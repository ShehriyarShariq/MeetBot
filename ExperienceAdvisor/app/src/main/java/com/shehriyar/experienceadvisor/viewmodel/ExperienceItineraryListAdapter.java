package com.shehriyar.experienceadvisor.viewmodel;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.shehriyar.experienceadvisor.R;
import com.shehriyar.experienceadvisor.databinding.ItineraryListSingleItemLayoutBinding;
import com.shehriyar.experienceadvisor.model.ExpDay;

import java.util.ArrayList;

public class ExperienceItineraryListAdapter extends RecyclerView.Adapter<ExperienceItineraryListAdapter.ExperienceItineraryListViewHolder> {

    ArrayList<ExpDay> itinerary;

    public ExperienceItineraryListAdapter(ArrayList<ExpDay> itinerary) {
        this.itinerary = itinerary;
    }

    @NonNull
    @Override
    public ExperienceItineraryListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.itinerary_list_single_item_layout, parent, false);

        ItineraryListSingleItemLayoutBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.itinerary_list_single_item_layout, parent, false);
        return new ExperienceItineraryListAdapter.ExperienceItineraryListViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ExperienceItineraryListViewHolder holder, int position) {
        ExpDay expDay = itinerary.get(position);

        holder.binding.title.setText(expDay.getLocation());
        holder.binding.dayID.setText("Day " + (position + 1));

        if(position == itinerary.size() - 1){
            holder.binding.line.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return itinerary.size();
    }

    public class ExperienceItineraryListViewHolder extends RecyclerView.ViewHolder {

        private ItineraryListSingleItemLayoutBinding binding;

        public ExperienceItineraryListViewHolder(@NonNull ItineraryListSingleItemLayoutBinding binding) {
            super(binding.getRoot());

            this.binding = binding;
        }
    }
}
