package com.example.easyteamup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.UnderlineSpan;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CreateEventActivity extends AppCompatActivity {

    private final String[] types = new String[] {"ACADEMIC", "SPORTS", "ENTERTAINMENT"};
    private EventTypes type = EventTypes.ACADEMIC;
    private static Map<String, Integer> datetime;
    private static TextView tvToChange;
    private static ListView timeslotLV;
    private static ListView emailLV;
    private static Timestamp dueDate = null;
    private boolean isPublic = true;
    private static ArrayList<TimeSlot> timeslots;
    private static int timeslotListCursor = -1;
    private static boolean timestampIsStart = true;
    private static ArrayList<String> inviteeEmails;
    private static boolean timeslotsHasError = false;

    private static TimeSlotsCreateAdapter TSCAdapter;
    private static InviteUsersAdapter IUAdapter;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        datetime = new HashMap<>();
        timeslots = new ArrayList<>();
        inviteeEmails = new ArrayList<>();
        TSCAdapter = new TimeSlotsCreateAdapter(this, timeslots);
        IUAdapter = new InviteUsersAdapter(this, inviteeEmails);
        populateInfo();
        refreshTimeslotList();
        refreshEmailList();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(ev);
    }

    private void populateInfo() {
        // Event type
        Spinner typeSpinner = (Spinner) findViewById(R.id.createEventType);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(
                CreateEventActivity.this, R.layout.spinner_item, types);
        typeSpinner.setAdapter(spinnerArrayAdapter);
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                TextView selectedTV = (TextView) view;
                String currType = selectedTV.getText().toString();
                type = EventTypes.valueOf(currType);
            }

            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        // Event due date
        TextView dueDateTV = (TextView) findViewById(R.id.createEventDueDate);
        dueDateTV.setText(Util.toUnderlinedString("Click to select"));

        // Event visibility
        RadioButton publicButton = (RadioButton) findViewById(R.id.radio_public);
        publicButton.performClick();

        // Timeslots
        TextView timeslotsAddTV = (TextView) findViewById(R.id.createEventTimeSlotsAdd);
        timeslotsAddTV.setText(Util.toUnderlinedString(timeslotsAddTV.getText().toString()));
        timeslotLV = findViewById(R.id.createEventTimeSlotsList);

        // Invite
        emailLV = findViewById(R.id.createEventInviteeList);
    }

    public void eventDueDateOnClick(View view) {
        tvToChange = (TextView) view;
        timeslotListCursor = -1;
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public static Timestamp getDueDate() {
        return dueDate;
    }

    public static void putDatetime(String key, Integer value) {
        datetime.put(key, value);
    }

    public static void setTVToChange(TextView tv) {
        tvToChange = tv;
    }

    public static void changeDatetimeText() {
        int year = datetime.get("year");
        int month = datetime.get("month");
        int day = datetime.get("day");
        int hour = datetime.get("hour");
        int minute = datetime.get("minute");
        int second = 0;
        Timestamp t = Timestamp.valueOf(
                String.format(Locale.ENGLISH, "%04d-%02d-%02d %02d:%02d:%02d",
                        year, month, day, hour, minute, second)
        );
        String formatted = Util.formatTimestamp(t);
        tvToChange.setText(Util.toUnderlinedString(formatted));

        if(timeslotListCursor >= 0) {
            if(timestampIsStart) {
                timeslots.get(timeslotListCursor).setStart(t);
            }
            else {
                timeslots.get(timeslotListCursor).setEnd(t);
            }
        }
        else {
            dueDate = t;
        }
        timeslotsHasError = false;
        TSCAdapter.notifyDataSetChanged();
        Util.setListViewHeightBasedOnItems(timeslotLV);
    }

    public void onRadioButtonClicked(View view) {
        RadioButton button = (RadioButton) view;
        String text = button.getText().toString();
        isPublic = text.equals("Public");
    }

    public void addTimeslotOnClick(View view) {
        timeslots.add(new TimeSlot());
        refreshTimeslotList();
    }

    public void addEmailOnClick(View view) {
        EditText emailET = findViewById(R.id.createEventInviteeEditText);
        String email = emailET.getText().toString();

        TextView errorTV = findViewById(R.id.createEventInviteeError);

        String hostEmail = auth.getCurrentUser().getEmail();
        auth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                    @Override
                    public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {

                        boolean isNewUser = task.getResult().getSignInMethods().isEmpty();

                        if (isNewUser) {
                            errorTV.setText("User does not exist");
                            errorTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                        } else {
                            if(inviteeEmails.contains(email)) {
                                errorTV.setText("Duplicate user");
                                errorTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                            }
                            else if(hostEmail.equals(email.trim())) {
                                errorTV.setText("Please enter an email other than your own");
                                errorTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                            }
                            else {
                                errorTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 0);
                                inviteeEmails.add(email);
                                refreshEmailList();
                            }
                        }
                        emailET.setText("");

                    }
                });

    }

    private void refreshTimeslotList() {
        timeslotLV.setAdapter(TSCAdapter);
        timeslotsHasError = false;
        TSCAdapter.notifyDataSetChanged();
        Util.setListViewHeightBasedOnItems(timeslotLV);
    }

    private void refreshEmailList() {
        emailLV.setAdapter(IUAdapter);
        IUAdapter.notifyDataSetChanged();
        Util.setListViewHeightBasedOnItems(emailLV);
    }

    public static void setTimeslotListCursor(int pos) {
        timeslotListCursor = pos;
    }

    public static void setTimestampIsStart(boolean flag) {
        timestampIsStart = flag;
    }

    public static void removeTimeslot(int pos) {
        timeslots.remove(pos);
        timeslotsHasError = false;
        TSCAdapter.notifyDataSetChanged();
        Util.setListViewHeightBasedOnItems(timeslotLV);
    }

    public static void removeEmail(int pos) {
        inviteeEmails.remove(pos);
        IUAdapter.notifyDataSetChanged();
        Util.setListViewHeightBasedOnItems(emailLV);
    }

    public void confirmCreateButtonOnClick(View view) {
        if(validateFields()) {
            Intent intent = new Intent(CreateEventActivity.this, ConfirmCreateActivity.class);
            intent.putExtra("eventName", Util.getEditTextString(findViewById(R.id.createEventName)));
            intent.putExtra("eventDescription", Util.getEditTextString(findViewById(R.id.createEventDescription)));
            intent.putExtra("eventLocation", Util.getEditTextString(findViewById(R.id.createEventLocation)));
            Spinner typeSpinner = findViewById(R.id.createEventType);
            intent.putExtra("eventType", typeSpinner.getSelectedItem().toString());
            intent.putExtra("eventDueDate", dueDate);
            intent.putExtra("eventIsPublic", isPublic);
            intent.putExtra("eventTimeslots", timeslots);
            intent.putExtra("invitees", inviteeEmails);
            intent.putExtra("isCreate", true);
            startActivity(intent);
        }
    }

    public void cancelCreateButtonOnClick(View view) {
        Intent intent = new Intent(CreateEventActivity.this, MainActivity2.class);
        startActivity(intent);
    }

    private boolean validateFields() {

        TextView errorTV = findViewById(R.id.createEventError);
        boolean hasError = true;

        // EditTexts
        EditText nameET = findViewById(R.id.createEventName);
        EditText locationET = findViewById(R.id.createEventLocation);
        if(Util.editTextIsEmpty(nameET)) {
            errorTV.setText("Please enter an event name");
        }
        else if(Util.editTextIsEmpty(locationET)) {
            errorTV.setText("Please enter an event location");
        }

        // Due date
        else if(dueDate == null) {
            errorTV.setText("Please select a due date");
        }

        // Timeslots
        else if(timeslots.isEmpty()) {
            errorTV.setText("Please select at least one time slot");
        }
        else if(timeslotsHasNull()) {
            errorTV.setText("Please select a start/end time for all time slots");
        }
        else if(timeslotsHasError) {
            errorTV.setText("Please select valid start/end times for all time slots");
        }

        // No errors
        else {
            hasError = false;
        }

        errorTV.setVisibility(hasError ? View.VISIBLE : View.INVISIBLE);
        return !hasError;
    }

    private boolean timeslotsHasNull() {
        for(TimeSlot ts: timeslots) {
            if(ts.getStart() == null || ts.getEnd() == null) {
                return true;
            }
        }
        return false;
    }

    public static void setTimeslotsHasError(boolean hasError) {
        timeslotsHasError = timeslotsHasError || hasError;
    }

}