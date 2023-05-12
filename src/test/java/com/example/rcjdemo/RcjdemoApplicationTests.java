package com.example.rcjdemo;

import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
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
    private final String itemID="xyz12345678";

    @Test
    @Order(1)
    void testRegister() throws IOException, OperatorCreationException, PKCSException, InterruptedException {
        //向区块链上提交数据，需要将对应的私钥置于src/main/resources
        TxHelper txh = new TxHelper("EPAI", "cert_EPAI", "epai.pem");
        Dict argDict = Dict.create()
                .set("id", itemID) //必填，需保证唯一性，这个是业务层面的ID，尽量能与原始数据记录有一定对应关系
                .set("itemName", "测试文物") //必填
                .set("registerType", "拍行") //必填，拍行、文物商店、其它
                .set("imgList", new String[]{"http://example.com/e.jpg"}) //至少提供一个图片地址
                .set("ownerStr", "某平台用户xxx") // 用于首次登记时显示所有者名称，实际上首次登记所有权是记录在提交者名下
                .set("其它可选属性1", "你好我是取值") //其它非必填属性
                .set("其它可选属性2", "你好我是取值");
        Peer.Transaction tx = txh.callArgTx("CREvidence", 1, "register_item", argDict);
        String txid = tx.getId();
        JSONObject r = tranPostClient.postSignedTran(tx);   // 此处仅能判断是否成功提交给API，并不意味着已经在链上打包出块
        // 是否成功打包出块需要通过数据同步判断
        // 因此涉及成功打包出块后处理的业务逻辑均应写到BlockSync
        System.out.println(JSONUtil.toJsonStr(r));
        Thread.sleep(30000); // 等待BlockSync同步，上限为30s
        assertTrue(d.containsKey(txid));
        System.out.println(JSONUtil.toJsonStr(d));

    }

    /**
     * 用于查询链上最新kv状态
     * @param key
     * @return
     */
    cn.hutool.json.JSONObject queryState(String key){
        String scName = "CREvidence";
        Dict queryParam = Dict.create()
                .set("chainCodeName", scName)
                .set("key", key);
        System.out.println(JSONUtil.toJsonStr(queryParam));
        String resp =  HttpRequest.post(repchainConfig.getHost()+"/leveldb/query")
                .header(Header.CONTENT_TYPE,"application/json")
                .body(JSONUtil.toJsonStr(queryParam)).execute().body();
        System.out.println(resp);
        cn.hutool.json.JSONObject respObj = JSONUtil.parseObj(resp);
        Object result = respObj.get("result");
        if (result.equals(null)){
            throw new DataRetrievalFailureException("查询不到该状态！");
        }
        return JSONUtil.parseObj(result.toString());


    }

