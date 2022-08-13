package com.example.easyteamup;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.sql.Array;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class FragmentList extends Fragment {
    private ArrayList<Event> allEvents = new ArrayList<>();
    private String[] filters = new String[]{"All", "Attending", "Not attending", "Academic", "Sports", "Entertainment"};
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private boolean initialDisplay = true; //need to tweak this for switching between activities, maybe set another flag

    //returns the view passed in - this idea can be utilized in any async
    private View returnView(View view){
        return view;
    }

    //converted this to void since i must delegate returning the view to an outside function because of async
    private void populatePublicList (View view, ArrayList <Event> events) {

        //ASYNC query UsersToInvitations
        //LOOP through all documents: if the document is declined, remove it from events
        //now I can use events normally move all code here

        db.collection("UsersToInvitations")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                //if the event is declined, remove it from events
                                if (document.getBoolean("declined")) {
                                    //event id to be removed
                                    String removeID = document.getString("eventID");
                                    String userEmail = document.getString("userEmail");
                                    //find the event within events
                                    for (Event event : events) {
                                        //if the id matches for the current user
                                        if ((event.getEventID()).equals(removeID) && userEmail.equals(auth.getCurrentUser().getEmail())) {
                                            events.remove(event);
                                            break; //stop searching
                                        }
                                    }
                                }
                            }

                            //now that the declined events have been removed, show the remaining ones

                            //here I am setting the on click events, but I need to consider what I'm opening

                            //If I am simply viewing an event, the below code is good

                            //if I am viewing an event I have been invited to, the onclick should lead me to accept / decline

                            //the big question is, do i have 2 separate activities?

                            EventsAdapter ea = new EventsAdapter(getContext(), events);

                            ListView listView = (ListView) view.findViewById(R.id.event_list);
                            listView.setAdapter(ea);

                            //NEW UPDATE

                            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
                                    Event event = (Event) adapter.getItemAtPosition(position);
                                    Intent intent = new Intent(getContext(), DisplayEventActivity.class);
                                    intent.putExtra("event", event);
                                    startActivity(intent);
                                }
                            });
                            returnView(view);
                        }
                    }
                });
    }

    @Override
    public void onCreate (@Nullable Bundle savedInstanceState){

        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

    }

    @Nullable
    @Override
    public View onCreateView (@NonNull LayoutInflater inflater, @Nullable ViewGroup
            container, @Nullable Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_list, container, false);
        db.collection("Events")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            // Read in the query result into a list of public events
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                String id = document.getId();
                                Map<String, Object> eventInfo = document.getData();
                                String name = (String) eventInfo.get("name");
                                String hostID = (String) eventInfo.get("hostID");
                                String location = (String) eventInfo.get("location");
                                Boolean isPublic = (Boolean) eventInfo.get("isPublic");
                                Timestamp dueDate = new Timestamp(document.getTimestamp("dueDate").getSeconds() * 1000);
                                Timestamp selectedStartTime = null, selectedEndTime = null;
                                if(document.getTimestamp("selectedStartTime") != null) {
                                    selectedStartTime = new Timestamp(document.getTimestamp("selectedStartTime").getSeconds() * 1000);
                                }
                                if(document.getTimestamp("selectedEndTime") != null) {
                                    selectedEndTime = new Timestamp(document.getTimestamp("selectedEndTime").getSeconds() * 1000);
                                }
                                String description = (String) eventInfo.get("description");
                                String type = (String) eventInfo.get("type");
                                ArrayList<TimeSlot> timeSlots = new ArrayList<>();
                                ArrayList<String> timeSlotIDs = new ArrayList<>();
                                db.collection("TimeSlots")
                                        .whereEqualTo("eventID", id)
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    for(QueryDocumentSnapshot document: task.getResult()) {
                                                        timeSlotIDs.add(document.getId());
                                                        Timestamp start = new Timestamp(document.getTimestamp("startTime").getSeconds() * 1000);
                                                        Timestamp end = new Timestamp(document.getTimestamp("endTime").getSeconds() * 1000);
                                                        TimeSlot ts = new TimeSlot(start, end);
                                                        timeSlots.add(ts);
                                                    }
                                                    Event event = new Event(id, name, timeSlots, timeSlotIDs,
                                                            null, description, hostID, new ArrayList<>(), location, dueDate,
                                                            isPublic, EventTypes.valueOf(type));
                                                    allEvents.add(event);
                                                    // Use the public events to populate the list view
                                                    Collections.sort(allEvents, new Comparator<Event>(){
                                                        public int compare(Event e1, Event e2){
                                                            return (e1.getEventDueDate()).compareTo(e2.getEventDueDate());
                                                        }
                                                    });
                                                    populatePublicList(view, allEvents);
                                                }
                                            }
                                        });
                            }
                        }
                    }
                });

        return view;
    }



}
