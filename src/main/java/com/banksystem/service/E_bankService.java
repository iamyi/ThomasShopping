package com.banksystem.service;

import com.banksystem.entity.E_bankAccount;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
@EnableAutoConfiguration
public class E_bankService {
    @Autowired
    private JdbcTemplate jdbc;
    public static final int CORRECT_PASSOWRD  = 0;
    public static final int WRONG_PASSWORD = 1;
    public static final int ACCOUNT_NOT_EXIT = 2;
    public static final int CREATE_SUCCESS = 3;
    public static HashMap<Integer,String> error_message_map = new HashMap<Integer, String>(){
        {
            put(CORRECT_PASSOWRD,"CORRECT_PASSWORD");
            put(WRONG_PASSWORD,"WRONG_PASSWORD");
            put(ACCOUNT_NOT_EXIT,"ACCOUNT_NOT_EXIT");
            put(CREATE_SUCCESS,"CREATE_SUCCESS");
        }
    };

    public int checkLogin(String e_bank_account,String password){
        int error_code = 0;
        String sql = "select * from e_bank_account where e_bank_account regexp \"%s\"";
        sql = String.format(sql,e_bank_account);
        List<E_bankAccount> list = jdbc.query(sql,new BeanPropertyRowMapper<>(E_bankAccount.class));
        if(list.size() == 0) error_code = ACCOUNT_NOT_EXIT;
        else {
            E_bankAccount e_bankAccount = list.get(0);
            System.out.println(password + " " +BCrypt.checkpw( "123",e_bankAccount.getPassword()) + "\n" +e_bankAccount.getPassword());
            if(BCrypt.checkpw(password,e_bankAccount.getPassword())){
                error_code = CORRECT_PASSOWRD;
            }
            else {
                error_code = WRONG_PASSWORD;
            }
        }
        return error_code;
    }
    public int checkCreate(E_bankAccount e_bankAccount){
        int error_code = CREATE_SUCCESS ;
        return error_code;
    }
    public E_bankAccount createE_bankAccount(E_bankAccount e_bankAccount){

        String sql = "insert into e_bank_account values(\"%s\",\"%s\",\"%s\",\"%s\")";
        sql = String.format(sql,e_bankAccount.getAccount(),e_bankAccount.getE_bank_account(),BCrypt.hashpw(e_bankAccount.getPassword(),
                BCrypt.gensalt()),TimeService.getNowTime());
        jdbc.update(sql);
        return e_bankAccount;
    }
    public E_bankAccount findE_bankAccountByEAccount(String id){
        SqlRowSet res = jdbc.queryForRowSet("select * from e_bank_account where e_bank_account=?", id);
        E_bankAccount account=new E_bankAccount();
        if(res.next()){
            account.setAccount(res.getString("account"));
            account.setE_bank_account(res.getString("e_bank_account"));
            account.setUpdate_time(res.getTimestamp("update_time"));
        }
        return account;
    }
}
