package com.bitcoin.merchant.app.network;

import android.content.Intent;

import com.bitcoin.merchant.app.network.ExpectedAmounts;

public class PaymentReceived {
    public final String addr;
    public final long bchReceived;
    public final long bchExpected;
    public final String fiatExpected;
    public final String txHash;
    public final long timeInSec;
    public final int confirmations;

    public PaymentReceived(String addr, long bchReceived, String txHash, long timeInSec, int confirmations,
                           ExpectedAmounts expected) {
        this.addr = addr;
        this.bchReceived = bchReceived;
        this.bchExpected = expected.bch;
        this.fiatExpected = expected.fiat;
        this.txHash = txHash;
        this.timeInSec = timeInSec;
        this.confirmations = confirmations;
    }

    public PaymentReceived(Intent intent) {
        addr = intent.getStringExtra("payment_address");
        bchReceived = intent.getLongExtra("payment_received_amount", 0L);
        bchExpected = intent.getLongExtra("payment_expected_amount", 0L);
        fiatExpected = intent.getStringExtra("payment_expected_fiat");
        txHash = intent.getStringExtra("payment_tx_hash");
        timeInSec = intent.getLongExtra("payment_ts_seconds", 0L);
        confirmations = intent.getIntExtra("payment_conf", 0);
    }

    public void toIntent(Intent intent) {
        intent.putExtra("payment_address", addr);
        intent.putExtra("payment_received_amount", bchReceived);
        intent.putExtra("payment_expected_amount", bchExpected);
        intent.putExtra("payment_expected_fiat", fiatExpected);
        intent.putExtra("payment_tx_hash", txHash);
        intent.putExtra("payment_ts_seconds", timeInSec);
        intent.putExtra("payment_conf", confirmations);
    }

    public boolean isUnderpayment() {
        return bchReceived < bchExpected;
    }

    public boolean isOverpayment() {
        return bchReceived > bchExpected;
    }

    @Override
    public String toString() {
        return "PaymentReceived{" +
                "txHash='" + txHash + '\'' +
                ", addr='" + addr + '\'' +
                ", bchReceived=" + bchReceived +
                ", bchExpected=" + bchExpected +
                ", fiatExpected='" + fiatExpected + '\'' +
                ", timeInSec=" + timeInSec +
                ", confirmations=" + confirmations +
                '}';
    }
}
