package com.banksystem.controller;

import com.banksystem.entity.User;
import com.banksystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.jws.WebParam;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@Controller
@EnableAutoConfiguration
public class UserController {
    @Autowired
    UserService userService;
    @GetMapping("/banksystem/manage/account/create")
    public String getCreateUserPage(Model model, HttpSession httpSession, HttpServletRequest request, RedirectAttributes ra){
        model.addAttribute("user",httpSession.getAttribute("user"));
        model.addAttribute("user_type",new String());
        return "manage/account/create_account";
    }
    @PostMapping("banksystem/manage/account/create")
    public String createUser(String user_type,Model model,HttpServletRequest request,RedirectAttributes ra){
        int message_code;
        //System.out.println("user_type"+ user_type);
        User user = userService.createUser(Integer.parseInt(user_type));
        ra.addFlashAttribute("new_user",user);

        return "redirect:/banksystem/manage/account/create/success";
    }
    @GetMapping("/banksystem/manage/account/create/success")
    public String getCreateSuccessPage(Model model,RedirectAttributes ra,HttpServletRequest request,HttpSession session){
        model.addAttribute("user",session.getAttribute("user"));
        Map<String,?> m = RequestContextUtils.getInputFlashMap(request);
        User new_user = (User)m.get("new_user");
        model.addAttribute("new_user",new_user);
        return "manage/account/create_success";
    }
    @GetMapping("/banksystem/manage/account/edit")
    public String editUser(Model model,RedirectAttributes ra,HttpSession session){
        model.addAttribute("user",session.getAttribute("user"));
        List<User> allUser=userService.findAll();
        model.addAttribute("allUser",allUser);
        return "manage/account/edit_account";
    }
    @PostMapping("/banksystem/manage/account/editPsd")
    @ResponseBody
    public String editPsd(@RequestParam String id,String psd){
        return userService.editPSD(id,psd);
    }
    @PostMapping("/banksystem/manage/account/delAccount")
    @ResponseBody
    public String editPsd(@RequestParam String id){
        return userService.delAccount(id);
    }
}
