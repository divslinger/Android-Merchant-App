package org.bitcoindotcom.bchprocessor.bip70;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Bip70Manager {
    public final Application app;
    public WebSocketHandler socketHandler;

    public Bip70Manager(Application app) {
        this.app = app;
    }

    public void reconnectIfNecessary() {
        final ConnectivityManager connectivityManager = (ConnectivityManager) app.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return;
        }
        final NetworkInfo ni = connectivityManager.getActiveNetworkInfo();
        if (ni != null && ni.isConnectedOrConnecting()) {
            if (socketHandler != null && !socketHandler.isConnected()) {
                socketHandler.start();
            }
        }
        if (socketHandler != null) {
            WebSocketHandler.notifyConnectionStatus(app, socketHandler.isConnected());
        }
    }

    public void startWebsockets(String invoiceId) {
        socketHandler = new Bip70SocketHandler(app, invoiceId);
        socketHandler.start();
    }

    public void stopSocket() {
        if (socketHandler != null) {
            socketHandler.stop();
            socketHandler = null;
        }
    }
}
