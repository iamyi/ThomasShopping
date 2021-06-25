package com.banksystem.service;

import com.banksystem.entity.DebitCard;
import com.banksystem.entity.DebitSubAccount;
import com.banksystem.entity.InterestRate;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;

@Service
@EnableAutoConfiguration
public class DebitSubAccountService {
    @Autowired
    JdbcTemplate jdbc;
    @Autowired
    SavingService savingService;
    @Autowired
    InterestRateService interestRateService;
    @Autowired
    DebitCardService debitCardService;
    public void deleteByAccountAndSubAccount(String account,String sub_account){
        String sql = "delete from debit_sub_accounts where account regexp \"%s\" and sub_account regexp \"%s\"";
        sql = String.format(sql,account,sub_account);
        jdbc.execute(sql);
    }
    public void updateSubAccount(String account,String sub_account,DebitSubAccount subAccount){
        String sql = "update debit_sub_accounts set amount=%f,interest=%f,update_time=%s where account regexp \"%s\" and sub_account regexp \"%s\"";
        sql = String.format(sql,subAccount.getAmount(),subAccount.getInterest(),subAccount.getUpdate_time());
        jdbc.update(sql);
    }
    public List<DebitSubAccount> findByAccount(String account){
        String sql = String.format("select * from debit_sub_accounts where account regexp \"%s\" order by create_time desc",account);
        return (List<DebitSubAccount>) jdbc.query(sql,new BeanPropertyRowMapper<>(DebitSubAccount.class));
    }
    public List<DebitSubAccount> findByAccountAndSubAccount(String account,String sub_account){
        String sql = String.format("select * from debit_sub_accounts where account regexp \"%s\" and sub_account regexp \"%s\" order by sub_account desc,update_time desc",account,sub_account);
        return (List<DebitSubAccount>)jdbc.query(sql,new BeanPropertyRowMapper<>(DebitSubAccount.class));
    }
    public String checkDepositAmount(DebitSubAccount subAccount,String name){
        List<DebitCard> list = debitCardService.finalByAccount(subAccount.getAccount());
        if(list.size() == 0){
            return error_message_map.get(ACCOUNT_NOT_EXIT);
        }
        if(!list.get(0).getName().equals(name)){
            return error_message_map.get(WRONG_NAME);
        }
        if(debitCardService.accountIsLoss(subAccount.getAccount())){
            return "ACCOUNT IS LOSS";
        }
        return "DEPOSIT SUCCESS";
    }
    public DebitSubAccount depositAmount(DebitSubAccount subAccount,String detail){
        String currency = subAccount.getCurrency();
        String saving_type = subAccount.getSaving_type();
        String sub_account = "";
        sub_account += new Integer(subAccount.getCurrencyIndex()).toString();
        sub_account += new Integer(subAccount.getSavingTypeIndex()).toString();

        List<DebitSubAccount> list = findByAccountAndSubAccount(subAccount.getAccount(),"^"+sub_account+".*");
        System.out.println("^"+sub_account+".*");
        System.out.println(list.size());
        Double transaction_amount = subAccount.getAmount();
        if(list.size() == 0 || list.get(0).getSavingTypeIndex() != 1 ){ // Create New Account
            System.out.println("CREATE_NEW_ACCOUNT");
            if(list.size() == 0)
            sub_account += "000";
            else{
                sub_account = new Integer(Integer.parseInt(list.get(0).getSub_account()) +1 ).toString();
            }
            subAccount.setSub_account(sub_account);
            String sql = "insert into debit_sub_accounts values(?,?,?,?,?,?,?,?)";
            jdbc.update(sql,new Object[]{subAccount.getAccount(),
            subAccount.getSub_account(),subAccount.getAmount(),0,TimeService.getNowTime(),TimeService.getNowTime(),
            subAccount.getDeposit_time(),subAccount.getRenew_time()});
        }
        else{
            System.out.println("UPDATE ACCOUNT");
            DebitSubAccount tmp_account = list.get(0);
            Double tmp_amount = tmp_account.getAmount() + subAccount.getAmount();
            tmp_account.setInterest(subAccount.getInterest());
            subAccount.setAmount(tmp_amount);
            subAccount = tmp_account;
            subAccount.setAmount(tmp_amount);
            String sql = String.format("update debit_sub_accounts set amount = %f,interest = %f,update_time=\"%s\" where account=\"%s\" and sub_account=\"%s\"",subAccount.getAmount(),subAccount.getInterest(),TimeService.getNowTime(),subAccount.getAccount(),subAccount.getSub_account());
            jdbc.update(sql);
        }
        subAccount.setCreate_time(TimeService.getSetTime());
        subAccount.setUpdate_time(TimeService.getSetTime());
        savingService.createTransaction(subAccount.getAccount(),subAccount.getSub_account(),transaction_amount,detail);
        return subAccount;
    }
    public static final int DRAW_AMOUNT_SUCCESS = 0;
    public static final int ACCOUNT_NOT_EXIT = 1;
    public static final int AMOUNT_NOT_ENOUGH = 2;
    public static final int DRAW_TOO_MANY_TIMES = 3;
    public static final int WRONG_NAME = 4;
    public static final int WRONG_PASSWORD = 5;
    public static final int ACCOUNT_IS_LOSS = 6;
    public static HashMap<Integer,String> error_message_map = new HashMap<Integer, String>(){
        {
            put(0, "DRAW_AMOUNT_SUCCESS");
            put(1,"ACCOUNT_NOT_EXIT");
            put(2,"DRAW_LARGER_THAN_DEPOSIT");
            put(3,"DRAW_TOO_MANY_TIMES");
            put(4,"WRONG_NAME");
            put(5,"WRONG_PASSWORD");
            put(ACCOUNT_IS_LOSS,"ACCOUNT_IS_LOSS");
        }
    };
    public int checkDrawAmount(String account,String sub_account,Double amount,String password){
        int ERROR_CODE = DRAW_AMOUNT_SUCCESS;
        List<DebitSubAccount> list = this.findByAccountAndSubAccount(account,sub_account);
        if(list.size() == 0){
            return ACCOUNT_NOT_EXIT;
        }
        if(amount > list.get(0).getAmount()){
            return AMOUNT_NOT_ENOUGH;
        }
        if(list.get(0).getSavingTypeIndex() == 2 ){
            if(!list.get(0).getCreate_time().equals(list.get(0).getUpdate_time()))
                return DRAW_TOO_MANY_TIMES;
        }
        List<DebitCard> debitCardList = debitCardService.finalByAccount(account);
        if(debitCardList.size() == 0){
            return ACCOUNT_NOT_EXIT;
        }
        System.out.println(password + debitCardList.get(0).getPassword());
        if(!BCrypt.checkpw(password,debitCardList.get(0).getPassword())){
            return WRONG_PASSWORD;
        }
        if(debitCardService.accountIsLoss(account)){
            return ACCOUNT_IS_LOSS;
        }
        return DRAW_AMOUNT_SUCCESS;
    }
    public String drawAmount(String account,String sub_account,Double amount,String password,String detail){
        int ERROR_CODE = checkDrawAmount(account,sub_account,amount,password);
        String error_message = error_message_map.get(ERROR_CODE);
        if(ERROR_CODE != DRAW_AMOUNT_SUCCESS){
            return error_message;
        }
        List<DebitSubAccount> list = this.findByAccountAndSubAccount(account,sub_account);
        DebitSubAccount subAccount = list.get(0);
        if(subAccount.getSavingTypeIndex() == 3){
            interestRateService.transferInterrest(subAccount.getAccount(),subAccount.getSub_account());
            amount = subAccount.getAmount();
        }
        if(subAccount.getSavingTypeIndex() == 2){
            InterestRate now_rate = interestRateService.findInterestRateByType("0").get(0);
            Double interest = subAccount.getInterest();
            amount = interest + amount*(TimeService.system_time - subAccount.getCreate_time().getTime())/(24.0*60*60*1000)/365*now_rate.getRate();
        }
        String sql = String.format("update debit_sub_accounts set amount=%f,update_time='%s' where account='%s\' and sub_account='%s' ",subAccount.getAmount()-amount,TimeService.getNowTime(),subAccount.getAccount(),subAccount.getSub_account());
        jdbc.update(sql);
        savingService.createTransaction(account,sub_account,amount*-1,detail);
        return error_message;
    }
    public String drawAmountNoPSD(String account,String sub_account,Double amount,String detail){

        List<DebitSubAccount> list = this.findByAccountAndSubAccount(account,sub_account);
        DebitSubAccount subAccount = list.get(0);
        if(subAccount.getSavingTypeIndex() == 3){
            interestRateService.transferInterrest(subAccount.getAccount(),subAccount.getSub_account());
            amount = subAccount.getAmount();
        }
        if(subAccount.getSavingTypeIndex() == 2){
            InterestRate now_rate = interestRateService.findInterestRateByType("0").get(0);
            Double interest = subAccount.getInterest();
            amount = interest + amount*(TimeService.system_time - subAccount.getCreate_time().getTime())/(24.0*60*60*1000)/365*now_rate.getRate();
        }
        String sql = String.format("update debit_sub_accounts set amount=%f,update_time='%s' where account='%s\' and sub_account='%s' ",subAccount.getAmount()-amount,TimeService.getNowTime(),subAccount.getAccount(),subAccount.getSub_account());
        jdbc.update(sql);
        savingService.createTransaction(account,sub_account,amount*-1,detail);
        return "1";
    }
}
