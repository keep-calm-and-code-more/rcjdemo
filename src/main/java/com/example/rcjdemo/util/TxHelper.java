package com.example.rcjdemo.util;


import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.rcjava.protos.Peer;
import com.rcjava.tran.impl.InvokeTran;
import com.rcjava.util.KeyUtil;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.pkcs.PKCSException;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.PrivateKey;

public class TxHelper {

    private  Peer.CertId certId;
    private PrivateKey privateKey;

    /**
     *
     * @param creditCode
     * @param certName
     * @param pkFile 放resources里
     * @throws IOException
     * @throws OperatorCreationException
     * @throws PKCSException
     */
    public TxHelper(String creditCode, String certName, String pkFile) throws IOException, OperatorCreationException, PKCSException {
        ClassPathResource resource = new ClassPathResource(pkFile);
        this.privateKey = KeyUtil.generatePrivateKey(new PEMParser(resource.getReader(Charset.defaultCharset())), "");
        this.certId = Peer.CertId.newBuilder()
                .setCreditCode(creditCode)
                .setCertName(certName)
                .build(); // 签名ID

    }

    /**
     *
     * @param scName 智能合约名称
     * @param scVer 智能合约版本
     * @param funcName 智能合约方法
     * @param argDict 参数
     * @return
     */
    public Peer.Transaction callArgTx(String scName, Integer scVer, String funcName, Dict argDict){
        Peer.ChaincodeId chaincodeId = Peer.ChaincodeId.newBuilder()
                .setChaincodeName(scName)
                .setVersion(scVer)
                .build();
        Peer.ChaincodeInput chaincodeInput = Peer.ChaincodeInput.newBuilder()
                .setFunction(funcName)
                .addArgs(JSONUtil.toJsonStr(argDict))
                .build();

        InvokeTran invokeTran = InvokeTran.newBuilder()
                .setTxid(IdUtil.simpleUUID())
                .setChaincodeInput(chaincodeInput)
                .setCertId(certId)
                .setChaincodeId(chaincodeId)
                .setPrivateKey(privateKey)
                .setSignAlgorithm("SHA256withECDSA")
                .build();
        return invokeTran.getSignedTran();
    }
}
