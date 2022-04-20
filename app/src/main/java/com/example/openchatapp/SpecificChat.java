package com.example.openchatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class SpecificChat extends AppCompatActivity {
    EditText mGetMessage;
    ImageButton mSendMessageButton;
    CardView mSendMessageCardView;
    androidx.appcompat.widget.Toolbar mToolBarOfSpecificChat;
    ImageView mImageViewOfSpecificUser;
    TextView mNameOfSpecificUser;

    private String enteredMessage;
    Intent intent;
    String mReceiverName, mReceiverUID, mSenderUID;
    private FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;

    String senderRoom, receiverRoom;
    ImageButton mBackButtonOfSpecificChat;

    RecyclerView mMessageRecyclerView;
    String currentTime;
    Calendar calendar;
    SimpleDateFormat simpleDateFormat;

    MessagesAdapter messagesAdapter;
    ArrayList<Messages> messagesArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specific_chat);

        mGetMessage = findViewById(R.id.getmessage);
        mSendMessageCardView = findViewById(R.id.cardviewofsendmessage);
        mSendMessageButton = findViewById(R.id.imageviewofsendmessage);
        mToolBarOfSpecificChat = findViewById(R.id.toolbarOfSpecificChat);
        mNameOfSpecificUser = findViewById(R.id.nameofspecificuser);
        mImageViewOfSpecificUser = findViewById(R.id.specificimageuserinimageview);
        mBackButtonOfSpecificChat = findViewById(R.id.backButtonOfSpecificChat);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        calendar = Calendar.getInstance();
        simpleDateFormat = new SimpleDateFormat("hh:mm a");

        messagesArrayList = new ArrayList<>();
        mMessageRecyclerView = findViewById(R.id.recyclerviewofspecificchat);

        LinearLayoutManager linearLayoutManager= new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        mMessageRecyclerView.setLayoutManager(linearLayoutManager);
        messagesAdapter = new MessagesAdapter(SpecificChat.this, messagesArrayList);
        mMessageRecyclerView.setAdapter(messagesAdapter);

        mSenderUID = firebaseAuth.getUid();
        mReceiverUID = getIntent().getStringExtra("recieveruid");
        mReceiverName = getIntent().getStringExtra("name");

        senderRoom = mSenderUID + mReceiverUID;
        receiverRoom = mReceiverUID + mSenderUID;

        intent = getIntent();
        setSupportActionBar(mToolBarOfSpecificChat);
        mToolBarOfSpecificChat.setOnClickListener(view -> Toast.makeText(getApplicationContext(), "Toolbar Is Clicked", Toast.LENGTH_SHORT).show());

        DatabaseReference databaseReference = firebaseDatabase.getReference().child("chats").child(senderRoom).child("messages");
        messagesAdapter = new MessagesAdapter(SpecificChat.this, messagesArrayList);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messagesArrayList.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Messages messages = snapshot1.getValue(Messages.class);
                    messagesArrayList.add(messages);
                }
                messagesAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // If back button is pressed, leave the chat
        mBackButtonOfSpecificChat.setOnClickListener(view -> finish());

        mNameOfSpecificUser.setText(mReceiverName);
        String uri = getIntent().getStringExtra("imageuri");
        if (uri.isEmpty()) {
            Toast.makeText(getApplicationContext(), "NULL IS RECEIVED", Toast.LENGTH_SHORT).show();
        } else {
            Picasso.get().load(uri).into(mImageViewOfSpecificUser);
        }

        mSendMessageButton.setOnClickListener(view -> {
            enteredMessage = mGetMessage.getText().toString();
            if (enteredMessage.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Enter Message First", Toast.LENGTH_SHORT).show();
            } else {
                Date date = new Date();
                currentTime = simpleDateFormat.format(calendar.getTime()); // Date for timestamp
                Messages messages = new Messages(enteredMessage, firebaseAuth.getUid(), date.getTime(), currentTime);
                firebaseDatabase = FirebaseDatabase.getInstance();
                firebaseDatabase.getReference().child("chats")
                            .child(senderRoom)
                            .child("messages")
                            .push().setValue(messages).addOnCompleteListener(task -> firebaseDatabase.getReference()
                                    .child("chats")
                                    .child(receiverRoom)
                                    .child("messages")
                                    .push()
                                    .setValue(messages).addOnCompleteListener(task1 -> {

                                    }));
                mGetMessage.setText(null);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        messagesAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (messagesAdapter != null) {
            messagesAdapter.notifyDataSetChanged();
        }
    }
}