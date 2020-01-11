package com.shehriyar.meetingsetter.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.shehriyar.meetingsetter.R;
import com.shehriyar.meetingsetter.databinding.ActivityCalendarBinding;
import com.shehriyar.meetingsetter.listener.MeetingsTimeSetterListEventListener;
import com.shehriyar.meetingsetter.model.TimeRange;
import com.shehriyar.meetingsetter.util.LoaderDialog;
import com.shehriyar.meetingsetter.viewmodel.MeetingsTimeSetterListAdapter;

import java.sql.Array;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class CalendarActivity extends AppCompatActivity {

    ActivityCalendarBinding binding;

    String meetingID;
    CalendarDay minDate, maxDate;

    ArrayList<TimeRange> timeRanges;

    MeetingsTimeSetterListAdapter meetingsTimeSetterListAdapter;

    int currentMeetingPos = -1;

    LoaderDialog loaderDialog, uploadDialog;

    DatabaseReference firebaseDatabase;

    long durationInMs;
    ArrayList<HashMap<String, Long>> allowedTimeRanges, selectedAllowedTimeRanges;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_calendar);

        Intent meetingIntent = getIntent();

        meetingID = meetingIntent.getStringExtra("meetingID");

        loaderDialog = new LoaderDialog(this, "Load");
        uploadDialog = new LoaderDialog(this, "Upload");

        loaderDialog.showDialog();

        selectedAllowedTimeRanges = new ArrayList<>();

        firebaseDatabase = FirebaseDatabase.getInstance().getReference();

        firebaseDatabase.child("Meetings").child(meetingID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String durationStr = dataSnapshot.child("duration").getValue().toString();
                durationStr = durationStr.replace(" ", "");

                durationInMs = getDurationFromStr(durationStr);
                allowedTimeRanges = (ArrayList<HashMap<String, Long>>) dataSnapshot.child("timeSlots").getValue();

                loaderDialog.hideDialog();

                LinearLayoutManager meetingsListLinearLayoutManager = new LinearLayoutManager(CalendarActivity.this);
                binding.meetingsList.setLayoutManager(meetingsListLinearLayoutManager);

                meetingsTimeSetterListAdapter = new MeetingsTimeSetterListAdapter(timeRanges, allowedTimeRanges, new MeetingsTimeSetterListEventListener() {
                    @Override
                    public void OnSetStartTimeClicked(int pos, Time allowedStartTime, Time allowedEndTime) {
                        currentMeetingPos = pos;

                        timeRanges.get(currentMeetingPos).setAllowedStartTime(allowedStartTime);
                        timeRanges.get(currentMeetingPos).setAllowedEndTime(allowedEndTime);

                        Calendar mCalendar = Calendar.getInstance();
                        new TimePickerDialog(CalendarActivity.this, startTimePicker, mCalendar.get(Calendar.HOUR_OF_DAY), mCalendar.get(Calendar.MINUTE), false).show();
                    }

                    @Override
                    public void OnSetEndTimeClicked(int pos) {
                        currentMeetingPos = pos;
                        Calendar mCalendar = Calendar.getInstance();
                        new TimePickerDialog(CalendarActivity.this, endTimePicker, mCalendar.get(Calendar.HOUR_OF_DAY), mCalendar.get(Calendar.MINUTE), false).show();
                    }
                });
                binding.meetingsList.setAdapter(meetingsTimeSetterListAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        timeRanges = new ArrayList<>();

        binding.dateSelector.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                for(HashMap<String, Long> timeRange : allowedTimeRanges){
                    long startTime = timeRange.get("start");
                    long endTime = timeRange.get("end");
                    Calendar startTimeCal = Calendar.getInstance();
                    Calendar endTimeCal = Calendar.getInstance();
                    startTimeCal.setTimeInMillis(startTime);
                    endTimeCal.setTimeInMillis(endTime);

                    Date startTimeDate = startTimeCal.getTime();
                    Date endTimeDate = endTimeCal.getTime();

                    CalendarDay rangeStartDate = CalendarDay.from(startTimeDate.getYear() + 1900, startTimeDate.getMonth() + 1, startTimeDate.getDate());
                    CalendarDay rangeEndDate = CalendarDay.from(endTimeDate.getYear() + 1900, endTimeDate.getMonth() + 1, endTimeDate.getDate());

                    boolean isRange = rangeStartDate.isBefore(rangeEndDate) && rangeEndDate.isAfter(rangeStartDate);
                    boolean isValid = isRange ? date.isInRange(rangeStartDate, rangeEndDate) : !date.isBefore(rangeStartDate) && !date.isAfter(rangeStartDate);

                    if (!(isValid && (binding.dateSelector.getSelectedDates().size() <= 7))) {
                        binding.dateSelector.setDateSelected(date, false);
                    } else {
                        if (selected){
                            selectedAllowedTimeRanges.add(timeRange);
                        } else {
                            selectedAllowedTimeRanges.remove(timeRange);
                        }
                    }

                }
            }
        });

        binding.nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<CalendarDay> selectedDatesCurr = binding.dateSelector.getSelectedDates();
                if(selectedDatesCurr.size() > 0){
                    ArrayList<TimeRange> tempTimeRanges = new ArrayList<>();
                    ArrayList<CalendarDay> tempSelectedDays = new ArrayList<>();
                    for(int i = 0; i < timeRanges.size(); i++){
                        TimeRange timeRange = timeRanges.get(i);
                        if(selectedDatesCurr.contains(timeRange.getDate())){
                            tempTimeRanges.add(timeRange);

                            tempSelectedDays.add(timeRange.getDate());
                        }
                    }
                    timeRanges.clear();
                    timeRanges.addAll(tempTimeRanges);

                    for(CalendarDay date : selectedDatesCurr){
                        if(!tempSelectedDays.contains(date)){
                            timeRanges.add(new TimeRange(date, null, null));
                        }
                    }

                    binding.dateSelectionLayout.setVisibility(View.GONE);
                    binding.dateSelectionControls.setVisibility(View.GONE);
                    binding.meetingsTimeSetterLayout.setVisibility(View.VISIBLE);
                    binding.meetingsTimeSetterControls.setVisibility(View.VISIBLE);

                    ascSortDates();
                    meetingsTimeSetterListAdapter.setAllowedTimeRanges(selectedAllowedTimeRanges);
                    meetingsTimeSetterListAdapter.refreshDataset(timeRanges);
                } else {
                    Toast.makeText(CalendarActivity.this, "Select at least 1 date...", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.meetingsTimeSetterControls.setVisibility(View.GONE);
                binding.meetingsTimeSetterLayout.setVisibility(View.GONE);
                binding.dateSelectionLayout.setVisibility(View.VISIBLE);
                binding.dateSelectionControls.setVisibility(View.VISIBLE);
            }
        });

        binding.doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadDialog.showDialog();

                ArrayList<HashMap<String, Long>> timeSlots = new ArrayList<>();
                for(TimeRange timeRange : timeRanges){
                    timeSlots.add(timeRange.getDBMap());
                }

                firebaseDatabase.child("Meetings").child(meetingID).child("timeSlots").setValue(timeSlots).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            firebaseDatabase.child("Meetings").child(meetingID).child("ping").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    long ping = (long) dataSnapshot.getValue();
                                    ping++;
                                    firebaseDatabase.child("Meetings").child(meetingID).child("ping").setValue(ping).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            uploadDialog.hideDialog();
                                            finish();
                                        }
                                    });
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }
                });
            }
        });
    }

    private final TimePickerDialog.OnTimeSetListener startTimePicker = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            TimeRange currTimeRange = timeRanges.get(currentMeetingPos);

            CalendarDay date = currTimeRange.getDate();

            Calendar calendar = Calendar.getInstance();
            calendar.set(date.getYear(), date.getMonth(), date.getDay(), hourOfDay, minute);

            Time time = new Time(calendar.getTimeInMillis());
            Time expectedEndTime = new Time(time.getTime() + durationInMs);

            if(!time.before(currTimeRange.getAllowedStartTime()) && !expectedEndTime.after(currTimeRange.getAllowedEndTime())){
                currTimeRange.setStartTime(time);
                timeRanges.set(currentMeetingPos, currTimeRange);
                meetingsTimeSetterListAdapter.refreshDataset(timeRanges);
            } else {
                Toast.makeText(CalendarActivity.this, "Incorrect Start Time...", Toast.LENGTH_SHORT).show();
            }

            currentMeetingPos = -1;
        }
    };

    private final TimePickerDialog.OnTimeSetListener endTimePicker = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            TimeRange currTimeRange = timeRanges.get(currentMeetingPos);

            CalendarDay date = currTimeRange.getDate();

            Calendar calendar = Calendar.getInstance();
            calendar.set(date.getYear(), date.getMonth(), date.getDay(), hourOfDay, minute);

            Time startTime = currTimeRange.getStartTime();
            Time time = new Time(calendar.getTimeInMillis());

            if(currTimeRange.getStartTime() == null){
                Toast.makeText(CalendarActivity.this, "Set StartTime first...", Toast.LENGTH_SHORT).show();
            } else if(currTimeRange.getStartTime().after(time)){
                Toast.makeText(CalendarActivity.this, "Invalid time...", Toast.LENGTH_SHORT).show();
            } else {
                if(time.after(startTime) && ((time.getTime() - startTime.getTime()) >= durationInMs) && !time.after(currTimeRange.getAllowedEndTime())){
                    currTimeRange.setEndTime(time);
                    timeRanges.set(currentMeetingPos, currTimeRange);
                    meetingsTimeSetterListAdapter.refreshDataset(timeRanges);
                } else {
                    Toast.makeText(CalendarActivity.this, "Invalid End Time...", Toast.LENGTH_SHORT).show();
                }
            }

            currentMeetingPos = -1;
        }
    };

    private void ascSortDates(){
        for(int i = 0; i < timeRanges.size(); i++){
            for(int j = i + 1; j < timeRanges.size(); j++){
                TimeRange timeRange_i = timeRanges.get(i);
                TimeRange timeRange_j = timeRanges.get(j);
                HashMap<String, Long> selectedTimeRange_i = selectedAllowedTimeRanges.get(i);
                HashMap<String, Long> selectedTimeRange_j = selectedAllowedTimeRanges.get(j);
                if(timeRange_i.getDate().isAfter(timeRange_j.getDate())){
                    timeRanges.set(j, timeRange_i);
                    timeRanges.set(i, timeRange_j);

                    selectedAllowedTimeRanges.set(j, selectedTimeRange_i);
                    selectedAllowedTimeRanges.set(i, selectedTimeRange_j);
                }
            }
        }
    }

    private long getDurationFromStr(String durationStr){
        double timeInSec = 0;

        ArrayList<String> hourKeywords = new ArrayList<>(Arrays.asList("h", "hr", "hrs", "hour", "hours"));
        ArrayList<String> minutesKeywords = new ArrayList<>(Arrays.asList("m", "min", "mins", "minutes"));
        ArrayList<String> secondsKeywords = new ArrayList<>(Arrays.asList("s", "sec", "secs", "seconds"));

        char[] durationCharArr = durationStr.toCharArray();

        ArrayList<Character> digits = new ArrayList<>();
        String word = "";
        for(int i = 0; i < durationCharArr.length; i++){
            if((durationCharArr[i] >= '0' && durationCharArr[i] <= '9') || durationCharArr[i] == '.'){
                if(!word.equals("")){
                    word = word.toLowerCase();
                    String valStr = "";
                    for(int j = 0; j < digits.size(); j++){
                        valStr += digits.get(j);
                    }

                    double val = Double.parseDouble(valStr);

                    if(hourKeywords.contains(word)){
                        timeInSec += val * 3600;
                    } else if(minutesKeywords.contains(word)){
                        timeInSec += val * 60;
                    } else {
                        timeInSec += val;
                    }

                    digits.clear();
                    word = "";
                }

                digits.add(durationCharArr[i]);
            } else {
                word += durationCharArr[i];
            }
        }

        if(!word.equals("")){
            word = word.toLowerCase();
            String valStr = "";
            for(int j = 0; j < digits.size(); j++){
                valStr += digits.get(j);
            }

            double val = Double.parseDouble(valStr);

            if(hourKeywords.contains(word)){
                timeInSec += val * 3600;
            } else if(minutesKeywords.contains(word)){
                timeInSec += val * 60;
            } else {
                timeInSec += val;
            }

            digits.clear();
            word = "";
        }

        return (long) (timeInSec * 1000);
    }
}
