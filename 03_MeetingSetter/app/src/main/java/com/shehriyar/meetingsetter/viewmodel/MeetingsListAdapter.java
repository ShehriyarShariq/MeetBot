package com.shehriyar.meetingsetter.viewmodel;

import android.app.Activity;
import android.os.Build;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.shehriyar.meetingsetter.R;
import com.shehriyar.meetingsetter.listener.MeetingsListEventListener;
import com.shehriyar.meetingsetter.model.Meeting;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class MeetingsListAdapter extends RecyclerView.Adapter<MeetingsListAdapter.MeetingsListViewHolder> {

    Activity activity;
    ArrayList<String> meetingsID;
    MeetingsListEventListener meetingsListEventListener;

    DatabaseReference firebaseDatabase;

//    ArrayList<ArrayList<HashMap<String, String>>> allMeetingsAttendees;

    public MeetingsListAdapter(Activity activity, ArrayList<String> meetingsID, MeetingsListEventListener meetingsListEventListener) {
        this.activity = activity;
        this.meetingsID = meetingsID;
        this.meetingsListEventListener = meetingsListEventListener;

        firebaseDatabase = FirebaseDatabase.getInstance().getReference();

//        allMeetingsAttendees = new ArrayList<>();
    }

    @NonNull
    @Override
    public MeetingsListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.meetings_list_single_item_layout, parent, false);
        return new MeetingsListAdapter.MeetingsListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MeetingsListViewHolder holder, int position) {
        String meetingID = meetingsID.get(position);

        firebaseDatabase.child("Meetings").child(meetingID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String initiatorID = dataSnapshot.child("initiator").getValue().toString();

                firebaseDatabase.child("Users").child(initiatorID).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            holder.initiator.setText(Html.fromHtml("<b>" + dataSnapshot.getValue().toString() + "</b> has scheduled a meeting", Html.FROM_HTML_MODE_LEGACY));
                        } else {
                            holder.initiator.setText(Html.fromHtml("<b>" + dataSnapshot.getValue().toString() + "</b> has scheduled a meeting"));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                String location = dataSnapshot.child("location").getValue().toString();
                location = location.length() > 0 ? location : "N/A";

                holder.location.setText(location);

                final ArrayList<String> attendeesIDs = (ArrayList<String>) dataSnapshot.child("invitees").getValue();

                long ping = (long) dataSnapshot.child("ping").getValue();
                if(ping == attendeesIDs.size()){ // Scheduled
                    ArrayList<HashMap<String, Long>> timeSlots = (ArrayList<HashMap<String, Long>>) dataSnapshot.child("timeSlots").getValue();
                    HashMap<String, Long> timeRange = timeSlots.get(0);

                    long timeInMs = timeRange.get("start");
                    Date date = new Date(timeInMs);
                    Time time = new Time(timeInMs);

                    holder.date.setText(String.format("%td %tb %tY", date, date, date));
                    holder.time.setText(String.format("%tR", time));

                } else { // Requested
                    holder.date.setText("N/A");
                    holder.time.setText("N/A");
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            meetingsListEventListener.OnMeetingClicked(holder.getLayoutPosition());
                        }
                    });
                }

                final ArrayList<HashMap<String, String>> attendees = new ArrayList<>();

                for(int i = 0; i < attendeesIDs.size(); i++){
                    String id = attendeesIDs.get(i);
                    firebaseDatabase.child("Users").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            HashMap<String, String> userMap = new HashMap<>();
                            userMap.put("name", (String) dataSnapshot.child("name").getValue());
                            userMap.put("email", (String) dataSnapshot.child("email").getValue());
                            attendees.add(userMap);

                            if(attendees.size() == attendeesIDs.size()){
                                holder.attendeesList.removeAllViews();
                                int i = 1;
                                for(HashMap<String, String> attendee : attendees){
                                    TextView itemTextView = new TextView(activity);
                                    itemTextView.setText(i  + ") " + attendee.get("name") + "   (" + attendee.get("email") + ")");
                                    holder.attendeesList.addView(itemTextView);
                                    i++;
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }



            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return meetingsID.size();
    }

    public class MeetingsListViewHolder extends RecyclerView.ViewHolder {

        TextView initiator, date, time, location;
        LinearLayout attendeesList;

        public MeetingsListViewHolder(@NonNull View itemView) {
            super(itemView);

            initiator = itemView.findViewById(R.id.initiator);
            date = itemView.findViewById(R.id.date);
            time = itemView.findViewById(R.id.time);
            location = itemView.findViewById(R.id.location);
            attendeesList = itemView.findViewById(R.id.attendeesList);
        }
    }

    @Override
    public void setHasStableIds(boolean hasStableIds) {
        super.setHasStableIds(hasStableIds);
    }

    public void refreshDataset(ArrayList<String> meetingsID){
        this.meetingsID = meetingsID;
        notifyDataSetChanged();
    }
}
