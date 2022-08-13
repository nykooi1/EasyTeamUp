package com.example.easyteamup;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class EventsAdapter extends ArrayAdapter<Event> {

    private ArrayList<Event> events;
    private Context context;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public EventsAdapter(Context context, ArrayList<Event> events) {
        super(context, 0, events);
        this.context = context;
        this.events = events;
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Event event = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        convertView = LayoutInflater.from(getContext()).inflate(R.layout.public_events_list, parent, false);
        // Lookup view for data population
        TextView eventID = (TextView) convertView.findViewById(R.id.eventID);
        TextView eventName = (TextView) convertView.findViewById(R.id.eventName);
        TextView eventStatus = (TextView) convertView.findViewById(R.id.eventStatus);
        TextView eventDescription = (TextView) convertView.findViewById(R.id.eventDescription);
        TextView eventHost = (TextView) convertView.findViewById(R.id.eventHost);
        // Populate the data into the template view using the data object
        eventID.setText(event.getEventID());
        eventName.setText(event.getEventName());
        eventDescription.setText(event.getEventDescription());
        String hostID = event.getEventHostID();
        DocumentReference docRef = db.collection("Users").document(hostID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String fName = document.getString("fName");
                        String lName = document.getString("lName");
                        eventHost.setText("Host: " + fName + " " + lName);
                    }
                }
            }
        });

        String userID = auth.getCurrentUser().getUid();

        //show if the user is attending
        db.collection("AttendeesToEvents")
                .whereEqualTo("attendeeID", userID)
                .whereEqualTo("eventID", event.getEventID())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if(!task.getResult().isEmpty()) {
                                eventStatus.setText("Attending");
                            }
                        }
                    }
                });

        //might need to nest this, or nvm they can never been invited and attending at the same time i think???

        String userEmail = auth.getCurrentUser().getEmail();

        //show if there is a pending invite for this event
        db.collection("UsersToInvitations")
                .whereEqualTo("eventID", event.getEventId())
                .whereEqualTo("userEmail", userEmail)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if(!task.getResult().isEmpty()) {
                                eventStatus.setText("Invited");
                            }
                        }
                    }
                });

        if((auth.getCurrentUser().getUid()).equals(event.getEventHostID())){
            eventStatus.setText("Hosting");
        }

        // Return the completed view to render on screen
        return convertView;
    }

    @Override
    public Event getItem(int position){
        return events.get(position);
    }
}
