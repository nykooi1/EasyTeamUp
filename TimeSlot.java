package com.example.easyteamup;

import android.os.Parcel;
import android.os.Parcelable;

import java.sql.Timestamp;
import java.util.ArrayList;

public class TimeSlot implements Parcelable {

    //member variables
    private Timestamp start;
    private Timestamp end;

    //constructors
    public TimeSlot() {
        this.start = null;
        this.end = null;
    }

    public TimeSlot(Timestamp start, Timestamp end){
        this.start = start;
        this.end = end;
    }

    protected TimeSlot(Parcel in) {
        start = (Timestamp) in.readSerializable();
        end = (Timestamp) in.readSerializable();
    }

    public static final Creator<TimeSlot> CREATOR = new Creator<TimeSlot>() {
        @Override
        public TimeSlot createFromParcel(Parcel in) {
            return new TimeSlot(in);
        }

        @Override
        public TimeSlot[] newArray(int size) {
            return new TimeSlot[size];
        }
    };

    //getters
    public Timestamp getStart(){
        return this.start;
    }
    public Timestamp getEnd(){
        return this.end;
    }

    //setters
    public void setStart(Timestamp start) { this.start = start; }
    public void setEnd(Timestamp end) { this.end = end; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeSerializable(start);
        parcel.writeSerializable(end);
    }
}
