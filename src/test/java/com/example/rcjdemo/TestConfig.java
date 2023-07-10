package com.example.rcjdemo;

import cn.hutool.core.lang.Dict;
import com.rcjava.client.TranPostClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestConfig {
    @Bean
    public Dict createDict() {
        return Dict.create();
    }
    @Bean
    public String scOfInterest(){
        return  "ContractAssetsTPL";
    }
}
