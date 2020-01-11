package com.shehriyar.meetingsetter.model;

import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.sql.Time;
import java.util.Calendar;
import java.util.HashMap;

public class TimeRange {

    CalendarDay date;
    Time startTime, endTime;
    Time allowedStartTime, allowedEndTime;

    public TimeRange(CalendarDay date, Time startTime, Time endTime) {
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public CalendarDay getDate() {
        return date;
    }

    public Time getStartTime() {
        return startTime;
    }

    public Time getEndTime() {
        return endTime;
    }

    public void setStartTime(Time startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(Time endTime) {
        this.endTime = endTime;
    }

    public String getFormattedDate(){
        return date.getDay() + "/" + date.getMonth() + "/" + date.getYear();
    }

    public long getStartTimeInMs(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(date.getYear(), date.getMonth(), date.getDay(), startTime.getHours(), startTime.getMinutes());
        return calendar.getTimeInMillis();
    }

    public Time getAllowedStartTime() {
        return allowedStartTime;
    }

    public Time getAllowedEndTime() {
        return allowedEndTime;
    }

    public void setAllowedStartTime(Time allowedStartTime) {
        this.allowedStartTime = allowedStartTime;
    }

    public void setAllowedEndTime(Time allowedEndTime) {
        this.allowedEndTime = allowedEndTime;
    }

    public HashMap<String, Long> getDBMap(){
        HashMap<String, Long> dbMap = new HashMap<>();

        Calendar startTimeCal = Calendar.getInstance();
        Calendar endTimeCal = Calendar.getInstance();
        startTimeCal.set(date.getYear(), date.getMonth(), date.getDay(), startTime.getHours(), startTime.getMinutes());
        endTimeCal.set(date.getYear(), date.getMonth(), date.getDay(), endTime.getHours(), endTime.getMinutes());


        dbMap.put("start", startTimeCal.getTimeInMillis());
        dbMap.put("end", endTimeCal.getTimeInMillis());

        return dbMap;
    }
}
