package com.example.easyteamup;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {

    //member variables
    private String userID;
    private String userEmail;
    private String firstName;
    private String lastName;
    private String username;
    private String profilePic;
    private Event[] eventsAttending;
    private Event[] eventRequests;
    private Event[] hostedEvents;

    //constructor
    public User(String userID,
                String userEmail,
                String firstName,
                String lastName,
                String username,
                String profilePic,
                Event[] eventsAttending,
                Event[] eventRequests,
                Event[] hostedEvents){
        this.userID = userID;
        this.userEmail = userEmail;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.profilePic = profilePic;
        this.eventsAttending = eventsAttending;
        this.eventRequests = eventRequests;
        this.hostedEvents = hostedEvents;
    }

    protected User(Parcel in) {
        userID = in.readString();
        userEmail = in.readString();
        firstName = in.readString();
        lastName = in.readString();
        username = in.readString();
        profilePic = in.readString();
        eventsAttending = in.createTypedArray(Event.CREATOR);
        eventRequests = in.createTypedArray(Event.CREATOR);
        hostedEvents = in.createTypedArray(Event.CREATOR);
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    //getters
    public String getUserID(){
        return this.userID;
    }

    public String getUserEmail(){
        return this.userEmail;
    }

    public String getFirstname(){
        return this.firstName;
    }

    public String getLastName(){
        return this.lastName;
    }

    public String getUsername(){
        return this.username;
    }

    public String getProfilePic(){
        return this.profilePic;
    }

    public Event[] getEventsAttending(){
        return this.eventsAttending;
    }

    public Event[] getEventRequests(){
        return this.eventRequests;
    }

    public Event[] getHostedEvents(){
        return this.hostedEvents;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(userID);
        parcel.writeString(userEmail);
        parcel.writeString(firstName);
        parcel.writeString(lastName);
        parcel.writeString(username);
        parcel.writeString(profilePic);
        parcel.writeParcelableArray(eventsAttending, 0);
        parcel.writeParcelableArray(eventRequests, 0);
        parcel.writeParcelableArray(hostedEvents, 0);
    }
}
