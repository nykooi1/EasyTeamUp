package com.example.easyteamup;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
//import com.google.firebase.auth.UserRecord;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Map;

public class DBHelper {

    private final FirebaseFirestore db;

    public DBHelper() {
        db = FirebaseFirestore.getInstance();
    }

    User queriedUser;
    public User queryUserByEmail(String email) {
        db.collection("Users")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String id = document.getId();
                                Map<String, Object> userInfo = document.getData();
                                String fName = (String) userInfo.get("fName");
                                String lName = (String) userInfo.get("lName");
                                String username = (String) userInfo.get("username");
                                String profilePic = (String) userInfo.get("profilePic");
                                setUser(new User(id, email, fName, lName, username, profilePic,
                                        new Event[]{}, new Event[]{}, new Event[]{}));

                            }
                        } else {

                        }
                    }
                });

//        try {
//            UserRecord userRecord = FirebaseAuth.getInstance().getUserByEmail(email);
//            String id = userRecord.getUid();
//            String fName = userRecord.get
//        } catch (FirebaseAuthException e) {
//            e.printStackTrace();
//        }
        return queriedUser;
    }
    public void setUser(User user) {
        queriedUser = user;
    }

    public User queryUserByID(String userID) {

        return null;
    }

}
