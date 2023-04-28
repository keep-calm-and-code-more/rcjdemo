package com.example.rcjdemo;

import cn.hutool.core.lang.Dict;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.example.rcjdemo.util.TxHelper;
import com.rcjava.client.TranPostClient;
import com.rcjava.protos.Peer;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.pkcs.PKCSException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;

@ActiveProfiles("test")
@SpringBootTest(classes=RcjdemoApplication.class)
class RcjdemoApplicationTests {
    @Autowired
    private TranPostClient tranPostClient;
    @Autowired
    public Dict d;

    @Test
    void contextLoads() {
    }

    @Test
    void testTransPost() throws IOException, OperatorCreationException, PKCSException, InterruptedException {
        //向区块链上提交数据，需要将对应的私钥置于src/main/resources
        TxHelper txh = new TxHelper("121000005l35120456", "node1", "somekey.pem");
        Dict argDict = Dict.create()
                .set("id", "xyz123456")
                .set("itemName","测试文物")
                .set("registerType","拍行")
                .set("imgList",new String[]{"http://example.com/e.jpg"})
                .set("ownerStr", "某平台用户xxx")
                .set("杂七杂八属性", "你好我是取值");
        Peer.Transaction tx = txh.callArgTx("CREvidence",1, "register_item", argDict);
        JSONObject r = tranPostClient.postSignedTran(tx);   // 此处仅能判断是否成功提交给API，并不意味着已经在链上打包出块
                                                            // 是否成功打包出块需要通过数据同步判断
                                                            // 因此涉及成功打包出块后处理的业务逻辑均应写到BlockSync
        System.out.println(JSONUtil.toJsonStr(r));
        System.out.println(JSONUtil.toJsonStr(d));
        Thread.sleep(30000);
        System.out.println(JSONUtil.toJsonStr(d));
    }

}

