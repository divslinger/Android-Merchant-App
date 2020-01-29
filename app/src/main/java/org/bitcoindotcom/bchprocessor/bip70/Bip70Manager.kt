package org.bitcoindotcom.bchprocessor.bip70

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager

class Bip70Manager(val app: Application) {
    var socketHandler: WebSocketHandler? = null
    fun reconnectIfNecessary() {
        val connectivityManager = app.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return
        val ni = connectivityManager.activeNetworkInfo
        val ws = socketHandler
        if (ni != null && ni.isConnectedOrConnecting) {
            if (ws != null && !ws.isConnected) {
                ws.start()
            }
        }
        if (ws != null) {
            WebSocketHandler.notifyConnectionStatus(app, ws.isConnected)
        }
    }

    fun startWebsockets(invoiceId: String) {
        socketHandler = Bip70SocketHandler(app, invoiceId).start()
    }

    fun stopSocket() {
        socketHandler?.stop()
        socketHandler = null
    }
}