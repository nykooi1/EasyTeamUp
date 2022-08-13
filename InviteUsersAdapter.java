package com.example.easyteamup;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import java.sql.Timestamp;
import java.util.ArrayList;

public class InviteUsersAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final ArrayList<String> emails;

    public InviteUsersAdapter(Context context, ArrayList<String> emails) {
        super(context, 0, emails);
        this.context = context;
        this.emails = emails;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        String email = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.invite_user_list, parent, false);
        }

        TextView emailTV = convertView.findViewById(R.id.inviteUserEmail);
        TextView deleteButton = convertView.findViewById(R.id.inviteUserDelete);

        emailTV.setText(email);

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                CreateEventActivity.removeEmail(position);
            }
        });

        return convertView;
    }

    @Override
    public String getItem(int position){
        return emails.get(position);
    }

}