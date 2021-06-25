package com.banksystem.controller;
import com.banksystem.entity.*;
import com.banksystem.service.DebitCardService;
import com.banksystem.service.DebitSubAccountService;

import com.banksystem.service.E_bankService;
import com.banksystem.service.SavingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.jws.WebParam;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.awt.*;
import java.util.HashMap;
import java.util.List;

@Controller
@EnableAutoConfiguration
public class EBankDCardCmotraller {
    @Autowired
    private DebitCardService service;
    @Autowired
    private E_bankService e_bankService;
    @Autowired
    private DebitSubAccountService subAccountService;
    @Autowired
    private SavingService savingService;

    @GetMapping("/banksystem/e_bank/debitcard/debitcardinfo")
    public String debitCardInfo(Model model,HttpSession session){
        E_bankAccount ea= (E_bankAccount) session.getAttribute("euser");
        model.addAttribute("euser",ea);
        String id=ea.getAccount();
        List<DebitCard> cardList=service.finalByAccount(id);
        if(cardList.size()>=1){
            model.addAttribute("card",cardList.get(0));
        }

        List<DebitSubAccount> allSubAccount=subAccountService.findByAccount(id);
        int len=allSubAccount.size();
        int []index=new int[len];
        String []currency=new String[len];
        String []depositTime=new String[len];
        for (int i=0;i<len;i++) {
            index[i] = i;
            String account=allSubAccount.get(i).getSub_account();
            int time=allSubAccount.get(i).getDeposit_time();
            switch (account.charAt(0)){
                case '1':currency[i]="CNY";break;
                case '2':currency[i]="USD";break;
                case '3':currency[i]="HKD";break;
                case '4':currency[i]="JPY";break;
                case '5':currency[i]="EUR";
            }
            switch (time){
                case 0:depositTime[i]="null";break;
                case 1:depositTime[i]="1";break;
                case 5:depositTime[i]="5";
            }
        }
        model.addAttribute("depositTime",depositTime);
        model.addAttribute("index",index);
        model.addAttribute("currency",currency);
        model.addAttribute("allSubAccount",allSubAccount);
        model.addAttribute("account",id);
        return "e_bank/debitcard/card_info";
    }
    @GetMapping("/banksystem/e_bank/debitcard/transaction")
    public String getTransactionDeatilPage(Model model,HttpSession session){
        E_bankAccount ea= (E_bankAccount) session.getAttribute("euser");
        model.addAttribute("euser",ea);
        model.addAttribute("list",savingService.findTransactionsByAccount(ea.getAccount()));
        session.setAttribute("fromTime","");
        session.setAttribute("toTime","");
        session.setAttribute("subaccount","");
        return "e_bank/debitcard/transaction";
    }
    @PostMapping("/banksystem/e_bank/debitcard/transaction")
    public String getTransactionList(@RequestParam String sub_account,String detail,String from_date,String to_date,Model model, HttpSession session){
        String account=((E_bankAccount) session.getAttribute("euser")).getAccount();
        System.out.println(account + " " + sub_account + " " + detail + " " + from_date + " " + to_date);
        model.addAttribute("euser",session.getAttribute("euser"));
        List<Transaction> list = savingService.findTransactions(account,sub_account,detail,from_date,to_date);
        model.addAttribute("list",list);
        session.setAttribute("fromTime",from_date);
        session.setAttribute("toTime",to_date);
        session.setAttribute("subaccount",sub_account);
        return "e_bank/debitcard/transaction";
    }
    @GetMapping("/banksystem/e_bank/debitcard/transfer")
    String getTranser(Model model,HttpSession session,HttpServletRequest request){
        E_bankAccount eb=(E_bankAccount)session.getAttribute("euser");
        model.addAttribute("euser",session.getAttribute("euser"));
        String ts=request.getParameter("ts");
        if(ts!=null&&!ts.equals(""))
            model.addAttribute("ts",ts);
        List<FrequentContacts> list=savingService.findFrequentContact(eb.getAccount());
        int len=list.size();
        int []index;
        if(len>5){
            index= new int[]{0, 1, 2, 3, 4};
        }else{
            index=new int[len];
            for(int i=0;i<len;i++)
                index[i]=i;
        }
        model.addAttribute("id","");
        model.addAttribute("name","");
        model.addAttribute("index",index);
        model.addAttribute("list",list);
        return "e_bank/debitcard/transfer";
    }

