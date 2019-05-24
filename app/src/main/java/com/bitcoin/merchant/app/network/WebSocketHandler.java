package com.bitcoin.merchant.app.network;

import android.os.AsyncTask;

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
import java.util.Timer;
import java.util.TimerTask;
//import android.util.Log;

public class WebSocketHandler {
    private final long pingInterval = 20000L;//ping every 20 seconds
    private final long pongTimeout = 5000L;//pong timeout after 5 seconds
    private volatile WebSocket mConnection = null;
    private WebSocketListener webSocketListener = null;
    private HashSet<String> sentMessageSet = new HashSet<>();
    private volatile Timer pingTimer = null;
    private volatile boolean pingPongSuccess = false;

    public WebSocketHandler() {
    }

    public void start() {
        try {
            stop();
            connect();
            startPingTimer();
        } catch (IOException | com.neovisionaries.ws.client.WebSocketException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        stopPingTimer();
        if (mConnection != null && mConnection.isOpen()) {
            mConnection.clearListeners();
            mConnection.disconnect();
        }
    }

    private synchronized void startPingTimer() {
        pingTimer = new Timer();
        pingTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                ping();
            }
        }, pingInterval, pingInterval);
    }

    private void ping() {
        if (mConnection != null) {
            pingPongSuccess = false;
            if (mConnection.isOpen()) mConnection.sendPing();
            startPongTimer();
        }
    }

    private void startPongTimer() {
        final Timer pongTimer = new Timer();
        pongTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                pongTimer.cancel();
                pongCheckForTimedOut();
            }
        }, pongTimeout);
    }

    private synchronized void stopPingTimer() {
        if (pingTimer != null) pingTimer.cancel();
    }

    private void pongCheckForTimedOut() {
        if (!pingPongSuccess) {
            // ping pong unsuccessful after x seconds - restart connection
            start();
        }
    }

    private void connect() throws IOException, WebSocketException {
        new ConnectionTask().execute();
    }

    public boolean isConnected() {
        return mConnection != null && mConnection.isOpen();
    }

    public void addListener(WebSocketListener webSocketListener) {
        this.webSocketListener = webSocketListener;
    }

    private void send(String message) {
        //Make sure each message is only sent once per socket lifetime
        if (!sentMessageSet.contains(message)) {
            try {
                if (mConnection != null && mConnection.isOpen()) {
                    mConnection.sendText(message);
                    sentMessageSet.add(message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
//            Log.d("WebSocketHandler", "Message sent already: "+message);
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
                mConnection = new WebSocketFactory()
                        .createSocket("wss://ws.blockchain.info/bch/inv")
                        .addHeader("Origin", "https://blockchain.info").recreate()
                        .addListener(new WebSocketAdapter() {
                            @Override
                            public void onPongFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
                                super.onPongFrame(websocket, frame);
                                pingPongSuccess = true;
                            }

                            public void onTextMessage(WebSocket websocket, String message) {
                                try {
                                    parseTx(message);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                mConnection.connect();
            } catch (Exception e) {
                e.printStackTrace();
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
                        if (ExpectedIncoming.getInstance().getBTC().containsKey(outObj.get("addr"))) {
                            foundAddr = (String) outObj.get("addr");
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
