package com.bitcoin.merchant.app.model.rest;

import java.util.Arrays;
import java.util.Objects;

// https://rest.bitcoin.com/v2/block/detailsByHeight/593285
/*
{
"hash": "000000000000000000b82cebde15d7434a3261b2233eec47cb1d72c416247b69",
"size": 48965,
"height": 593285,
"version": 536870912,
"merkleroot": "43d7b0caed4b7bc605058670a5e414f8c600c6bef4864103165e326ba3e88de6",
"tx": [
"f3596edf72314fa4620244c62341e31caf217c70af9d0c1341501910ac4527fa",
"fc3d7d2c78155436f2d4c9724533dc60a38d8f3c68fce67e44458bd0b2a40f0d"
],
"time": 1564306551,
"nonce": 3410441831,
"bits": "180389e9",
"difficulty": 310704852795.2299,
"chainwork": "000000000000000000000000000000000000000000f6860d0ac1f783768da085",
"confirmations": 2,
"previousblockhash": "0000000000000000011c141b0d2e903e8309e37c6d62e4a12286f5182debb987",
"nextblockhash": "000000000000000003763b3ded750d030dcbeed3d09a690f79209fc4dc0eeb02",
"reward": 12.5,
"isMainChain": true,
"poolInfo": { "poolName": "BTC.com", "url": "https://btc.com/" }
}
 */
public class Block {
    // do NOT change names as they are used by Gson
    public String hash;
    public long height;
    public String[] tx;
    public long time;
    public long confirmations;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Block block = (Block) o;
        return Objects.equals(hash, block.hash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash);
    }

    @Override
    public String toString() {
        return "Block{" +
                "hash='" + hash + '\'' +
                ", height=" + height +
                ", time=" + time +
                ", confirmations=" + confirmations +
                ", tx=" + Arrays.toString(tx) +
                '}';
    }
}
