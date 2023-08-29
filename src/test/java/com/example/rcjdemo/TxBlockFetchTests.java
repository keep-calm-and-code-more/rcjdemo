package com.example.rcjdemo;

import cn.hutool.core.lang.Dict;
import com.example.rcjdemo.common.RepchainConfig;
import com.rcjava.client.ChainInfoClient;
import com.rcjava.client.TranPostClient;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
@SpringBootTest(classes = RcjdemoApplication.class)
@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TxBlockFetchTests {

    @Autowired
    private RepchainConfig repchainConfig;
    @Test
    void testTxNotSuccessFetch(){
        String id = "7cee56f6045443878cf17e89bd33b107";
        ChainInfoClient cic = new ChainInfoClient(repchainConfig.getHost());
        ChainInfoClient.TranAndTranResult tatr =  cic.getTranAndResultByTranId(id);
        ChainInfoClient.TranInfoAndHeight taah = cic.getTranInfoAndHeightByTranId(id);
        System.out.printf("height: %d\n", taah.getHeight());
        System.out.printf("result_err_code: %d\n", tatr.getTranResult().getErr().getCode());
        assertEquals(tatr.getTranResult().getErr().getCode(),102);
    }

    @Test
    void testTxSuccessFetch(){
        String id = "d7c3c292-365c-4952-ae78-e38f25385955";
        ChainInfoClient cic = new ChainInfoClient(repchainConfig.getHost());
        ChainInfoClient.TranAndTranResult tatr =  cic.getTranAndResultByTranId(id);
        ChainInfoClient.TranInfoAndHeight taah = cic.getTranInfoAndHeightByTranId(id);
        System.out.printf("height: %d\n", taah.getHeight());
        System.out.printf("result_err_code: %d\n", tatr.getTranResult().getErr().getCode());
        assertEquals(tatr.getTranResult().getErr().getCode(),0);
    }

}
