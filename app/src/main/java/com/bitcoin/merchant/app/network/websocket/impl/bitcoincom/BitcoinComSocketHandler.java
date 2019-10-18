package com.bitcoin.merchant.app.network.websocket.impl.bitcoincom;

import android.os.Build;
import android.util.Log;

import com.bitcoin.merchant.app.model.websocket.Tx;
import com.bitcoin.merchant.app.network.ExpectedAmounts;
import com.bitcoin.merchant.app.network.ExpectedPayments;
import com.bitcoin.merchant.app.network.websocket.impl.TxWebSocketHandlerImpl;
import com.bitcoin.merchant.app.screens.PaymentReceived;
import com.bitcoin.merchant.app.util.AppUtil;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketFactory;

import java.io.IOException;

public class BitcoinComSocketHandler extends TxWebSocketHandlerImpl {
    public BitcoinComSocketHandler() {
        TAG = "BitcoinComSocket";
    }

    @Override
    protected WebSocket createWebSocket(WebSocketFactory factory) throws IOException {
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            // Hostname verification fails on Android 5.x & 6
            factory.setVerifyHostname(false);
        }
        return factory.createSocket("wss://bch.api.wallet.bitcoin.com/bws/api/socket/v1/address");
    }

    @Override
    protected void parseTx(String message) throws Exception {
        Tx tx = AppUtil.GSON.fromJson(message, Tx.class);
        if (tx != null && tx.outputs != null) {
            Log.i(TAG, "TX found:" + tx);
            for (Tx.Output o : tx.outputs) {
                String addr = o.address;
                if (ExpectedPayments.getInstance().isValidAddress(addr)) {
                    ExpectedAmounts expected = ExpectedPayments.getInstance().getExpectedAmounts(addr);
                    long bchReceived = o.value;
                    if (webSocketListener != null) {
                        long timeInSec = System.currentTimeMillis() / 1000;
                        PaymentReceived payment = new PaymentReceived(addr, bchReceived, tx.txid, timeInSec, 0, expected);
                        Log.i(TAG, "expected payment:" + payment);
                        webSocketListener.onIncomingPayment(payment);
                    }
                }
            }
        }
    }
}
