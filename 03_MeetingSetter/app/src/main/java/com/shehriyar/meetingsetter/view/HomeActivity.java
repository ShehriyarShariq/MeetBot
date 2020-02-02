package com.shehriyar.meetingsetter.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.shehriyar.meetingsetter.R;
import com.shehriyar.meetingsetter.databinding.ActivityHomeBinding;
import com.shehriyar.meetingsetter.listener.MeetingsListEventListener;
import com.shehriyar.meetingsetter.util.UtilFunctions;
import com.shehriyar.meetingsetter.viewmodel.MeetingsListAdapter;

import java.util.ArrayList;
import java.util.HashMap;

public class HomeActivity extends AppCompatActivity {

    ActivityHomeBinding binding;

    FirebaseAuth firebaseAuth;
    DatabaseReference firebaseDatabase;

    ArrayList<String> scheduledMeetingsID, requestedMeetingsID;
    MeetingsListAdapter scheduledMeetingsListAdapter, requestedMeetingListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance().getReference();

        binding.greetings.setText("Hi, " + firebaseAuth.getCurrentUser().getDisplayName() + ",");

        scheduledMeetingsID = new ArrayList<>();
        requestedMeetingsID = new ArrayList<>();

        LinearLayoutManager scheduledMeetingsListLinearLayoutManager = new LinearLayoutManager(this);
        binding.scheduledMeetingsList.setLayoutManager(scheduledMeetingsListLinearLayoutManager);

        LinearLayoutManager requestedMeetingsListLinearLayoutManager = new LinearLayoutManager(this);
        binding.requestedMeetingsList.setLayoutManager(requestedMeetingsListLinearLayoutManager);

        scheduledMeetingsListAdapter = new MeetingsListAdapter(this, scheduledMeetingsID, new MeetingsListEventListener() {
            @Override
            public void OnMeetingClicked(int pos) {

            }
        });
        scheduledMeetingsListAdapter.setHasStableIds(true);
        binding.scheduledMeetingsList.setAdapter(scheduledMeetingsListAdapter);

        requestedMeetingListAdapter = new MeetingsListAdapter(this, scheduledMeetingsID, new MeetingsListEventListener() {
            @Override
            public void OnMeetingClicked(int pos) {
                Intent meetingIntent = new Intent(HomeActivity.this, CalendarActivity.class);
                meetingIntent.putExtra("meetingID", requestedMeetingsID.get(pos));
                startActivity(meetingIntent);
            }
        });
        binding.requestedMeetingsList.setAdapter(requestedMeetingListAdapter);

        getMeetings();

        binding.refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getMeetings();
            }
        });

    }

    private void getMeetings(){
        scheduledMeetingsID.clear();
        requestedMeetingsID.clear();

        scheduledMeetingsListAdapter.refreshDataset(scheduledMeetingsID);
        requestedMeetingListAdapter.refreshDataset(requestedMeetingsID);

        firebaseDatabase.child("Users").child(UtilFunctions.md5(firebaseAuth.getCurrentUser().getEmail())).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HashMap<String, String> scheduledMeetings = (HashMap<String, String>) dataSnapshot.child("Scheduled").getValue();
                HashMap<String, String> requestedMeetings = (HashMap<String, String>) dataSnapshot.child("Requests").getValue();

                scheduledMeetings.remove("meetingID");
                requestedMeetings.remove("meetingID");

                scheduledMeetingsID.addAll(scheduledMeetings.keySet());
                requestedMeetingsID.addAll(requestedMeetings.keySet());

                scheduledMeetingsListAdapter.refreshDataset(scheduledMeetingsID);
                requestedMeetingListAdapter.refreshDataset(requestedMeetingsID);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
