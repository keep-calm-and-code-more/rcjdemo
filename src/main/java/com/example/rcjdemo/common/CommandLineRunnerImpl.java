package com.example.rcjdemo.common;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSONObject;
import com.example.rcjdemo.util.HexUtil;
import com.rcjava.client.TranPostClient;
import com.rcjava.protos.Peer;
import com.rcjava.util.PemUtil;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.util.*;


/**
 * @author lhc
 * @date 2020-10-09
 * @description springboot启动执行配置类
 */
@Component
public class CommandLineRunnerImpl implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(CommandLineRunnerImpl.class);
    String[] apiAuthList = new String[]{"chaininfo.chaininfo", "chaininfo.node", "chaininfo.getcachetransnumber", "chaininfo.getAcceptedTransNumber", "block.hash", "block.blockHeight", "block.getTransNumberOfBlock", "block.blocktime", "block.blocktimeoftran", "block.stream", "transaction", "transaction.stream", "transaction.postTranByString", "transaction.postTranStream", "transaction.postTran", "transaction.tranInfoAndHeight", "db.query"};

    final TranPostClient tranPostClient;
    final RepchainConfig repchainConfig;
    //    @Autowired
//    List<CacheService> cacheServices;
    public CommandLineRunnerImpl(TranPostClient tranPostClient,RepchainConfig repchainConfig) {
        this.tranPostClient = tranPostClient;
        this.repchainConfig = repchainConfig;
    }

    @Override
    public void run(String... args) throws Exception {
//        String pemString = FileUtil.readString(new File(repchainConfig.getPemPath()), StandardCharsets.UTF_8);
//        PEMParser stringParser = new PEMParser(new StringReader(pemString));
//        PrivateKey privateKey = new JcaPEMKeyConverter().setProvider(new BouncyCastleProvider()).getPrivateKey((PrivateKeyInfo) stringParser.readObject());
//        SysCert sysCert = SysCert.builder()
//                .creditCode(repchainConfig.getCreditCode())
//                .certName(repchainConfig.getCertName())
//                .privateKey(PemUtil.toPemString(privateKey,false))
//                .build();
//        byte[] bytes = build(JSONObject.toJSONString(buildAuthList(repchainConfig.getGrantedCreditCode())), "RdidOperateAuthorizeTPL", "grantOperate",sysCert).toByteArray();
//        String tran = HexUtil.bytesToHex(bytes);
//        JSONObject res = tranPostClient.postSignedTran(tran);
//        logger.info(res.toString());
//        logger.info("start success");
    }
    private String[] buildAuthList(String creditCode){
        List<String> list = new ArrayList<>();
        for (String auth : apiAuthList) {
            Map<String, Object> grantedMap = MapUtil.builder(new HashMap<String, Object>(8))
                    .put("id", UUID.randomUUID().toString())
                    .put("grant", repchainConfig.getCreditCode())
                    .put("opId", new String[]{DigestUtil.sha256Hex(repchainConfig.getNetworkId()+":"+auth)})
                    .put("isTransfer", 0)
                    .put("authorizeValid", true)
                    .put("createTime", DateUtil.format(new Date(), "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))
                    .put("version", "1.0")
                    .put("granted", new String[]{String.valueOf(creditCode)}).build();
            list.add(JSONObject.toJSONString(grantedMap));
        }
        return list.toArray(new String[]{});
    }
    public  Peer.Transaction build(String data, String chainCodeName, String functionName,SysCert sysCert) {
        // 获取系统用户信息
        String tranId = UUID.randomUUID().toString();
        TranClient tranClient = TranClient.getClient(sysCert.getCreditCode(), sysCert.getCertName(), sysCert.getPrivateKey(), chainCodeName, 1);
        return tranClient
                .getTranCreator()
                .createInvokeTran(tranId, tranClient.getCertId(), tranClient.getChaincodeId(), functionName, data,0,"");
    }
}
