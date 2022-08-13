package com.example.easyteamup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.sql.Array;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ConfirmCreateActivity extends AppCompatActivity {

    private String eventID, name, description, location, hostID;
    private EventTypes type;
    private Timestamp dueDate;
    private Boolean isPublic, isCreate;
    private ArrayList<TimeSlot> timeslots;
    private ArrayList<String> timeslotIDs = new ArrayList<>();
    private ArrayList<String> inviteeEmails;

    private FirebaseFirestore db;
    private FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_create);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        eventID = bundle.getString("eventID");
        name = bundle.getString("eventName");
        description = bundle.getString("eventDescription");
        location = bundle.getString("eventLocation");
        type = EventTypes.valueOf(bundle.getString("eventType"));
        dueDate = (Timestamp) bundle.getSerializable("eventDueDate");
        isPublic = bundle.getBoolean("eventIsPublic");
        timeslots = bundle.getParcelableArrayList("eventTimeslots");
        inviteeEmails = bundle.getStringArrayList("invitees");
        isCreate = bundle.getBoolean("isCreate");
        hostID = auth.getCurrentUser().getUid();
        if(isCreate) {
            timeslotIDs = bundle.getStringArrayList("timeslotIDs");
        }

        populateInfo();
        insertToDB();
    }

    private void populateInfo() {

        // Header
        TextView headerTV = findViewById(R.id.confirmCreateHeader);
        if(!isCreate) {
            headerTV.setText("Event Changed!");
        }

        // TextViews
        TextView nameTV = findViewById(R.id.confirmCreateName);
        TextView descriptionTV = findViewById(R.id.confirmCreateDescription);
        TextView locationTV = findViewById(R.id.confirmCreateLocation);
        TextView typeTV = findViewById(R.id.confirmCreateType);
        TextView dueDateTV = findViewById(R.id.confirmCreateDueDate);
        TextView visibilityTV = findViewById(R.id.confirmCreateVisibility);

        nameTV.setText(name);
        descriptionTV.setText(description);
        locationTV.setText(location);
        char typeStringFirstLetter = type.name().charAt(0);
        String typeStringOther = type.name().substring(1).toLowerCase(Locale.ROOT);
        String typeString = typeStringFirstLetter + typeStringOther;
        typeTV.setText(typeString);
        dueDateTV.setText(Util.formatTimestamp(dueDate));
        visibilityTV.setText(isPublic ? "Public" : "Private");

        // ListViews
        ArrayList<String> timeslotStrings = new ArrayList<>();
        for(TimeSlot ts: timeslots) {
            String s = Util.formatTimestamp(ts.getStart()) + " — " + Util.formatTimestamp(ts.getEnd());
            timeslotStrings.add(s);
        }
        ArrayAdapter<String> timeslotsAdapter = new ArrayAdapter<>(this, R.layout.simple_string_list, timeslotStrings);
        ListView timeslotsLV = findViewById(R.id.confirmCreateTimeslotsList);
        timeslotsLV.setAdapter(timeslotsAdapter);
        Util.setListViewHeightBasedOnItems(timeslotsLV);

        ListView inviteesLV = findViewById(R.id.confirmCreateInviteesList);
        ArrayAdapter<String> inviteesAdapter = new ArrayAdapter<>(this, R.layout.simple_string_list, inviteeEmails);
        inviteesLV.setAdapter(inviteesAdapter);
        Util.setListViewHeightBasedOnItems(inviteesLV);
    }

    private void insertToDB() {
        Map<String, Object> eventMap = new HashMap<>();
        eventMap.put("name", name);
        eventMap.put("description", description);
        eventMap.put("location", location);
        eventMap.put("hostID", hostID);
        eventMap.put("isPublic", isPublic);
        eventMap.put("type", type.name());
        eventMap.put("dueDate", dueDate);
        eventMap.put("selectedStartTime", null);
        eventMap.put("selectedEndTime", null);


        // Insert events
        if(isCreate) {
            db.collection("Events")
                    .add(eventMap)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            String eventID = documentReference.getId();

                            // Insert timeslots
                            for(TimeSlot ts: timeslots) {
                                Map<String, Object> timeslotsMap = new HashMap<>();
                                timeslotsMap.put("eventID", eventID);
                                timeslotsMap.put("startTime", ts.getStart());
                                timeslotsMap.put("endTime", ts.getEnd());
                                db.collection("TimeSlots").add(timeslotsMap);
                            }

                            System.out.println("=== INVITED AMOUNT: " + inviteeEmails.size() + " ===");

                            // Insert invitations
                            for(String email: inviteeEmails) {
                                System.out.println("=== INVITED: " + email + " ===");
                                Map<String, Object> invitationMap = new HashMap<>();
                                invitationMap.put("userEmail", email);
                                invitationMap.put("eventID", eventID);
                                invitationMap.put("seen", false);
                                invitationMap.put("declined", false);
                                db.collection("UsersToInvitations").add(invitationMap);
                            }
                        }
                    });
        }
        else {
            for(String tsid: timeslotIDs) {
                db.collection("TimeSlots").document(tsid).delete();
            }
            Map<String, String> attendingUserEmails = new HashMap<>();
            db.collection("UsersToInvitations")
                    .whereEqualTo("eventID", eventID)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.isSuccessful()) {
                                for(QueryDocumentSnapshot document: task.getResult()) {
                                    String docID = document.getId();
                                    String email = document.getString("userEmail");
                                    attendingUserEmails.put(docID, email);
                                }
                            }
                        }
                    });
            for(String selectedEmail: inviteeEmails) {
                if(!attendingUserEmails.containsValue(selectedEmail)) {
                    Map<String, Object> inviteMap = new HashMap<>();
                    inviteMap.put("eventID", eventID);
                    inviteMap.put("userEmail", selectedEmail);
                    inviteMap.put("seen", false);
                    inviteMap.put("declined", false);
                    db.collection("UsersToInvitations").add(inviteMap);
                }
            }
            for(Map.Entry<String, String> entry: attendingUserEmails.entrySet()) {
                if(!inviteeEmails.contains(entry.getValue())) {
                    db.collection("UsersToInvitations").document(entry.getKey()).delete();
                }
            }
            db.collection("Events").document(eventID)
                    .set(eventMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            for(TimeSlot ts: timeslots) {
                                Map<String, Object> timeslotsMap = new HashMap<>();
                                timeslotsMap.put("eventID", eventID);
                                timeslotsMap.put("startTime", ts.getStart());
                                timeslotsMap.put("endTime", ts.getEnd());
                                db.collection("TimeSlots").add(timeslotsMap);
                            }
                        }
                    });
        }

        if(!isCreate) {
            Map<String, Object> eventsChangedMap = new HashMap<>();
            ArrayList<String> users = new ArrayList<>();
            eventsChangedMap.put("eventID", eventID);
            eventsChangedMap.put("users", users);
            db.collection("EventsChanged").add(eventsChangedMap);
        }

    }

    public void confirmCreateBackOnClick(View view) {
        Intent intent = new Intent(ConfirmCreateActivity.this, MainActivity2.class);
        startActivity(intent);
    }
}