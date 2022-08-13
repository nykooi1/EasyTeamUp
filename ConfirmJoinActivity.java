package com.example.easyteamup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ConfirmJoinActivity extends AppCompatActivity {

    private Event event;
    private ArrayList<String> selectedTimeSlotIDs;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_join);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        event = (Event) bundle.get("event");
        selectedTimeSlotIDs = bundle.getStringArrayList("timeslots");
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        populateInfo();

        insertAttendee();
    }

    protected void populateInfo() {
        TextView eventNameTV = (TextView) findViewById(R.id.confirmJoinName);
        TextView timeSlotsTV = (TextView) findViewById(R.id.confirmJoinTimeSlots);

        String nameText = "For: " + event.getEventName();
        eventNameTV.setText(nameText);

        timeSlotsTV.setText("Selected time slot(s):\n");

        for(String tsid: selectedTimeSlotIDs) {
            DocumentReference docRef = db.collection("TimeSlots").document(tsid);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Timestamp start = new Timestamp(document.getTimestamp("startTime").getSeconds() * 1000);
                            Timestamp end = new Timestamp(document.getTimestamp("endTime").getSeconds() * 1000);
                            String timeSlotsText = Util.formatTimestamp(start) + "  —  " + Util.formatTimestamp(end) + "\n";
                            timeSlotsTV.setText(timeSlotsTV.getText().toString() + timeSlotsText);
                        }
                    }
                }
            });

        }
    }

    protected void insertAttendee() {
        String userID = auth.getCurrentUser().getUid();
        Map<String, String> existingTimeSlotIDs = new HashMap<String, String>();
        db.collection("AttendeesToEvents")
                .whereEqualTo("attendeeID", userID)
                .whereEqualTo("eventID", event.getEventID())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for(QueryDocumentSnapshot document: task.getResult()) {
                                existingTimeSlotIDs.put(document.getString("timeSlotID"), document.getId());
                            }
                            for(String timeSlotID: selectedTimeSlotIDs) {
                                if(!existingTimeSlotIDs.containsKey(timeSlotID)) {
                                    Map<String, Object> attendeeMap = new HashMap<>();
                                    attendeeMap.put("attendeeID", userID);
                                    attendeeMap.put("eventID", event.getEventID());
                                    attendeeMap.put("timeSlotID", timeSlotID);

                                    db.collection("AttendeesToEvents").add(attendeeMap);
                                }
                            }
                            for(Map.Entry<String, String> entry: existingTimeSlotIDs.entrySet()) {
                                if(!selectedTimeSlotIDs.contains(entry.getKey())) {
                                    db.collection("AttendeesToEvents").document(entry.getValue()).delete();
                                }
                            }
                        }
                    }
                });
    }

    public void backButtonOnClick(View view) {
        Intent intent = new Intent(ConfirmJoinActivity.this, MainActivity2.class);
        startActivity(intent);
    }

}