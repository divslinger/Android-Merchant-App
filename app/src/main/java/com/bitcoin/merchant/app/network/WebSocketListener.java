package com.bitcoin.merchant.app.network;

import com.bitcoin.merchant.app.screens.PaymentReceived;

public interface WebSocketListener {
    void onIncomingPayment(PaymentReceived payment);
}
