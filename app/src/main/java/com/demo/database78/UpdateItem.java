package com.demo.database78;

public class UpdateItem {
    private String message;
    private String timestamp;

    public UpdateItem(String message, String timestamp) {
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getMessage() { return message; }
    public String getTimestamp() { return timestamp; }
}