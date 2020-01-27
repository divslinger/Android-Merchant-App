package org.bitcoindotcom.bchprocessor.bip70;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;

import org.bitcoindotcom.bchprocessor.Action;

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
public abstract class WebSocketHandler {
    private static final long PING_INTERVAL = 20 * 1000L; // ping every 20 seconds
    private final WebSocketFactory webSocketFactory;
    protected String TAG = "WebSocketHandler";
    private volatile ConnectionHandler handler;

    public WebSocketHandler() {
        this.webSocketFactory = new WebSocketFactory();
    }

    public void start() {
        try {
            Log.i(TAG, "start threads:" + Thread.activeCount());
            stop();
            new ConnectionThread().start();
        } catch (Exception e) {
            Log.e(TAG, "start", e);
        }
    }

    public void setAutoReconnect(boolean autoReconnect) {
        if (handler != null) {
            handler.setAutoReconnect(autoReconnect);
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

    abstract protected WebSocket createWebSocket(WebSocketFactory factory) throws IOException;

    abstract protected void parseTx(String message) throws Exception;

    private class ConnectionThread extends Thread {
        public ConnectionThread() {
            setName("Bip70WebSocket");
            setDaemon(true);
        }

        public void run() {
            // The loop is only required for the initial connection
            // after that reconnections are being handled by the WebSocket
            long doubleBackOff = 1000;
            while (true) {
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
                    doubleBackOff *= 2;
                }
            }
        }
    }

    private class ConnectionHandler extends WebSocketAdapter {
        public static final int MINUTE_IN_MS = 60 * 1000;
        private final Set<String> sentMessageSet = new HashSet<>();
        private final WebSocket mConnection;
        private long timeLastAlive;
        private volatile boolean autoReconnect = true;

        public ConnectionHandler() throws Exception {
            timeLastAlive = System.currentTimeMillis();
            mConnection = createWebSocket(WebSocketHandler.this.webSocketFactory)
                    .recreate()
                    .addListener(this);
            mConnection.setPingInterval(PING_INTERVAL);
            mConnection.connect();
            timeLastAlive = System.currentTimeMillis();
        }

        public void onPongFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
            super.onPongFrame(websocket, frame);
            timeLastAlive = System.currentTimeMillis();
            Log.d(TAG, "PongSuccess threads:" + Thread.activeCount());
        }

        public void onPingFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
            super.onPingFrame(websocket, frame);
            timeLastAlive = System.currentTimeMillis();
            Log.d(TAG, "PingSuccess threads:" + Thread.activeCount());
        }

        public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
            super.onConnected(websocket, headers);
            Log.i(TAG, "onConnected threads:" + Thread.activeCount());
        }

        public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
            super.onDisconnected(websocket, serverCloseFrame, clientCloseFrame, closedByServer);
            Log.e(TAG, "onDisconnected threads:" + Thread.activeCount());
            if (autoReconnect) {
                // reconnect on involuntary disconnection
                start();
            }
        }

        public void onTextMessage(WebSocket websocket, String message) {
            try {
                parseTx(message);
            } catch (Exception e) {
                Log.e(TAG, message, e);
            }
        }

        public void setAutoReconnect(boolean autoReconnect) {
            this.autoReconnect = autoReconnect;
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

    public static void notifyConnectionStatus(Context context, boolean connected) {
        Intent i = new Intent(Action.UPDATE_CONNECTION_STATUS);
        i.putExtra(Action.PARAM_CONNECTION_STATUS_ENABLED, connected);
        LocalBroadcastManager.getInstance(context).sendBroadcast(i);
    }
}
