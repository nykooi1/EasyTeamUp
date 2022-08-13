package com.example.easyteamup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class EditUserInfoActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    //updates the user information in the database
    public void updateUserInformation(View view){

        System.out.println("UPDATE USER INFO");

        //update the firstname, lastname and username
        TextInputEditText firstNameView = (TextInputEditText) findViewById(R.id.firstName);
        TextInputEditText lastNameView = (TextInputEditText) findViewById(R.id.lastName);
        TextInputEditText usernameView = (TextInputEditText) findViewById(R.id.username);

        DocumentReference docRef = db.collection("Users").document(auth.getCurrentUser().getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        DocumentReference docRef = db.collection("Users").document(document.getId());
                        docRef.update("fName", firstNameView.getText().toString());
                        docRef.update("lName", lastNameView.getText().toString());
                        docRef.update("username", usernameView.getText().toString());
                        Intent intent = new Intent(EditUserInfoActivity.this, ViewCurrentUserInfo.class);
                        startActivity(intent);
                    }
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_info);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        //get the user information
        //get the event document
        DocumentReference docRef = db.collection("Users").document(auth.getCurrentUser().getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        //populate the edit fields
                        TextInputEditText firstName = (TextInputEditText) findViewById(R.id.firstName);
                        firstName.setText(document.getString("fName"));
                        TextInputEditText lastName = (TextInputEditText) findViewById(R.id.lastName);
                        lastName.setText(document.getString("lName"));
                        TextInputEditText username = (TextInputEditText) findViewById(R.id.username);
                        username.setText(document.getString("username"));
                    }
                }
            }
        });



    }
}