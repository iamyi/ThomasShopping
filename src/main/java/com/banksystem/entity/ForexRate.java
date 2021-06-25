package com.banksystem.entity;

import java.sql.Timestamp;

public class ForexRate {
    private String currency;



    private Integer level;
    private Double rate1;
    private Double rate2;
    private Timestamp update_time;
    private String[] currency_table = {"","CNY","USD","HKD","JPY","EUR"};

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }


    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Double getRate1() {
        return rate1;
    }

    public void setRate1(Double rate1) {
        this.rate1 = rate1;
    }

    public Double getRate2() {
        return rate2;
    }

    public void setRate2(Double rate2) {
        this.rate2 = rate2;
    }

    public Timestamp getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(Timestamp update_time) {
        this.update_time = update_time;
    }

    public String getCurrencyString(){
        return currency_table[currency.charAt(0)-'0'] + "/" + currency_table[currency.charAt(1)-'0'];
    }
}
