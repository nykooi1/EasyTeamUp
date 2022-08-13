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
import android.widget.DatePicker;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EditEventActivity extends AppCompatActivity {

    private Event event;
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
        setContentView(R.layout.activity_edit_event);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        event = bundle.getParcelable("event");

        datetime = new HashMap<>();
        dueDate = event.getEventDueDate();
        timeslots = event.getEventDates();
        inviteeEmails = new ArrayList<>();
        db.collection("UsersToInvitations")
                .whereEqualTo("eventID", event.getEventID())
                .whereEqualTo("declined", false)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            for(QueryDocumentSnapshot document: task.getResult()) {
                                String userEmail = document.getString("userEmail");
                                inviteeEmails.add(userEmail);
                            }
                            IUAdapter = new InviteUsersAdapter(EditEventActivity.this, inviteeEmails);
                            refreshEmailList();
                        }
                    }
                });

        TSCAdapter = new TimeSlotsCreateAdapter(this, timeslots);
        TSCAdapter.setIsCreate(false);
        populateInfo();
        refreshTimeslotList();
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
        // EditTexts
        EditText nameET = findViewById(R.id.editEventName);
        EditText descriptionET = findViewById(R.id.editEventDescription);
        EditText locationET = findViewById(R.id.editEventLocation);
        nameET.setText(event.getEventName());
        descriptionET.setText(event.getEventDescription());
        locationET.setText(event.getEventLocation());

        // Event type
        Spinner typeSpinner = (Spinner) findViewById(R.id.editEventType);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(
                EditEventActivity.this, R.layout.spinner_item, types);
        typeSpinner.setAdapter(spinnerArrayAdapter);
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                TextView selectedTV = (TextView) view;
                String currType = selectedTV.getText().toString();
                type = EventTypes.valueOf(currType);
            }

            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
        switch (event.getEventType()) {
            case ACADEMIC: {
                typeSpinner.setSelection(0);
                break;
            }
            case SPORTS: {
                typeSpinner.setSelection(1);
                break;
            }
            case ENTERTAINMENT: {
                typeSpinner.setSelection(2);
                break;
            }
        }

        // Event due date
        TextView dueDateTV = (TextView) findViewById(R.id.editEventDueDate);
        if(event.getEventDueDate() == null) {
            dueDateTV.setText(Util.toUnderlinedString("Click to select"));
        }
        else {
            dueDateTV.setText(Util.toUnderlinedString(Util.formatTimestamp(event.getEventDueDate())));
        }

        // Event visibility
        RadioButton publicButton = findViewById(R.id.edit_radio_public);
        RadioButton privateButton = findViewById(R.id.edit_radio_private);
        if(event.getIsPublic()) {
            publicButton.performClick();
        }
        else {
            privateButton.performClick();
        }

        // Timeslots
        TextView timeslotsAddTV = (TextView) findViewById(R.id.editEventTimeSlotsAdd);
        timeslotsAddTV.setText(Util.toUnderlinedString(timeslotsAddTV.getText().toString()));
        timeslotLV = findViewById(R.id.editEventTimeSlotsList);

        // Invite
        emailLV = findViewById(R.id.editEventInviteeList);
    }

    public void editEventDueDateOnClick(View view) {
        tvToChange = (TextView) view;
        timeslotListCursor = -1;
        DatePickerFragment newFragment = new DatePickerFragment();
        newFragment.setIsCreate(false);
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
        TSCAdapter.setIsCreate(false);
        TSCAdapter.notifyDataSetChanged();
        Util.setListViewHeightBasedOnItems(timeslotLV);
    }

    public void onEditRadioButtonClicked(View view) {
        RadioButton button = (RadioButton) view;
        String text = button.getText().toString();
        isPublic = text.equals("Public");
    }

    public void editAddTimeslotOnClick(View view) {
        timeslots.add(new TimeSlot());
        refreshTimeslotList();
    }

    public void editAddEmailOnClick(View view) {
        EditText emailET = findViewById(R.id.editEventInviteeEditText);
        String email = emailET.getText().toString();

        TextView errorTV = findViewById(R.id.editEventInviteeError);

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
        TSCAdapter.setIsCreate(false);
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
        TSCAdapter.setIsCreate(false);
        TSCAdapter.notifyDataSetChanged();
        Util.setListViewHeightBasedOnItems(timeslotLV);
    }

    public static void removeEmail(int pos) {
        inviteeEmails.remove(pos);
        IUAdapter.notifyDataSetChanged();
        Util.setListViewHeightBasedOnItems(emailLV);
    }

    public void confirmEditButtonOnClick(View view) {
        if(validateFields()) {
            Intent intent = new Intent(EditEventActivity.this, ConfirmCreateActivity.class);
            intent.putExtra("eventID", event.getEventID());
            intent.putExtra("eventName", Util.getEditTextString(findViewById(R.id.editEventName)));
            intent.putExtra("eventDescription", Util.getEditTextString(findViewById(R.id.editEventDescription)));
            intent.putExtra("eventLocation", Util.getEditTextString(findViewById(R.id.editEventLocation)));
            Spinner typeSpinner = findViewById(R.id.editEventType);
            intent.putExtra("eventType", typeSpinner.getSelectedItem().toString());
            intent.putExtra("eventDueDate", dueDate);
            intent.putExtra("eventIsPublic", isPublic);
            intent.putExtra("eventTimeslots", timeslots);
            intent.putExtra("invitees", inviteeEmails);
            intent.putExtra("isCreate", false);
            startActivity(intent);
        }
    }

    public void cancelEditButtonOnClick(View view) {
        Intent intent = new Intent(EditEventActivity.this, MainActivity2.class);
        startActivity(intent);
    }

    private boolean validateFields() {

        TextView errorTV = findViewById(R.id.editEventError);
        boolean hasError = true;

        // EditTexts
        EditText nameET = findViewById(R.id.editEventName);
        EditText locationET = findViewById(R.id.editEventLocation);
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