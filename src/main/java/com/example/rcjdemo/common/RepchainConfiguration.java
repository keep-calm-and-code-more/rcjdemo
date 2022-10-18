package com.example.rcjdemo.common;

import com.rcjava.client.TranPostClient;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author lhc
 * @version 1.0
 * @className RepchainConfiguration
 * @date 2021年07月08日 9:21 上午
 * @description 描述
 */
@Configuration
@EnableConfigurationProperties(RepchainConfig.class)
@AutoConfigureAfter(RepchainConfig.class)
public class RepchainConfiguration {

    private final RepchainConfig repchainConfig;

    public RepchainConfiguration(RepchainConfig repchainConfig) {
        this.repchainConfig = repchainConfig;
    }

    @Bean
    public TranPostClient tranPostClient() {
        return new TranPostClient(repchainConfig.getHost());
    }
}
