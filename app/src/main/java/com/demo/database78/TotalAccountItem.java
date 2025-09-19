package com.demo.database78;

public class TotalAccountItem {
    private String totalAmount;
    private String currency;
    private String timestamp;

    public TotalAccountItem(String totalAmount, String currency, String timestamp) {
        this.totalAmount = totalAmount;
        this.currency = currency;
        this.timestamp = timestamp;
    }

    public String getTotalAmount() { return totalAmount; }
    public String getCurrency() { return currency; }
    public String getTimestamp() { return timestamp; }
}