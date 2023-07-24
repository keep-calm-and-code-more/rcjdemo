package com.example.rcjdemo.entity;

import cn.hutool.json.JSONObject;
import com.rcjava.protos.Peer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ParsedTx {
    private String txid;
    private int idxInBlock; // 为区块中第几个交易，从1开始
    private long inBlockHeight;  // 所属区块高度，从1开始
    private Peer.Transaction tx; // 签名交易原始对象
    private JSONObject argJSONObject; // 提交签名交易tx时构造的JSON参数
    private String argObject; // 非JSON参数
    private String contractName; // 智能合约名称
    private int contractVer; // 智能合约版本
    private String funcName; // 调用的智能合约方法名称
    private Peer.TransactionResult txResult; // 签名交易调用智能合约执行的结果，如成功则包含合约内部状态读写
    private String txSubmitter; // 签名交易提交者的链上身份，或者说tx是谁签的名
    private Boolean successFlag; // 签名交易调用智能合约是否成功执行

}
