package com.bitcoin.merchant.app.network.websocket.impl;

import android.util.Log;

import com.bitcoin.merchant.app.network.ExpectedPayments;
import com.bitcoin.merchant.app.network.websocket.TxWebSocketHandler;
import com.bitcoin.merchant.app.network.websocket.WebSocketListener;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Socket will reconnect even without calling again webSocketHandler.start()
 * and without ACTION_INTENT_RECONNECT being sent by the ConnectivityManager.
 * The number of Thread will stay constant about 17 or 18 on OS v5.
 */
public abstract class TxWebSocketHandlerImpl implements TxWebSocketHandler {
    private static final long PING_INTERVAL = 20 * 1000L; // ping every 20 seconds
    private static AtomicInteger socketThreadCount = new AtomicInteger();
    private final WebSocketFactory webSocketFactory;
    protected WebSocketListener webSocketListener;
    protected String TAG = "WebSocketHandler";
    private volatile ConnectionHandler handler;
    // A stopped Socket is not reusable, as it will exit its connection thread
    private volatile boolean stopped;

    public TxWebSocketHandlerImpl() {
        this.webSocketFactory = new WebSocketFactory();
    }

    private static String getSubscribeMessage(String address) {
        return "{\"op\":\"addr_sub\", \"addr\":\"" + address + "\"}";
    }

    @Override
    public void setListener(WebSocketListener webSocketListener) {
        this.webSocketListener = webSocketListener;
    }

    @Override
    public void start() {
        try {
            Log.i(TAG, "start threads:" + getSocketThreadCount());
            new ConnectionThread().start();
        } catch (Exception e) {
            Log.e(TAG, "start", e);
        }
    }

    @Override
    public void stop() {
        stopped = true;
        if (handler != null) {
            handler.stop();
        }
    }

    @Override
    public boolean isConnected() {
        return handler != null && handler.isConnected() && !handler.isBroken();
    }

    private void send(String message) {
        if (handler != null) {
            handler.send(message);
        }
    }

    @Override
    public synchronized void subscribeToAddress(String address) {
        send(getSubscribeMessage(address));
    }

    abstract protected WebSocket createWebSocket(WebSocketFactory factory) throws IOException;

    abstract protected void parseTx(String message) throws Exception;

    private class ConnectionThread extends Thread {
        public ConnectionThread() {
            setName("ConnectWebSocket");
            setDaemon(true);
        }

        @Override
        public void run() {
            socketThreadCount.addAndGet(1);
            Log.i(TAG, "entering ConnectionThread.run() threads:" + getSocketThreadCount());
            long doubleBackOff = 1000;
            // This while loop should be removed once a cleaner implementation
            // automatically detects connections & disconnections
            // This can be done by registering a network callback which requires the API 24 (OS 7).
            // https://developer.android.com/reference/android/net/ConnectivityManager#registerDefaultNetworkCallback(android.net.ConnectivityManager.NetworkCallback)
            while (!stopped) {
                try {
                    handler = new ConnectionHandler();
                    break;
                } catch (Exception e) {
                    Log.e(TAG, "Connect", e);
                    try {
                        Thread.sleep(doubleBackOff);
                    } catch (InterruptedException ex) {
                        // fail silently
                    }
                    // Allow a maximum delay of 10 seconds between retry
                    doubleBackOff = Math.min(doubleBackOff * 2, 10_000);
                }
            }
            socketThreadCount.addAndGet(-1);
            Log.i(TAG, "exiting ConnectionThread.run() threads:" + getSocketThreadCount());
        }
    }

    private class ConnectionHandler extends WebSocketAdapter {
        public static final int MINUTE_IN_MS = 60 * 1000;
        private final Set<String> sentMessageSet = new HashSet<>();
        private final WebSocket mConnection;
        private volatile boolean autoReconnect = true;
        private long timeLastAlive;

        public ConnectionHandler() throws Exception {
            timeLastAlive = System.currentTimeMillis();
            mConnection = createWebSocket(TxWebSocketHandlerImpl.this.webSocketFactory)
                    .recreate()
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
            Log.d(TAG, "PongSuccess threads:" + getSocketThreadCount());
        }

        @Override
        public void onPingFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
            super.onPingFrame(websocket, frame);
            timeLastAlive = System.currentTimeMillis();
            Log.d(TAG, "PingSuccess threads:" + getSocketThreadCount());
        }

        @Override
        public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
            super.onConnected(websocket, headers);
            Log.i(TAG, "onConnected threads:" + getSocketThreadCount());
        }

        @Override
        public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
            super.onDisconnected(websocket, serverCloseFrame, clientCloseFrame, closedByServer);
            Log.e(TAG, "onDisconnected threads:" + getSocketThreadCount());
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
            if (mConnection != null) {
                autoReconnect = false;
                mConnection.clearListeners();
                if (mConnection.isOpen()) {
                    // Use a daemon thread otherwise it can lock the UI for up to 10 seconds
                    Thread t = new Thread(() -> {
                        socketThreadCount.addAndGet(1);
                        Log.i(TAG, "entering DisconnectionThread.run() threads:" + getSocketThreadCount());
                        try {
                            mConnection.disconnect();
                        } catch (Exception e) {
                            Log.e(TAG, "disconnect", e);
                        }
                        socketThreadCount.addAndGet(-1);
                        Log.i(TAG, "exiting DisconnectionThread.run() threads:" + getSocketThreadCount());
                    });
                    t.setName("DisconnectionThread");
                    t.setDaemon(true);
                    t.start();
                }
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

    private String getSocketThreadCount() {
        return "Sockets:#" + socketThreadCount.get() + ", TotalThreads:#" + Thread.activeCount();
    }
}
