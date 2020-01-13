package com.bitcoin.merchant.app.network.websocket.impl.blockchaininfo;

import android.util.Log;

import com.bitcoin.merchant.app.network.ExpectedAmounts;
import com.bitcoin.merchant.app.network.ExpectedPayments;
import com.bitcoin.merchant.app.network.websocket.impl.TxWebSocketHandlerImpl;
import com.bitcoin.merchant.app.model.PaymentReceived;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

@Deprecated
public class BlockchainInfoSocketSocketHandler extends TxWebSocketHandlerImpl {
    public BlockchainInfoSocketSocketHandler() {
        TAG = "BlockchainInfoSocket";
    }

    @Override
    protected WebSocket createWebSocket(WebSocketFactory factory) throws IOException {
        return factory.createSocket("wss://ws.blockchain.info/bch/inv")
                .addHeader("Origin", "https://blockchain.info");
    }

    @Override
    protected void parseTx(String message) throws JSONException {
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(message);
        } catch (JSONException je) {
            jsonObject = null;
        }
        if (jsonObject == null) {
            return;
        }
        String op = (String) jsonObject.get("op");
        if (op.equals("utx") && jsonObject.has("x")) {
            JSONObject objX = (JSONObject) jsonObject.get("x");
            String foundAddr = null;
            long bchReceived = 0L;
            String txHash = (String) objX.get("hash");
            Log.i(TAG, "TX found:" + txHash);
            if (objX.has("out")) {
                JSONArray outArray = (JSONArray) objX.get("out");
                for (int j = 0; j < outArray.length(); j++) {
                    JSONObject outObj = (JSONObject) outArray.get(j);
                    if (outObj.has("addr")) {
                        String addr = (String) outObj.get("addr");
                        if (ExpectedPayments.getInstance().isValidAddress(addr)) {
                            foundAddr = addr;
                            bchReceived = outObj.has("value") ? outObj.getLong("value") : 0;
                            break;
                        }
                    }
                }
            }
            if ((bchReceived > 0L) && (foundAddr != null) && ExpectedPayments.getInstance().isValidAddress(foundAddr)) {
                ExpectedAmounts expected = ExpectedPayments.getInstance().getExpectedAmounts(foundAddr);
                if (webSocketListener != null) {
                    long timeInSec = System.currentTimeMillis() / 1000;
                    PaymentReceived payment = new PaymentReceived(foundAddr, bchReceived, txHash, timeInSec, 0, expected);
                    Log.i(TAG, "expected payment:" + payment);
                    webSocketListener.onIncomingPayment(payment);
                }
            }
        }
    }
}
