package com.banksystem.entity;

import java.sql.Timestamp;

public class E_bankAccount {
    private String account;
    private String e_bank_account;
    private String password;
    private Timestamp update_time;

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getE_bank_account() {
        return e_bank_account;
    }

    public void setE_bank_account(String e_bank_account) {
        this.e_bank_account = e_bank_account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Timestamp getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(Timestamp update_time) {
        this.update_time = update_time;
    }
}
