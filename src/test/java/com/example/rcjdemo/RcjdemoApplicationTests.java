package com.example.rcjdemo;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSONObject;
import com.example.rcjdemo.common.RepchainConfig;
import com.example.rcjdemo.common.SysCert;
import com.example.rcjdemo.util.HexUtil;
import com.example.rcjdemo.util.TxHelper;
import com.google.protobuf.util.JsonFormat;
import com.rcjava.client.TranPostClient;
import com.rcjava.protos.Peer;
import com.rcjava.util.PemUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.pkcs.PKCSException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static com.rcjava.util.CertUtil.createX509Certificate;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.security.*;
import java.security.cert.X509Certificate;
import java.security.spec.ECGenParameterSpec;
import java.util.*;
import com.example.rcjdemo.common.TranClient;


/**
 * 重要提示：私钥不对的话签名交易不会出现在链上
 */

@ActiveProfiles("test")
@SpringBootTest(classes = RcjdemoApplication.class)
@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RcjdemoApplicationTests {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    String[] apiAuthList = new String[]{"chaininfo.chaininfo", "chaininfo.node", "chaininfo.getcachetransnumber", "chaininfo.getAcceptedTransNumber", "block.hash", "block.blockHeight", "block.getTransNumberOfBlock", "block.blocktime", "block.blocktimeoftran", "block.stream", "transaction", "transaction.stream", "transaction.postTranByString", "transaction.postTranStream", "transaction.postTran", "transaction.tranInfoAndHeight", "db.query"};
    SysCert sysCert = SysCert
            .builder()
            .cert("-----BEGIN CERTIFICATE-----\n" +
                    "MIIBZDCCAQqgAwIBAgIGAYEEX3LxMAoGCCqGSM49BAMCMDkxFDASBgNVBAMMC3N1\n" +
                    "cGVyX2FkbWluMQ4wDAYDVQQLDAVpc2NhczERMA8GA1UECgwIUmVwQ2hhaW4wHhcN\n" +
                    "MjIwNTI3MDcxNjEzWhcNMjcwNTI2MDcxNjEzWjA5MRQwEgYDVQQDDAtzdXBlcl9h\n" +
                    "ZG1pbjEOMAwGA1UECwwFaXNjYXMxETAPBgNVBAoMCFJlcENoYWluMFkwEwYHKoZI\n" +
                    "zj0CAQYIKoZIzj0DAQcDQgAE0SsXKYp6BqzCk0BvfKPSIeCtHEAE2N53xM2CkPNE\n" +
                    "3lueJp8G6S3jHJuzesV/1IMmbzVzk8xMotgMfJFEOcAtBDAKBggqhkjOPQQDAgNI\n" +
                    "ADBFAiEAh8uOog9ZkpP939xaTqna8kGpLwYmZ6FukL6C85VgweQCIC/2hSUY/bJO\n" +
                    "UQ27QWcjGxBTGdW1GfLS/4HNr9h923IJ\n" +
                    "-----END CERTIFICATE-----")
            .certName("super_admin")
            .creditCode("identity-net:951002007l78123233")
            .privateKey("-----BEGIN PRIVATE KEY-----\n" +
                    "MIGTAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBHkwdwIBAQQg9tcuQ6E4xLDtasB/\n" +
                    "xofAdDsxE4OeYcUp1DuTIswsZhWgCgYIKoZIzj0DAQehRANCAATRKxcpinoGrMKT\n" +
                    "QG98o9Ih4K0cQATY3nfEzYKQ80TeW54mnwbpLeMcm7N6xX/UgyZvNXOTzEyi2Ax8\n" +
                    "kUQ5wC0E\n" +
                    "-----END PRIVATE KEY-----")
            .build();



    @Autowired
    private TranPostClient tranPostClient;
    @Autowired
    public Dict d;
    @Autowired
    private RepchainConfig repchainConfig;

    private final String itemID = "xyz12345678";

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
        System.out.printf("queryState  key: %s\tresult: %s\n", key, resp);
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
     * 基础存证合约测试，与数据提交无关
     *
     * @throws IOException
     * @throws OperatorCreationException
     * @throws PKCSException
     * @throws InterruptedException
     */
    @Test
    @Order(1)
    void testBasic() throws IOException, OperatorCreationException, PKCSException, InterruptedException {
        String scOfInterest = "ContractAssetsTPL";  //测试的是基础存证合约
//        TxHelper txh = new TxHelper("identity-net:951002007l78123233", "super_admin", "951002007l78123233.super_admin2.0.pem");
        TxHelper txh = new TxHelper("identity-net:user", "cert_identity-net:user", "identity-net-user.pem");
        String randomkey = RandomUtil.randomString(10);
        Dict argDict = Dict.create()
                .set(randomkey, "value");
        Peer.Transaction tx = txh.callArgTx(scOfInterest, 1, "putProof", argDict);
        String txid = tx.getId();
        JSONObject r = tranPostClient.postSignedTran(tx);
        System.out.println(JSONUtil.toJsonStr(r));
        Thread.sleep(30000); // 等待BlockSync同步，上限为30s
        assertTrue(d.containsKey(txid));
        String result = (String) queryState(randomkey, scOfInterest);
        System.out.println(result);
        assertEquals(result, "value");
    }

    /**
     * 管理员注册epai公钥证书上链
     *
     * @throws IOException
     * @throws OperatorCreationException
     * @throws PKCSException
     * @throws InterruptedException
     */
    @Test
    @Order(2)
    void testSignUpUser() throws IOException, OperatorCreationException, PKCSException, InterruptedException {
        String creditCode = "identity-net:EPAI";
        String certName = "cert_EPAI";
        TxHelper txh = new TxHelper();  //加载
        // 证书pem字符串
        String certPem =
                "-----BEGIN CERTIFICATE-----\n" +
                        "MIIBLzCB1qADAgECAhEh7GFgbHCjSTVeSxwRXuJibDAKBggqhkjOPQQDAjAZMRcw\n" +
                        "FQYDVQQDDA5jb3BSZWdVc2VyRVBBSTAiGA8yMDIyMDMyNTAyMTE0M1oYDzIxMjEw\n" +
                        "MzAxMDIxMTQzWjAZMRcwFQYDVQQDDA5jb3BSZWdVc2VyRVBBSTBWMBAGByqGSM49\n" +
                        "AgEGBSuBBAAKA0IABAZRRIwuTTHLwvy+Sah5t0RX5qix0ShtobpYFDMCD8FM4PgJ\n" +
                        "NmS47nogxqp1pgpYF00ra61mx3e424sW+teCPe4wCgYIKoZIzj0EAwIDSAAwRQIg\n" +
                        "K2s7S/95jXgDaYujkagUv28tq1OD7gYDhks1/wGtjcsCIQDMYjXxzFqvKltAkzNX\n" +
                        "2gbVq28+BX69qtAZFCDLX+PFDw==\n" +
                        "-----END CERTIFICATE-----";
        Peer.Certificate cert_proto = Peer.Certificate.newBuilder()
                .setCertificate(certPem)
                .setAlgType("SHA256withECDSA")
                .setCertValid(true)
                .setCertType(Peer.Certificate.CertType.CERT_AUTHENTICATION)
                .setId(Peer.CertId.newBuilder().setCreditCode(creditCode).setCertName(certName).build())
                .setCertHash(DigestUtils.sha256Hex(certPem.replaceAll("\r\n|\r|\n|\\s", "")))
                .build();

        Dict infoDict = Dict.create().set("userType", "submitter");
        Peer.Signer signer = Peer.Signer.newBuilder()
                .setName("epai")
                .setCreditCode(creditCode)
                .setMobile("18888888888")
                .setSignerInfo(JSONUtil.toJsonStr(infoDict))
                .addAllAuthenticationCerts(Arrays.asList(cert_proto))
                .setSignerValid(true)
                .build();

        Peer.Transaction tran = txh.callArgTx("RdidOperateAuthorizeTPL", 1, "signUpSigner", JsonFormat.printer().print(signer));
        JSONObject res = tranPostClient.postSignedTran(tran);
        System.out.println(res);
        Thread.sleep(30000);
        assertTrue(d.containsKey(tran.getId()));
    }

    /**
     * 用于提交文物身份上链
     *
     * @throws IOException
     * @throws OperatorCreationException
     * @throws PKCSException
     * @throws InterruptedException
     */
    @Test
    @Order(3)
    void testRegister() throws IOException, OperatorCreationException, PKCSException, InterruptedException {
        String scOfInterest = "CREvidence";  //测试的是文物合约
        //向区块链上提交数据，需要将对应的私钥置于src/main/resources
        TxHelper txh = new TxHelper("identity-net:EPAI", "cert_EPAI", "epai.pem");
        Dict argDict = Dict.create()
                .set("id", itemID) //必填，需保证唯一性，这个是业务层面的ID，尽量能与原始数据记录有一定对应关系
                .set("itemName", "测试文物") //必填
                .set("registerType", "拍行") //必填，拍行、文物商店、其它
                .set("imgList", new String[]{"http://example.com/e.jpg"}) //至少提供一个图片地址
                .set("ownerStr", "某平台用户xxx") // 用于首次登记时显示所有者名称，实际上首次登记所有权是记录在提交者名下
                .set("其它可选属性1", "你好我是取值") //其它非必填属性
                .set("其它可选属性2", "你好我是取值");
        Peer.Transaction tx = txh.callArgTx(scOfInterest, 1, "register_item", argDict);
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
     * 管理员冻结功能测试,与数据提交无关
     *
     * @throws IOException
     * @throws OperatorCreationException
     * @throws PKCSException
     * @throws InterruptedException
     */
    @Test
    @Order(4)
    void testStatusFreeze() throws IOException, OperatorCreationException, PKCSException, InterruptedException {
        String scOfInterest = "CREvidence";  //测试的是文物合约
        TxHelper txh = new TxHelper();
        Dict argDict = Dict.create()
                .set("id", itemID)
                .set("status", "freeze");
        Peer.Transaction tx = txh.callArgTx(scOfInterest, 1, "set_item_status", argDict);
        String txid = tx.getId();
        JSONObject r = tranPostClient.postSignedTran(tx);
        System.out.println(JSONUtil.toJsonStr(r));
        Thread.sleep(30000); // 等待BlockSync同步，上限为30s
        assertTrue(d.containsKey(txid));
        cn.hutool.json.JSONObject result = JSONUtil.parseObj(queryState("_app_" + itemID + "\\status", scOfInterest));
        assertEquals(result.get("status"), "freeze");
    }

    /**
     * 管理员冻结功能测试,与数据提交无关
     *
     * @throws IOException
     * @throws OperatorCreationException
     * @throws PKCSException
     * @throws InterruptedException
     */
    @Test
    @Order(5)
    void testStatusFreezeNotSuccess() throws IOException, OperatorCreationException, PKCSException, InterruptedException {
        String scOfInterest = "CREvidence";  //测试的是文物合约
        TxHelper txh = new TxHelper("identity-net:EPAI", "cert_EPAI", "epai.pem");
        Dict argDict = Dict.create()
                .set("id", itemID)
                .set("status", "invalid");
        Peer.Transaction tx = txh.callArgTx(scOfInterest, 1, "set_item_status", argDict);
        String txid = tx.getId();
        JSONObject r = tranPostClient.postSignedTran(tx);
        System.out.println(JSONUtil.toJsonStr(r));
        Thread.sleep(30000); // 等待BlockSync同步，上限为30s
        assertFalse(d.containsKey(txid));
    }

    /**
     * 管理员冻结功能测试,与数据提交无关
     *
     * @throws IOException
     * @throws OperatorCreationException
     * @throws PKCSException
     * @throws InterruptedException
     */
    @Test
    @Order(6)
    void testTransferNotSuccess() throws IOException, OperatorCreationException, PKCSException, InterruptedException {
        String scOfInterest = "CREvidence";  //测试的是文物合约
        TxHelper txh = new TxHelper("identity-net:EPAI", "cert_EPAI", "epai.pem");

        Dict to = Dict.create()
                .set("uid", "EPAI")
                .set("ownerStr", "XX平台XX商家用户1001");
        Dict remark = Dict.create()
                .set("随便设置的", "各种其它属性");
        Dict argDict = Dict.create()
                .set("id", itemID)
                .set("transferType", "拍卖") //直购
                .set("to", to)
                .set("remark", remark);
        Peer.Transaction tx = txh.callArgTx(scOfInterest, 1, "transfer_item", argDict);
        String txid = tx.getId();
        JSONObject r = tranPostClient.postSignedTran(tx);
        System.out.println(JSONUtil.toJsonStr(r));
        Thread.sleep(30000); // 等待BlockSync同步，上限为30s
        assertFalse(d.containsKey(txid));
    }

    /**
     * 管理员冻结功能测试,与数据提交无关
     *
     * @throws IOException
     * @throws OperatorCreationException
     * @throws PKCSException
     * @throws InterruptedException
     */
    @Test
    @Order(7)
    void testStatusValid() throws IOException, OperatorCreationException, PKCSException, InterruptedException {
        String scOfInterest = "CREvidence";  //测试的是文物合约
        TxHelper txh = new TxHelper();
        Dict argDict = Dict.create()
                .set("id", itemID)
                .set("status", "valid");
        Peer.Transaction tx = txh.callArgTx(scOfInterest, 1, "set_item_status", argDict);
        String txid = tx.getId();
        JSONObject r = tranPostClient.postSignedTran(tx);
        System.out.println(JSONUtil.toJsonStr(r));
        Thread.sleep(30000); // 等待BlockSync同步，上限为30s
        assertTrue(d.containsKey(txid));
        cn.hutool.json.JSONObject result = JSONUtil.parseObj(queryState("_app_" + itemID + "\\status", scOfInterest));
        assertEquals(result.get("status"), "valid");
    }

    /**
     * 文物所有权转移
     *
     * @throws IOException
     * @throws OperatorCreationException
     * @throws PKCSException
     * @throws InterruptedException
     */
    @Test
    @Order(8)
    void testTransfer() throws IOException, OperatorCreationException, PKCSException, InterruptedException {
        String scOfInterest = "CREvidence";  //测试的是文物合约
        TxHelper txh = new TxHelper("identity-net:EPAI", "cert_EPAI", "epai.pem");
        Dict to = Dict.create()
                .set("uid", "identity-net:EPAI") // ! 注意，新版本提交流转记录需要网络前缀identity-net:  否则会出现错误-所有权转移目标用户不存在
                .set("ownerStr", "XX平台XX商家用户1001");
        Dict remark = Dict.create()
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
        cn.hutool.json.JSONObject owner = JSONUtil.parseObj(queryState("_app_" + itemID + "\\owner", scOfInterest)); // 查询当前所有者
        assertTrue(ObjectUtil.equal(JSONUtil.parseObj(owner).get("uid"), to.get("uid")));
        assertTrue(ObjectUtil.equal(JSONUtil.parseObj(owner).get("ownerStr"), to.get("ownerStr")));

    }

    /**
     * 注册用户并授权
     * @throws Exception
     */
    @Test
    void addSubmitterToRepchain() throws Exception {
        addPersionalService("identity-net:user");
    }

    void addPersionalService(String creditCode) throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC", "BC");
        keyPairGenerator.initialize(new ECGenParameterSpec("P-256"));
        // 生成keypair
        KeyPair keyPair = keyPairGenerator.genKeyPair();
        // 初始化Jks
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null);
        // 生成证书
        X500NameBuilder x500NameBuilder = new X500NameBuilder(BCStyle.INSTANCE);
        X500Name x500Name = x500NameBuilder.addRDN(BCStyle.CN, "cert_"+creditCode).build();
        X509Certificate x509Certificate = createX509Certificate(x500Name, keyPair, "SHA256withECDSA", 24 * 365 * 5);
        PrivateKey privateKey = keyPair.getPrivate();
        String cert = PemUtil.toPemString("CERTIFICATE", x509Certificate.getEncoded());
        System.out.println(creditCode);
        System.out.println("cert_"+creditCode);
        System.out.println(PemUtil.toPemString(privateKey, false));
        System.out.println(cert);

        // 注册用户
        // 注册用户参数
        Map<String, Object> tranMap = new HashMap<>(7);
        tranMap.put("creditCode",creditCode);
        tranMap.put("name", creditCode);
        tranMap.put("signerInfo", JSONUtil.toJsonStr(MapUtil.of(new String[][]{
                {"userType", "submitter"},
        })));
        tranMap.put("signerValid", true);
        tranMap.put("createTime", DateUtil.format(new Date(), "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
        tranMap.put("version", "1.0");
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(MapUtil.builder(new HashMap<String, Object>(7))
                .put("certificate", cert)
                .put("algType", "SHA256withECDSA")
                .put("certValid", true)
                .put("regTime", DateUtil.format(new Date(), "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))
                .put("certType", "CERT_AUTHENTICATION")
                .put("certHash", DigestUtil.sha256Hex(cert.replaceAll("\r\n|\r|\n|\\s", "")))
                .put("id", MapUtil.builder(new HashMap<String, Object>(3))
                        .put("creditCode", creditCode)
                        .put("certName", "cert_" + creditCode)
                        .put("version", "1.0").build())
                .build());
        tranMap.put("authenticationCerts", list);
        byte[] bytes = build(JSONObject.toJSONString(tranMap), "RdidOperateAuthorizeTPL","signUpSigner").toByteArray();
        String tran = HexUtil.bytesToHex(bytes);
        JSONObject res = tranPostClient.postSignedTran(tran);
        System.out.println(JSONUtil.toJsonPrettyStr(res));
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
//        // 用户授权
        bytes = build(JSONObject.toJSONString(buildAuthList(creditCode)).toString(), "RdidOperateAuthorizeTPL", "grantOperate").toByteArray();
        tran = HexUtil.bytesToHex(bytes);
        res = tranPostClient.postSignedTran(tran);
        System.out.println(JSONUtil.toJsonPrettyStr(res));
    }

    private String[] buildAuthList(String creditCode){
        List<String> list = new ArrayList<>();
        for (String auth : apiAuthList) {
            Map<String, Object> grantedMap = MapUtil.builder(new HashMap<String, Object>(8))
                    .put("id", UUID.randomUUID().toString())
                    .put("grant", sysCert.getCreditCode())
                    .put("opId", new String[]{DigestUtil.sha256Hex("identity-net:"+auth)})
                    .put("isTransfer", 0)
                    .put("authorizeValid", true)
                    .put("createTime", DateUtil.format(new Date(), "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))
                    .put("version", "1.0")
                    .put("granted", new String[]{String.valueOf(creditCode)}).build();
            list.add(JSONObject.toJSONString(grantedMap));
        }
        return list.toArray(new String[]{});
    }


    public  Peer.Transaction build(String data, String chainCodeName, String functionName) {
        // 获取系统用户信息
        String tranId = UUID.randomUUID().toString();
        TranClient tranClient = TranClient.getClient(sysCert.getCreditCode(), sysCert.getCertName(), sysCert.getPrivateKey(), chainCodeName, 1);
        return tranClient
                .getTranCreator()
                .createInvokeTran(tranId, tranClient.getCertId(), tranClient.getChaincodeId(), functionName, data,0,"");
    }

}

