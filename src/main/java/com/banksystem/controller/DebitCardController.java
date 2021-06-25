package com.banksystem.controller;

import com.banksystem.entity.DebitCard;
import com.banksystem.entity.DebitSubAccount;
import com.banksystem.entity.E_bankAccount;
import com.banksystem.service.DebitCardService;
import com.banksystem.service.DebitSubAccountService;
import com.banksystem.service.E_bankService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.websocket.server.PathParam;
import java.util.*;

@Controller
@EnableAutoConfiguration
public class DebitCardController {
    @Autowired
    private DebitCardService service;
    @Autowired
    private DebitSubAccountService subAccountService;
    @Autowired
    private E_bankService e_bankService;
    @GetMapping("/banksystem/manage/debitcard/create")
    public String getCreatePage(Model model, HttpSession session){
        model.addAttribute("user",session.getAttribute("user"));
        model.addAttribute("debitcard",new DebitCard());
        return "manage/debitcard/create_debitcard";
    }
    @PostMapping("/banksystem/manage/debitcard/create")
    public String createDebitCard(@RequestParam String province,String city,String area,String street,String e_bank, String e_bank_account,String e_bank_password,Model model, DebitCard debitCard, HttpSession session, RedirectAttributes ra){
        HashMap<String,String> address = new HashMap<String,String>();
        address.put("province",province);
        address.put("city",city);
        address.put("area",area);
        address.put("street",street);
        debitCard.setAddress(address);
        debitCard.setE_bank(e_bank != null?true:false);
        debitCard.setLoss(false);
        try {
            if(service.checkCreate(debitCard) == DebitCardService.CREATE_SUCCESS) {
                debitCard = service.createDebitCard(debitCard);
                if (debitCard.isE_bank()) {
                    E_bankAccount e_bankAccount = new E_bankAccount();
                    e_bankAccount.setAccount(debitCard.getAccount());
                    e_bankAccount.setE_bank_account(e_bank_account);
                    e_bankAccount.setPassword(e_bank_password);
                    if(e_bankService.checkCreate(e_bankAccount) == E_bankService.CREATE_SUCCESS)
                        e_bankService.createE_bankAccount(e_bankAccount);
                }
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        ra.addFlashAttribute("debit_card",debitCard);
        return "redirect:/banksystem/manage/debitcard/create/success";

    }
    @GetMapping("/banksystem/manage/debitcard/create/success")
    public String createDebitCardSuccess(Model model, HttpSession httpSession, HttpServletRequest request, RedirectAttributes ra){
        Map<String,?> m = RequestContextUtils.getInputFlashMap(request);
        DebitCard debitCard = (DebitCard)m.get("debit_card");
        model.addAttribute("debit_card",debitCard);
        return "manage/debitcard/create_success";
    }
    @GetMapping("/banksystem/manage/debitcard/edit")
    public String editDebitCard(Model model, HttpSession session, HttpServletRequest request,RedirectAttributes ra){
        model.addAttribute("user",session.getAttribute("user"));
        String es=request.getParameter("editSuccess");
        if(es!=null&&!es.equals(""))
            model.addAttribute("es",es);
        List<DebitCard> allCard=service.finalByAccount(".*");
        model.addAttribute("allDebitCard",allCard);
        return "manage/debitcard/edit_debitcard";
    }
    @GetMapping("/banksystem/manage/account/editCardUnit/{id}")
    public String editCardUnit(@PathVariable String id,Model model,HttpSession session){
        model.addAttribute("user",session.getAttribute("user"));
        DebitCard card=service.finalByAccount(id).get(0);
        model.addAttribute("debitcard",card);
        return "manage/debitcard/edit_card_unit";

    }
    @GetMapping("/banksystem/manage/account/show_subaccount/{id}")
    public String showSubAccount(@PathVariable String id,Model model,HttpSession session){
        model.addAttribute("user",session.getAttribute("user"));
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
        return "manage/debitcard/show_subaccount";
    }
    @PostMapping("/banksystem/manage/debitcard/submit_edit")
    public String submitEdit(@RequestParam String account,String name,
                             String province,String city,String area,
                             String street, Model model,
                             String phone,DebitCard debitCard,
                             HttpSession session,RedirectAttributes ra){
        HashMap<String,String> address = new HashMap<String,String>();
        address.put("province",province);
        address.put("city",city);
        address.put("area",area);
        address.put("street",street);
        debitCard.setName(name);
        debitCard.setPhone(phone);
        debitCard.setAddress(address);
        debitCard.setAccount(account);
        String es="editSuccess";
        ra.addAttribute("editSuccess",es);
        try {
            service.editDebitCard(debitCard);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return "redirect:/banksystem/manage/debitcard/edit";

    }
    @PostMapping("/banksystem/manage/debitcard/edit/changePSD")
    @ResponseBody
    public String changePSD(@RequestParam String id,String psd){
        return service.changePSD(id,psd);
    }
    @PostMapping("/banksystem/manage/debitcard/edit/reportLoss")
    @ResponseBody
    public String reportLoss(@RequestParam String id){
        return service.reportLoss(id);
    }
    @PostMapping("/banksystem/manage/debitcard/edit/cancelReportLoss")
    @ResponseBody
    public String cancelReportLoss(@RequestParam String id){
        return service.cancelReportLoss(id);
    }
    @PostMapping("/banksystem/manage/debitcard/edit/deleteAccount")
    @ResponseBody
    public String deleteAccount(@RequestParam String id){
        return service.deleteAccount(id);
    }

}
