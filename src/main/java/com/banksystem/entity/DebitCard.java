package com.banksystem.entity;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.HashMap;

public class DebitCard {
    private String account;
    private String name;
    private String id_card;
    private HashMap<String,String> address;
    private String phone;

    private String password;
    private boolean e_bank;
    private boolean loss;
    private Timestamp create_time;
    private Timestamp update_time;
    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId_card() {
        return id_card;
    }

    public HashMap<String, String> getAddress() {
        return address;
    }
    public void setAddress(HashMap<String, String> address) {
        this.address = address;
    }

    public void setId_card(String id_card) {
        this.id_card = id_card;
    }


    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isE_bank() {
        return e_bank;
    }

    public void setE_bank(boolean e_bank) {
        this.e_bank = e_bank;
    }

    public boolean isLoss() {
        return loss;
    }

    public void setLoss(boolean loss) {
        this.loss = loss;
    }


    public Timestamp getCreate_time() {
        return create_time;
    }

    public void setCreate_time(Timestamp create_time) {
        this.create_time = create_time;
    }

    public Timestamp getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(Timestamp update_time) {
        this.update_time = update_time;
    }
}

