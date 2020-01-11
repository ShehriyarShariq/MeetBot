package com.shehriyar.experienceadvisor.viewmodel;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.shehriyar.experienceadvisor.R;
import com.shehriyar.experienceadvisor.model.ExpReview;

import java.util.ArrayList;

public class ExperienceRatingsListAdapter extends RecyclerView.Adapter<ExperienceRatingsListAdapter.ExperienceRatingsListViewHolder> {

    ArrayList<ExpReview> ratings;

    public ExperienceRatingsListAdapter(ArrayList<ExpReview> ratings) {
        this.ratings = ratings;
    }

    @NonNull
    @Override
    public ExperienceRatingsListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.exps_ratings_list_single_item_layout, parent, false);
        return new ExperienceRatingsListAdapter.ExperienceRatingsListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExperienceRatingsListViewHolder holder, int position) {
        ExpReview rating = ratings.get(position);

        holder.msg.setText(rating.getMessage());
        holder.rating.setProgress((int) rating.getRating());
    }

    @Override
    public int getItemCount() {
        return ratings.size();
    }

    public class ExperienceRatingsListViewHolder extends RecyclerView.ViewHolder {

        TextView msg;
        RatingBar rating;

        public ExperienceRatingsListViewHolder(@NonNull View itemView) {
            super(itemView);

            msg = itemView.findViewById(R.id.msg);
            rating = itemView.findViewById(R.id.rating);
        }
    }
}
