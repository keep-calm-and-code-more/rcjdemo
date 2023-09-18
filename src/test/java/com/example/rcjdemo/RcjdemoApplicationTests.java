package com.example.rcjdemo;

import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSONObject;
import com.example.rcjdemo.common.RepchainConfig;
import com.example.rcjdemo.util.TxHelper;
import com.rcjava.client.TranPostClient;
import com.rcjava.protos.Peer;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.pkcs.PKCSException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;


/**
 * 重要提示：私钥不对的话签名交易不会出现在链上
 */

@ActiveProfiles("test")
@SpringBootTest(classes = RcjdemoApplication.class)
@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RcjdemoApplicationTests {
    @Autowired
    private TranPostClient tranPostClient;
    @Autowired
    public Dict d;
    @Autowired
    private RepchainConfig repchainConfig;
    @Autowired
    public String scOfInterest; // 用于过滤同步哪个合约的数据

    /**
     * 用于查询链上最新kv状态
     *
     * @param key
     * @return
     */
    Object queryState(String key, String scName) {
        Dict queryParam = Dict.create()
                .set("netId", "identity-net")
                .set("chainCodeName", scName)
                .set("oid", "")
                .set("key", key);
        System.out.println(JSONUtil.toJsonStr(queryParam));
        String resp = HttpRequest.post(repchainConfig.getHost() + "/db/query")
                .header(Header.CONTENT_TYPE, "application/json")
                .body(JSONUtil.toJsonStr(queryParam)).execute().body();
        System.out.println(resp);
        cn.hutool.json.JSONObject respObj = JSONUtil.parseObj(resp);
        Object result = respObj.get("result");
        if (result.equals(null)) {
            throw new DataRetrievalFailureException("查询不到该状态！");
        }
        return result;


    }


//    @Test
//    void tempTest(){
//        cn.hutool.json.JSONObject r = queryState("_app_"+itemID+"\\owner");
//        System.out.println(r.toString());
//    }


    /**
     * 测试存证功能
     *
     * @throws IOException
     * @throws OperatorCreationException
     * @throws PKCSException
     * @throws InterruptedException
     */
    @Test
    @Order(1)
    void testBasic() throws IOException, OperatorCreationException, PKCSException, InterruptedException {
        TxHelper txh = new TxHelper("identity-net:951002007l78123233", "super_admin", "951002007l78123233.super_admin2.0.pem");
        String randomkey = RandomUtil.randomString(10);
        Dict argDict = Dict.create()
                .set(randomkey, "value");
        Peer.Transaction tx = txh.callArgTx(scOfInterest, 1, "putProof", argDict);
        String txid = tx.getId();
        JSONObject r = tranPostClient.postSignedTran(tx);
        System.out.println(JSONUtil.toJsonStr(r));
        Thread.sleep(30000); // 等待BlockSync同步，上限为30s所以这里设为30s，一般不会等那么久。
        assertTrue(d.containsKey(txid));
        String result = (String) queryState(randomkey, scOfInterest);
        System.out.println(result);
        assertEquals(result, "value");
    }


}

