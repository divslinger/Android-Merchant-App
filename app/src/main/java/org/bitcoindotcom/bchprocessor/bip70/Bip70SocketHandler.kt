package org.bitcoindotcom.bchprocessor.bip70

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bitcoin.merchant.app.R
import com.bitcoin.merchant.app.model.Analytics
import com.neovisionaries.ws.client.WebSocket
import com.neovisionaries.ws.client.WebSocketFactory
import org.bitcoindotcom.bchprocessor.bip70.model.Bip70Action
import org.bitcoindotcom.bchprocessor.bip70.model.InvoiceStatus
import java.io.IOException

class Bip70SocketHandler(private val context: Context, val invoiceId: String) : WebSocketHandler() {
    private val url = "${context.getText(R.string.bip70_bitcoin_com_socket)}/s/$invoiceId"

    @Throws(IOException::class)
    override fun createWebSocket(factory: WebSocketFactory): WebSocket {
        return factory.createSocket(url)
    }

    override fun parseInvoice(message: String?) {
        try {
            val status = InvoiceStatus.fromJson(message)
            Log.i(TAG, status.toString())
            if (status.isPaid) {
                val i = Intent(Bip70Action.INVOICE_PAYMENT_ACKNOWLEDGED)
                i.putExtra(Bip70Action.PARAM_INVOICE_STATUS, message)
                LocalBroadcastManager.getInstance(context).sendBroadcast(i)
                // it is important to prevent reconnection
                setAutoReconnect(false)
            } else if (status.isExpired) {
                val i = Intent(Bip70Action.INVOICE_PAYMENT_EXPIRED)
                i.putExtra(Bip70Action.PARAM_INVOICE_STATUS, message)
                LocalBroadcastManager.getInstance(context).sendBroadcast(i)
                // invoice has become invalid, useless to listen any further
                setAutoReconnect(false)
            } else if (status.isOpen) {
                notifyConnectionStatus(context, true)
            }
        } catch (e: Exception) {
            Analytics.error_parse_invoice.sendError(e, message)
            Log.e(TAG, "InvoiceStatus error:$e")
        }
    }
}