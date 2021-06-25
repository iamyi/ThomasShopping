package com.banksystem.service;

import com.banksystem.entity.ForexOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

@Service
@EnableAutoConfiguration
public class TimeService {
    @Autowired
    private ForexService fservice;
    public static long system_time = new java.util.Date().getTime();
    public static String getNowTime(){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = format.format(system_time);
        return time;
    }
    public static Timestamp getSetTime(){
         return new Timestamp(system_time);
    }
    @Scheduled(cron="0/1 * *  * * ? ")
    public static void time_flow(){
        system_time += 1000;

        //System.out.println(TimeService.getNowTime());
    }

    public static String setTimeByString(String date){

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            long set_time = format.parse(date).getTime();
            if( set_time < system_time) {
                return "CAN NOT RETURN TO PAST";
            }
            system_time = set_time;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return "SET SUCCESS";
    }
}
