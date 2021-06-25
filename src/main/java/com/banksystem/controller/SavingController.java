package com.banksystem.controller;

import com.banksystem.entity.DebitSubAccount;
import com.banksystem.entity.Transaction;
import com.banksystem.service.DebitSubAccountService;
import com.banksystem.service.SavingService;
import org.hibernate.validator.constraints.pl.REGON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.jws.WebParam;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@Controller
@EnableAutoConfiguration
public class SavingController {
    @Autowired
    DebitSubAccountService service;
    @Autowired
    SavingService savingService;
    @GetMapping("/banksystem/manage/saving/depositing_drawing")
    public String getSavingPage(Model model, HttpSession session){
        model.addAttribute("user",session.getAttribute("user"));
        model.addAttribute("sub_account",new DebitSubAccount());
        return "manage/saving/depositing_drawing/depositing_drawing";
    }
    @GetMapping("/banksystem/manage/saving/transactions")
    public String getTransactionDeatilPage(Model model,HttpSession session){
        model.addAttribute("user",session.getAttribute("user"));
        model.addAttribute("list",savingService.findTransactionsByAccount(".*"));
        return "manage/saving/transactions/transactions";
    }
    @PostMapping("/banksystem/manage/saving/transactions")
    public String getTransactionList(@RequestParam String account,String sub_account,String detail,String from_date,String to_date,Model model, HttpSession session){
        System.out.println(account + " " + sub_account + " " + detail + " " + from_date + " " + to_date);
        model.addAttribute("user",session.getAttribute("user"));
        List<Transaction> list = savingService.findTransactions(account,sub_account,detail,from_date,to_date);
        model.addAttribute("list",list);

        return "manage/saving/transactions/transactions";
    }
    @PostMapping("/banksystem/manage/saving/deposit")
    public String depositMoney(@RequestParam String name, DebitSubAccount subAccount, Model model, HttpSession session
    , RedirectAttributes ra){
        model.addAttribute("user",session.getAttribute("user"));
        System.out.println(name + "\n" + subAccount.toString());
        String message = service.checkDepositAmount(subAccount,name);
        if(message.equals("DEPOSIT SUCCESS"))
            service.depositAmount(subAccount,"Deposit");
        ra.addFlashAttribute("message",message);

        return "redirect:/banksystem/manage/saving/depositing_drawing/message";
    }
    @PostMapping("/banksystem/manage/saving/draw")
    public String drawAmount(@RequestParam String draw_account,String sub_account,String password,Double draw_amount,
                             Model model,HttpSession session,RedirectAttributes ra) {
        String message = service.drawAmount(draw_account,sub_account,draw_amount,password,"Draw");
        model.addAttribute("user",session.getAttribute("user"));
        model.addAttribute("sub_account",new DebitSubAccount());
        ra.addFlashAttribute("message",message);
        return "redirect:/banksystem/manage/saving/depositing_drawing/message";

    }
    @GetMapping("/banksystem/manage/saving/depositing_drawing/message")
    public String getMessagePage(Model model, HttpServletRequest request,HttpSession session,RedirectAttributes ra){
        Map<String,?> m = RequestContextUtils.getInputFlashMap(request);
        String message = (String)m.get("message");
        model.addAttribute("message",message);
        return "manage/saving/depositing_drawing/message";
    } // Need to modify
    @GetMapping("/banksystem/manage/saving/transfer")
    public String getTransferPage(Model model,HttpSession session){
        model.addAttribute("user",session.getAttribute("user"));
        return "manage/saving/transfer/transfer";
    }
    @PostMapping("/banksystem/manage/saving/transfer")
    public String transferAmount(@RequestParam String from,String to,String to_name,String password,String currency,Double amount,
                                 RedirectAttributes ra,HttpSession session,Model model){
        System.out.println(from+ "  " + "  " + to + " " + to_name + " " + password + " " + amount);
        model.addAttribute("user",session.getAttribute("user"));
        savingService.transferAmount(from,password,to,to_name,currency,amount);
        return "manage/saving/transfer/message";
    }

}
