package com.example.openchatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

public class OTPAuthentication extends AppCompatActivity {
    TextView mChangeNumber;
    EditText mGetOTP;
    android.widget.Button mVerifyOTP;
    String enteredOTP;

    FirebaseAuth firebaseAuth;
    ProgressBar mProgressBarOfOTPAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otpauthentication);

        mChangeNumber = findViewById(R.id.changenumber);
        mVerifyOTP = findViewById(R.id.verifyOTP);
        mGetOTP = findViewById(R.id.getOTP);
        mProgressBarOfOTPAuth = findViewById(R.id.progressbarofOTPauth);

        firebaseAuth = FirebaseAuth.getInstance();

        mChangeNumber.setOnClickListener(view -> {
            Intent intent = new Intent(OTPAuthentication.this, MainActivity.class);
            startActivity(intent);
        });

        mVerifyOTP.setOnClickListener(view -> {
            enteredOTP = mGetOTP.getText().toString();

            if (enteredOTP.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Enter Your OTP First", Toast.LENGTH_SHORT).show();

            } else {
                mProgressBarOfOTPAuth.setVisibility(View.VISIBLE);
                String codeReceived = getIntent().getStringExtra("OTP"); // Code received by Firebase
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(codeReceived, enteredOTP);
                signInWithPhoneAuthCredential(credential);
            }
        });
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Send the user to SetProfile
                mProgressBarOfOTPAuth.setVisibility(View.INVISIBLE);
                Toast.makeText(getApplicationContext(), "Login Successful", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(OTPAuthentication.this, SetProfile.class);
                startActivity(intent);
                finish();
            }
            else {
                if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(getApplicationContext(), "Login Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
        Toast.makeText(getApplicationContext(),"Verifying code Automatically",Toast.LENGTH_SHORT).show();
        firebaseAuth.signInWithCredential(phoneAuthCredential);
    }
}