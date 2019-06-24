package com.bitcoin.merchant.app.network;

import android.os.AsyncTask;
import android.util.Log;

import com.bitcoin.merchant.app.screens.PaymentReceived;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Socket will reconnect even without calling again webSocketHandler.start()
 * and without ACTION_INTENT_RECONNECT being sent by the ConnectivityManager.
 * The number of Thread will stay constant about 17 or 18 on OS v5.
 */
public class WebSocketHandler {
    private static final long PING_INTERVAL = 20 * 1000L; // ping every 20 seconds
    private static final String TAG = "WebSocketHandler";
    private WebSocketListener webSocketListener;
    private final WebSocketFactory webSocketFactory;
    private volatile ConnectionHandler handler;

    public WebSocketHandler() {
        this.webSocketFactory = new WebSocketFactory();
    }

    public void setListener(WebSocketListener webSocketListener) {
        this.webSocketListener = webSocketListener;
    }

    public void start() {
        try {
            Log.i(TAG, "start threads:" + Thread.activeCount());
            stop();
            new ConnectionTask().execute();
        } catch (Exception e) {
            Log.e(TAG, "start", e);
        }
    }

    public void stop() {
        if (handler != null) {
            handler.stop();
        }
    }

    public boolean isConnected() {
        return handler != null && handler.isConnected() && !handler.isBroken();
    }

    private void send(String message) {
        if (handler != null) {
            handler.send(message);
        }
    }

    public synchronized void subscribeToAddress(String address) {
        send(getSubscribeMessage(address));
    }

    private static String getSubscribeMessage(String address) {
        return "{\"op\":\"addr_sub\", \"addr\":\"" + address + "\"}";
    }

    private class ConnectionTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... args) {
            try {
                handler = new ConnectionHandler();
            } catch (Exception e) {
                Log.e(TAG, "Connect", e);
            }
            return null;
        }
    }

    private class ConnectionHandler extends WebSocketAdapter {
        public static final int MINUTE_IN_MS = 60 * 1000;
        private final Set<String> sentMessageSet = new HashSet<>();
        private final WebSocket mConnection;
        private volatile boolean autoReconnect = true;
        private long timeLastAlive;

        public ConnectionHandler() throws Exception {
            // https://www.blockchain.com/api/api_websocket
            timeLastAlive = System.currentTimeMillis();
            mConnection = webSocketFactory
                    .createSocket("wss://ws.blockchain.info/bch/inv")
                    .addHeader("Origin", "https://blockchain.info").recreate()
                    .addListener(this);
            mConnection.setPingInterval(PING_INTERVAL);
            mConnection.connect();
            for (String address : ExpectedPayments.getInstance().getAddresses()) {
                directSend(getSubscribeMessage(address));
            }
            timeLastAlive = System.currentTimeMillis();
        }

        @Override
        public void onPongFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
            super.onPongFrame(websocket, frame);
            timeLastAlive = System.currentTimeMillis();
            Log.d(TAG, "PongSuccess threads:" + Thread.activeCount());
        }

        @Override
        public void onPingFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
            super.onPingFrame(websocket, frame);
            timeLastAlive = System.currentTimeMillis();
            Log.d(TAG, "PingSuccess threads:" + Thread.activeCount());
        }

        @Override
        public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
            super.onConnected(websocket, headers);
            Log.i(TAG, "onConnected threads:" + Thread.activeCount());
        }

        @Override
        public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
            super.onDisconnected(websocket, serverCloseFrame, clientCloseFrame, closedByServer);
            Log.e(TAG, "onDisconnected threads:" + Thread.activeCount());
            if (autoReconnect) {
                // reconnect on involuntary disconnection
                start();
            }
        }

        @Override
        public void onTextMessage(WebSocket websocket, String message) {
            try {
                parseTx(message);
            } catch (Exception e) {
                Log.e(TAG, message, e);
            }
        }

        public boolean isConnected() {
            return mConnection != null && mConnection.isOpen();
        }

        public void stop() {
            if (isConnected()) {
                autoReconnect = false;
                mConnection.clearListeners();
                mConnection.disconnect();
            }
        }

        private void send(String message) {
            // Make sure each message is only sent once per socket lifetime
            if (!sentMessageSet.contains(message)) {
                try {
                    if (isConnected()) {
                        directSend(message);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "send:" + message);
                }
            }
        }

        private void directSend(String message) {
            mConnection.sendText(message);
            sentMessageSet.add(message);
        }

        public boolean isBroken() {
            // considered broken when older than 1 minute and with no ping or pong during that time
            return (timeLastAlive + MINUTE_IN_MS) < System.currentTimeMillis();
        }
    }

    private boolean parseTx(String message) throws JSONException {
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(message);
        } catch (JSONException je) {
            jsonObject = null;
        }
        if (jsonObject == null) {
            return true;
        }
        String op = (String) jsonObject.get("op");
        if (op.equals("utx") && jsonObject.has("x")) {
            JSONObject objX = (JSONObject) jsonObject.get("x");
            String foundAddr = null;
            long bchReceived = 0L;
            String txHash = (String) objX.get("hash");
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
            if ((bchReceived > 0L) && (foundAddr != null) && (webSocketListener != null)) {
                ExpectedAmounts expected = ExpectedPayments.getInstance().getExpectedAmounts(foundAddr);
                if (bchReceived >= expected.bch) {
                    ExpectedPayments.getInstance().removePayment(foundAddr);
                }
                webSocketListener.onIncomingPayment(new PaymentReceived(foundAddr, bchReceived, txHash, expected));
            }
        }
        return false;
    }
}
