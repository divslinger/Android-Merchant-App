package org.bitcoindotcom.bchprocessor.bip70

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import java.lang.Boolean.FALSE

class Bip70Manager(val app: Application) {
    var socketHandler: Bip70SocketHandler? = null
    fun reconnectIfNecessary() {
        val connectivityManager = app.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val networkInfo = connectivityManager?.activeNetworkInfo
        val ws = socketHandler
        if (networkInfo?.isConnectedOrConnecting ?: FALSE) {
            if (ws != null && !ws.isConnected) {
                ws.start()
            }
        }
        if (ws != null) {
            WebSocketHandler.notifyConnectionStatus(app, ws.isConnected)
        }
    }

    fun startWebsockets(invoiceId: String) {
        val ms = System.currentTimeMillis()
        socketHandler = Bip70SocketHandler(app, invoiceId)
        socketHandler?.start()
        Log.d(WebSocketHandler.TAG, "startWebsockets for invoice:$invoiceId in ${System.currentTimeMillis()-ms} ms")
    }

    fun stopSocket() {
        val ms = System.currentTimeMillis()
        val invoiceId = socketHandler?.invoiceId
        socketHandler?.stop()
        socketHandler = null
        Log.d(WebSocketHandler.TAG, "stopWebsockets for invoice:$invoiceId in ${System.currentTimeMillis()-ms} ms")
    }
}