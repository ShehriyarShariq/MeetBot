package com.shehriyar.meetingsetter.viewmodel;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.shehriyar.meetingsetter.R;
import com.shehriyar.meetingsetter.listener.MeetingsTimeSetterListEventListener;
import com.shehriyar.meetingsetter.model.TimeRange;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class MeetingsTimeSetterListAdapter extends RecyclerView.Adapter<MeetingsTimeSetterListAdapter.MeetingsTimeSetterListViewHolder> {

    ArrayList<TimeRange> timeRanges;
    ArrayList<HashMap<String, Long>> allowedTimeRanges;

    MeetingsTimeSetterListEventListener meetingsTimeSetterListEventListener;

    public MeetingsTimeSetterListAdapter(ArrayList<TimeRange> timeRanges, ArrayList<HashMap<String, Long>> allowedTimeRanges, MeetingsTimeSetterListEventListener meetingsTimeSetterListEventListener) {
        this.timeRanges = timeRanges;
        this.allowedTimeRanges = allowedTimeRanges;
        this.meetingsTimeSetterListEventListener = meetingsTimeSetterListEventListener;
    }

    @NonNull
    @Override
    public MeetingsTimeSetterListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.meetings_time_setter_list_single_item_layout, parent, false);
        return new MeetingsTimeSetterListAdapter.MeetingsTimeSetterListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MeetingsTimeSetterListViewHolder holder, int position) {
        TimeRange timeRange = timeRanges.get(position);
        HashMap<String, Long> allowedTimeRange = allowedTimeRanges.get(position);

        long startTime = allowedTimeRange.get("start");
        long endTime = allowedTimeRange.get("end");
        Calendar startTimeCal = Calendar.getInstance();
        Calendar endTimeCal = Calendar.getInstance();
        startTimeCal.setTimeInMillis(startTime);
        endTimeCal.setTimeInMillis(endTime);

        Date startTimeDate = startTimeCal.getTime();
        Date endTimeDate = endTimeCal.getTime();

        CalendarDay rangeStartDate = CalendarDay.from(startTimeDate.getYear() + 1900, startTimeDate.getMonth() + 1, startTimeDate.getDate());
        CalendarDay rangeEndDate = CalendarDay.from(endTimeDate.getYear() + 1900, endTimeDate.getMonth() + 1, endTimeDate.getDate());

        final Time startTimeActual = new Time(startTime);
        final Time endTimeActual = new Time(endTime);

        CalendarDay date = timeRange.getDate();
        Date currDate = new Date(date.getYear() - 1900, date.getMonth(), date.getDay());

        boolean isRange = rangeStartDate.isBefore(rangeEndDate) && rangeEndDate.isAfter(rangeStartDate);

        if(isRange){
            Calendar minTimeCal = Calendar.getInstance();
            Calendar maxTimeCal = Calendar.getInstance();
            minTimeCal.set(date.getYear(), date.getMonth(), date.getDay(), 0, 0);
            maxTimeCal.set(date.getYear(), date.getMonth(), date.getDay(), 23, 59);

            startTimeActual.setTime(minTimeCal.getTimeInMillis());
            endTimeActual.setTime(maxTimeCal.getTimeInMillis());
        }

        holder.date.setText(String.format("%td %tb %tY", currDate, currDate, currDate));

        if(timeRange.getStartTime() == null){
            holder.startTime.setText("None");
        } else {
            holder.startTime.setText(String.format("%tR", timeRange.getStartTime()));
        }

        if(timeRange.getEndTime() == null){
            holder.endTime.setText("None");
        } else {
            holder.endTime.setText(String.format("%tR", timeRange.getEndTime()));
        }

        holder.startTimeSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                meetingsTimeSetterListEventListener.OnSetStartTimeClicked(holder.getLayoutPosition(), startTimeActual, endTimeActual);
            }
        });

        holder.endTimeSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                meetingsTimeSetterListEventListener.OnSetEndTimeClicked(holder.getLayoutPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return timeRanges.size();
    }

    public class MeetingsTimeSetterListViewHolder extends RecyclerView.ViewHolder {

        TextView date, startTime, endTime;
        CardView startTimeSelector, endTimeSelector;

        public MeetingsTimeSetterListViewHolder(@NonNull View itemView) {
            super(itemView);

            date = itemView.findViewById(R.id.date);
            startTime = itemView.findViewById(R.id.startTime);
            endTime = itemView.findViewById(R.id.endTime);

            startTimeSelector = itemView.findViewById(R.id.startTimeSelector);
            endTimeSelector = itemView.findViewById(R.id.endTimeSelector);
        }
    }

    public void refreshDataset(ArrayList<TimeRange> timeRanges){
        this.timeRanges = timeRanges;
        notifyDataSetChanged();
    }

    public void setAllowedTimeRanges(ArrayList<HashMap<String, Long>> allowedTimeRanges){
        this.allowedTimeRanges = allowedTimeRanges;
    }
}
