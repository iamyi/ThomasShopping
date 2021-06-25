package com.banksystem.controller;

import com.banksystem.entity.E_bankAccount;
import com.banksystem.entity.ForexOrder;
import com.banksystem.entity.ForexRate;
import com.banksystem.entity.ForexSubAccount;
import com.banksystem.service.ForexService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.jws.WebParam;
import javax.net.ssl.HttpsURLConnection;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.List;


@Controller
@EnableAutoConfiguration
public class ForexController {
    @Autowired
    ForexService fservice;

    @GetMapping("/banksystem/e_bank/forex/openfaccount")
    String toOpenFAccount(Model model, HttpSession session){
        E_bankAccount eb= (E_bankAccount) session.getAttribute("euser");
        model.addAttribute("euser",session.getAttribute("euser"));
        String id=eb.getAccount();
        if(fservice.checkAccount(id)){
            return "e_bank/forex/open_forex_account";
        }else{
            return "e_bank/forex/no_forex_account";
        }
    }
    @GetMapping("/banksystem/e_bank/openforexaccount")
    String openForexAcccount(Model model,HttpSession session){
        E_bankAccount eb= (E_bankAccount) session.getAttribute("euser");
        model.addAttribute("euser",session.getAttribute("euser"));
        String id=eb.getAccount();
        fservice.addForexAccount(id);
        return "e_bank/forex/open_forex_account";
    }
    @GetMapping("/banksystem/e_bank/forex/faccountinfo")
    String forexSubAccountInfo(Model model,HttpSession session){
        E_bankAccount eb= (E_bankAccount) session.getAttribute("euser");
        model.addAttribute("euser",session.getAttribute("euser"));
        String id=eb.getAccount();
        if(!fservice.checkAccount(id)){
            return "e_bank/forex/no_forex_account";
        }
        List<ForexSubAccount> allSubAccount=fservice.getSubAccount(id);
        int len=allSubAccount.size();
        String []currency=new String[len];
        int []index=new int[len];
        for(int i=0;i<len;i++){
            index[i]=i;
            switch (allSubAccount.get(i).getSub_account()){
                case "1":currency[i]="CNY";break;
                case "2":currency[i]="USD";break;
                case "3":currency[i]="HKD";break;
                case "4":currency[i]="JPY";break;
                case "5":currency[i]="EUR";
            }
        }
        model.addAttribute("index",index);
        model.addAttribute("currency",currency);
        model.addAttribute("allSubAccount",allSubAccount);
        return "e_bank/forex/forex_account_info";
    }
    @GetMapping("/banksystem/e_bank/forex/transfer")
    String toForexTransfer(Model model, HttpSession session, HttpServletRequest request){
        E_bankAccount eb= (E_bankAccount) session.getAttribute("euser");
        model.addAttribute("euser",session.getAttribute("euser"));
        String id=eb.getAccount();
        if(!fservice.checkAccount(id)){
            return "e_bank/forex/no_forex_account";
        }
        String info=request.getParameter("info");
        if(info!=null&&!info.equals("")) {
            if(info.charAt(0)=='1')
                model.addAttribute("ts","1");
            else
                model.addAttribute("tns","0");
            model.addAttribute("info", info.substring(1,info.length()));
        }
        return "e_bank/forex/transfer";
    }
    @PostMapping("/banksystem/e_bank/forex/transfer")
    String transferBtnFandD(@RequestParam String direction, String currency, double amount,
                            RedirectAttributes ra, HttpSession session, Model model){
        String id=((E_bankAccount)session.getAttribute("euser")).getAccount();
        String info=fservice.transfer(id,direction,currency,amount);
        ra.addAttribute("info",info);
        return "redirect:/banksystem/e_bank/forex/transfer";
    }
    @GetMapping("/banksystem/e_bank/forex/exchange")
    String exchange(Model model, HttpSession session, HttpServletRequest request){
        model.addAttribute("euser",session.getAttribute("euser"));
        String id=((E_bankAccount)session.getAttribute("euser")).getAccount();
        if(!fservice.checkAccount(id)){
            return "e_bank/forex/no_forex_account";
        }
        String info=request.getParameter("info");
        if(info!=null&&!info.equals("")) {
            if(info.charAt(0)=='1')
                model.addAttribute("ts","1");
            else
                model.addAttribute("tns","0");
            model.addAttribute("info", info.substring(1,info.length()));
        }
        List<ForexRate> list=fservice.getAllLatestRate();
        //fservice.tmpAdd();
        int len=list.size();
        int []index=new int[len];
        String []type=new String[len];
        String []rate=new String[len];
        String []level=new String[len];
        for(int i=0;i<len;i++){
            index[i]=i;
            String c=list.get(i).getCurrency();
            switch (c.charAt(0)){
                case '1':type[i]="CNY";break;
                case '2':type[i]="USD";break;
                case '3':type[i]="HKD";break;
                case '4':type[i]="JPY";break;
                case '5':type[i]="EUR";
            }
            type[i]+="/";
            switch (c.charAt(1)){
                case '1':type[i]+="CNY";break;
                case '2':type[i]+="USD";break;
                case '3':type[i]+="HKD";break;
                case '4':type[i]+="JPY";break;
                case '5':type[i]+="EUR";
            }
            rate[i]=list.get(i).getRate1()+"/"+list.get(i).getRate2();
            int l=list.get(i).getLevel();
            switch (l){
                case 1:level[i]="base";break;
                case 2:level[i]="preferential";break;
                case 3:level[i]="vip";
            }
        }
        model.addAttribute("list",list);
        model.addAttribute("type",type);
        model.addAttribute("index",index);
        model.addAttribute("rate",rate);
        model.addAttribute("level",level);
        return "e_bank/forex/exchange";
    }
    @PostMapping("/banksystem/e_bank/forex/exchange")
    String submitExchange(@RequestParam String sellcurrency,String buycurrency,double amount,
                          RedirectAttributes ra,HttpSession session,Model model){
        String id=((E_bankAccount)session.getAttribute("euser")).getAccount();
        if(sellcurrency==null||buycurrency==null||sellcurrency.equals("Sell Currency")||buycurrency.equals("Buy Currency"))
        {
            String info="0Attribute Missing...";
            ra.addAttribute("info",info);
            return "redirect:/banksystem/e_bank/forex/exchange";
        }
        if(sellcurrency.equals("JPY")&&amount<10000){
            String info="0Amount should be bigger than 10000...";
            ra.addAttribute("info",info);
            return "redirect:/banksystem/e_bank/forex/exchange";
        }
        if(!sellcurrency.equals("JPY")&&amount<100){
            String info="0Amount should be bigger than 100...";
            ra.addAttribute("info",info);
            return "redirect:/banksystem/e_bank/forex/exchange";
        }
        String s=null;
        String b=null;
        switch (sellcurrency.charAt(0)){
            case 'C':s="1";break;
            case 'U':s="2";break;
            case 'H':s="3";break;
            case 'J':s="4";break;
            case 'E':s="5";
        }
        switch (buycurrency.charAt(0)){
            case 'C':b="1";break;
            case 'U':b="2";break;
            case 'H':b="3";break;
            case 'J':b="4";break;
            case 'E':b="5";
        }
        String info=fservice.addRealTimeOrder(id,s,b,amount);
        ra.addAttribute("info",info);
        return "redirect:/banksystem/e_bank/forex/exchange";
    }
    @GetMapping("/banksystem/ebank/forex/tofigure/{type}/{level}/{frompage}")
    String toFigure(@PathVariable String type,@PathVariable String level,@PathVariable String frompage,
                    HttpSession session,Model model){
        model.addAttribute("euser",session.getAttribute("euser"));
        model.addAttribute("frompage",frompage);
        String str="";
        switch (type.charAt(0)){
            case '1':str="CNY";break;
            case '2':str="USD";break;
            case '3':str="HKD";break;
            case '4':str="JPY";break;
            case '5':str="EUR";
            }
        str+=" To ";
        switch (type.charAt(1)){
            case '1':str+="CNY";break;
            case '2':str+="USD";break;
            case '3':str+="HKD";break;
            case '4':str+="JPY";break;
            case '5':str+="EUR";
        }
        model.addAttribute("type",str);
        return "e_bank/forex/rate_figure";
    }
    @GetMapping("/banksystem/e_bank/forex/addplan")
    String addplan(Model model, HttpSession session, HttpServletRequest request){
        model.addAttribute("euser",session.getAttribute("euser"));
        String id=((E_bankAccount)session.getAttribute("euser")).getAccount();
        if(!fservice.checkAccount(id)){
            return "e_bank/forex/no_forex_account";
        }

        String info=request.getParameter("info");
        if(info!=null&&!info.equals("")) {
            if(info.charAt(0)=='1')
                model.addAttribute("ts","1");
            else
                model.addAttribute("tns","0");
            model.addAttribute("info", info.substring(1,info.length()));
        }
        List<ForexRate> list=fservice.getAllLatestRate();
        //fservice.tmpAdd();
        int len=list.size();
        int []index=new int[len];
        String []type=new String[len];
        String []rate=new String[len];
        String []level=new String[len];
        for(int i=0;i<len;i++){
            index[i]=i;
            String c=list.get(i).getCurrency();
            switch (c.charAt(0)){
                case '1':type[i]="CNY";break;
                case '2':type[i]="USD";break;
                case '3':type[i]="HKD";break;
                case '4':type[i]="JPY";break;
                case '5':type[i]="EUR";
            }
            type[i]+="/";
            switch (c.charAt(1)){
                case '1':type[i]+="CNY";break;
                case '2':type[i]+="USD";break;
                case '3':type[i]+="HKD";break;
                case '4':type[i]+="JPY";break;
                case '5':type[i]+="EUR";
            }
            rate[i]=list.get(i).getRate1()+"/"+list.get(i).getRate2();
            int l=list.get(i).getLevel();
            switch (l){
                case 1:level[i]="base";break;
                case 2:level[i]="preferential";break;
                case 3:level[i]="vip";
            }
        }
        model.addAttribute("list",list);
        model.addAttribute("type",type);
        model.addAttribute("index",index);
        model.addAttribute("rate",rate);
        model.addAttribute("level",level);
        return "e_bank/forex/add_plan";
    }
    @PostMapping("/banksystem/e_bank/forex/addplan")
    String submitAddPlan(@RequestParam String sellcurrency,String buycurrency,double amount,
                         String plantype,String lowprice,String highprice,
                         HttpSession session,RedirectAttributes ra){
        String id=((E_bankAccount)session.getAttribute("euser")).getAccount();
        if(sellcurrency.equals("Sell Currency")){
            ra.addAttribute("info","0Please choose a sell currency!");
            return "redirect:/banksystem/e_bank/forex/addplan";
        }
        if(buycurrency.equals("Buy Currency")){
            ra.addAttribute("info","0Please choose a buy currency!");
            return "redirect:/banksystem/e_bank/forex/addplan";
        }
        if(amount<100){
            ra.addAttribute("info","0Amount should be greater than 100!");
            return "redirect:/banksystem/e_bank/forex/addplan";
        }
        if(plantype.equals("choose a kind of plan")){
            ra.addAttribute("info","0Please choose a kind of plan!");
            return "redirect:/banksystem/e_bank/forex/addplan";
        }
        String s=null;
        String b=null;
        switch (sellcurrency.charAt(0)){
            case 'C':s="1";break;
            case 'U':s="2";break;
            case 'H':s="3";break;
            case 'J':s="4";break;
            case 'E':s="5";
        }
        switch (buycurrency.charAt(0)){
            case 'C':b="1";break;
            case 'U':b="2";break;
            case 'H':b="3";break;
            case 'J':b="4";break;
            case 'E':b="5";
        }
        double hp,lp;
        if(plantype.equals("board order")){
            hp=Double.valueOf(highprice);
            fservice.addBoardOrder(id,s,b,amount,hp);
        }
        if(plantype.equals("limit losses order")){
            lp=Double.valueOf(lowprice);
            fservice.addLimitLossesOrder(id,s,b,amount,lp);
        }
        if(plantype.equals("OCO order")){
            hp=Double.valueOf(highprice);
            lp=Double.valueOf(lowprice);
            fservice.addOCOOrder(id,s,b,amount,lp,hp);
        }
        String info="1Add plan successfully!";
        ra.addAttribute("info",info);
        return "redirect:/banksystem/e_bank/forex/addplan";
    }
    @GetMapping("/banksystem/e_bank/forex/editplan")
    String editPlan(Model model,HttpSession session){
        model.addAttribute("euser",session.getAttribute("euser"));
        String id=((E_bankAccount)session.getAttribute("euser")).getAccount();
        if(!fservice.checkAccount(id)){
            return "e_bank/forex/no_forex_account";
        }
        //fservice.tmpAdd(101.0,100.0);
        fservice.checkOutOfDate();
        fservice.checkExecuteable();
        List<ForexOrder> list=fservice.getAllForexOrderById(id);
        int len=list.size();
        int []index=new int[len];
        String []type=new String[len];
        String []sc=new String[len];
        String []bc=new String[len];
        boolean []cancelflag=new boolean[len];
        boolean []additionflag=new boolean[len];
        boolean []lossefficacyflag=new boolean[len];
        boolean []doneflag=new boolean[len];
        for(int i=0;i<len;i++){
            index[i]=i;
            switch (list.get(i).getOrder_type()){
                case 0:type[i]="Real time";list.get(i).setLow_price(null);list.get(i).setHigh_price(null);break;
                case 1:type[i]="Board";list.get(i).setLow_price(null);break;
                case 2:type[i]="Limit losser";list.get(i).setHigh_price(null);break;
                case 3:type[i]="OCO";
            }
            switch (list.get(i).getSell_currency().charAt(0)){
                case '1':sc[i]="CNY";break;
                case '2':sc[i]="USD";break;
                case '3':sc[i]="HKD";break;
                case '4':sc[i]="JPY";break;
                case '5':sc[i]="EUR";
            }
            switch (list.get(i).getBuy_currency().charAt(0)){
                case '1':bc[i]="CNY";break;
                case '2':bc[i]="USD";break;
                case '3':bc[i]="HKD";break;
                case '4':bc[i]="JPY";break;
                case '5':bc[i]="EUR";
            }
            switch (list.get(i).getAddition().charAt(0)){
                case '0':additionflag[i]=true;cancelflag[i]=true;lossefficacyflag[i]=false;doneflag[i]=false;break;
                case '1':additionflag[i]=false;cancelflag[i]=true;lossefficacyflag[i]=false;doneflag[i]=false;break;
                case '2':additionflag[i]=false;cancelflag[i]=false;lossefficacyflag[i]=true;doneflag[i]=false;break;
                case '3':additionflag[i]=false;cancelflag[i]=false;lossefficacyflag[i]=false;doneflag[i]=true;
            }
        }
        model.addAttribute("index",index);
        model.addAttribute("type",type);
        model.addAttribute("sc",sc);
        model.addAttribute("bc",bc);
        model.addAttribute("cancelflag",cancelflag);
        model.addAttribute("additionflag",additionflag);
        model.addAttribute("lossefficacyflag",lossefficacyflag);
        model.addAttribute("doneflag",doneflag);
        model.addAttribute("list",list);
        return "e_bank/forex/edit_plan";
    }
    @GetMapping("/banksystem/e_bank/forex/cancleorder/{create_time}")
    String cancelOrder(@PathVariable String create_time,HttpSession session){
        String id=((E_bankAccount)session.getAttribute("euser")).getAccount();
        fservice.cancelOrder(id,create_time);
        return "redirect:/banksystem/e_bank/forex/editplan";
    }
    @GetMapping("/banksystem/e_bank/forex/addition/{create_time}")
    String add(@PathVariable String create_time,Model model, HttpSession session){
        String id=((E_bankAccount)session.getAttribute("euser")).getAccount();
        model.addAttribute("euser",session.getAttribute("euser"));

        ForexOrder order=fservice.getForexOrderByIdAndCreateTime(id,create_time);
        Double amount=order.getAmount()*fservice.getLatestRateByCurrency(
                order.getSell_currency(),order.getBuy_currency(),order.getAmount());
        String sc="";
        String bc="";
        switch (order.getSell_currency().charAt(0)){
            case '1':bc="CNY";break;
            case '2':bc="USD";break;
            case '3':bc="HKD";break;
            case '4':bc="JPY";break;
            case '5':bc="EUR";
        }
        switch (order.getBuy_currency().charAt(0)){
            case '1':sc="CNY";break;
            case '2':sc="USD";break;
            case '3':sc="HKD";break;
            case '4':sc="JPY";break;
            case '5':sc="EUR";
        }
        model.addAttribute("sc",sc);
        model.addAttribute("bc",bc);
        model.addAttribute("amount",amount);
        return "e_bank/forex/add_addition_plan";
    }
    @PostMapping("/banksystem/e_bank/forex/addadditionplan")
    String addAdditaionPlan(@RequestParam String plantype,Double lowprice,
                            Double highprice,String create_time, HttpSession session) {
        String id = ((E_bankAccount) session.getAttribute("euser")).getAccount();
        if (plantype.equals("board order")) {
            fservice.additionBoard(id, create_time, highprice);
            return "redirect:/banksystem/e_bank/forex/editplan";
        }
        if (plantype.equals("limit losses order")) {
            fservice.additionLimitLosses(id, create_time, lowprice);
            return "redirect:/banksystem/e_bank/forex/editplan";
        }
        if (plantype.equals("OCO order")) {
            fservice.additionOCO(id, create_time, lowprice, highprice);
            return "redirect:/banksystem/e_bank/forex/editplan";
        }
        return "redirect:/banksystem/e_bank/forex/editplan";
    }

    @GetMapping("/banksystem/manage/forex_rate")
    public String getSetPage(Model model, HttpSession session){


        model.addAttribute("user",session.getAttribute("user"));

        model.addAttribute("list",fservice.getAllLatestRate());
        return "manage/forex_rate/forex_rate";
    }
    @PostMapping("/banksystem/manage/forex_rate/set/{index}")
    public String setForex(@RequestParam String rate1, String rate2, @PathVariable String index, Model model, HttpSession session){
        model.addAttribute("user",session.getAttribute("user"));
        ForexRate forexRate = new ForexRate();
        forexRate.setCurrency(index.substring(0,2));
        forexRate.setLevel(Integer.parseInt(index.substring(2,3)));
        forexRate.setRate1(Double.parseDouble(rate1));
        forexRate.setRate2(Double.parseDouble(rate2));
        fservice.setForex(forexRate);
        return "redirect:/banksystem/manage/forex_rate";
    }
}
