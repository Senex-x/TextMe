package com.example.textme.FirebaseItems;

import java.util.Date;

public class Message {
    public String userName;
    public String bitmapPortrait;
    public String userEmail;
    public String textMessage;
    private long messageTime;

    public Message() {}

    public Message(String userName, String userEmail, String textMessage, String bitmapPortrait) {
        this.userName = userName;
        this.userEmail = userEmail;
        this.textMessage = textMessage;
        this.bitmapPortrait = bitmapPortrait;
        this.messageTime = new Date().getTime();
    }

    public Message(String userName, String userEmail, String textMessage, String bitmapPortrait, long messageTime) {
        this.userName = userName;
        this.userEmail = userEmail;
        this.textMessage = textMessage;
        this.bitmapPortrait = bitmapPortrait;
        this.messageTime = messageTime;

    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setBitmapPortrait(String bitmapPortrait) {
        this.bitmapPortrait = bitmapPortrait;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getBitmapPortrait() {
        return bitmapPortrait;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getTextMessage() {
        return textMessage;
    }

    public void setTextMessage(String textMessage) {
        this.textMessage = textMessage;
    }

    public long getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(long messageTime) {
        this.messageTime = messageTime;
    }
}
