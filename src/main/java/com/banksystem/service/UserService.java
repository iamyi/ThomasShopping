package com.banksystem.service;

import com.banksystem.entity.User;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

@Service
@EnableAutoConfiguration
public class UserService {
    @Autowired
    private JdbcTemplate jdbc;
    public static final int UPDATE_PASSWORD = 0;
    public static final int WRONG_PASSWORD = 1;
    public static final int ACCOUNT_NOT_EXIST = 2;
    public static final int CORRECT_PASSWORD  =3;
    public static final int ACCOUNT_AREADY_EXIST = 4;
    public static final int ACCOUNT_NOT_OBEY_THE_RULE = 5;
    public static final int PASSWORD_NOT_OBEY_THE_RULE = 6;
    public static final int CREATE_SUCCESS = 7;
    String[] letter = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l",
            "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z",
            "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N",
            "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z",};
    String[] num = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9",};
    String[] symbol = {"!","@","#","$","%","^","&","*","(",")","~","`",",",".","/","{","}","[","]"};
    public List<User> findAll(){
        String sql = "select * from users";
        return (List<User>)jdbc.query(sql,new BeanPropertyRowMapper<>(User.class));
    }
    public String tmpadd(){
        jdbc.update("delete from users where account=10000");
        jdbc.update("insert into users values(?,?,?)", "10000",BCrypt.hashpw("123",BCrypt.gensalt()),new Timestamp(new Date().getTime()).toString());
        return "add successfully";
    }
    public List<User> findByAccount(String account){
        String sql = String.format("select * from users where account regexp \"%s\" order by account desc,update_time desc",account);
        return (List<User>)jdbc.query(sql,new BeanPropertyRowMapper<>(User.class));
    }
    public int checkUser(User user){
        List<User> userList = findByAccount(user.getAccount());
        if(userList.size() == 0 ) return ACCOUNT_NOT_EXIST;
        User past_user = userList.get(0);
        if(BCrypt.checkpw(user.getPassword(),past_user.getPassword())){
            long time_diff = (new Date().getTime()) - past_user.getUpdate_time().getTime();
            long days = time_diff / (1000*60*60*24);
            if(days > 90) return UPDATE_PASSWORD;
            return CORRECT_PASSWORD;
        }
        return WRONG_PASSWORD;
    }
    private int checkCreateUser(User user){
        List<User> list = findByAccount(user.getAccount());
        String account = user.getAccount();
        String password = user.getPassword();
        if(list.size() != 0) return ACCOUNT_AREADY_EXIST;
        if(account.length() != 5) return ACCOUNT_NOT_OBEY_THE_RULE;
        if(account.charAt(0) <='0' || account.charAt(0) >'4') return ACCOUNT_NOT_OBEY_THE_RULE;
        for(int i=0;i<account.length();i++){
            if(account.charAt(i) >='0' && account.charAt(i) <= '9') continue;
            return ACCOUNT_NOT_OBEY_THE_RULE;
        }
        if(password.length() < 8 || password.length() > 16) return PASSWORD_NOT_OBEY_THE_RULE;
        boolean has_letter = false;
        boolean has_num = false;
        boolean has_symbol = false;
        for(int i=0;i<password.length();i++){
            if(password.charAt(i) >= '0' && password.charAt(i) <= '9') has_num = true;
            else if(password.charAt(i) >= 'a' && password.charAt(i) <= 'z') has_letter = true;
            else if(password.charAt(i) >= 'A' && password.charAt(i) <= 'Z') has_letter = true;
            else has_symbol = true;
        }
        if(!has_letter || !has_num || !has_symbol) return PASSWORD_NOT_OBEY_THE_RULE;
        return CREATE_SUCCESS;

    }
    public User createUser(int user_type){
        User user = new User();
        String account ;
        String password;
        //create password
        Random random = new Random();
        int password_length = random.nextInt(9)+8;
        StringBuilder tmp_password = new StringBuilder();
        for(int i=0;i<password_length;i++){
            int now_char_type = i%3;
            String now_char="";
            int tmp_index;
            if(now_char_type == 0) {tmp_index = random.nextInt(letter.length);now_char = letter[tmp_index];}
            if(now_char_type == 1) {tmp_index = random.nextInt(num.length);now_char = num[tmp_index];}
            if(now_char_type == 2) {tmp_index = random.nextInt(symbol.length);now_char = symbol[tmp_index];}
            tmp_password.append(now_char);
        }
        ArrayList<String> shuffle_password = new ArrayList<String>();
        for(int i=0;i<tmp_password.length();i++){
            shuffle_password.add(tmp_password.substring(i,i+1));
        }
        Collections.shuffle(shuffle_password);
        for(int i=0;i<shuffle_password.size();i++) {
            tmp_password.setCharAt(i, shuffle_password.get(i).charAt(0));
        }
        user.setPassword(tmp_password.toString());

        //create account
        String account_pattern = "^"+num[user_type]+".*";
        List<User> user_list = findByAccount(account_pattern);
        System.out.println(account_pattern);
        if(user_list.size() == 0){
            account = num[user_type] + "0000";
        }else{
            User user1 = user_list.get(0);
            System.out.println(user1.getAccount());
            account = new Integer(Integer.parseInt(user1.getAccount())+1).toString();
        }
        user.setAccount(account);
        //create user
        user.setUpdate_time(new java.sql.Date(new Date().getTime()));
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        jdbc.update("insert into users values(?,?,?)",new Object[]{user.getAccount(),BCrypt.hashpw(user.getPassword(),BCrypt.gensalt()),format.format(user.getUpdate_time())});
        return user;
    }
    public String editPSD(String id,String psd){
        int len=psd.length();
        boolean hasNum=false;
        boolean hasChar=false;
        boolean hasSymbol=false;
        if(len<8||len>16){
            return "Change failed! password length in range 8-16!";
        }
        for(int i =0;i<len;i++){
            for (String ch:letter) {
                if(String.valueOf(psd.charAt(i)).equals(ch)){
                    hasChar=true;
                    break;
                }
            }
            for (String ch:symbol) {
                if(String.valueOf(psd.charAt(i)).equals(ch)){
                    hasSymbol=true;
                    break;
                }
            }
            for (String ch:num) {
                if(String.valueOf(psd.charAt(i)).equals(ch)){
                    hasNum=true;
                    break;
                }
            }
        }
        if(hasChar&&hasNum&&hasSymbol){
            jdbc.update("update users set password=?,update_time=?  where account=?",BCrypt.hashpw(psd,BCrypt.gensalt()),new Timestamp(new Date().getTime()),id);
            return "Change Successfully";
        }
        return "Change failed! Password must contains number, letter, symbol";
    }
    public String delAccount(String id){
        jdbc.update("delete from users where account=?",id);
        return "Delete successfully!";
    }
}
