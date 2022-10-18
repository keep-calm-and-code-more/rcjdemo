package com.example.rcjdemo.common;

import com.rcjava.protos.Peer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;

@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Data
public class Context {
    private Peer.Block block;
    private HashMap<String, Peer.Transaction> transactionMap;
    private HashMap<String, Peer.TransactionResult> transactionResultMap;
}
