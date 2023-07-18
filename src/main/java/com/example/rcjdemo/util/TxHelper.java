package com.example.rcjdemo.util;


import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.rcjava.protos.Peer;
import com.rcjava.tran.TranCreator;
import com.rcjava.tran.impl.InvokeTran;
import com.rcjava.util.CertUtil;
import com.rcjava.util.KeyUtil;
import lombok.Getter;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.pkcs.PKCSException;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

@Getter
public class TxHelper {

    private Peer.CertId certId;
    private PrivateKey privateKey;

    public TranCreator superTranCreator;
    public Peer.CertId superCertId;

    /**
     * 构建superAdmin
     */
    public TxHelper() throws IOException, OperatorCreationException, PKCSException {
        ClassPathResource resource = new ClassPathResource("951002007l78123233.super_admin2.0.pem");
        PrivateKey super_pri = KeyUtil.generatePrivateKey(new PEMParser(resource.getReader(Charset.defaultCharset())), "");
        superTranCreator = TranCreator.newBuilder().setPrivateKey(super_pri).setSignAlgorithm("SHA256withECDSA").build();
        superCertId = Peer.CertId.newBuilder()
                .setCreditCode("identity-net:951002007l78123233")
                .setCertName("super_admin")
                .build(); // 管理员签名ID
    }

    /**
     * @param creditCode
     * @param certName
     * @param pkFile     放resources里
     * @throws IOException
     * @throws OperatorCreationException
     * @throws PKCSException
     */
    public TxHelper(String creditCode, String certName, String pkFile) throws IOException, OperatorCreationException, PKCSException {
        this();
        ClassPathResource resource = new ClassPathResource(pkFile);
        this.privateKey = KeyUtil.generatePrivateKey(new PEMParser(resource.getReader(Charset.defaultCharset())), "");
        this.certId = Peer.CertId.newBuilder()
                .setCreditCode(creditCode)
                .setCertName(certName)
                .build(); // 签名ID

    }

    /**
     * @param creditCode
     * @param certName
     * @param pkFile
     * @throws IOException
     * @throws OperatorCreationException
     * @throws PKCSException
     */
    public TxHelper(String creditCode, String certName, File pkFile) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, OperatorCreationException, PKCSException {
        this();
        // 以后优化吧
        String pemString = FileUtil.readString(pkFile, StandardCharsets.UTF_8);
        PEMParser stringParser = new PEMParser(new StringReader(pemString));
        PrivateKey privateKey = new JcaPEMKeyConverter().setProvider(new BouncyCastleProvider()).getPrivateKey((PrivateKeyInfo) stringParser.readObject());
        // 交易的签名算法根据对应RepChain版本进行设置
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKey.getEncoded());
        this.privateKey = KeyFactory.getInstance("EC").generatePrivate(keySpec);
        this.certId = Peer.CertId.newBuilder().setCreditCode(creditCode).setCertName(certName).build(); // 签名ID

    }

    /**
     * @param scName   智能合约名称
     * @param scVer    智能合约版本
     * @param funcName 智能合约方法
     * @param argDict  参数
     * @return
     */
    public Peer.Transaction callArgTx(String scName, Integer scVer, String funcName, Dict argDict) {
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
