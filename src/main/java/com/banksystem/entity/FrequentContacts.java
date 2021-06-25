package com.banksystem.entity;
import java.sql.Timestamp;

public class FrequentContacts {
    private String account="";
    private String contact="";
    private String contactName="";
    private int count=0;
    private Timestamp updateTime;

    public String getContactName(){
        return contactName;
    }
    public void setContactName(String contactName){
        this.contactName=contactName;
    }
    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "FrequentContacts{" +
                "account='" + account + '\'' +
                ", contact='" + contact + '\'' +
                ", count=" + count +
                ", updateTime=" + updateTime +
                '}';
    }

}
