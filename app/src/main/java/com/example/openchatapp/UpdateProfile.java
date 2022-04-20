package com.example.openchatapp;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class UpdateProfile extends AppCompatActivity {
    private EditText mNewUserName;
    private FirebaseAuth firebaseAuth;

    private FirebaseFirestore firebaseFirestore;

    private ImageView mGetNewImageInImageView;

    private StorageReference storageReference;

    private String imageURIAccessToken;

    private ProgressBar mProgressBarOfUpdateProfile;

    private Uri imagepath;
    Intent intent;
    String newName;
    ActivityResultLauncher<String> mGetContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        androidx.appcompat.widget.Toolbar mToolbarOfUpdateProfile = findViewById(R.id.toolbarofupdateprofile);
        ImageButton mBackButtonOfUpdateProfile = findViewById(R.id.backbuttonofupdateprofile);
        mGetNewImageInImageView = findViewById(R.id.getnewuserimageinimageview);
        mProgressBarOfUpdateProfile = findViewById(R.id.progressbarofupdateprofile);
        mNewUserName = findViewById(R.id.getnewusername);
        android.widget.Button mUpdateProfileButton = findViewById(R.id.updateprofilebutton);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();

        intent = getIntent();
        setSupportActionBar(mToolbarOfUpdateProfile);

        mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
            mGetNewImageInImageView.setImageURI(result);
            imagepath = result;
        });

        mGetNewImageInImageView.setOnClickListener(view -> {
            // Open a picker to choose the profile picture
            mGetContent.launch("image/*");
        });

        mBackButtonOfUpdateProfile.setOnClickListener(view -> finish());

        mNewUserName.setText(intent.getStringExtra("nameOfUser"));

        DatabaseReference databaseReference = firebaseDatabase.getReference(Objects.requireNonNull(firebaseAuth.getUid()));
        mUpdateProfileButton.setOnClickListener(view -> {
            newName = mNewUserName.getText().toString();
            if (newName.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Name Not Updated", Toast.LENGTH_SHORT).show();
            } else if (imagepath != null) { // Not updating picture
                mProgressBarOfUpdateProfile.setVisibility(View.VISIBLE);
                UserProfile mUserProfile = new UserProfile(newName, firebaseAuth.getUid());
                databaseReference.setValue(mUserProfile);

                updateImageToStorage();

                Toast.makeText(getApplicationContext(), "Profile Updated", Toast.LENGTH_SHORT).show();
                mProgressBarOfUpdateProfile.setVisibility(View.INVISIBLE);
                Intent intent = new Intent(UpdateProfile.this, ChatActivity.class);
                startActivity(intent);
                finish();

            } else { // Updating both
                mProgressBarOfUpdateProfile.setVisibility(View.VISIBLE);
                UserProfile mUserProfile = new UserProfile(newName, firebaseAuth.getUid());
                databaseReference.setValue(mUserProfile);
                updateNameOnCloudFirestore();
                Toast.makeText(getApplicationContext(), "Profile Updated", Toast.LENGTH_SHORT).show();

                mProgressBarOfUpdateProfile.setVisibility(View.INVISIBLE);
                Intent intent = new Intent(UpdateProfile.this, ChatActivity.class);
                startActivity(intent);
                finish();
            }
        });

        storageReference = firebaseStorage.getReference();
        storageReference.child("Images").child(firebaseAuth.getUid()).child("Profile Pic").getDownloadUrl().addOnSuccessListener(uri -> {
            imageURIAccessToken = uri.toString();
            Picasso.get().load(uri).into(mGetNewImageInImageView);
        });


    }

    private void updateNameOnCloudFirestore() {
        DocumentReference documentReference = firebaseFirestore.collection("Users").document(Objects.requireNonNull(firebaseAuth.getUid()));
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", newName);
        userData.put("image", imageURIAccessToken);
        userData.put("uid", firebaseAuth.getUid());
        userData.put("status", "Online");


        documentReference.set(userData).addOnSuccessListener(unused -> Toast.makeText(getApplicationContext(), "Profile Updated Successfully", Toast.LENGTH_LONG).show());
    }

    private void updateImageToStorage() {
        StorageReference imageRef = storageReference.child("Images").child(Objects.requireNonNull(firebaseAuth.getUid())).child("Profile Pic");

        //Image Compression
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imagepath);
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
                imageURIAccessToken = uri.toString();
                Toast.makeText(getApplicationContext(), "URI Get Success", Toast.LENGTH_LONG).show();
                updateNameOnCloudFirestore();
            }).addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "URI Get Failed", Toast.LENGTH_LONG).show());

            Toast.makeText(getApplicationContext(), "Image Is Updated", Toast.LENGTH_LONG).show();
        }).addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "Image Not Updated (Compression Failed)", Toast.LENGTH_LONG).show());


    }

    // Application closed
    @Override
    protected void onStop() {
        super.onStop();
        DocumentReference documentReference = firebaseFirestore.collection("Users").document(Objects.requireNonNull(firebaseAuth.getUid()));
        documentReference.update("status", "Offline").addOnSuccessListener(unused -> Toast.makeText(getApplicationContext(), "User is offline", Toast.LENGTH_SHORT).show());
    }

    // Application opened
    @Override
    protected void onStart() {
        super.onStart();
        DocumentReference documentReference = firebaseFirestore.collection("Users").document(Objects.requireNonNull(firebaseAuth.getUid()));
        documentReference.update("status", "Online").addOnSuccessListener(unused -> Toast.makeText(getApplicationContext(), "User is online", Toast.LENGTH_SHORT).show());
    }
}