package com.banksystem.entity;

import java.sql.Date;

public class User {
    private String account;
    private String password;
    private Date update_time;

    public String getPassword() {
        return password;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Date getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(Date update_time) {
        this.update_time = update_time;
    }

}
