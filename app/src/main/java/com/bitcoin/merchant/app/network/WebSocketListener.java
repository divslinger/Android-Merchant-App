package com.bitcoin.merchant.app.network;

public interface WebSocketListener {
    void onIncomingPayment(String addr, long paymentAmount, String txHash);
}
