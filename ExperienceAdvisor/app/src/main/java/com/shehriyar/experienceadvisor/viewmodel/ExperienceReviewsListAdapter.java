package com.shehriyar.experienceadvisor.viewmodel;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.shehriyar.experienceadvisor.R;
import com.shehriyar.experienceadvisor.model.ExpReview;

import java.util.ArrayList;

public class ExperienceReviewsListAdapter extends RecyclerView.Adapter<ExperienceReviewsListAdapter.ExperienceReviewsListViewHolder> {

    ArrayList<ExpReview> reviews;

    public ExperienceReviewsListAdapter(ArrayList<ExpReview> reviews) {
        this.reviews = reviews;
    }

    @NonNull
    @Override
    public ExperienceReviewsListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.exps_reviews_list_single_item_layout, parent, false);
        return new ExperienceReviewsListAdapter.ExperienceReviewsListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExperienceReviewsListViewHolder holder, int position) {
        ExpReview review = reviews.get(position);

        holder.name.setText(review.getReviewer());
        holder.msg.setText(review.getMessage());
        holder.rating.setProgress((int) review.getRating());
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    public class ExperienceReviewsListViewHolder extends RecyclerView.ViewHolder {

        TextView msg, name;
        RatingBar rating;
        ImageView reviewerImg;

        public ExperienceReviewsListViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.name);
            msg = itemView.findViewById(R.id.msg);
            rating = itemView.findViewById(R.id.rating);
            reviewerImg = itemView.findViewById(R.id.reviewerProfileImg);
        }
    }
}

