package com.shehriyar.experienceadvisor.viewmodel;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.shehriyar.experienceadvisor.R;
import com.shehriyar.experienceadvisor.listeners.PopularExperiencesListOnClickListener;
import com.shehriyar.experienceadvisor.model.Experience;

import java.util.ArrayList;

public class PopularExperiencesListAdapter extends RecyclerView.Adapter<PopularExperiencesListAdapter.PopularExperiencesListViewHolder> {

    ArrayList<Experience> experiences;

    PopularExperiencesListOnClickListener popularExperiencesListOnClickListener;

    public PopularExperiencesListAdapter(ArrayList<Experience> experiences, PopularExperiencesListOnClickListener popularExperiencesListOnClickListener) {
        this.experiences = experiences;
        this.popularExperiencesListOnClickListener = popularExperiencesListOnClickListener;
    }

    @NonNull
    @Override
    public PopularExperiencesListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.popular_exps_list_single_item_layout, parent, false);
        return new PopularExperiencesListAdapter.PopularExperiencesListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PopularExperiencesListViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return experiences.size();
    }

    public class PopularExperiencesListViewHolder extends RecyclerView.ViewHolder {
        public PopularExperiencesListViewHolder(@NonNull final View itemView) {
            super(itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popularExperiencesListOnClickListener.OnItemClicked(getLayoutPosition());
                }
            });
        }
    }
}
