package com.example.rcjdemo;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.io.file.FileWriter;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSONObject;
import com.example.rcjdemo.common.RepchainConfig;
import com.example.rcjdemo.util.HexUtil;
import com.example.rcjdemo.util.TxHelper;
import com.rcjava.client.TranPostClient;
import com.rcjava.protos.Peer;
import com.rcjava.util.CertUtil;
import com.rcjava.util.KeyUtil;
import com.rcjava.util.PemUtil;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
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

import javax.swing.plaf.ListUI;

import static com.rcjava.util.CertUtil.createX509Certificate;
import static org.junit.jupiter.api.Assertions.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Date;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.Enumeration;

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

    final String tempStoreFile = "tempStore.txt";

    /**
     * 用于查询链上最新kv状态
     *
     * @param key
     * @return
     */
    Object queryState(String key, String scName) {
        return queryState("identity-net", key, scName);
    }

    Object queryState(String netID, String key, String scName) {
        Dict queryParam = Dict.create()
                .set("netId", netID)
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


    KeyStore KeyStoreFromJKSFile(String kspath, String kspass) throws CertificateException, IOException, NoSuchAlgorithmException, KeyStoreException {
        FileInputStream fis = new FileInputStream(kspath);
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(fis, kspass.toCharArray());
        fis.close();
        return keyStore;
    }

//    ArrayList<String> pvkCertFromKeyStore(KeyStore ks, String credit_code_dot_cert, String keypass) throws UnrecoverableEntryException, NoSuchAlgorithmException, KeyStoreException, IOException, CertificateException, OperatorCreationException {
//        String alias = credit_code_dot_cert;
//        KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) ks.getEntry(alias,
//                new KeyStore.PasswordProtection(keypass.toCharArray()));
//        PrivateKey privateKey = privateKeyEntry.getPrivateKey();
//        Certificate certificate = privateKeyEntry.getCertificate();
//        PublicKey publicKey = certificate.getPublicKey();
//        String pvkString = PemUtil.toPemString(privateKey, false);
//        X500NameBuilder x500NameBuilder = new X500NameBuilder(BCStyle.INSTANCE);
//        X500Name x500Name = x500NameBuilder.addRDN(BCStyle.CN, "cert_" + "121000005l35120456").build();
//        X509Certificate x509Certificate = createX509Certificate(x500Name, new KeyPair(publicKey, privateKey), "SHA256withECDSA", 24 * 365 * 5);
//        String certStr = PemUtil.toPemString("CERTIFICATE", x509Certificate.getEncoded());
//        ArrayList<String> result = new ArrayList<>();
//        result.add(pvkString);
//        result.add(certStr);
//        return result;
//    }

    //
    PrivateKey pvkFromPEMStr(String pvkPEMstr) throws IOException, OperatorCreationException, PKCSException {
        PrivateKey pvk = KeyUtil.generatePrivateKey(new PEMParser(new StringReader(pvkPEMstr)), null);
        return pvk;
    }


    boolean hasProvider(String providerName) {
        Provider[] providers = Security.getProviders();
        for (Provider provider : providers) {
            if (provider.getName().equalsIgnoreCase(providerName)) {
                return true;
            }
        }
        return false;
    }

    KeyPair generateKeyPair() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
        if (!hasProvider("BC")) {
            Security.addProvider(new BouncyCastleProvider());
        }
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC", "BC");
        keyPairGenerator.initialize(new ECGenParameterSpec("P-256"));
        // 生成keypair
        KeyPair keyPair = keyPairGenerator.genKeyPair();
        return keyPair;
    }

    ArrayList<String> pvkCertFromKeyPair(KeyPair keyPair, String creditCode) throws CertificateException, OperatorCreationException, IOException {
        X500NameBuilder x500NameBuilder = new X500NameBuilder(BCStyle.INSTANCE);
        X500Name x500Name = x500NameBuilder.addRDN(BCStyle.CN, "cert_" + creditCode).build();
        X509Certificate x509Certificate = createX509Certificate(x500Name, keyPair, "SHA256withECDSA", 24 * 365 * 5);
        PrivateKey privateKey = keyPair.getPrivate();
        String pvkString = PemUtil.toPemString(privateKey, false);
        String certStr = PemUtil.toPemString("CERTIFICATE", x509Certificate.getEncoded());
        ArrayList<String> result = new ArrayList<>();
        result.add(pvkString);
        result.add(certStr);
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
        Thread.sleep(30000); // 此处仅为了本单元测试等待BlockSync同步，上限为30s所以这里设为30s，一般不会等那么久。
        assertTrue(d.containsKey(txid));
        String result = (String) queryState(randomkey, scOfInterest);
        System.out.println(result);
        assertEquals(result, "value");
    }


    @Test
    @Order(2)
    void registerUser() throws Exception {
        String creditCode = "identity-net:110105XXXXXXXX0X3X";
        Dict txDict = Dict.create()
                .set("creditCode", creditCode)
                .set("name", creditCode) // 这里简化为使用creditCode作为用户名
                .set("signerValid", true)
                .set("createTime", DateUtil.format(new Date(), "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))
                .set("version", "1.0")
                .set("signerInfo", "{\"somekey\":\"somevalue\"}"); //支持在signerInfo中保存字符串，也可嵌入序列化为字符串的JSON

        KeyPair kp = generateKeyPair();
        ArrayList<String> pvk_cert = pvkCertFromKeyPair(kp, creditCode);
        String pvkStr_kp = pvk_cert.get(0);
        String certStr_kp = pvk_cert.get(1);
        System.out.println(pvkStr_kp); //私钥需要保存
        System.out.println(certStr_kp);
        String certName = "app";  // 这里简化为均以app喂certName，实际上用户跟cert为一对多关系，可以有不同的cert
        Dict auth = Dict.create()
                .set("certificate", certStr_kp)
                .set("algType", "SHA256withECDSA")
                .set("certValid", true)
                .set("regTime", DateUtil.format(new Date(), "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))
                .set("certType", "CERT_AUTHENTICATION")
                .set("certHash", DigestUtil.sha256Hex(certStr_kp.replaceAll("\r\n|\r|\n|\\s", "")))
                .set("id", JSONUtil.createObj()
                        .set("creditCode", creditCode)
                        .set("certName", certName)
                        .set("version", "1.0"));

        txDict.set("authenticationCerts", ListUtil.toList(auth));

        TxHelper txh = new TxHelper("identity-net:951002007l78123233", "super_admin", "951002007l78123233.super_admin2.0.pem");
        Peer.Transaction tx = txh.callArgTx("RdidOperateAuthorizeTPL", 1, "signUpSigner", txDict);
        JSONObject res = tranPostClient.postSignedTran(tx);
        System.out.println(JSONUtil.toJsonPrettyStr(res));
        Thread.sleep(30000); // 此处仅为了本单元测试等待BlockSync同步，上限为30s所以这里设为30s，一般不会等那么久。
        //注册成功后在数据库中保存用户私钥（托管模式）或分发给用户，此处为测试保存到了文件
        FileWriter writer = new FileWriter(tempStoreFile);
        cn.hutool.json.JSONObject jsonO = JSONUtil.createObj().set("1", ListUtil.toList(creditCode, certName, pvkStr_kp));
        writer.write(jsonO.toString());
    }

    /**
     * 新注册用户能够用私钥提交签名交易
     * 新注册用户可调用公共合约方法
     * @throws Exception
     */
    @Test
    @Order(3)
    void txNewUser() throws Exception {
        //模拟从数据库中取得用户私钥等信息
        FileReader fileReader = new FileReader(tempStoreFile);
        JSONArray record = (JSONArray) JSONUtil.parseObj(fileReader.readString()).get("1");
        String creditCode = (String) record.get(0);
        String certName = (String) record.get(1);
        String pvkStr_kp = (String) record.get(2);
        TxHelper txh = new TxHelper(creditCode, certName, pvkFromPEMStr(pvkStr_kp));
        String randomkey = RandomUtil.randomString(10);
        Dict argDict = Dict.create()
                .set(randomkey, "value");
        Peer.Transaction tx = txh.callArgTx(scOfInterest, 1, "putProof", argDict);
        String txid = tx.getId();
        JSONObject r = tranPostClient.postSignedTran(tx);
        System.out.println(JSONUtil.toJsonStr(r));
        Thread.sleep(30000); // 此处仅为了本单元测试等待BlockSync同步，上限为30s所以这里设为30s，一般不会等那么久。
        assertTrue(d.containsKey(txid));
        String result = (String) queryState(randomkey, scOfInterest);
        System.out.println(result);
        assertEquals(result, "value");

    }


    /**
     * 用私钥进行签名
     *
     * @param privateKey    私钥
     * @param message       原始数据
     * @param signAlgorithm 签名算法
     * @return
     */
    byte[] sign(PrivateKey privateKey, byte[] message, String signAlgorithm) {
        try {
            Signature s1 = Signature.getInstance(signAlgorithm);
            s1.initSign(privateKey);
            s1.update(message);
            return s1.sign();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return new byte[0];
    }

    /**
     * 验证签名
     *
     * @param signature     签名数据
     * @param message       原始数据
     * @param publicKey     公钥
     * @param signAlgorithm 签名算法
     * @return
     */
    public Boolean verify(byte[] signature, byte[] message, PublicKey publicKey, String signAlgorithm) {
        try {
            Signature s2 = Signature.getInstance(signAlgorithm);
            s2.initVerify(publicKey);
            s2.update(message);
            return s2.verify(signature);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Test
    @Order(4)
    void tempGetPubKey() throws Exception {
        FileReader fileReader = new FileReader(tempStoreFile);
        JSONArray record = (JSONArray) JSONUtil.parseObj(fileReader.readString()).get("1");
        String creditCode = (String) record.get(0);
        String certName = (String) record.get(1);
        String pvkStr_kp = (String) record.get(2);
        final String prefix = "cert-";
        String key = prefix + creditCode + "." + certName;
        // 给定数据（例如随机字符串），从链上取得公开的公钥的一方可对持有私钥的一方对该数据的签名进行验证

        cn.hutool.json.JSONObject r = (cn.hutool.json.JSONObject) queryState(key, "RdidOperateAuthorizeTPL");
        String certPEMstr = (String) r.get("certificate");
        System.out.println(certPEMstr);
        X509Certificate x509Certificate = CertUtil.generateX509Cert(certPEMstr);

        PrivateKey pvk = pvkFromPEMStr(pvkStr_kp);
        //用私钥对字符串asd签名，如果用于身份验证最好用随机字符串
        byte[] sign = sign(pvk, "asd".getBytes(StandardCharsets.UTF_8), "SHA256withECDSA");
        String hex = HexUtil.bytesToHex(sign);
        System.out.println(hex);
        //用公钥验签
        assertTrue(verify(HexUtil.hexToByteArray(hex), "asd".getBytes(StandardCharsets.UTF_8), x509Certificate.getPublicKey(), "SHA256withECDSA"));

    }

}

