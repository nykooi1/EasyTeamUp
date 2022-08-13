package com.example.easyteamup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.security.NoSuchAlgorithmException;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    public void login(View loginButton){
//        System.out.println("called login function");
        //get the email
        String email = ((EditText)loginButton.getRootView().findViewById(R.id.editTextTextEmailAddress)).getText().toString();
        //get the password
        String password = ((EditText)loginButton.getRootView().findViewById(R.id.editTextTextPassword)).getText().toString();
        System.out.println("email: " + email + " password: " + password);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            System.out.println("=== log in successful ===");
                            Intent intent = new Intent(LoginActivity.this, MainActivity2.class);
                            startActivity(intent);
                        } else {
                            // If sign in fails, display a message to the user.
                            System.out.println("=== log in failed ===");
                        }
                    }
                });
    }

    public void createAccount(View view) {
        Intent intent = new Intent(LoginActivity.this, CreateAccountActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_login);
    }
}