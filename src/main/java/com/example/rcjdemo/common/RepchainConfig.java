package com.example.rcjdemo.common;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author lhc
 * @version 1.0
 * @className RepchainConfig
 * @date 2021年07月08日 9:14 上午
 * @description 描述
 */
@Component
@ConfigurationProperties(prefix = "repchain")
@Data
public class RepchainConfig {
    private String host;
    private Long blockHeight;
}
