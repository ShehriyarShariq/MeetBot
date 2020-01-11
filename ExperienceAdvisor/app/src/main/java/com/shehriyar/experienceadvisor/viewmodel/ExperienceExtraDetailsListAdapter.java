package com.shehriyar.experienceadvisor.viewmodel;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.shehriyar.experienceadvisor.R;

import java.util.ArrayList;

public class ExperienceExtraDetailsListAdapter extends RecyclerView.Adapter<ExperienceExtraDetailsListAdapter.ExperienceExtraDetailsListViewHolder> {

    ArrayList<String> extraDetails;

    public ExperienceExtraDetailsListAdapter(ArrayList<String> extraDetails) {
        this.extraDetails = extraDetails;
    }

    @NonNull
    @Override
    public ExperienceExtraDetailsListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.exps_extra_details_list_single_item_layout, parent, false);
        return new ExperienceExtraDetailsListAdapter.ExperienceExtraDetailsListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExperienceExtraDetailsListViewHolder holder, int position) {
        holder.detail.setText(" - " + extraDetails.get(position));
    }

    @Override
    public int getItemCount() {
        return extraDetails.size();
    }

    public class ExperienceExtraDetailsListViewHolder extends RecyclerView.ViewHolder {

        TextView detail;

        public ExperienceExtraDetailsListViewHolder(@NonNull View itemView) {
            super(itemView);

            detail = itemView.findViewById(R.id.itemTxt);
        }
    }
}
