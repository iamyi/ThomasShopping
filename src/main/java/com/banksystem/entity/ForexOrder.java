package com.banksystem.entity;

import java.sql.Timestamp;

public class ForexOrder {
    private String account;
    private int order_type;
    private String sell_currency;
    private String buy_currency;
    private double amount;
    private Double low_price;
    private Double high_price;
    private String addition;
    private Timestamp create_time;

    public Timestamp getCreate_time() {
        return create_time;
    }

    public void setCreate_time(Timestamp create_time) {
        this.create_time = create_time;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public int getOrder_type() {
        return order_type;
    }

    public void setOrder_type(int order_type) {
        this.order_type = order_type;
    }

    public String getSell_currency() {
        return sell_currency;
    }

    public void setSell_currency(String sell_currency) {
        this.sell_currency = sell_currency;
    }

    public String getBuy_currency() {
        return buy_currency;
    }

    public void setBuy_currency(String buy_currency) {
        this.buy_currency = buy_currency;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Double getLow_price() {
        return low_price;
    }

    public void setLow_price(Double low_price) {
        this.low_price = low_price;
    }

    public Double getHigh_price() {
        return high_price;
    }

    public void setHigh_price(Double high_price) {
        this.high_price = high_price;
    }

    public String getAddition() {
        return addition;
    }

    public void setAddition(String addition) {
        this.addition = addition;
    }
}
