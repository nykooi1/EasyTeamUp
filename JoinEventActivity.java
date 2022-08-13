package com.example.easyteamup;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class JoinEventActivity extends AppCompatActivity {

    private Event event;
    private static ArrayList<String> userSelectedTimeSlots;

    private static TimeSlotsAdapter TSAdapter;

    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_event);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        event = (Event) bundle.get("event");
        userSelectedTimeSlots = (ArrayList<String>) bundle.get("timeSlotIDs");
        TSAdapter = new TimeSlotsAdapter(this, event.getEventTimeSlotIDs());
        populateInfo();
    }

    protected void populateInfo() {
        // Text
        TextView nameTV = (TextView) findViewById(R.id.joinEventName);
        TextView errorTV = (TextView) findViewById(R.id.joinEventError);
        String nameText = "for " + event.getEventName();
        nameTV.setText(nameText);
        errorTV.setText(null);

        // Checkbox list

        ListView listView = (ListView) findViewById(R.id.timeSlotCheckboxList);
        listView.setAdapter(TSAdapter);

    }

    public void confirmJoinButtonOnClick(View view) {
        if(userSelectedTimeSlots.isEmpty()) {
            TextView errorTV = (TextView) findViewById(R.id.joinEventError);
            errorTV.setText("Please select at least one time slot.");
        }
        else {
            Intent intent = new Intent(JoinEventActivity.this, ConfirmJoinActivity.class);
            intent.putExtra("event", event);
            intent.putExtra("timeslots", userSelectedTimeSlots);
            startActivity(intent);
        }
    }

    public void cancelJoinButtonOnClick(View view) {
        Intent intent = new Intent(JoinEventActivity.this, DisplayEventActivity.class);
        intent.putExtra("event", event);
        startActivity(intent);
    }

    public static void toggleSelectedTimeSlots(String id, boolean isChecked) {
        if(userSelectedTimeSlots.contains(id) && !isChecked) {
            int pos = userSelectedTimeSlots.indexOf(id);
            userSelectedTimeSlots.remove(id);
        }
        else if(!userSelectedTimeSlots.contains(id) && isChecked) {
            userSelectedTimeSlots.add(id);
        }
    }
}