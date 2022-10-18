package com.example.rcjdemo.common;

import com.rcjava.client.ChainInfoClient;
import com.rcjava.exception.SyncBlockException;
import com.rcjava.protos.Peer;
import com.rcjava.sync.SyncInfo;
import com.rcjava.sync.SyncListener;
import com.rcjava.sync.SyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;

/**
 * 区块同步服务
 *
 * @author zyf
 */
public class BlockSync implements SyncListener {


    private final Logger logger = LoggerFactory.getLogger(BlockSync.class);

    final RepchainConfig repchainConfig;

    public BlockSync(RepchainConfig repchainConfig) {
        this.repchainConfig = repchainConfig;
        this.syncBlock();
    }

    /**
     * 同步区块
     */
    private void syncBlock() {
        String host = repchainConfig.getHost();
        long locHeight = repchainConfig.getBlockHeight();
        // 获取最新区块高度
        //Map<String, Object> block = baseMapper.selectOneSql("SELECT BLOCK_HEIGHT FROM SG_OP_BLOCK ORDER BY BLOCK_HEIGHT DESC LIMIT 0,1");
        Map<String, Object> block = new HashMap<>();
        long tempLocHeight;
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


    @Override
    public void onSuccess(Peer.Block block) throws SyncBlockException {
        try {
            // 将区块数据持久化
            // blockSyncService.saveBlock(block);
            HashMap<String, Peer.Transaction> transMap = new HashMap<String, Peer.Transaction>();
            block.getTransactionsList().forEach(trans -> transMap.put(trans.getId(), trans));
            HashMap<String, Peer.TransactionResult> transResultMap = new HashMap<>();
            Context context = new Context(block, transMap, transResultMap);
        } catch (Exception ex) {
            throw new SyncBlockException(ex);
        }
    }

    @Override
    public void onError(SyncBlockException syncBlockException) {
        logger.error("同步区块出现错误：{}", syncBlockException.getMessage(), syncBlockException);
    }
}