    @PostMapping("/banksystem/e_bank/debitcard/transfer")
    public String postTransfer(@RequestParam String to,String to_name,
                           String currency,Double amount,
                           RedirectAttributes ra,HttpSession session,Model model){
        model.addAttribute("euser",session.getAttribute("euser"));
        String from=((E_bankAccount) session.getAttribute("euser")).getAccount();
        String ts=savingService.transferAmountNoPSD(from,to,to_name,currency,amount);
        ts="Transfer successfully!";      //ts 返回转账结果  当前默认为转账成功
        ra.addAttribute("ts",ts);
        return "redirect:/banksystem/e_bank/debitcard/transfer";
    }
    @GetMapping("/banksystem/ebank/debitcard/chooseFrequentContact/{id}/{name}")
    public String chooseFrequentContact(@PathVariable String id, @PathVariable String name,
                                        Model model,HttpSession session){
        E_bankAccount eb=(E_bankAccount)session.getAttribute("euser");
        model.addAttribute("euser",session.getAttribute("euser"));
        List<FrequentContacts> list=savingService.findFrequentContact(eb.getAccount());
        int len=list.size();
        int []index;
        if(len>5){
            index= new int[]{0, 1, 2, 3, 4};
        }else{
            index=new int[len];
            for(int i=0;i<len;i++)
                index[i]=i;
        }
        model.addAttribute("id",id);
        model.addAttribute("name",name);
        model.addAttribute("index",index);
        model.addAttribute("list",list);
        return "e_bank/debitcard/transfer";
    }

    @GetMapping("/banksystem/e_bank/debitcard/getallincome")
    @ResponseBody
    public HashMap<String,Object> getAllInccome(HttpSession session){
        HashMap<String,Object> map=new HashMap<>();
        String fromTime= (String) session.getAttribute("fromTime");
        String toTime=(String) session.getAttribute("toTime");
        String sub_account=(String) session.getAttribute("subaccount");
        String account=((E_bankAccount)session.getAttribute("euser")).getAccount();
        List<Transaction> list = savingService.findTransactions(account,sub_account,"1",fromTime,toTime);
        Double transfer=0.0;
        Double deposit=0.0;
        Double interest=0.0;
        Double fromfixed=0.0;
        for(Transaction t:list){
            String type=t.getDetail().split(" ")[0];
            if(type.equals("Transfer")){
                transfer+=t.getAmount();
            }
            if(type.equals("Deposit")){
                deposit+=t.getAmount();
            }
            if(type.equals("GET")){
                interest+=t.getAmount();
            }
            if(type.equals("FROM")){
                fromfixed+=t.getAmount();
            }
        }
        map.put("Transfer",transfer);
        map.put("Deposit",deposit);
        map.put("Interest",interest);
        map.put("From Fixed Account",fromfixed);
        System.out.println(map.toString());

        return map;
    }
    @GetMapping("/banksystem/e_bank/debitcard/getalloutcome")
    @ResponseBody
    public HashMap<String,Object> getAllOutccome(HttpSession session){
        HashMap<String,Object> map=new HashMap<>();
        String fromTime= (String) session.getAttribute("fromTime");
        String toTime=(String) session.getAttribute("toTime");
        String sub_account=(String) session.getAttribute("subaccount");
        String account=((E_bankAccount)session.getAttribute("euser")).getAccount();
        List<Transaction> list = savingService.findTransactions(account,sub_account,"2",fromTime,toTime);
        Double transfer=0.0;
        Double draw=0.0;
        for(Transaction t:list){
            String type=t.getDetail().split(" ")[0];
            if(type.equals("Transfer")){
                transfer+=t.getAmount();
            }
            if(type.equals("Draw")){
                draw+=t.getAmount();
            }
        }
        map.put("Transfer",-1*transfer);
        map.put("Draw",-1*draw);
        System.out.println(map.toString());
        return map;
    }
}
