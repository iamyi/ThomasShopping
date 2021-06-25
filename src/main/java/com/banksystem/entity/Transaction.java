package com.banksystem.entity;

import java.security.UnresolvedPermission;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class Transaction {
    private String account;
    private String sub_account;
    private Double amount;
    private String detail;
    private Timestamp update_time;
    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }



    public String getSub_account() {
        return sub_account;
    }

    public void setSub_account(String sub_account) {
        this.sub_account = sub_account;
    }

    public Double getAmount() {
        return amount;
    }

    public Timestamp getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(Timestamp update_time) {
        System.out.println(update_time);
        this.update_time = update_time;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }


    public String getRealUpdate_time(){
        //System.out.println(update_time.toString().substring(0,update_time.toString().length()-2));
        return update_time.toString().substring(0,update_time.toString().length()-2);
    }


    public String getAccount() {

        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }
}
