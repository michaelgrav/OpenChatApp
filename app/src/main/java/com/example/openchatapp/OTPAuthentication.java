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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
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

        mChangeNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(OTPAuthentication.this, MainActivity.class);
                startActivity(intent);
            }
        });

        mVerifyOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enteredOTP = mGetOTP.getText().toString();

                if (enteredOTP.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Enter Your OTP First", Toast.LENGTH_SHORT).show();

                } else {
                    mProgressBarOfOTPAuth.setVisibility(View.VISIBLE);
                    String codeReceived = getIntent().getStringExtra("OTP"); // Code received by Firebase
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(codeReceived, enteredOTP);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Send the user to SetProfile
                    mProgressBarOfOTPAuth.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(), "Login Successful", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(OTPAuthentication.this, SetProfile.class);
                    finish();
                }
                else {
                    if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(getApplicationContext(), "Login Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}