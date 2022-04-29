package com.example.openchatapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;

public class ChatFragment extends Fragment {
    LinearLayoutManager linearLayoutManager;
    public static final String TAG = "ChatFragment.class";
    RecyclerView mRecyclerView;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chatfragment, container, false);
        mRecyclerView = view.findViewById(R.id.recyclerview);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FirestoreRecyclerAdapter<FirebaseModel, NoteViewHolder> chatAdapter;

        Log.i(TAG, "onViewCreated: Created");
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

        // Get all users except the current one
        Query query = firebaseFirestore.collection("Users").whereNotEqualTo("uid", firebaseAuth.getUid());
        FirestoreRecyclerOptions<FirebaseModel> allUserName = new FirestoreRecyclerOptions
                .Builder<FirebaseModel>()
                .setQuery(query, FirebaseModel.class)
                .setLifecycleOwner(this)
                .build();

        chatAdapter = new FirestoreRecyclerAdapter<FirebaseModel, NoteViewHolder>(allUserName) {
            @Override
            protected void onBindViewHolder(@NonNull NoteViewHolder noteViewHolder, int i, @NonNull FirebaseModel firebaseModel) {
                Log.i(TAG, "onBindViewHolder: " + firebaseModel.getName());
                Log.i(TAG, "onBindViewHolder index: " + i);
                noteViewHolder.setUserName(firebaseModel.getName());
                noteViewHolder.renderImage(firebaseModel.getImage());

                // User is online
                if (firebaseModel.getStatus().equalsIgnoreCase("Online")) {
                    noteViewHolder.setStatus(firebaseModel.getStatus(), Color.GREEN);
                } else { // User is offline
                    noteViewHolder.setStatus(firebaseModel.getStatus(), Color.RED);
                }

                // User clicks on chat
                noteViewHolder.itemView.setOnClickListener(view1 -> {
                    Intent intent = new Intent(getActivity(), SpecificChat.class);
                    intent.putExtra("name", firebaseModel.getName());
                    intent.putExtra("recieveruid", firebaseModel.getUid());
                    intent.putExtra("imageuri", firebaseModel.getImage());
                    startActivity(intent);
                });
            }

            @NonNull
            @Override
            public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chatviewlayout, parent, false);
                return new NoteViewHolder(view);
            }
        };

        mRecyclerView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(chatAdapter);
    }



    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        private final TextView particularUserName;
        private final TextView statusOfUser;
        private final ImageView mImageViewOfUser;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);

            particularUserName = itemView.findViewById(R.id.nameofuser);
            statusOfUser = itemView.findViewById(R.id.statusofuser);
            mImageViewOfUser = itemView.findViewById(R.id.imageviewofuser);
        }

        public void renderImage(String uri) {
            Picasso.get().load(uri).into(mImageViewOfUser);
        }

        public void setUserName(String name) {
            this.particularUserName.setText(name);
        }

        public void setStatus(String status, int color) {
            this.statusOfUser.setText(status);
            this.statusOfUser.setTextColor(color);
        }
    }
}
