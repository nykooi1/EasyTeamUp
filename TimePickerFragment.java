package com.example.easyteamup;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

public class TimePickerFragment
        extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {

    private boolean isCreate = true;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        if(isCreate) {
            CreateEventActivity.putDatetime("hour", hourOfDay);
            CreateEventActivity.putDatetime("minute", minute);
            CreateEventActivity.changeDatetimeText();
        }
        else {
            EditEventActivity.putDatetime("hour", hourOfDay);
            EditEventActivity.putDatetime("minute", minute);
            EditEventActivity.changeDatetimeText();
        }
    }

    public void setIsCreate(boolean flag) {
        isCreate = flag;
    }
}
