package com.example.easyteamup;

import android.os.Parcel;
import android.os.Parcelable;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Map;

enum EventTypes {
    ACADEMIC,
    SPORTS,
    ENTERTAINMENT
}

public class Event implements Parcelable {

    //member variables
    private String eventID;
    private String eventName;
    private ArrayList<TimeSlot> eventDates;
    private ArrayList<String> eventTimeSlotIDs;
    private TimeSlot selectedDate;
    private String eventDescription;
    private String eventHostID;
    private ArrayList<String> eventAttendees;
    private String eventLocation;
    private Timestamp eventDueDate;
    private Boolean isPublic;
    private EventTypes eventType;

    //constructor
    public Event(
            String eventID,
            String eventName,
            ArrayList<TimeSlot> eventDates,
            ArrayList<String> eventTimeSlotIDs,
            TimeSlot selectedDate,
            String eventDescription,
            String eventHostID,
            ArrayList<String> eventAttendees,
            String eventLocation,
            Timestamp eventDueDate,
            Boolean isPublic,
            EventTypes eventType
    ){
        this.eventID = eventID;
        this.eventName = eventName;
        this.eventDates = eventDates;
        this.eventTimeSlotIDs = eventTimeSlotIDs;
        this.selectedDate = selectedDate;
        this.eventDescription = eventDescription;
        this.eventHostID = eventHostID;
        this.eventAttendees = eventAttendees;
        this.eventLocation = eventLocation;
        this.eventDueDate = eventDueDate;
        this.isPublic = isPublic;
        this.eventType = eventType;
    }

    @SuppressWarnings("unchecked")
    protected Event(Parcel in) {
        eventID = in.readString();
        eventName = in.readString();
        eventDates = in.createTypedArrayList(TimeSlot.CREATOR);
        eventTimeSlotIDs = in.readArrayList(String.class.getClassLoader());
        selectedDate = in.readParcelable(TimeSlot.class.getClassLoader());
        eventDescription = in.readString();
        eventHostID = in.readString();
        eventAttendees = in.readArrayList(String.class.getClassLoader());
        eventLocation = in.readString();
        eventDueDate = (Timestamp) in.readSerializable();
        isPublic = in.readInt() != 0;
        eventType = EventTypes.valueOf(in.readString());
    }

    public static final Creator<Event> CREATOR = new Creator<Event>() {
        @Override
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];
        }
    };

    //getters
    public String getEventID(){
        return this.eventID;
    }

    public String getEventName(){
        return this.eventName;
    }

    public String getEventId(){
        return this.eventID;
    }

    public ArrayList<TimeSlot>  getEventDates(){
        return this.eventDates;
    }

    public ArrayList<String> getEventTimeSlotIDs() { return this.eventTimeSlotIDs; }

    public TimeSlot getSelectedDate(){
        return this.selectedDate;
    }

    public String getEventDescription(){
        return this.eventDescription;
    }

    public String getEventHostID(){
        return this.eventHostID;
    }

    public ArrayList<String> getEventAttendees(){
        return this.eventAttendees;
    }

    public String getEventLocation(){
        return this.eventLocation;
    }

    public Timestamp getEventDueDate(){
        return this.eventDueDate;
    }

    public Boolean getIsPublic(){
        return this.isPublic;
    }

    public EventTypes getEventType(){
        return this.eventType;
    }

    @Override
    public String toString() {
        return eventName + "\n" + eventDescription;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(eventID);
        parcel.writeString(eventName);
        parcel.writeTypedList(eventDates);
        parcel.writeList(eventTimeSlotIDs);
        parcel.writeParcelable(selectedDate, 0);
        parcel.writeString(eventDescription);
        parcel.writeString(eventHostID);
        parcel.writeList(eventAttendees);
        parcel.writeString(eventLocation);
        parcel.writeSerializable(eventDueDate);
        parcel.writeInt(isPublic ? 1 : 0);
        parcel.writeString(eventType.name());

    }
}
