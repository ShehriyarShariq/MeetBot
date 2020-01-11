package com.shehriyar.meetingsetter.model;

import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;

public class Meeting {

    private String meetingID, initiatorID, initiatorName, initiatorEmail, duration, location;
    private CalendarDay date;
    private Time startTime;
    private ArrayList<HashMap<String, String>> attendees;

    public Meeting(String meetingID, String initiatorID, String initiatorName, String initiatorEmail, String duration, String location, CalendarDay date, Time startTime, ArrayList<HashMap<String, String>> attendees) {
        this.meetingID = meetingID;
        this.initiatorID = initiatorID;
        this.initiatorName = initiatorName;
        this.initiatorEmail = initiatorEmail;
        this.duration = duration;
        this.location = location;
        this.date = date;
        this.startTime = startTime;
        this.attendees = attendees;
    }

    public String getMeetingID() {
        return meetingID;
    }

    public String getInitiatorID() {
        return initiatorID;
    }

    public String getInitiatorName() {
        return initiatorName;
    }

    public String getInitiatorEmail() {
        return initiatorEmail;
    }

    public String getDuration() {
        return duration;
    }

    public String getLocation() {
        return location;
    }

    public CalendarDay getDate() {
        return date;
    }

    public Time getStartTime() {
        return startTime;
    }

    public ArrayList<HashMap<String, String>> getAttendees() {
        return attendees;
    }
}
