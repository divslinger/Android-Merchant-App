package com.bitcoin.merchant.app.model.rest;

import java.util.Objects;

public class Utxo {
    // do NOT change names as they are used by Gson
    public String txid;
    public long vout;
    public double amount;
    public long satoshis;
    public long height; // either height or ts is 0
    public long confirmations; // when confirmations is 0, then ts is usually valid
    public long ts;

    @Override
    public String toString() {
        return "Utxo{" +
                "txid='" + txid + '\'' +
                ", vout=" + vout +
                ", amount=" + amount +
                ", satoshis=" + satoshis +
                ", height=" + height +
                ", confirmations=" + confirmations +
                ", ts=" + ts +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Utxo utxo = (Utxo) o;
        return txid.equals(utxo.txid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(txid);
    }
}
