package com.example.easyteamup;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class ViewCurrentUserInfo extends AppCompatActivity  {
    public BottomNavigationView bottomNavigationView;


    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseStorage storage;

    private String getRealPathFromURI(Uri contentURI) {
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            return contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(idx);
        }
    }

    // You can do the assignment inside onAttach or onCreate, i.e, before the activity is displayed
    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {

                        // There are no request codes
                        Intent data = result.getData();
                        Uri imageURI = data.getData();

                        String filePath = getRealPathFromURI(imageURI);
                        System.out.println("file path: " + filePath);

                        //insert the blob into the database
                        // Create a storage reference from our app
                        StorageReference storageRef = storage.getReference();
                        // Create a reference to store the image under images/user_uid/profile.jpg
                        UploadTask uploadTask = storageRef.child("images/" + auth.getCurrentUser().getUid().toString() + "/profile.jpg").putFile(imageURI);

                        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                //show the profile photo
                                storageRef.child("images/" + auth.getCurrentUser().getUid().toString() + "/profile.jpg").getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                    @Override
                                    public void onSuccess(byte[] bytes) {
                                        // Use the bytes to display the image
                                        ImageView profileImage = (ImageView) findViewById(R.id.profileImage);
                                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                        profileImage.setImageBitmap(bitmap);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        // Handle any errors
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });
                    }
                }
            });

    public void openSomeActivityForResult() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        someActivityResultLauncher.launch(intent);
    }

    //inserts a profile image
    public void buttonInsert(View view) {

        System.out.println("Button Insert!");

        //open the file explorer
        openSomeActivityForResult();

    }

    //deletes profile image
    public void buttonDelete(View view){

        System.out.println("Button Remove!");

        int userID = getSharedPreferences("Login", MODE_PRIVATE).getInt("id", -1);

        // Create a reference to the file to delete
        StorageReference storageRef = storage.getReference();
        StorageReference desertRef = storageRef.child("images/" + auth.getCurrentUser().getUid().toString() + "/profile.jpg");

        // Delete the file
        desertRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // File deleted successfully
                //after removing the image from the DB, need to show the default image
                ImageView profileImage = (ImageView) findViewById(R.id.profileImage);
                Drawable myDrawable = getResources().getDrawable(R.drawable.default_profile);
                profileImage. setImageDrawable(myDrawable);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Uh-oh, an error occurred!
            }
        });

    }

    //opens the edit info activity
    public void openEditInfo(View view){
        Intent intent = new Intent(this, EditUserInfoActivity.class);
        startActivity(intent);
    }

    private void replaceFragment(Fragment frag){
        FragmentManager fragmentManager=getSupportFragmentManager();
        FragmentTransaction transaction=fragmentManager.beginTransaction();
        transaction.replace(R.id.fl_wrapper2, frag);
        transaction.commit();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_current_user_info);

        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        db = FirebaseFirestore.getInstance();

        /**
         * nav bar
         */
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavigationView2);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId ()) {
                case R.id.ic_home:
                    System.out.println("home");
                    replaceFragment(new HomeFragment());
                    break;
                case R.id.ic_profile:
                    System.out.println("profile");
                    replaceFragment(new com.example.easyteamup.ProfileFragment());
                    break;

            }

            return true;
        });





        // set the profile image (TBD)

        //set the username and full name
        //get the event document
        DocumentReference docRef = db.collection("Users").document(auth.getCurrentUser().getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        //set the username
                        TextView username = (TextView) findViewById(R.id.username);
                        username.setText(document.getString("username"));

                        //set the full name
                        TextView fullName = (TextView) findViewById(R.id.fullName);
                        fullName.setText(document.getString("fName") + " " + document.getString("lName"));
                    }
                }
            }
        });

        //show the profile photo
        StorageReference storageRef = storage.getReference();
        storageRef.child("images/" + auth.getCurrentUser().getUid().toString() + "/profile.jpg").getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                // Use the bytes to display the image
                ImageView profileImage = (ImageView) findViewById(R.id.profileImage);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                profileImage.setImageBitmap(bitmap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                //the file was not found???
                ImageView profileImage = (ImageView) findViewById(R.id.profileImage);
                Drawable myDrawable = getResources(). getDrawable(R.drawable.default_profile);
                profileImage. setImageDrawable(myDrawable);
            }
        });
    }
}