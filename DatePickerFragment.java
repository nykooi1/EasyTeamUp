package com.example.easyteamup;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.DatePicker;

import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

public class DatePickerFragment
        extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    private boolean isCreate = true;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        if(isCreate) {
            CreateEventActivity.putDatetime("year", year);
            CreateEventActivity.putDatetime("month", month+1);
            CreateEventActivity.putDatetime("day", day);
        }
        else {
            EditEventActivity.putDatetime("year", year);
            EditEventActivity.putDatetime("month", month+1);
            EditEventActivity.putDatetime("day", day);
        }

        TimePickerFragment newFragment = new TimePickerFragment();
        newFragment.setIsCreate(isCreate);
        newFragment.show(getActivity().getSupportFragmentManager(), "datePicker");
    }

    public void setIsCreate(boolean flag) {
        isCreate = flag;
    }

}