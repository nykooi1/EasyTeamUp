package com.example.easyteamup;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Map;

public class TimeSlotsAdapter extends ArrayAdapter<String> {

    private ArrayList<String> timeslots;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public TimeSlotsAdapter(Context context, ArrayList<String> timeslots) {
        super(context, 0, timeslots);
        this.timeslots = timeslots;
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        String timeslotID = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.timeslot_checkbox_list, parent, false);
        }
        // Lookup view for data population
        CheckBox tsc = (CheckBox) convertView.findViewById(R.id.timeSlotCheckBox);
        TextView tsid = (TextView) convertView.findViewById(R.id.timeSlotID);
        // Populate the data into the template view using the data object
        DocumentReference docRef = db.collection("TimeSlots").document(timeslotID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String text, start, end;
                        Timestamp startTime = new Timestamp(document.getTimestamp("startTime").getSeconds() * 1000);
                        Timestamp endTime = new Timestamp(document.getTimestamp("endTime").getSeconds() * 1000);
                        start = Util.formatTimestamp(startTime);
                        end = Util.formatTimestamp(endTime);
                        text = start + "  —  " + end;
                        tsc.setText(text);

                        tsid.setText(timeslotID);

                        String userID = auth.getCurrentUser().getUid();
                        db.collection("AttendeesToEvents")
                                .whereEqualTo("attendeeID", userID)
                                .whereEqualTo("timeSlotID", timeslotID)
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            if(!task.getResult().isEmpty()) {
                                                tsc.setChecked(true);
                                            }
                                            else {
                                                tsc.setChecked(false);
                                            }
                                            tsc.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                                @Override
                                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                                    String id = tsid.getText().toString();
                                                    JoinEventActivity.toggleSelectedTimeSlots(id, isChecked);
                                                }
                                            });
                                        }
                                    }
                                });
                    }
                }
            }
        });

        // Return the completed view to render on screen
        return convertView;
    }

    @Override
    public String getItem(int position){
        return timeslots.get(position);
    }
}
