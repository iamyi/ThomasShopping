package com.banksystem.service;

import com.banksystem.entity.DebitCard;
import com.banksystem.entity.DebitSubAccount;
import com.banksystem.entity.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@EnableAutoConfiguration
public class DebitCardService {
    @Autowired
    private JdbcTemplate jdbc;
    public static final int ERROR_INFO = 1;
    public static final int CREATE_SUCCESS = 2;
    public static final int NEED_MORE_INFORMATION = 3;
    public static final int WRONG_FORMAT = 4;
    private int checkDebitCard(DebitCard debitCard){
        return CREATE_SUCCESS;
    }

    public boolean accountIsLoss(String account){
        List<DebitCard> list = finalByAccount(account);
        return list.get(0).isLoss();
    }
    public List<DebitCard> finalByAccount(String account){
        String sql = String.format("select * from debit_cards where account regexp \"%s\" order by account desc,create_time desc",account);
        SqlRowSet result = jdbc.queryForRowSet(sql);
        System.out.println(result);
        List<DebitCard> list = new ArrayList<DebitCard>();
        ObjectMapper mapper = new ObjectMapper();
        while(result.next()){
            DebitCard debitCard = new DebitCard();
            debitCard.setE_bank(result.getBoolean("e_bank"));
            debitCard.setLoss(result.getBoolean("loss"));
            debitCard.setName(result.getString("name"));
            debitCard.setCreate_time(result.getTimestamp("create_time"));
            debitCard.setUpdate_time(result.getTimestamp("update_time"));
            debitCard.setId_card(result.getString("id_card"));
            debitCard.setAccount(result.getString("account"));
            debitCard.setPhone(result.getString("phone"));
            debitCard.setPassword(result.getString("password"));
            HashMap<String,String> m = new HashMap<String,String>();
            String address = result.getString("address");
            try {
                m = (HashMap<String,String>)mapper.readValue(address,Map.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
            debitCard.setAddress(m);
            list.add(debitCard);
        }
        return list;
    }

    public int checkCreate(DebitCard debitCard){
        int error_code = CREATE_SUCCESS;
        return error_code;
    }



    public DebitCard createDebitCard(DebitCard debitCard) throws JsonProcessingException {

        String account = "";
        List<DebitCard> list = finalByAccount("^1.*");
        if(list.size() == 0) {
            account = "1000000000";
        }
        else {
            account = list.get(0).getAccount();
            account = new Long(Long.parseLong(account) + 1).toString();
        }
        String password = BCrypt.hashpw(debitCard.getPassword(),BCrypt.gensalt());
        debitCard.setAccount(account);
        debitCard.setUpdate_time(TimeService.getSetTime());
        debitCard.setCreate_time(TimeService.getSetTime());
        String sql = String.format("insert into debit_cards values(?,?,?,?,?,?,?,?,?,?)");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        jdbc.update(sql,new Object[]{debitCard.getAccount(),debitCard.getName(),
                debitCard.getId_card(),mapper.writeValueAsString(debitCard.getAddress()),debitCard.getPhone(),password,debitCard.isE_bank(),debitCard.isLoss(),
                TimeService.getNowTime(),TimeService.getNowTime()
        });
        return debitCard;
    }
    public void editDebitCard(DebitCard card) throws JsonProcessingException {
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        String sql="update debit_cards set name=?,address=?,phone=? where account=?";
        String address="{\"area\": \""+card.getAddress().get("area")+"\", \"city\": \""+
                card.getAddress().get("city")+"\", \"street\": \""+card.getAddress().get("street")+
                "\", \"province\": \""+card.getAddress().get("province")+"\"}";
        jdbc.update(sql,card.getName(),address,card.getPhone(),card.getAccount());
    }
    public String changePSD(String id,String psd){
        if(psd.length()!=6)
            return "Change failed! Expected a six-character password!";
        for(int i=0;i<psd.length();i++){
            if(psd.charAt(i)<'0'||psd.charAt(i)>'9')
                return "Change failed! Expected a numeric password!";
        }
        jdbc.update("update debit_cards set password=? where account=?",BCrypt.hashpw(psd,BCrypt.gensalt()),id);
        return "Change password successfully!";
    }
    public String reportLoss(String id) {
        SqlRowSet result = jdbc.queryForRowSet("select * from debit_cards where account=?", id);
        result.next();
        if (!result.getBoolean("loss")) {
            jdbc.update("update debit_cards set loss=? where account=?", true, id);
            return "Report loss successfully!";
        } else {
            return "Report loss unsuccessfully! Your card has been reported loss already";
        }

    }
    public String cancelReportLoss(String id){
        SqlRowSet result = jdbc.queryForRowSet("select * from debit_cards where account=?",id);
        result.next();
        if(result.getBoolean("loss")){
            jdbc.update("update debit_cards set loss=? where account=?",false,id);
            return "Cancel reporting loss successfully!";
        }else{
            return "Cancel reporting loss unsuccessfully! Your card has not been reported loss!";
        }
    }
    public String deleteAccount(String id){
        jdbc.update("delete from debit_cards where account=?",id);
        return "Delete successfully";
    }
}
