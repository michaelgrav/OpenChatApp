package com.example.openchatapp;

// Things I want to pass to the database
public class UserProfile {
    public String userName, userUID;

    public UserProfile() {}

    public UserProfile(String userName, String userUID) {
        this.userName = userName;
        this.userUID = userUID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserUID() {
        return userUID;
    }

    public void setUserUID(String userUID) {
        this.userUID = userUID;
    }
}
