package com.example.easyteamup;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class FirebaseListener {

    public Context context;

    public FirebaseListener(Context context){
        this.context = context;
    }

    //private static Boolean initial = true;
    private static Boolean settingsSet = false;

    //builds + show the notification
    private void buildNotification(String email, String eventName, String eventDescription){
        //build
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "invites")
                .setSmallIcon(R.drawable.ic_event_icon)
                .setContentTitle("New Invitation!")
                .setContentText(eventName + ": " + eventDescription)
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        //show the built notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        //I think I need to set a unique id each time or it will keep showing the same thing
        notificationManager.notify(1, builder.build());
    }

    //builds notification for event change
    private void buildEventChangeNotification(String eventID){

        //build
        //get the event document
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("Events").document(eventID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "invites")
                                .setSmallIcon(R.drawable.ic_event_icon)
                                .setContentTitle("Event Has Been Modified")
                                .setContentText("Event Name: " + document.getString("name"))
                                .setPriority(NotificationCompat.PRIORITY_HIGH);
                        //show the built notification
                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                        //I think I need to set a unique id each time or it will keep showing the same thing
                        notificationManager.notify(1, builder.build());
                    }
                }
            }
        });

    }

    //listens for invites for the current user
    public void listenForInvites(){

        /*if(initial){
            initial = false;
            return;
        }*/

        System.out.println("BEGIN LISTENING FOR INVITES");

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        //listen to the eventsChanged table
        //the eventsChanged documents will store the eventID mapped to an array of users that have already seen the document
        db.collection("EventsChanged").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {

                System.out.println("EVENT CHANGED");

                //loop through all the event IDs
                for(QueryDocumentSnapshot document: snapshot) {
                    //current document
                    String docID = document.getId();
                    //event that was changed
                    String eventID = document.getString("eventID");
                    //users that have seen the notification
                    ArrayList<String> usersThatHaveSeen = (ArrayList<String>) document.get("users");

                    //query all the events that I am attending
                    db.collection("AttendeesToEvents")
                            .whereEqualTo("attendeeID", auth.getCurrentUser().getUid())
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        //loop through the events I am attending
                                        for(QueryDocumentSnapshot document: task.getResult()){
                                            //If I am attending the event that changed, check if i have seen it
                                            if(eventID.equals(document.getString("eventID"))){
                                                //If I haven't seen the notification for this update yet
                                                if(!usersThatHaveSeen.contains(auth.getCurrentUser().getUid())){
                                                    //show the notification
                                                    buildEventChangeNotification(eventID);
                                                    //update the database to reflect that I have seen it by updating "users" to be usersThatHaveSeen + myUserID
                                                    DocumentReference docRef = db.collection("EventsChanged").document(docID);
                                                    usersThatHaveSeen.add(auth.getCurrentUser().getUid());
                                                    docRef.update("declined", usersThatHaveSeen);
                                                }
                                            }
                                        }
                                    }
                                }
                            });
                }
            }
        });

        //listen to the users to invitations table
        db.collection("UsersToInvitations").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {

                //show notification
                System.out.println("FIRESTORE QUERY DETECTED!");

                //loop through all the documents returned from the collection
                for(QueryDocumentSnapshot document: snapshot) {

                    //do something with the document (row) data
                    String inviteeEmail = document.getString("userEmail");
                    String newEventID = document.getString("eventID");
                    String invitationID = document.getId();
                    Boolean seen = document.getBoolean("seen");

                    System.out.println(inviteeEmail);

                    //if this notification has already been seen by the intended user, do nothing
                    if(seen){
                        continue;
                    } //otherwise, continue

                    System.out.println("Invitation has not been seen yet");
                    System.out.println("Check for document with eventID: " + newEventID);

                    //get the event document
                    DocumentReference docRef = db.collection("Events").document(newEventID);
                    docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    System.out.println("CHECK IF: " + auth.getCurrentUser().getEmail() + " == " + inviteeEmail);
                                    //if there is an invite for this user show the notification with the information for the event
                                    if((auth.getCurrentUser().getEmail()).equals(inviteeEmail)){
                                        buildNotification(inviteeEmail, document.getString("name"), document.getString("description"));
                                        //after showing the notification, update it to seen in the database
                                        DocumentReference eventRef = db.collection("UsersToInvitations").document(invitationID);
                                        eventRef.update("seen", true);
                                    }
                                }
                            }
                        }
                    });
                }
            }
        });

    }
}
