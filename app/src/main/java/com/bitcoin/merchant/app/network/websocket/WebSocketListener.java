package com.bitcoin.merchant.app.network.websocket;

import com.bitcoin.merchant.app.network.PaymentReceived;

public interface WebSocketListener {
    void onIncomingPayment(PaymentReceived payment);
}
