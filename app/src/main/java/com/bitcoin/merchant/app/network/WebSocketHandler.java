package com.bitcoin.merchant.app.network;

import android.os.AsyncTask;
import android.util.Log;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
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
    private volatile WebSocket mConnection;
    private WebSocketListener webSocketListener;
    private Set<String> sentMessageSet = new HashSet<>();
    private final WebSocketFactory webSocketFactory;

    public WebSocketHandler() {
        this.webSocketFactory = new WebSocketFactory();
    }

    public void start() {
        try {
            Log.i(TAG, "start threads:" + Thread.activeCount());
            stop();
            connect();
        } catch (IOException | WebSocketException e) {
            Log.e(TAG, "start", e);
        }
    }

    public void stop() {
        if (isConnected()) {
            mConnection.clearListeners();
            mConnection.disconnect();
        }
    }

    private void connect() throws IOException, WebSocketException {
        new ConnectionTask().execute();
    }

    public boolean isConnected() {
        return mConnection != null && mConnection.isOpen();
    }

    public void setListener(WebSocketListener webSocketListener) {
        this.webSocketListener = webSocketListener;
    }

    private void send(String message) {
        // Make sure each message is only sent once per socket lifetime
        if (!sentMessageSet.contains(message)) {
            try {
                if (isConnected()) {
                    mConnection.sendText(message);
                    sentMessageSet.add(message);
                }
            } catch (Exception e) {
                Log.e(TAG, "send:" + message);
            }
        }
    }

    public synchronized void subscribeToAddress(String address) {
        send("{\"op\":\"addr_sub\", \"addr\":\"" + address + "\"}");
    }

    private class ConnectionTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... args) {
            try {
                sentMessageSet.clear();
                // https://www.blockchain.com/api/api_websocket
                mConnection = webSocketFactory
                        .createSocket("wss://ws.blockchain.info/bch/inv")
                        .addHeader("Origin", "https://blockchain.info").recreate()
                        .addListener(new WebSocketAdapter() {
                            @Override
                            public void onPongFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
                                super.onPongFrame(websocket, frame);
                                Log.d(TAG, "PongSuccess threads:" + Thread.activeCount());
                            }

                            @Override
                            public void onPingFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
                                super.onPingFrame(websocket, frame);
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
                            }

                            @Override
                            public void onTextMessage(WebSocket websocket, String message) {
                                try {
                                    parseTx(message);
                                } catch (Exception e) {
                                    Log.e(TAG, message, e);
                                }
                            }
                        });
                mConnection.setPingInterval(PING_INTERVAL);
                mConnection.connect();
            } catch (Exception e) {
                Log.e(TAG, "Connect", e);
            }
            return null;
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
            long txValue = 0L;
            String txHash = (String) objX.get("hash");
            if (objX.has("out")) {
                JSONArray outArray = (JSONArray) objX.get("out");
                for (int j = 0; j < outArray.length(); j++) {
                    JSONObject outObj = (JSONObject) outArray.get(j);
                    if (outObj.has("addr")) {
                        String addr = (String) outObj.get("addr");
                        if (ExpectedIncoming.getInstance().isValidAddress(addr)) {
                            foundAddr = addr;
                            txValue = outObj.has("value") ? outObj.getLong("value") : 0;
                            break;
                        }
                    }
                }
            }
            if ((txValue > 0L) && (webSocketListener != null)) {
                webSocketListener.onIncomingPayment(foundAddr, txValue, txHash);
            }
        }
        return false;
    }
}
