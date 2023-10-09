package com.example.rcjdemo.common;

import com.rcjava.protos.Peer;
import com.rcjava.tran.TranCreator;
import com.rcjava.util.KeyUtil;
import org.bouncycastle.openssl.PEMParser;

import java.io.StringReader;
import java.security.PrivateKey;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lhc
 * @date 2022-04-22 10:59
 * @description 构建交易客户端
 */
public class TranClient {


    private String certFullName;
    private Peer.CertId certId;
    private Peer.ChaincodeId chaincodeId;
    private TranCreator tranCreator;
    private static final ConcurrentHashMap<String, TranClient> userClientMap = new ConcurrentHashMap<>();

    public TranClient(String creditCode, String certName, String privateKeyPem, String chianCodeName, Integer version) {
        this.chaincodeId = Peer.ChaincodeId.newBuilder().setChaincodeName(chianCodeName).setVersion(version).build();
        certFullName = creditCode + "." + certName;
        certId = Peer.CertId.newBuilder().setCreditCode(creditCode).setCertName(certName).build();
        PEMParser parser = new PEMParser(new StringReader(privateKeyPem));
        try {
            PrivateKey privateKey = KeyUtil.generatePrivateKey(parser,  "");
            tranCreator = TranCreator.newBuilder().setPrivateKey(privateKey).setSignAlgorithm("sha256withecdsa").build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static TranClient getClient(String creditCode, String certName, String privateKeyPem, String chianCodeName, Integer version) {
        if (userClientMap.containsKey(creditCode + chianCodeName + "_" + version)) {
            return userClientMap.get(creditCode + chianCodeName + "_" + version);
        } else {
            TranClient userClient = new TranClient(creditCode, certName, privateKeyPem, chianCodeName,version);
            userClientMap.put(creditCode + chianCodeName + "_" + version, userClient);
            return userClientMap.get(creditCode + chianCodeName + "_" + version);
        }
    }

    public String getCertFullName() {
        return certFullName;
    }

    public void setCertFullName(String certFullName) {
        this.certFullName = certFullName;
    }

    public Peer.CertId getCertId() {
        return certId;
    }

    public void setCertId(Peer.CertId certId) {
        this.certId = certId;
    }

    public Peer.ChaincodeId getChaincodeId() {
        return chaincodeId;
    }

    public void setElectricId(Peer.ChaincodeId chaincodeId) {
        this.chaincodeId = chaincodeId;
    }

    public TranCreator getTranCreator() {
        return tranCreator;
    }

    public void setTranCreator(TranCreator tranCreator) {
        this.tranCreator = tranCreator;
    }
}

