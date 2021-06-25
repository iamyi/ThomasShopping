package com.banksystem.config;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

//@Configuration
public class WebSecurityConfig extends WebMvcConfigurationSupport {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        InterceptorRegistration addInterceptor = registry.addInterceptor(new SecurityInterceptor());
        addInterceptor.addPathPatterns("/banksystem/manage/**");
        System.out.println("添加拦截器");
    }

    private class SecurityInterceptor extends HandlerInterceptorAdapter{
        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
                throws Exception {
            HttpSession session = request.getSession();
            if (session.getAttribute("user") != null)
               return true;
            // 跳转登录
            String url = "/banksystem/login/manage";
            response.sendRedirect(url);
            return false;
        }
    }
}
