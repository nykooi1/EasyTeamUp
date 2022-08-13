package com.example.easyteamup;

import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.UnderlineSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

public class TimeSlotsCreateAdapter extends ArrayAdapter<TimeSlot> {
    private final Context context;
    private final ArrayList<TimeSlot> timeslots;
    private boolean isCreate;

    public TimeSlotsCreateAdapter(Context context, ArrayList<TimeSlot> timeslots) {
        super(context, 0, timeslots);
        this.context = context;
        this.timeslots = timeslots;
        isCreate = true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        TimeSlot timeslot = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.timeslot_create_list, parent, false);
        }

        TextView startTV = convertView.findViewById(R.id.timeSlotCreateStartText);
        TextView endTV = convertView.findViewById(R.id.timeSlotCreateEndText);
        TextView deleteButton = convertView.findViewById(R.id.timeSlotCreateDelete);
        TextView errorTV = convertView.findViewById(R.id.timeSlotCreateError);

        String startText, endText;
        if(timeslot.getStart() != null) {
            startText = Util.formatTimestamp(timeslot.getStart());
        }
        else {
            startText = "Pick start time";
        }
        if(timeslot.getEnd() != null) {
            endText = Util.formatTimestamp(timeslot.getEnd());
        }
        else {
            endText = "Pick end time";
        }
        startTV.setText(Util.toUnderlinedString(startText));
        endTV.setText(Util.toUnderlinedString(endText));

        startTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                if(isCreate) {
                    CreateEventActivity.setTVToChange((TextView) v);
                    CreateEventActivity.setTimeslotListCursor(position);
                    CreateEventActivity.setTimestampIsStart(true);
                }
                else {
                    EditEventActivity.setTVToChange((TextView) v);
                    EditEventActivity.setTimeslotListCursor(position);
                    EditEventActivity.setTimestampIsStart(true);
                }
                DatePickerFragment newFragment = new DatePickerFragment();
                newFragment.setIsCreate(isCreate);
                newFragment.show(((FragmentActivity) context).getSupportFragmentManager(), "datePicker");
            }
        });
        endTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                if(isCreate) {
                    CreateEventActivity.setTVToChange((TextView) v);
                    CreateEventActivity.setTimeslotListCursor(position);
                    CreateEventActivity.setTimestampIsStart(false);
                }
                else {
                    EditEventActivity.setTVToChange((TextView) v);
                    EditEventActivity.setTimeslotListCursor(position);
                    EditEventActivity.setTimestampIsStart(false);
                }
                DatePickerFragment newFragment = new DatePickerFragment();
                newFragment.setIsCreate(isCreate);
                newFragment.show(((FragmentActivity) context).getSupportFragmentManager(), "datePicker");
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                if(isCreate) {
                    CreateEventActivity.removeTimeslot(position);
                }
                else {
                    EditEventActivity.removeTimeslot(position);
                }
            }
        });

        if(isCreate) {
            CreateEventActivity.setTimeslotsHasError(!validateTimeslot(timeslot, errorTV));
        }
        else {
            EditEventActivity.setTimeslotsHasError(!validateTimeslot(timeslot, errorTV));
        }

        return convertView;
    }

    @Override
    public TimeSlot getItem(int position){
        return timeslots.get(position);
    }

    private boolean validateTimeslot(TimeSlot timeslot, TextView errorTV) {
        if(timeslot.getStart() != null) {
            Timestamp dueDate = isCreate ? CreateEventActivity.getDueDate() : EditEventActivity.getDueDate();
            if(dueDate != null && dueDate.compareTo(timeslot.getStart()) > 0) {
                errorTV.setText("The start time cannot be earlier than the due date");
                errorTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                return false;
            }
        }
        if(timeslot.getStart() != null && timeslot.getEnd() != null) {
            if(timeslot.getStart().compareTo(timeslot.getEnd()) > 0) {
                errorTV.setText("The start time cannot be later than the end time");
                errorTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                return false;
            }
            else if(timeslot.getStart().compareTo(timeslot.getEnd()) == 0) {
                errorTV.setText("The start time cannot be the same as the end time");
                errorTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                return false;
            }
        }
        errorTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 0);
        return true;
    }

    public void setIsCreate(boolean flag) {
        isCreate = flag;
    }

}
