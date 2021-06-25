package com.banksystem;
import com.banksystem.entity.User;
import com.banksystem.service.UserService;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@EnableAutoConfiguration

public class HelloWorld {

    @RequestMapping("/")
    public String hello() {
        return "hello,Spring boot!";
    }
    @Autowired
    UserService userService;
    @RequestMapping("/tmp")
    public List<User> tmp(){
        userService.tmpadd();
        List<User> u = userService.findAll();
        return u;
    }
}