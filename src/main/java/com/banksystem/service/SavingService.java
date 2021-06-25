package com.banksystem.service;

import com.banksystem.entity.DebitCard;
import com.banksystem.entity.DebitSubAccount;
import com.banksystem.entity.FrequentContacts;
import com.banksystem.entity.Transaction;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
@EnableAutoConfiguration
public class SavingService {
    @Autowired
    JdbcTemplate jdbc;
    @Autowired
    DebitCardService debitCardService;
    @Autowired
    DebitSubAccountService subAccountService;
    public static final int TRANSFER_SUCCESS = 0;
    public static final int TRANSFER_OUT_ACCOUNT_NOT_EXIT = 1;
    public static final int WRONG_PASSWORD = 2;
    public static final int TRANSFER_IN_ACCOUNT_NOT_EXIT = 3;
    public static final int WRONG_NAME = 4;
    public static final int AMOUNT_NOT_ENOUGH = 5;
    public static final int OUT_IS_LOSS = 6;
    public static final int IN_IS_LOSS = 7;
    public static HashMap<Integer,String> error_message_map = new HashMap<Integer, String>(){
        {
            put(0, "TRANSFER_SUCCESS");
            put(1,"TRANSFER_OUT_ACCOUNT_NOT_EXIT");
            put(2,"WRONG_PASSWORD");
            put(3,"TRANSFER_IN_ACCOUNT_NOT_EXIT");
            put(4,"WRONG_NAME");
            put(5,"AMOUNT_NOT_ENOUGH");
            put(OUT_IS_LOSS,"OUT_IS_LOSS");
            put(IN_IS_LOSS,"IN_IS_LOSS");
        }
    };
    public List<Transaction> findTransactionsByAccount(String account){
        String sql = String.format("select * from debit_sub_transactions where account regexp \"%s\"",account);
        return (List<Transaction>)jdbc.query(sql,new BeanPropertyRowMapper<>(Transaction.class));
    }
    public List<Transaction> findTransactionsByAccountAndSubAccount(String account,String sub_account){
        String sql = String.format("select * from debit_sub_transactions where account regexp \"%s\" and sub_account regexp \"%s\"",account,sub_account);
        return (List<Transaction>)jdbc.query(sql,new BeanPropertyRowMapper<>(Transaction.class));
    }
    public List<Transaction> findTransactions(String account,String sub_account,String type,String from_date,String to_date){
        if(account == null || account.length() < 1) account = ".*";
        if(sub_account == null || sub_account.length() < 1) sub_account = ".*";
        Double low = new Double(-9999999999.0);
        Double high = new Double(9999999999.0);
        if(type.equals("1")) low = 0.0;
        if(type.equals("2")) high = 0.0;
        if(from_date == null || from_date.length() < 1) from_date = "1000-01-01";
        if(to_date == null || to_date.length() < 1) to_date ="9999-01-01";
        String sql = String.format("select * from debit_sub_transactions where account regexp \"%s\" and " +
                "sub_account regexp \"%s\" and amount > %f and amount < %f and update_time > \"%s\" and update_time < \"%s\"",account,sub_account,low,high,from_date,to_date);
        return (List<Transaction>) jdbc.query(sql,new BeanPropertyRowMapper<>(Transaction.class));
    }
    public void createTransaction(String account,String sub_account,Double money,String detail){
        String sql = String.format("insert into debit_sub_transactions values (\"%s\",\"%s\",%f,\"%s\",\"%s\")",account,sub_account,money,TimeService.getNowTime(),detail);
        jdbc.update(sql);
    }
    public int checkTransfer(String from,String password,String to,String to_name,String currency,Double amount){
        List<DebitCard> out_list = debitCardService.finalByAccount(from);
        if(out_list.size() == 0) return TRANSFER_OUT_ACCOUNT_NOT_EXIT;
        List<DebitCard> in_list = debitCardService.finalByAccount(to);
        if(in_list.size() == 0 ) return TRANSFER_IN_ACCOUNT_NOT_EXIT;
        if(!in_list.get(0).getName().equals(to_name)) return WRONG_NAME;
        if(!BCrypt.checkpw(password,out_list.get(0).getPassword()))
            return WRONG_PASSWORD;
        if(out_list.get(0).isLoss()) return OUT_IS_LOSS;
        if(in_list.get(0).isLoss()) return IN_IS_LOSS;
        return TRANSFER_SUCCESS;
    }
    public int checkTransfer(String from,String to,String to_name,String currency,Double amount){
        return TRANSFER_SUCCESS;
    }
    public String transferAmount(String from,String password,String to,String to_name,String currency,Double amount){
        int message_code = checkTransfer(from,password,to,to_name,currency,amount);
        if(message_code != TRANSFER_SUCCESS) return error_message_map.get(message_code);
        DebitSubAccount from_account = new DebitSubAccount();
        from_account.setCurrency(currency);
        List<DebitSubAccount> list = subAccountService.findByAccountAndSubAccount(from,"^"+from_account.getCurrencyIndex()+"1.*");
        from_account = list.get(0);
        String message = subAccountService.drawAmount(from_account.getAccount(),from_account.getSub_account(),amount,password,"Transfer to " + to + " " + to_name);
        if(!message.equals("DRAW_AMOUNT_SUCCESS")) return message;
        DebitSubAccount to_account = new DebitSubAccount();
        to_account.setCurrency(currency);
        to_account.setSaving_type("1");
        to_account.setAccount(to);
        to_account.setAmount(amount);
        subAccountService.depositAmount(to_account,"Transfer from " + from);
        updateFrequentContact(from,to,to_name);
        return error_message_map.get(message_code);
    }
    public String transferAmountNoPSD(String from,String to,String to_name,String currency,Double amount){
        int message_code = checkTransfer(from,to,to_name,currency,amount);
        if(message_code != TRANSFER_SUCCESS) return error_message_map.get(message_code);
        DebitSubAccount from_account = new DebitSubAccount();
        from_account.setCurrency(currency);
        List<DebitSubAccount> list = subAccountService.findByAccountAndSubAccount(from,"^"+from_account.getCurrencyIndex()+"1.*");
        from_account = list.get(0);
        subAccountService.drawAmountNoPSD(from_account.getAccount(),from_account.getSub_account(),amount,"Transfer to " + to + " " + to_name);
        DebitSubAccount to_account = new DebitSubAccount();
        to_account.setCurrency(currency);
        to_account.setSaving_type("1");
        to_account.setAccount(to);
        to_account.setAmount(amount);
        subAccountService.depositAmount(to_account,"Transfer from " + from);
        updateFrequentContact(from,to,to_name);
        return error_message_map.get(message_code);
    }
    public String updateFrequentContact(String from,String to,String to_name){
        SqlRowSet res=jdbc.queryForRowSet("select * from debit_cards where account=?",to);
        if(res.next()){
            if(!res.getString("name").equals(to_name))
                return "receiver`s account and name don`t match";
        }else{
            return "no receiver";
        }
        SqlRowSet ress=jdbc.queryForRowSet("select * from frequent_contacts where account=? and contact=?",
                from,to);
        if(ress.next()){
            jdbc.update("update frequent_contacts set count=? where account=? and contact=?",
                    ress.getInt("count")+1,from,to);
        }else{
            jdbc.update("insert into frequent_contacts values (?,?,?,?,?)",
                    from,to,to_name,1,TimeService.getSetTime());
        }
        return "update successfully";
    }
    public List<FrequentContacts> findFrequentContact(String id){
        String sql=String.format("select * from frequent_contacts where account=%s order by count desc",id);
        return (List<FrequentContacts>)jdbc.query(sql,new BeanPropertyRowMapper<>(FrequentContacts.class));
    }
}
