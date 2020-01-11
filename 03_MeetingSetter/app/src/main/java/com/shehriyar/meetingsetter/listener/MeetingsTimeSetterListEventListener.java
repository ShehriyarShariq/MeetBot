package com.shehriyar.meetingsetter.listener;

import java.sql.Time;

public interface MeetingsTimeSetterListEventListener {

    void OnSetStartTimeClicked(int pos, Time allowedStartTime, Time allowedEndTime);
    void OnSetEndTimeClicked(int pos);

}
