package com.banksystem.controller;

import com.banksystem.entity.E_bankAccount;
import com.banksystem.entity.DebitSubAccount;
import com.banksystem.entity.InterestRate;
import com.banksystem.entity.User;
import com.banksystem.service.DebitSubAccountService;
import com.banksystem.service.E_bankService;
import com.banksystem.service.InterestRateService;
import com.banksystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

@Controller
@EnableAutoConfiguration
public class LoginController {
    @Autowired
    UserService userService;
    @Autowired
    E_bankService e_bankService;
    @Autowired
    InterestRateService interestRateService;
    @Autowired
    DebitSubAccountService subAccountService;
    @GetMapping("/banksystem/login/manage")
    public String loginHome(Model model, HttpServletRequest request){
        model.addAttribute("user",new User());
        Map<String,?> m = RequestContextUtils.getInputFlashMap(request);
        InterestRateService.init(subAccountService.findByAccount(".*"));
        if(m == null)
            model.addAttribute("login_message","LOGIN");
        else
            model.addAttribute("login_message",m.get("login_message").toString());
        return "login/manage_login";
    }
    @GetMapping("/banksystem/logout/manage")
    public String logout(Model model,HttpServletRequest request,HttpSession httpSession){
        httpSession.removeAttribute("user");
        return "redirect:/banksystem/login/manage";
    }
    @PostMapping("/banksystem/login/manage")
    public String login(User user, Model model, HttpSession httpSession,RedirectAttributes ra){
        int login_code = userService.checkUser(user);
        String login_message ="";
        switch (login_code){
            case UserService.ACCOUNT_NOT_EXIST:
                login_message = "ACCOUNT_NOT_EXIST";
                break;
                case UserService.CORRECT_PASSWORD:
                    login_message = "CORRECT_PASSWORD";
                    break;
                    case UserService.UPDATE_PASSWORD:
                        login_message = "UPDATE_PASSWORD";
                        break;
                        case UserService.WRONG_PASSWORD:
                            login_message = "WRONG_PASSWORD";
                            break;
        }
        if(login_code != UserService.CORRECT_PASSWORD){
            //model.addAttribute("login_message",login_message);
            ra.addFlashAttribute("login_message",login_message);
            return "redirect:/banksystem/login/manage";
        }
        httpSession.setAttribute("user",user);

        return "redirect:/banksystem/manage/home";
    }
    @GetMapping("/banksystem/manage/home")
    public String getHome(Model model,HttpSession httpSession){
        model.addAttribute("user",httpSession.getAttribute("user"));
        return "manage/home";
    }
    @GetMapping("/banksystem/login/e_bank")
    public String getEbankLoginPage(Model model){
        model.addAttribute("error_message","login");
        return "login/e_bank_login";
    }
    @PostMapping("/banksystem/login/e_bank")
    public String EbankLogin(@RequestParam String account,String password,Model model,HttpSession httpSession){
        int error_code = e_bankService.checkLogin(account,password);
        if(error_code != E_bankService.CORRECT_PASSOWRD){
            model.addAttribute("error_message",E_bankService.error_message_map.get(error_code));
            return "login/e_bank_login";
        }
        E_bankAccount user=e_bankService.findE_bankAccountByEAccount(account);
        //System.out.println("*******login"+account+" "+user.getAccount());
        httpSession.setAttribute("euser",user);
        return "redirect:/banksystem/e_bank/home";
    }
    @GetMapping("/banksystem/logout/e_bank")
    public String logOutEbank()
    {
        return "login/e_bank_login";
    }

    @GetMapping("/banksystem/e_bank/home")
    public String eBankHome(Model model,HttpSession httpSession){
        model.addAttribute("euser",httpSession.getAttribute("euser"));
        return "e_bank/home";
    }

}
