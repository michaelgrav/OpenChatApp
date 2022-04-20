package com.example.openchatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.*;
import com.google.firebase.auth.PhoneAuthOptions;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    EditText mGetPhoneNumber;
    android.widget.Button mSendOTP;
    CountryCodePicker mCountryCodePicker;
    String countryCode;
    String phoneNumber;

    FirebaseAuth firebaseAuth;
    ProgressBar mProgressBarOfMain;

    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBacks;
    String codeSent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Assign XML IDs to Java IDs
        mCountryCodePicker = findViewById(R.id.countrycodepicker);
        mSendOTP = findViewById(R.id.sendOTPbutton);
        mGetPhoneNumber = findViewById(R.id.getphonenumber);
        mProgressBarOfMain = findViewById(R.id.progressbarofmain);

        // Get the instance of the current user
        firebaseAuth = FirebaseAuth.getInstance();

        // Firebase requires a plus
        countryCode = mCountryCodePicker.getSelectedCountryCodeWithPlus();

        // If the user wants to change their country code
        mCountryCodePicker.setOnCountryChangeListener(() -> countryCode = mCountryCodePicker.getSelectedCountryCodeWithPlus());

        mSendOTP.setOnClickListener(view -> {
            String number; // Number for the OTP

            number = mGetPhoneNumber.getText().toString();

            // If the user does not input their number into the field
            if (number.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Please Enter Your Number", Toast.LENGTH_SHORT).show();
            }
            // The user did not enter enough digits
            else if (number.length() < 10) {
                Toast.makeText(getApplicationContext(), "Please Enter The Correct Number", Toast.LENGTH_SHORT).show();
            }
            // If everything is fine, send the OTP
            else {
                mProgressBarOfMain.setVisibility(View.VISIBLE);
                phoneNumber = countryCode + number;

                // Pass phone number?
                PhoneAuthOptions options = PhoneAuthOptions.newBuilder(firebaseAuth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(MainActivity.this)
                        .setCallbacks(mCallBacks)
                        .build();

                PhoneAuthProvider.verifyPhoneNumber(options);
            }
        });

        // Check if number is correct
        mCallBacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                // How to automatically fetch the OTP
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {

            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken); // Send the code
                Toast.makeText(getApplicationContext(), "OTP Sent!", Toast.LENGTH_SHORT).show();
                mProgressBarOfMain.setVisibility(View.INVISIBLE);

                codeSent = s; // Store the OTP which is sent by Firebase
                Intent intent = new Intent(MainActivity.this, OTPAuthentication.class);
                intent.putExtra("OTP", codeSent);
                startActivity(intent);
            }
        };
    }

    // See the user already has signed in
    @Override
    protected void onStart() {
        super.onStart();

        // If they have, send them to the chat screen
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            Intent intent = new Intent(MainActivity.this, ChatActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }
}