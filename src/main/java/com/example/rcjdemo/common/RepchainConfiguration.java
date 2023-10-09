package com.example.rcjdemo.common;

import com.rcjava.client.TranPostClient;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.ssl.SSLContexts;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

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
    public TranPostClient tranPostClient() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, UnrecoverableKeyException, KeyManagementException {
        if (repchainConfig.getEnableSSL()) {
            String keyPassword = "";
            if (StringUtils.hasText(repchainConfig.getKeyPassword())) {
                keyPassword = repchainConfig.getKeyPassword();
            }
            SSLContext sslContext = SSLContexts.custom()
                    .loadTrustMaterial(new File(repchainConfig.getServerCertJksPath()), repchainConfig.getServerJksPassword().toCharArray(), new TrustSelfSignedStrategy())
                    .loadKeyMaterial(new File(repchainConfig.getJksPath()), repchainConfig.getStorePassword().toCharArray(), keyPassword.toCharArray())
                    .build();
            return new TranPostClient(repchainConfig.getHost(), sslContext);
        }
        return new TranPostClient(repchainConfig.getHost());
    }
}
