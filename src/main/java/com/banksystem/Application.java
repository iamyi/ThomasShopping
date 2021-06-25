package com.banksystem;
import com.banksystem.service.UserService;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
public class Application {
    public static void main(String[] args) {
        //System.out.println(BCrypt.hashpw("123",BCrypt.gensalt()));
        SpringApplication.run(Application.class, args);

    }
}
