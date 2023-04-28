package com.example.rcjdemo;

import com.example.rcjdemo.common.EnableBlockSyncTask;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@SpringBootApplication
@EnableBlockSyncTask
@tk.mybatis.spring.annotation.MapperScan(basePackages = "com.example.rcjdemo.dao")
public class RcjdemoApplication {

    @RequestMapping("/")
    String home() {
        return "Hello World!";
    }

    public static void main(String[] args) {
        SpringApplication.run(RcjdemoApplication.class, args);
    }

}
