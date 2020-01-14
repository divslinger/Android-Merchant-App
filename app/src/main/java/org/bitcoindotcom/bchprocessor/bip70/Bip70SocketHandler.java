package org.bitcoindotcom.bchprocessor.bip70;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketFactory;

import org.bitcoindotcom.bchprocessor.Action;
import org.bitcoindotcom.bchprocessor.bip70.model.InvoiceStatus;

import java.io.IOException;

public class Bip70SocketHandler extends WebSocketHandler {
    private final Context context;
    private final String url;

    public Bip70SocketHandler(Context context, String invoiceId) {
        this.context = context;
        TAG = "BchProcessor-WebSocket";
        url = "wss://pay.bitcoin.com/s/" + invoiceId;
    }

    @Override
    protected WebSocket createWebSocket(WebSocketFactory factory) throws IOException {
        return factory.createSocket(url);
    }

    @Override
    protected void parseTx(String message) throws Exception {
        try {
            InvoiceStatus status = InvoiceStatus.fromJson(message);
            if (status != null) {
                Log.i(TAG, status.toString());
                if (status.isPaid()) {
                    Intent i = new Intent(Action.INVOICE_PAYMENT_ACKNOWLEDGED);
                    i.putExtra(Action.PARAM_INVOICE_STATUS, message);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(i);
                    // it is important to prevent reconnection
                    setAutoReconnect(false);
                } else if (status.isExpired()) {
                    Intent i = new Intent(Action.INVOICE_PAYMENT_EXPIRED);
                    i.putExtra(Action.PARAM_INVOICE_STATUS, message);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(i);
                    // invoice has become invalid, useless to listen any further
                    setAutoReconnect(false);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "InvoiceStatus error:" + e);
        }
    }
}