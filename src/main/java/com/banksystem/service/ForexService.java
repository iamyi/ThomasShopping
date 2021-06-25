package com.banksystem.service;

import com.banksystem.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import java.sql.Time;
import java.util.ArrayList;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@EnableAutoConfiguration
public class ForexService {
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private SavingService sservice;
    @Autowired
    private DebitSubAccountService subAccountService;

    public boolean checkAccount(String id){
        String sql=String.format("select * from forex_sub_account where account =%s",id);
        List<ForexSubAccount> list=jdbc.query(sql,new BeanPropertyRowMapper<>(ForexSubAccount.class));
        if(list.size()==0)
            return false;
        else
            return true;
    }

    public void addForexAccount(String id){
        if(checkAccount(id))
            return;
        for(int i=1;i<=5;i++)
            jdbc.update("insert into forex_sub_account values (?,?,?,?,?,?,?)",
                id,String.valueOf(i),0,TimeService.getNowTime(),TimeService.getNowTime(),
                TimeService.getNowTime(),TimeService.getNowTime());
    }
    public List<ForexSubAccount> getSubAccount(String id){
        String sql=String.format("select * from forex_sub_account where account=%s",id);
        return (List<ForexSubAccount>)jdbc.query(sql,new BeanPropertyRowMapper<>(ForexSubAccount.class));
    }
    public List<ForexSubAccount> findByAccountAndSubAccount(String account,String sub_account){
        String sql=String.format("select * from forex_sub_account where account=%s and sub_account=%s",account,sub_account);
        return (List<ForexSubAccount>)jdbc.query(sql,new BeanPropertyRowMapper<>(ForexSubAccount.class));
    }
    public String transfer(String id,String direction,String currency,Double amount){
        if(direction.equals("1")){
            boolean f=this.drawAmountFromForex(id,currency,amount);
            if(!f)
                return "0Transfer unsuccessfully! You don`t have sufficient funds!";
            DebitSubAccount to_account = new DebitSubAccount();
            to_account.setCurrency(currency);
            to_account.setSaving_type("1");
            to_account.setAccount(id);
            to_account.setAmount(amount);
            subAccountService.depositAmount(to_account,"Transfer from Forex Account");
            return "1Transfer successfully!";
        }
        if(direction.equals("2")){
            List<DebitSubAccount> list=subAccountService.findByAccount(id);
            DebitSubAccount sub_account=null;
            for(DebitSubAccount c:list){
                if(c.getSub_account().charAt(0)==currency.charAt(0)&&c.getSub_account().charAt(1)=='1'){
                    sub_account=c;
                    break;
                }
            }
            if(sub_account==null)
                return "0Transfer unsuccessfully! You don`t have sufficient funds!";
            if(drawAmountFromDebitCard(id,sub_account.getSub_account(),amount,"Transfer to Forex Account")){
                this.depositAmount(id,currency,amount);
                return "1Transfer successfully!";
            }else{
                return "0Transfer unsuccessfully! You don`t have sufficient funds!";
            }
        }
        return "";
    }
    private boolean drawAmountFromForex(String account, String sub_account, Double amount){
        List<ForexSubAccount> list = this.findByAccountAndSubAccount(account,sub_account);
        ForexSubAccount subAccount = list.get(0);
        if(subAccount.getAmount()<amount)
            return false;
        String sql = String.format("update forex_sub_account set amount=%f,update_time='%s' where account='%s\' and sub_account='%s' ",
                subAccount.getAmount()-amount,TimeService.getNowTime(),account,sub_account);
        jdbc.update(sql);
        return true;
    }
    private boolean drawAmountFromDebitCard(String account, String sub_account, Double amount, String detail){
        List<DebitSubAccount> list = subAccountService.findByAccountAndSubAccount(account,sub_account);
        DebitSubAccount subAccount = list.get(0);
        if(subAccount.getAmount()<amount)
            return false;
        String sql = String.format("update debit_sub_accounts set amount=%f,update_time='%s' where account='%s\' and sub_account='%s' ",subAccount.getAmount()-amount,TimeService.getNowTime(),subAccount.getAccount(),subAccount.getSub_account());
        jdbc.update(sql);
        sservice.createTransaction(account,sub_account,amount*-1,detail);
        return true;
    }
    private boolean depositAmount(String id, String subAccount, Double amount){
        List<ForexSubAccount> list = this.findByAccountAndSubAccount(id,subAccount);
        ForexSubAccount fa=list.get(0);
        jdbc.update("update forex_sub_account set amount=?,update_time=? where account=? and sub_account=?",
                fa.getAmount()+amount,TimeService.getNowTime(),id,subAccount);
        return true;
    }
    private String trade(String id,String sellCurrency,String buyCurrency,double amount){
        List<ForexSubAccount> saList=this.findByAccountAndSubAccount(id,sellCurrency);
        ForexSubAccount sa=saList.get(0);
        if(sa.getAmount()<amount)
            return "0Trade unsuccessfully! You have`t sufficient funds!";
        double rate=this.getLatestRateByCurrency(sellCurrency,buyCurrency,amount);
        this.drawAmountFromForex(id,sellCurrency,amount);
        double inAmount=amount*rate;
        this.depositAmount(id,buyCurrency,inAmount);
        return "1Trade succcessfully!";
    }
    public String addRealTimeOrder(String id,String sc,String bc,Double amount){
        String res=trade(id,sc,bc,amount);
        if(res.charAt(0)=='1') {
            jdbc.update("insert into forex_order value (?,?,?,?,?,?,?,?,?)",
                    id, 0, sc, bc, amount, 0.0, 0.0, "3", TimeService.getNowTime());
        }
        return res;
    }
    public double getLatestRateByCurrency(String sCurrency,String bCurrency,double amount){
        int level=1;
        if(amount>100000) level=3;
        if(amount<=100000&&amount>10000) level=2;
        if(amount<=100&&amount>0) level=1;
        String sql=String.format("select * from forex_rate where currency=%s and level=%d order by update_time desc",sCurrency+bCurrency,level);
        List<ForexRate> fList=jdbc.query(sql,new BeanPropertyRowMapper<>(ForexRate.class));
        if(fList.size()>0){
            return fList.get(0).getRate1();
        }else{
            String sqq=String.format("select * from forex_rate where currency=%s and level=%d order by update_time desc",bCurrency+sCurrency,level);
            List<ForexRate> fListt=jdbc.query(sqq,new BeanPropertyRowMapper<>(ForexRate.class));
            return 1.0/fListt.get(0).getRate2();
        }
    }
    public List<ForexRate> getAllLatestRate(){
        SqlRowSet res = jdbc.queryForRowSet("select distinct currency from forex_rate");
        System.out.println("***********"+res);
        List<String> currencyList=new ArrayList<String>();
        List<ForexRate> returnList=new ArrayList<ForexRate>();
        while(res.next()){
            currencyList.add(res.getString("currency"));
        }
        for(int level=1;level<=3;level++){
            for(String cu:currencyList){
                String sql=String.format("select * from forex_rate where currency=%s and level=%d order by update_time desc",cu,level);
                List<ForexRate> fList=jdbc.query(sql,new BeanPropertyRowMapper<>(ForexRate.class));
                if(fList.size()>0) returnList.add(fList.get(0));
            }
        }
        return returnList;
    }
    public void tmpAdd(Double rate1,Double rate2){
        //美元/港元，美元/欧元，美元/日元，欧元/港元，欧元/日元，港元/日元
        String []cu=new String[]{"23","25","24","53","54","34"};
        int []level=new int[]{1,2,3};
        for(String c:cu){
            for(int i:level){
                jdbc.update("insert into forex_rate value (?,?,?,?,?)",
                        c,i,rate1,rate2,TimeService.getNowTime());
            }
        }
    }
    public void addBoardOrder(String id,String sellcurrency,String buycurrency,double amount,double hp){
        jdbc.update("insert into forex_order value (?,?,?,?,?,?,?,?,?)",
                id,1,sellcurrency,buycurrency,amount,0.0,hp,"0",TimeService.getNowTime());
    }
    public void addLimitLossesOrder(String id,String sellcurrency,String buycurrency,double amount,double lp){
        jdbc.update("insert into forex_order value (?,?,?,?,?,?,?,?,?)",
                id,2,sellcurrency,buycurrency,amount,lp,0.0,"0",TimeService.getNowTime());
    }
    public void addOCOOrder(String id,String sellcurrency,String buycurrency,double amount,double lp,double hp){
        jdbc.update("insert into forex_order value (?,?,?,?,?,?,?,?,?)",
                id,3,sellcurrency,buycurrency,amount,lp,hp,"0",TimeService.getNowTime());
    }
    //forex_order中addition字段：0表示无追加委托，1* 第一位表示有追加委托，第二位表示追加委托的类型，2表示已失效委托,3表示已执行
    //有追加的委托在执行后立即向Foex_order表中添加一个与原委托交易方向相反，金额与买入金额相同，类型为指定类型的新委托
    public List<ForexOrder> getAllForexOrderById(String id){
        return jdbc.query("select * from forex_order order by create_time desc",new BeanPropertyRowMapper<>(ForexOrder.class));
    }
    public List<ForexOrder> getAllAvailableForexOrder(){
        return jdbc.query("select * from forex_order where addition!='2' and addition!='3' order by create_time desc",new BeanPropertyRowMapper<>(ForexOrder.class));
    }
    public ForexOrder getForexOrderByIdAndCreateTime(String id,String create_time){
        String sql=String.format("select * from forex_order where account='%s' and create_time='%s'",id,create_time.substring(0,19));
        List<ForexOrder> list=jdbc.query(sql,new BeanPropertyRowMapper<>(ForexOrder.class));
        if(list.size()>0)
            return list.get(0);
        else
            return null;
    }
    public void cancelOrder(String id,String create_time){
        String sql=String.format("update forex_order set addition='2' where account='%s' and create_time='%s'",id,create_time.substring(0,19));
        jdbc.update(sql);
    }
    public void additionBoard(String id,String create_time,Double highprice){
        String sql=String.format("update forex_order set addition='11 %.2f' where account='%s' and create_time='%s'",highprice,id,create_time.substring(0,19));
        jdbc.update(sql);
    }
    public void additionLimitLosses(String id,String create_time,Double lowprice){
        String sql=String.format("update forex_order set addition='12 %.2f' where account='%s' and create_time='%s'",lowprice,id,create_time.substring(0,19));
        jdbc.update(sql);
    }
    public void additionOCO(String id,String create_time,Double lowprice,Double highprice){
        String sql=String.format("update forex_order set addition='13 %.2f %.2f' where account='%s' and create_time='%s'",lowprice,highprice,id,create_time.substring(0,19));
        jdbc.update(sql);
    }
    public void checkExecuteable(){//检查更新所有计划，汇率达到的计划将被执行
        List<ForexOrder> list=this.getAllAvailableForexOrder();
        for(ForexOrder fo:list){
            Double currentRate=this.getLatestRateByCurrency(fo.getSell_currency(),fo.getBuy_currency(),fo.getAmount());
            Double expectLowRate,expectHighRate;
            expectHighRate=fo.getHigh_price();
            expectLowRate=fo.getLow_price();
            switch (fo.getOrder_type()){
                case 1:
                    if(currentRate>=expectHighRate){
                        this.executeOrder(fo.getAccount(),fo.getCreate_time().toString());
                    }
                    break;
                case 2:
                    if(currentRate<=expectLowRate) {
                        this.executeOrder(fo.getAccount(),fo.getCreate_time().toString());
                    }
                    break;
                case 3:
                    if(currentRate<expectLowRate||currentRate>expectHighRate){
                        this.executeOrder(fo.getAccount(),fo.getCreate_time().toString());
                    }
            }
        }
    }
    public void checkOutOfDate(){
        List<ForexOrder> list=this.getAllAvailableForexOrder();
        for(ForexOrder fo:list){
            if(TimeService.getSetTime().getTime()-fo.getCreate_time().getTime()>(long)3*24*60*60*1000){
                jdbc.update("update forex_order set addition=2 where account=? and create_time=?",fo.getAccount(),fo.getCreate_time().toString().substring(0,19));
            }
        }
    }
    private String executeOrder(String id,String create_time) {     //执行一个可执行的委托，汇率发生变化时触发
        ForexOrder fo = this.getForexOrderByIdAndCreateTime(id, create_time);
        if (fo.getAddition().charAt(0) == '2' || fo.getAddition().charAt(0) == '3')
            return "0";
        Double amout = fo.getAmount();
        if (fo.getOrder_type() == 3)
            amout = amout / 2.0;
        String rs = this.trade(id, fo.getSell_currency(), fo.getBuy_currency(), amout);
        if (rs.charAt(0) == '0') {//执行不成功，置该指令已失效，追加委托未投放
            jdbc.update("update forex_order set addition='2' where account=? and create_time=?",
                    fo.getAccount(), fo.getCreate_time().toString().substring(0, 19));
            return "0";
        }
        if (rs.charAt(0) == '1') {//执行成功
            String addition = fo.getAddition();
            if (addition.charAt(0) == '0') {//该委托未追加委托，直接置该委托已执行
                jdbc.update("update forex_order set addition='3' where account=? and create_time=?",
                        fo.getAccount(), fo.getCreate_time().toString().substring(0, 19));
                return "1";
            }
            if (addition.charAt(0) == '1') {//该委托追加委托，置该委托已执行，且投放追加委托
                jdbc.update("update forex_order set addition='3' where account=? and create_time=?",
                        fo.getAccount(), fo.getCreate_time().toString().substring(0, 19));
                int plantype;
                Double lp, hp;
                switch (addition.charAt(1)) {
                    case '1':
                        hp = Double.valueOf(addition.split(" ")[1]);
                        this.addBoardOrder(id, fo.getBuy_currency(), fo.getSell_currency(), fo.getAmount(), hp);
                        break;
                    case '2':
                        lp = Double.valueOf(addition.split(" ")[1]);
                        this.addLimitLossesOrder(id, fo.getBuy_currency(), fo.getSell_currency(), fo.getAmount(), lp);
                        break;
                    case '3':
                        lp = Double.valueOf(addition.split(" ")[1]);
                        hp = Double.valueOf(addition.split(" ")[2]);
                        this.addOCOOrder(id, fo.getBuy_currency(), fo.getSell_currency(), fo.getAmount(), lp, hp);
                        break;
                }
                return "1";
            }
        }
        return "";
    }

    public List<ForexRate> findAllLatestRate(){
        List<ForexRate> list = new ArrayList<ForexRate>();
        String[] currency = {"23","24","25","53","54","34"};
        for(int i=0;i<currency.length;i++){
            for(int j=1;j<=3;j++){
                list.add(findByCurrencyAndLevel(currency[i],j).get(0));
            }
        }
        return list;
    }
    public List<ForexRate> findByCurrencyAndLevel(String currency,Integer level){
        String sql = "select * from forex_rate where currency regexp \"%s\" and level = %d order by update_time desc";
        sql = String.format(sql,currency,level);
        return jdbc.query(sql,new BeanPropertyRowMapper<>(ForexRate.class));
    }
    public void setForex(ForexRate forex){
        String sql = "insert into forex_rate values(\"%s\",\"%s\",%f,%f,\"%s\")";
        sql = String.format(sql,forex.getCurrency(),forex.getLevel(),forex.getRate1(),forex.getRate2(),TimeService.getNowTime());
        jdbc.update(sql);
        this.checkOutOfDate();
    }

}
