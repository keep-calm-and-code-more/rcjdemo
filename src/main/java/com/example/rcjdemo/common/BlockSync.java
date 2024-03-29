package com.example.rcjdemo.common;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.EscapeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.rcjdemo.entity.ParsedTx;
import com.rcjava.client.ChainInfoClient;
import com.rcjava.exception.SyncBlockException;
import com.rcjava.protos.Peer;
import com.rcjava.sync.SyncInfo;
import com.rcjava.sync.SyncListener;
import com.rcjava.sync.SyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 区块同步服务
 *
 * @author zyf
 */
public class BlockSync implements SyncListener {


    private final Logger logger = LoggerFactory.getLogger(BlockSync.class);

    final RepchainConfig repchainConfig;
    @Autowired
    public Dict d; // 此处d仅为演示用，生产环境中应写入数据库
    @Autowired
    public String scOfInterest; // 用于过滤同步哪个合约的数据

//    @Autowired
//    @Profile("test")

    public BlockSync(RepchainConfig repchainConfig) {
        this.repchainConfig = repchainConfig;
        this.syncBlock();
    }

    /**
     * 同步区块
     */
    private void syncBlock() {
        String host = repchainConfig.getHost();
        long locHeight = repchainConfig.getBlockHeight(); //从哪个区块开始同步，拟同步的区块高度的下限，在application.yml里配置
        // 获取最新区块高度
        //Map<String, Object> block = baseMapper.selectOneSql("SELECT BLOCK_HEIGHT FROM SG_OP_BLOCK ORDER BY BLOCK_HEIGHT DESC LIMIT 0,1");

        // 在实际使用中，持久化存储block，在BLOCK_HEIGHT中保存的已经同步到的区块高度，然后在这里查询得到block，能够实现程序重新运行能继续上次同步
        Map<String, Object> block = new HashMap<>(); // 本示例为简便，直接新建的Map
        long tempLocHeight; // 用于保存当前同步到的区块高度
        if (block != null) {
            tempLocHeight = block.get("BLOCK_HEIGHT") == null ? 0 : Math.max(locHeight, Long.parseLong(String.valueOf(block.get("BLOCK_HEIGHT"))));
        } else {
            tempLocHeight = locHeight;
        }
        SyncInfo syncInfo;
        if (tempLocHeight == 0) {
            syncInfo = new SyncInfo(0, "");
        } else if (tempLocHeight > 0) {
            String locBlkHash = new ChainInfoClient(host).getBlockByHeight(tempLocHeight).getHeader().getHashPresent().toStringUtf8();
            syncInfo = new SyncInfo(tempLocHeight, locBlkHash);
        } else {
            logger.error("本地设置区块高度不能小于0");
            throw new RuntimeException("本地设置区块高度不能小于0");
        }
        SyncService syncService = SyncService.newBuilder().setHost(host).setSyncInfo(syncInfo).setSyncListener(this).build();
        Thread thread = new Thread(syncService::start, "SyncServiceThread");
        thread.start();
    }

    /**
     * 用于解析获取到的区块数据，对应RepChain上的接口/block/$idx返回的数据
     * 如需要得到链上数据，请修改此方法内部逻辑来进行解析、持久化到业务数据库
     *
     * @param block
     * @throws SyncBlockException
     */
    @Override
    public void onSuccess(Peer.Block block) throws SyncBlockException {
        try {
            // 将区块数据解析、持久化
            //先处理block，持久化存储block，把对应的BLOCK_HEIGHT存到数据库，用于实现程序重新运行能继续上次同步
            List<Peer.Transaction> txList = block.getTransactionsList(); //从Block取出其中交易
            Dict txResultDict = Dict.create();
            block.getTransactionResultsList().forEach(
                    (item) -> {
                        txResultDict.set(item.getTxId(), item);
                    }
            );
            List<Peer.TransactionResult> txResultList = block.getTransactionResultsList();

            for (int i = 0; i < txList.size(); i++) {
                Peer.Transaction tx = txList.get(i);
                Peer.TransactionResult txResult = (Peer.TransactionResult) txResultDict.get(tx.getId());
                if (!(tx.getType() == Peer.Transaction.Type.CHAINCODE_INVOKE && StrUtil.equals(tx.getCid().getChaincodeName(), scOfInterest))) {
//                    System.out.println(tx.getCid().getChaincodeName());
                    continue;
                }
                try {
                    JSONObject argobj = tx.getIpt().getArgsCount() > 0 ? JSONUtil.parseObj(tx.getIpt().getArgsList().get(0)) : null;
                    ParsedTx ptx = ParsedTx.builder()
                            .inBlockHeight(block.getHeader().getHeight())
                            .idxInBlock(i + 1)
                            .txid(tx.getId())
                            .tx(tx)
                            .contractName(tx.getCid().getChaincodeName())
                            .contractVer(tx.getCid().getVersion())
                            .funcName(tx.getIpt().getFunction())
                            .argJSONObject(argobj)
                            .txResult(txResult)
                            .txSubmitter(tx.getSignature().getCertId().getCreditCode() + "|" + tx.getSignature().getCertId().getCertName())
                            .successFlag(txResult.getErr().getCode()==0)
                            .build();
                    if (ptx.getSuccessFlag()) { // 可忽略未成功执行的tx
                        // 利用ptx这一对象，可在用原业务数据库中将已经上链出块的数据打上标记，也可以写入到一个专门维护的表，包括原业务id、inBlockHeight、idxInBlock、txid、状态等字段
                        // 在区块链定位数据所需要的参数：inBlockHeight、txid，上链的数据通过这两个参数可以查到，idxInBlock不是必须的
                        // 如有需要展示的属性，可自行从ptx获取
                        d.set(tx.getId(), ptx);
                    }
                } catch (Exception e) {
                    throw new SyncBlockException(e);
                }
            }

        } catch (Exception ex) {
            throw new SyncBlockException(ex);
        }
    }

    @Override
    public void onError(SyncBlockException syncBlockException) {
        logger.error("同步区块出现错误：{}", syncBlockException.getMessage(), syncBlockException);
    }
}
