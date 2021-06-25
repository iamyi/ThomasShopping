package com.banksystem.controller;

import com.banksystem.entity.InterestRate;
import com.banksystem.service.InterestRateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpSession;

@Controller
@EnableAutoConfiguration
public class InterestRateController {
    @Autowired
    InterestRateService interestRateService;
    @GetMapping("/banksystem/manage/interest_rate")
    public String getSetInterstRatePage(Model model, HttpSession session){
        model.addAttribute("user",session.getAttribute("user"));
        model.addAttribute("interest_rate",new InterestRate());
        model.addAttribute("cd_rate",interestRateService.findInterestRateByType("0").get(0).getRate());
        model.addAttribute("lt_rate",interestRateService.findInterestRateByType("1").get(0).getRate());
        return "manage/interest_rate/interest_rate";
    }
    @PostMapping("/banksystem/manage/interest_rate/set")
    public String setInterstRate(InterestRate rate,Model model,HttpSession session){
        model.addAttribute("user",session.getAttribute("user"));
        model.addAttribute("interest_rate",new InterestRate());
        interestRateService.setInterestRate(rate);
        return "redirect:/banksystem/manage/interest_rate";
    }
}
