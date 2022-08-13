package com.example.easyteamup;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.sql.Array;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FragmentMap extends Fragment implements GoogleMap.OnMarkerClickListener, OnMapReadyCallback {
    private GoogleMap mMap;
    private MapView mapView;
    private View mView;
    private FirebaseFirestore db;
    private FirebaseAuth auth;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView=inflater.inflate(R.layout.fragment_map,container,false);
        return mView;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mapView=(MapView) mView.findViewById(R.id.map);
        if(mapView!=null){
            mapView.onCreate(null);
            mapView.onResume();
            mapView.getMapAsync(this);
        }

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        MapsInitializer.initialize(getContext());
        mMap = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);




        // Add a marker in USC and move the camera
        LatLng usc = new LatLng(34.0224, -118.2851);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(usc, 14));
        //<eventID, <location, name>>

        /**
         * get all public + invited private events + attending private events
         * get the location and name of each event
         * use GoogleMaps API to get the long lat
         * generate marker on GoogleMap
         */
        //get all public events
        db.collection("Events")
                .whereEqualTo("isPublic", true)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for(QueryDocumentSnapshot document: task.getResult()) {
                                geoLocate(document.getString("location"),document.getString("name"),document.getId());
                            }
                        }
                    }
                });

        //get attending private events
        db.collection("Events")
                .whereEqualTo("isPublic", false)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    //get all private events
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            for(QueryDocumentSnapshot document: task.getResult()) {
                                String eventID = document.getId();
                                String userID = auth.getCurrentUser().getUid();
                                //use userID and eventID as foreign key to find private events that the user is attending
                                db.collection("AttendeesToEvents")
                                        .whereEqualTo("attendeeID", userID)
                                        .whereEqualTo("eventID", eventID)
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if(task.isSuccessful()) {
                                                    for(QueryDocumentSnapshot document: task.getResult()) {
                                                        String innerEventID = document.getString("eventID");
                                                        //get the event location and name using innerEventID
                                                        DocumentReference docRef = db.collection("Events").document(innerEventID);
                                                        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                if (task.isSuccessful()) {
                                                                    DocumentSnapshot document = task.getResult();
                                                                    if (document.exists()) {
                                                                        geoLocate(document.getString("location"),document.getString("name"),innerEventID);
                                                                    }
                                                                }
                                                            }
                                                        });
                                                    }
                                                }
                                            }
                                        });
                            }
                        }
                    }
                });

        //get all invited private events
        db.collection("Events")
                .whereEqualTo("isPublic", false)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    //get all private events
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            for(QueryDocumentSnapshot document: task.getResult()) {
                                String eventID = document.getId();
                                String email = auth.getCurrentUser().getEmail();
                                //use userEmail and eventID as foreign key to find private events that user is invited to
                                db.collection("UsersToInvitations")
                                        .whereEqualTo("userEmail", email)
                                        .whereEqualTo("eventID", eventID)
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if(task.isSuccessful()) {
                                                    for(QueryDocumentSnapshot document: task.getResult()) {
                                                        String innerEventID = document.getString("eventID");
                                                        //get the event location and name using innerEventID
                                                        DocumentReference docRef = db.collection("Events").document(innerEventID);
                                                        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                if (task.isSuccessful()) {
                                                                    DocumentSnapshot document = task.getResult();
                                                                    if (document.exists()) {
                                                                        geoLocate(document.getString("location"),document.getString("name"),innerEventID);
                                                                    }
                                                                }
                                                            }
                                                        });
                                                    }
                                                }
                                            }
                                        });
                            }
                        }
                    }
                });
        mMap.setOnMarkerClickListener(this);

    }

    //generate tags on GoogleMap
    private void geoLocate(String eventLoc,String eventName,String eventID){
        Geocoder geocoder=new Geocoder(getContext());
        List<Address> addressList = new ArrayList<>();
        try{
            addressList=geocoder.getFromLocationName(eventLoc,1);
        }catch(IOException e) {
            e.printStackTrace();
        }
        if(addressList.size()>0){
            Address addy = addressList.get(0);
            LatLng obj=new LatLng(addy.getLatitude(),addy.getLongitude());
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(obj)
                    .title(eventName));
            marker.setTag(eventID);
        }
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        String eventID=marker.getTag().toString();
        DocumentReference docRef = db.collection("Events").document(eventID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String id = document.getId();
                        Map<String, Object> eventInfo = document.getData();
                        String name = (String) eventInfo.get("name");
                        String hostID = (String) eventInfo.get("hostID");
                        String location = (String) eventInfo.get("location");
                        Boolean isPublic = true;
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
                                            // Use the public events to populate the list view
                                            Intent intent = new Intent(getContext(), DisplayEventActivity.class);
                                            intent.putExtra("event", event);
//                                            intent.putExtra("position",1);
                                            startActivity(intent);
                                        }
                                    }
                                });
                    }

                }
            }
        });

        return false;
    }
}
