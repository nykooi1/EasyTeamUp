package com.example.easyteamup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateAccountActivity extends AppCompatActivity {

    Map<String, EditText> editTexts = new HashMap<>();
    TextView errorTV;
    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        EditText fNameET = (EditText) findViewById(R.id.createAccountFirstName);
        editTexts.put("fName", fNameET);
        EditText lNameET = (EditText) findViewById(R.id.createAccountLastName);
        editTexts.put("lName", lNameET);
        EditText usernameET = (EditText) findViewById(R.id.createAccountUsername);
        editTexts.put("username", usernameET);
        EditText emailET = (EditText) findViewById(R.id.createAccountEmail);
        editTexts.put("email", emailET);
        EditText passwordET = (EditText) findViewById(R.id.createAccountPassword);
        editTexts.put("password", passwordET);

        errorTV = (TextView) findViewById(R.id.createAccountError);
        errorTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 0);

    }

    public void createAccountOnClick(View view) {

        if(validateInput()) {

            // check if email exists in db
            EditText emailET = editTexts.get("email");
            String email = emailET.getText().toString();
            String username = editTexts.get("username").getText().toString();
            mAuth.fetchSignInMethodsForEmail(email)
                    .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                        @Override
                        public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {

                            boolean isNewUser = task.getResult().getSignInMethods().isEmpty();

                            if (!isNewUser) {
                                emailET.setError("Email is already used");
                            }
                            else {
                                // check if username exists in db
                                db.collection("Users")
                                        .whereEqualTo("username", username)
                                        .get()
                                        .addOnCompleteListener(CreateAccountActivity.this, new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if(task.isSuccessful()) {
                                                    if(!task.getResult().isEmpty()) {
                                                        editTexts.get("username").setError("Username is already used");
                                                    }
                                                    else {
                                                        String fName = editTexts.get("fName").getText().toString();
                                                        String lName = editTexts.get("lName").getText().toString();
                                                        String password = editTexts.get("password").getText().toString();
                                                        mAuth.createUserWithEmailAndPassword(email, password)
                                                                .addOnCompleteListener(CreateAccountActivity.this, new OnCompleteListener<AuthResult>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                                                        if (task.isSuccessful()) {
                                                                            // Sign in success, update UI with the signed-in user's information
                                                                            FirebaseUser user = mAuth.getCurrentUser();
                                                                            String UID = user.getUid();
                                                                            Map<String, Object> userMap = new HashMap<>();
                                                                            userMap.put("fName", fName);
                                                                            userMap.put("lName", lName);
                                                                            userMap.put("username", username);
                                                                            userMap.put("profilePic", null);

                                                                            db.collection("Users").document(UID)
                                                                                    .set(userMap)
                                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                        @Override
                                                                                        public void onSuccess(Void aVoid) {
                                                                                            System.out.println("=== user insertion success ===");
                                                                                        }
                                                                                    })
                                                                                    .addOnFailureListener(new OnFailureListener() {
                                                                                        @Override
                                                                                        public void onFailure(@NonNull Exception e) {
                                                                                            System.out.println("=== user insertion failed ===");
                                                                                        }
                                                                                    });
                                                                            Intent intent = new Intent(CreateAccountActivity.this, LoginActivity.class);
                                                                            startActivity(intent);
                                                                        }
                                                                    }
                                                                });
                                                    }
                                                }
                                            }
                                        });
                            }
                        }
                    });
        }
    }

    protected boolean validateInput() {

        // check empty
        boolean hasEmpty = inputHasEmpty(editTexts);
        // check email format
        boolean badEmail = emailIsBad(editTexts.get("email"));

        return !hasEmpty && !badEmail;
    }

    protected boolean inputHasEmpty(Map<String, EditText> editTexts) {
        boolean hasEmpty = false;
        for(EditText et: editTexts.values()) {
            if( TextUtils.isEmpty(et.getText())){
                et.setError( "This field is required" );
                hasEmpty = true;
            }
        }
        return hasEmpty;
    }

    protected boolean emailIsBad(EditText emailET) {
        CharSequence email = emailET.getText();
        if(TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailET.setError( "Invalid email" );
            return true;
        }
        return false;
    }

//    protected DBHelper.InsertUserStatus insertUser() {
//        DBHelper db = new DBHelper(CreateAccountActivity.this);
//        String fName = editTexts.get("fName").getText().toString();
//        String lName = editTexts.get("lName").getText().toString();
//        String username = editTexts.get("username").getText().toString();
//        String email = editTexts.get("email").getText().toString();
//        String password = editTexts.get("password").getText().toString();
//        return db.insertUser(email, password, fName, lName, username, "");
//    }
}