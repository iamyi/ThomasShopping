package com.banksystem.entity;

import java.sql.Timestamp;

public class ForexSubAccount {
    private String account;
    private String sub_account="";
    private Timestamp create_time;
    private Timestamp update_time;
    private Double amount=0.0;
    private String currency="";
    private Integer deposit_time=0;
    private Integer renew_time=0;

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getSub_account() {
        return sub_account;
    }

    public void setSub_account(String sub_account) {
        this.sub_account = sub_account;
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

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Integer getDeposit_time() {
        return deposit_time;
    }

    public void setDeposit_time(Integer deposit_time) {
        this.deposit_time = deposit_time;
    }

    public Integer getRenew_time() {
        return renew_time;
    }

    public void setRenew_time(Integer renew_time) {
        this.renew_time = renew_time;
    }
}
