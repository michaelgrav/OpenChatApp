package com.example.openchatapp;

public class Messages {
    String message;
    String senderID;
    long timeStamp;
    String currentTime;

    public Messages() {
    }

    public Messages(String message, String senderID, long timeStamp, String currentTime) {
        this.message = message;
        this.senderID = senderID;
        this.timeStamp = timeStamp;
        this.currentTime = currentTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderID() {
        return senderID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(String currentTime) {
        this.currentTime = currentTime;
    }


}
