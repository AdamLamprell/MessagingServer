package com.lampsdesign;

import java.io.Serializable;

public class MessageObject implements Serializable {
    private String userName;
    private String dateTime;
    private String messageText;

    public MessageObject() {
    }

    public MessageObject(String userName, String dateTime, String messageText) {
        this.userName = userName;
        this.dateTime = dateTime;
        this.messageText = messageText;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    @Override
    public String toString() {
        return "Name: " + userName + " - Timestamp: " + dateTime + " - Message Text: " + messageText;
    }
}