//    @Test
//    void tempTest(){
//        cn.hutool.json.JSONObject r = queryState("_app_"+itemID+"\\owner");
//        System.out.println(r.toString());
//    }

    /**
     * 管理员冻结功能测试,与数据提交无关
     * @throws IOException
     * @throws OperatorCreationException
     * @throws PKCSException
     * @throws InterruptedException
     */
    @Test
    @Order(2)
    void testStatusFreeze() throws IOException, OperatorCreationException, PKCSException, InterruptedException {
        TxHelper txh = new TxHelper("121000005l35120456", "node1", "121000005l35120456.node1.pem");
        Dict argDict = Dict.create()
                .set("id", itemID)
                .set("status", "freeze");
        Peer.Transaction tx = txh.callArgTx("CREvidence", 1, "set_item_status", argDict);
        String txid = tx.getId();
        JSONObject r = tranPostClient.postSignedTran(tx);
        System.out.println(JSONUtil.toJsonStr(r));
        Thread.sleep(30000); // 等待BlockSync同步，上限为30s
        assertTrue(d.containsKey(txid));
        cn.hutool.json.JSONObject result = queryState("_app_"+itemID+"\\status");
        assertEquals(result.get("status"),"freeze");
    }

    /**
     * 管理员冻结功能测试,与数据提交无关
     * @throws IOException
     * @throws OperatorCreationException
     * @throws PKCSException
     * @throws InterruptedException
     */
    @Test
    @Order(3)
    void testStatusFreezeNotSuccess() throws IOException, OperatorCreationException, PKCSException, InterruptedException {
        TxHelper txh = new TxHelper("EPAI", "cert_EPAI", "epai.pem");
        Dict argDict = Dict.create()
                .set("id", itemID)
                .set("status", "invalid");
        Peer.Transaction tx = txh.callArgTx("CREvidence", 1, "set_item_status", argDict);
        String txid = tx.getId();
        JSONObject r = tranPostClient.postSignedTran(tx);
        System.out.println(JSONUtil.toJsonStr(r));
        Thread.sleep(30000); // 等待BlockSync同步，上限为30s
        assertFalse(d.containsKey(txid));
    }
    /**
     * 管理员冻结功能测试,与数据提交无关
     * @throws IOException
     * @throws OperatorCreationException
     * @throws PKCSException
     * @throws InterruptedException
     */
    @Test
    @Order(4)
    void testTransferNotSuccess() throws IOException, OperatorCreationException, PKCSException, InterruptedException {
        TxHelper txh = new TxHelper("EPAI", "cert_EPAI", "epai.pem");

        Dict to = Dict.create()
                .set("uid", "EPAI")
                .set("ownerStr", "XX平台XX商家用户1001");
        Dict remark =  Dict.create()
                .set("随便设置的", "各种其它属性");
        Dict argDict = Dict.create()
                .set("id", itemID)
                .set("transferType", "拍卖") //直购
                .set("to", to)
                .set("remark", remark);
        Peer.Transaction tx = txh.callArgTx("CREvidence", 1, "transfer_item", argDict);
        String txid = tx.getId();
        JSONObject r = tranPostClient.postSignedTran(tx);
        System.out.println(JSONUtil.toJsonStr(r));
        Thread.sleep(30000); // 等待BlockSync同步，上限为30s
        assertFalse(d.containsKey(txid));
    }

    /**
     * 管理员冻结功能测试,与数据提交无关
     * @throws IOException
     * @throws OperatorCreationException
     * @throws PKCSException
     * @throws InterruptedException
     */
    @Test
    @Order(5)
    void testStatusValid() throws IOException, OperatorCreationException, PKCSException, InterruptedException {
        TxHelper txh = new TxHelper("121000005l35120456", "node1", "121000005l35120456.node1.pem");
        Dict argDict = Dict.create()
                .set("id", itemID)
                .set("status", "valid");
        Peer.Transaction tx = txh.callArgTx("CREvidence", 1, "set_item_status", argDict);
        String txid = tx.getId();
        JSONObject r = tranPostClient.postSignedTran(tx);
        System.out.println(JSONUtil.toJsonStr(r));
        Thread.sleep(30000); // 等待BlockSync同步，上限为30s
        assertTrue(d.containsKey(txid));
        cn.hutool.json.JSONObject result = queryState("_app_"+itemID+"\\status");
        assertEquals(result.get("status"),"valid");
    }
    @Test
    @Order(6)
    void registerUser() throws IOException, OperatorCreationException, PKCSException, InterruptedException {

    }

    /**
     * 文物所有权转移
     * @throws IOException
     * @throws OperatorCreationException
     * @throws PKCSException
     * @throws InterruptedException
     */
    @Test
    @Order(7)
    void testTransfer() throws IOException, OperatorCreationException, PKCSException, InterruptedException {
        TxHelper txh = new TxHelper("EPAI", "cert_EPAI", "epai.pem");

        Dict to = Dict.create()
                .set("uid", "EPAI")
                .set("ownerStr", "XX平台XX商家用户1001");
        Dict remark =  Dict.create()
                .set("随便设置的", "各种其它属性");
        Dict argDict = Dict.create()
                .set("id", itemID) // 必填，要转移的文物ID
                .set("transferType", "拍卖") //必填,拍卖、直购、其它
                .set("to", to) // 必填，至少包含uid和ownerStr
                .set("remark", remark); // 必填，内部为嵌套对象，用于记录本次转移的各种信息，拍卖场次、交易时间等等
        Peer.Transaction tx = txh.callArgTx("CREvidence", 1, "transfer_item", argDict);
        String txid = tx.getId();
        JSONObject r = tranPostClient.postSignedTran(tx);
        System.out.println(JSONUtil.toJsonStr(r));
        Thread.sleep(30000); // 等待BlockSync同步，上限为30s
        assertTrue(d.containsKey(txid));
        cn.hutool.json.JSONObject owner = queryState("_app_"+itemID+"\\owner"); // 查询当前所有者
        assertTrue(ObjectUtil.equal(JSONUtil.parseObj(owner).get("uid"), to.get("uid")));
        assertTrue(ObjectUtil.equal(JSONUtil.parseObj(owner).get("ownerStr"), to.get("ownerStr")));

    }

}

