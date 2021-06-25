package com.banksystem.entity;

import org.omg.PortableInterceptor.INACTIVE;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.HashMap;

public class DebitSubAccount {
    private String account;
    private String sub_account="";
    private Timestamp create_time;
    private Timestamp update_time;
    private Double amount=0.0;
    private Double interest=0.0;
    private String currency="";
    private String saving_type="";
    private Integer deposit_time=0;
    private Integer renew_time=0;
    private static String[] currency_table = new String[]{"","CNY","USD","HKD","JPY","EUR"};
    private static String[] saving_type_table = new String[]{"","Current Deposit","Lump-sum Time Deposit","Consolidated Time &Savings"};
    public String[] getCurrency_table() {
        return currency_table;
    }
    public int getCurrencyIndex(){
        for(int i=0;i<currency_table.length;i++) if(currency_table[i].equals(this.getCurrency())) return i;
        return 0;
    }
    public int getSavingTypeIndex(){
        //System.out.println(this.getSaving_type());
        for(int i=0;i<saving_type_table.length;i++) if(saving_type_table[i].equals(this.getSaving_type())) return i;
        return 0;
    }
    public String[] getSaving_type_table() {
        return saving_type_table;
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
    public String getCurrency(){
        if(sub_account=="") return currency;
        return currency_table[Integer.parseInt(sub_account.substring(0,1))];
    }
    public String getSaving_type(){
        if(sub_account=="") return saving_type;
        return saving_type_table[Integer.parseInt(sub_account.substring(1,2))];
    }
    public void setCurrency(String currency) {
        if(currency.length() > 1)
            this.currency = currency;
        else
            this.currency = currency_table[Integer.parseInt(currency)];
    }

    public void setSaving_type(String saving_type) {
        if(saving_type.length() > 1)
            this.saving_type = saving_type;
        else
            this.saving_type = saving_type_table[Integer.parseInt(saving_type)];
    }

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

    public Double getInterest() {
        return interest;
    }

    public void setInterest(Double interst) {
        this.interest = interst;
    }
    public String toString(){
        return this.getAccount() + " " + this.getSub_account() + " " + this.getCurrency() + " " +this.getAmount() + " " + this.getInterest()
                + " " + this.getSaving_type() +" " + this.getDeposit_time() + " " + this.getRenew_time();
    }

    @Override
    public boolean equals(Object obj) {
        return account == ((DebitSubAccount)obj).getAccount() & sub_account == (((DebitSubAccount) obj).getSub_account());
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
    public long myHashCode(){
        return Long.parseLong(account+sub_account);
    }
}
