package com.banksystem.service;

import com.banksystem.entity.DebitSubAccount;
import com.banksystem.entity.InterestRate;
import javafx.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.lang.reflect.Array;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
@EnableAutoConfiguration
public class InterestRateService {
    @Autowired
    JdbcTemplate jdbc;
    private static HashMap<Long, Pair<Double,Long>> m;
    @Autowired
    private  DebitSubAccountService service;

    public List<InterestRate> findInterestRateByType(String type){
        String sql = "select * from interest_rates where type = \"%s\" order by update_time desc";
        sql = String.format(sql,type);
        return (List<InterestRate>)jdbc.query(sql,new BeanPropertyRowMapper<>(InterestRate.class));
    }
    public void setInterestRate(InterestRate rate){
        String sql = "insert into interest_rates values(%f,\"%s\",\"%s\")";
        rate.setUpdate_time(TimeService.getSetTime());
        sql = String.format(sql,rate.getRate(),rate.getType(),rate.getUpdate_time());
        jdbc.update(sql);
    }
    public static void init( List<DebitSubAccount> list){
        if(m != null) return ;
        m = new HashMap<Long, Pair<Double, Long>>();
        for(int i=0;i<list.size();i++){
            DebitSubAccount tmp_account = list.get(i);
            m.put(tmp_account.myHashCode(),new Pair<Double, Long>(tmp_account.getAmount(),(TimeService.system_time - tmp_account.getCreate_time().getTime())/(24*60*60*1000)-1));
        }
    }
    public void calInterest(){
        List<DebitSubAccount> list = service.findByAccount(".*");
            for(int i=0;i<list.size();i++){
                DebitSubAccount tmp_account = list.get(i);
                Pair<Double,Long> tmp_pair = m.get(tmp_account.myHashCode());
                if(tmp_pair == null)
                    tmp_pair = new Pair<Double, Long>(tmp_account.getAmount(),(long)1);
                else
                    tmp_pair = new Pair<Double, Long>(tmp_pair.getKey()+tmp_account.getAmount(),tmp_pair.getValue()+1);
                m.put(tmp_account.myHashCode(),tmp_pair);
            }
        System.out.println(m.toString());
    }
    public void transferInterrest(String account,String sub_account){
        //System.out.println("Useful");
        List<DebitSubAccount> list = service.findByAccountAndSubAccount(account,sub_account);
        if(list.get(0).getSavingTypeIndex() == 1)
        for(int i=0;i<list.size();i++){
            DebitSubAccount tmp_account = list.get(i);
            Pair<Double,Long> tmp_pair = m.get(tmp_account.myHashCode());
            Double interest = tmp_account.getInterest();
            interest = interest + tmp_pair.getKey()*findInterestRateByType("0").get(0).getRate()/365;
            Double amount = tmp_account.getAmount() + (int)interest.doubleValue();
            System.out.println(amount+ " " + interest+ " " + findInterestRateByType("0").get(0).getRate());
            interest = interest -  (int)interest.doubleValue();
            tmp_account.setAmount(amount - tmp_account.getAmount());
            tmp_account.setInterest(interest);
            tmp_account.setUpdate_time(TimeService.getSetTime());
            service.depositAmount(tmp_account,"GET INTEREST");
            m.put(tmp_account.myHashCode(),new Pair<Double, Long>(amount,(long)0));
        }
        if(list.get(0).getSavingTypeIndex() == 2 ){
            for(int i=0;i<list.size();i++){
                DebitSubAccount tmp_account = list.get(i);
                long sys_time = TimeService.system_time;
                long tmp_time = tmp_account.getCreate_time().getTime();
                InterestRate past_rate = new InterestRate();
                if(sys_time - tmp_time >= (long)tmp_account.getDeposit_time()*(long)365*24*60*60*1000){
                    List<InterestRate> interestRateList = this.findInterestRateByType("1");
                    for(int j=0;j<interestRateList.size();j++){
                        if(interestRateList.get(j).getUpdate_time().getTime() < tmp_account.getCreate_time().getTime()){
                            past_rate = interestRateList.get(j);
                            break;
                        }
                    }
                    Double amount = tmp_account.getAmount();
                    Double interest = tmp_account.getInterest();
                    interest += amount*past_rate.getRate()*tmp_account.getDeposit_time();
                    if(tmp_account.getRenew_time() == 0){
                        if(m.get(tmp_account.myHashCode())!=null)
                            m.remove(tmp_account.myHashCode());
                        service.deleteByAccountAndSubAccount(tmp_account.getAccount(),tmp_account.getSub_account());
                        StringBuilder now_sub_account = new StringBuilder(tmp_account.getSub_account());
                        now_sub_account.setCharAt(1,'1'); // get new sub account turn to current time
                        tmp_account.setSub_account(now_sub_account.toString());
                        tmp_account.setAmount(interest+amount);
                        tmp_account.setInterest(0.0);
                        tmp_account.setUpdate_time(TimeService.getSetTime());
                        tmp_account.setSaving_type("1");
                        service.depositAmount(tmp_account,"FROM LUMP SUM");
                    }
                    else {
                        Integer renew_time = tmp_account.getRenew_time();
                        if(sys_time - tmp_time >= ((long)tmp_account.getDeposit_time()+tmp_account.getRenew_time())*365*24*60*60*1000){
                            if(m.get(tmp_account.myHashCode())!=null)
                                m.remove(tmp_account.myHashCode());
                            service.deleteByAccountAndSubAccount(tmp_account.getAccount(),tmp_account.getSub_account());
                            interest += (amount+interest)*past_rate.getRate()*tmp_account.getRenew_time();
                            StringBuilder now_sub_account = new StringBuilder(tmp_account.getSub_account());
                            now_sub_account.setCharAt(1,'1'); // get new sub account turn to current time
                            tmp_account.setSub_account(now_sub_account.toString());
                            tmp_account.setAmount(interest+amount);
                            tmp_account.setInterest(0.0);
                            tmp_account.setUpdate_time(new Timestamp(tmp_account.getCreate_time().getTime()+((long)tmp_account.getDeposit_time()+tmp_account.getRenew_time())*365*24*60*60*1000));
                            tmp_account.setSaving_type("1");
                            service.depositAmount(tmp_account,"FROM LUMP SUM");
                        }
                        else{
                            tmp_account.setUpdate_time(TimeService.getSetTime());
                            tmp_account.setAmount(amount + interest);
                            service.updateSubAccount(tmp_account.getAccount(),tmp_account.getSub_account(),tmp_account);
                        }
                    }
                }
            }

        }
        if(list.get(0).getSavingTypeIndex() == 3){
            for(int i=0;i<list.size();i++){
                DebitSubAccount tmp_account = list.get(i);
                long sys_time = TimeService.system_time;
                InterestRate now_rate = this.findInterestRateByType("0").get(0);
                if(sys_time - tmp_account.getCreate_time().getTime() >= (long)365*24*60*60*1000) {
                     now_rate = this.findInterestRateByType("1").get(0);
                }
                Double amount = tmp_account.getAmount();
                Double interest = tmp_account.getInterest();
                interest += (amount * now_rate.getRate() * 0.6 * ((sys_time - tmp_account.getCreate_time().getTime())) / ((long) 365 * 24 * 60 * 60 * 1000));
                tmp_account.setAmount(amount);
                tmp_account.setUpdate_time(TimeService.getSetTime());
                tmp_account.setInterest(0.0);
                service.updateSubAccount(tmp_account.getAccount(), tmp_account.getSub_account(), tmp_account);
            }
        }
    }

}
