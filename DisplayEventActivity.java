package com.example.easyteamup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DisplayEventActivity extends AppCompatActivity {

    private Event event;
    private ArrayList<String> userSelectedTimeSlotIDs;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_event);

        userSelectedTimeSlotIDs = new ArrayList<>();
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        event = (Event) bundle.get("event");
        populateInfo(event);

        //after populating the information, handle the buttons

        //IF this event is within the UsersToInvitations Table AND it has not been decline yet...
        //show the Accept/Decline Buttons

        Button acceptButton = (Button) findViewById(R.id.acceptButton);
        Button declineButton = (Button) findViewById(R.id.declineButton);

        db.collection("UsersToInvitations")
                .whereEqualTo("eventID", event.getEventID())
                .whereEqualTo("userEmail", auth.getCurrentUser().getEmail())
                .whereEqualTo("declined", false)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        //if it found such an event
                        if (task.isSuccessful()) {
                            if(task.getResult().size() > 0){
                                System.out.println("INVITED");
                                //if there is a result that means that this event has not been accepted / declined yet
                                acceptButton.setVisibility(View.VISIBLE);
                                declineButton.setVisibility(View.VISIBLE);
                            }
                            //otherwise check for attending / not attending
                            else {
                                System.out.println("NOT INVITED");
                                //show the edit/join button
                                String userID = auth.getCurrentUser().getUid();
                                Button joinButton = (Button) findViewById(R.id.joinButton);
                                Button withdrawButton = (Button) findViewById(R.id.withdrawButton);
                                db.collection("AttendeesToEvents")
                                        .whereEqualTo("attendeeID", userID)
                                        .whereEqualTo("eventID", event.getEventID())
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    joinButton.setVisibility(View.VISIBLE);
                                                    if(!task.getResult().isEmpty() || event.getEventHostID().equals(userID)) {

                                                        //set the button to edit
                                                        joinButton.setText("EDIT");

                                                        //also show the withdraw button

                                                        if(!(auth.getCurrentUser().getUid()).equals(event.getEventHostID())){
                                                            withdrawButton.setVisibility(View.VISIBLE);
                                                        }

                                                    }
                                                    else {
                                                        joinButton.setText("JOIN");
                                                    }
                                                }
                                            }
                                        });
                            }
                        }
                    }
                });
    }

    protected void populateInfo(Event e) {
        TextView eventNameTV = (TextView) findViewById(R.id.displayEventName);
        TextView eventHostTV = (TextView) findViewById(R.id.displayEventHost);
        TextView eventDescriptionTV = (TextView) findViewById(R.id.displayEventDescription);
        TextView eventLocationTV = (TextView) findViewById(R.id.displayEventLocation);
        eventNameTV.setText(e.getEventName());
        String hostID = e.getEventHostID();
        DocumentReference docRef = db.collection("Users").document(hostID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String fName = document.getString("fName");
                        String lName = document.getString("lName");
                        String username = document.getString("username");
                        String hostText = "Host: " + fName + " " + lName + " (@" + username + ")";
                        eventHostTV.setText(hostText);
                    }
                }
            }
        });

        String descriptionText = "Description: " + e.getEventDescription();
        eventDescriptionTV.setText(descriptionText);
        String locationText = "Location: " + e.getEventLocation();
        eventLocationTV.setText(locationText);
    }

    public void backButtonOnClick(View view) {
        Intent intent = new Intent(DisplayEventActivity.this, MainActivity2.class);
        startActivity(intent);
    }

    public void acceptButtonOnClick(View view){
        //add to the AttendeesToEvents table for this user and event
        Map<String,Object> attendeeToEvent = new HashMap<>();
        attendeeToEvent.put("attendeeID", auth.getCurrentUser().getUid());
        attendeeToEvent.put("eventID", event.getEventID());
        db.collection("AttendeesToEvents").add(attendeeToEvent);
        //remove from the UsersToInvitations table for this user->event
        db.collection("UsersToInvitations")
                .whereEqualTo("eventID", event.getEventID())
                .whereEqualTo("userEmail", auth.getCurrentUser().getEmail())
                .whereEqualTo("declined", false)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for(QueryDocumentSnapshot document: task.getResult()) {
                                db.collection("UsersToInvitations").document(document.getId()).delete();
                            }
                            //go back to the main page
                            Intent intent = new Intent(DisplayEventActivity.this, MainActivity2.class);
                            startActivity(intent);
                        }
                    }
                });
    }

    public void declineButtonOnClick(View view){
        //set the declined flag in the UsersToInvitations table for this user->event
        db.collection("UsersToInvitations")
                .whereEqualTo("eventID", event.getEventID())
                .whereEqualTo("userEmail", auth.getCurrentUser().getEmail())
                .whereEqualTo("declined", false)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for(QueryDocumentSnapshot document: task.getResult()) {
                                DocumentReference docRef = db.collection("UsersToInvitations").document(document.getId());
                                docRef.update("declined", true);
                            }
                            //go back to the main page
                            Intent intent = new Intent(DisplayEventActivity.this, MainActivity2.class);
                            startActivity(intent);
                        }
                    }
                });
    }

    public void withdrawButtonOnClick(View view){
        //query the document where
        db.collection("AttendeesToEvents")
                .whereEqualTo("eventID", event.getEventID())
                .whereEqualTo("attendeeID", auth.getCurrentUser().getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for(QueryDocumentSnapshot document: task.getResult()) {
                                db.collection("AttendeesToEvents").document(document.getId()).delete();
                            }
                            //go back to the main page
                            Intent intent = new Intent(DisplayEventActivity.this, MainActivity2.class);
                            startActivity(intent);
                        }
                    }
                });
    }

    public void joinButtonOnClick(View view) {
        String userID = auth.getCurrentUser().getUid();
        db.collection("AttendeesToEvents")
                .whereEqualTo("attendeeID", userID)
                .whereEqualTo("eventID", event.getEventID())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for(QueryDocumentSnapshot document: task.getResult()) {
                                userSelectedTimeSlotIDs.add((String) document.get("timeSlotID"));
                            }
                            if(event.getEventHostID().equals(userID)) {
                                Intent intent = new Intent(DisplayEventActivity.this, EditEventActivity.class);
                                intent.putExtra("event", event);
                                startActivity(intent);
                            }
                            else {
                                Intent intent = new Intent(DisplayEventActivity.this, JoinEventActivity.class);
                                intent.putExtra("event", event);
                                intent.putExtra("timeSlotIDs", userSelectedTimeSlotIDs);
                                startActivity(intent);
                            }
                        }
                    }
                });

    }

}