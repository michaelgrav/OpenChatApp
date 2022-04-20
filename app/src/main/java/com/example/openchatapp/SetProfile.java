package com.example.openchatapp;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SetProfile extends AppCompatActivity {
    private ImageView mGetUserImageInImageView;
    private Uri imagePath;

    private EditText mGetUserName;

    private FirebaseAuth firebaseAuth;
    private String name;

    private StorageReference storageReference;

    private String ImageUriAccessToken;

    private FirebaseFirestore firebaseFirestore;

    ProgressBar mProgressBarOfSetProfile;
    ActivityResultLauncher<String> mGetContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_profile);

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();

        mGetUserName = findViewById(R.id.getusername);
        CardView mGetUserImage = findViewById(R.id.getuserimage);
        mGetUserImageInImageView = findViewById(R.id.getuserimageinimageview);
        android.widget.Button mSaveProfile = findViewById(R.id.saveProfile);
        mProgressBarOfSetProfile = findViewById(R.id.progressbarofsetprofile);

        
        mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
            mGetUserImageInImageView.setImageURI(result);
            imagePath = result;
        });

        mGetUserImage.setOnClickListener(view -> {
            // Open a picker to choose the profile picture
            mGetContent.launch("image/*");
        });

        mSaveProfile.setOnClickListener(view -> {
            name = mGetUserName.getText().toString();
            if (name.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Name Is Empty", Toast.LENGTH_LONG).show();
            } else if (imagePath == null) {
                Toast.makeText(getApplicationContext(), "Image Is Empty", Toast.LENGTH_LONG).show();
            } else { // Set the data on our database
                mProgressBarOfSetProfile.setVisibility(View.VISIBLE);
                sendDataForNewUser();
                mProgressBarOfSetProfile.setVisibility(View.INVISIBLE);
                Intent intent = new Intent(SetProfile.this, ChatActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    // All Firebase code
    private void sendDataForNewUser() {
        sendDataToRealTimeDataBase();
    }

    private void sendDataToRealTimeDataBase() {
        name = mGetUserName.getText().toString().trim();

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference(Objects.requireNonNull(firebaseAuth.getUid()));

        UserProfile mUserProfile = new UserProfile(name, firebaseAuth.getUid());
        databaseReference.setValue(mUserProfile); //Save the profile on the database
        sendImageToStorage();
    }

    private void sendImageToStorage() {
        StorageReference imageRef = storageReference.child("Images").child(Objects.requireNonNull(firebaseAuth.getUid())).child("Profile Pic");

        //Image Compression
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imagePath);
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "ERROR GETTING IMAGE FOR COMPRESSION", Toast.LENGTH_LONG).show();
            e.printStackTrace(); // Print error
        }

        // Compress the image
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 25, byteArrayOutputStream);
        byte[] data = byteArrayOutputStream.toByteArray();

        // Put the image on storage
        UploadTask uploadTask = imageRef.putBytes(data);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                ImageUriAccessToken = uri.toString();
                Toast.makeText(getApplicationContext(), "URI Get Success", Toast.LENGTH_LONG).show();
                sendDataToCloudFirestore();
            }).addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "URI Get Failed", Toast.LENGTH_LONG).show());

            Toast.makeText(getApplicationContext(), "Image Is Uploaded", Toast.LENGTH_LONG).show();
        }).addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "Image Not Uploaded (Compression Failed)", Toast.LENGTH_LONG).show());
    }

    private void sendDataToCloudFirestore() {
        DocumentReference documentReference = firebaseFirestore.collection("Users").document(Objects.requireNonNull(firebaseAuth.getUid()));
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("image", ImageUriAccessToken);
        userData.put("uid", firebaseAuth.getUid());
        userData.put("status", "Online");

        documentReference.set(userData).addOnSuccessListener(unused -> Toast.makeText(getApplicationContext(), "Data Sent Successfully To Firestore", Toast.LENGTH_LONG).show());
    }
}

