package com.banksystem.controller;

import com.banksystem.service.ForexService;
import com.banksystem.service.InterestRateService;
import com.banksystem.service.TimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.sql.Time;
import java.text.SimpleDateFormat;

@Controller
@EnableAutoConfiguration
public class TimeController {
    @Autowired
    private ForexService fservice;
    @Autowired
    InterestRateService interestRateService;
    @RequestMapping("/banksystem/manage/time/get_now_time")
    @ResponseBody
    public String getNowTime(){
        return TimeService.getNowTime();
    }
    @GetMapping("/banksystem/manage/time")
    public String getTimePage(Model model, HttpSession session){
        model.addAttribute("user",session.getAttribute("user"));
        model.addAttribute("now_time",TimeService.getNowTime());
        return "manage/time/time";
    }
    @RequestMapping("/banksystem/manage/time/set_time")
    @ResponseBody
    public String setTime(HttpServletRequest request){
        long sys_time = TimeService.getSetTime().getTime();
        long now_time = sys_time;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String message = TimeService.setTimeByString(request.getParameter("data"));
        interestRateService.transferInterrest(".*","^.2.*");
        if(message.equals("SET SUCCESS"))
        try {

            long set_time = format.parse(request.getParameter("data")).getTime();
            long day = (set_time - sys_time)/(24*60*60*1000);
            for(int i=0;i<day;i++){
                now_time += 24*60*60*1000;
                interestRateService.calInterest();
                System.out.println(format.format(now_time).substring(6));
                if(format.format(now_time).substring(6).equals("6-30")){
                    interestRateService.transferInterrest(".*","^.1.*");
                }
            }
            fservice.checkOutOfDate();
        }catch (Exception e){

        }
        return message;
    }
}
