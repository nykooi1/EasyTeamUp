package com.example.easyteamup;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;

import com.example.easyteamup.databinding.ActivityMain2Binding;
import com.example.easyteamup.HistoryFragment;
import com.example.easyteamup.HomeFragment;
import com.example.easyteamup.ProfileFragment;

import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.view.View;

import com.example.easyteamup.ui.main.SectionsPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class MainActivity2 extends AppCompatActivity {

    private ActivityMain2Binding binding;
    private FirebaseAuth mAuth;
    private TabLayout tabs;

    //creates the notification channel for invites
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "notificationChannel";
            String description = "notifications for EasyTeamUp";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("invites", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
            Intent intent = new Intent(MainActivity2.this, LoginActivity.class);
            startActivity(intent);
            return;
        }

        //LISTEN FOR CHANGES TO DISPLAY NOTIFICATIONS
        FirebaseListener listener = new FirebaseListener(this);
        listener.listenForInvites();

        binding = ActivityMain2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = binding.viewPager;
        viewPager.setAdapter(sectionsPagerAdapter);
        tabs = binding.tabs;
        tabs.setupWithViewPager(viewPager);

        binding.bottomNavigationView.setOnItemSelectedListener(item-> {
            switch (item.getItemId()) {
                case R.id.ic_home:
                    replaceFragment(new HomeFragment());
                    break;
                case R.id.ic_profile:
                    replaceFragment(new ProfileFragment());
                    break;
            }
            return true;
        });
    }

    private void replaceFragment(Fragment frag){
        FragmentManager fragmentManager=getSupportFragmentManager();
        FragmentTransaction transaction=fragmentManager.beginTransaction();
        transaction.replace(R.id.fl_wrapper, frag);
        transaction.commit();
    }

    public void logOut(View logoutLink) {
        mAuth.signOut();
        Intent intent = new Intent(MainActivity2.this, MainActivity2.class);
        startActivity(intent);
    }

    public void addButtonOnClick(View view) {
        Intent intent = new Intent(MainActivity2.this, CreateEventActivity.class);
        startActivity(intent);
    }

    /**
     * added to keep track of tab
     */
    /*@Override
    protected void onResume() {
        super.onResume();

        // Fetching the stored data
        // from the SharedPreference
        SharedPreferences sh = getSharedPreferences("MySharedPref", MODE_PRIVATE);

        int pos = sh.getInt("pos", 0);
        System.out.println("position: "+pos);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = binding.viewPager;
        viewPager.setAdapter(sectionsPagerAdapter);
        tabs = binding.tabs;
        tabs.setupWithViewPager(viewPager);
        // Setting the fetched data
        // in the EditTexts
        viewPager.setCurrentItem(pos);
    }*/

//    @Override
//    protected void onPause() {
//        super.onPause();
//        // Creating a shared pref object
//        // with a file name "MySharedPref"
//        // in private mode
//        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
//        SharedPreferences.Editor myEdit = sharedPreferences.edit();
//
//        // write all the data entered by the user in SharedPreference and apply
//        System.out.println("leaving position: "+tabs.getSelectedTabPosition());
//        myEdit.putInt("pos", tabs.getSelectedTabPosition());
//        myEdit.apply();
//    }

    /*@Override
    protected void onStop() {
        super.onStop();
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();

        // write all the data entered by the user in SharedPreference and apply
        System.out.println("leaving position2: "+tabs.getSelectedTabPosition());
        myEdit.putInt("pos", tabs.getSelectedTabPosition());
        myEdit.apply();
    }*/
}