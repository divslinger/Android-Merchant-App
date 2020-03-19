package com.bitcoin.merchant.app.model

import android.app.Application
import android.content.Context
import android.util.Log
import com.amplitude.api.Amplitude
import com.bitcoin.merchant.app.R
import com.bitcoin.merchant.app.util.AppUtil
import org.json.JSONObject

interface Analytics {
    object invoice_checkout : Analytics
    object invoice_created : Analytics
    object invoice_cancelled : Analytics
    object invoice_paid : Analytics
    object invoice_shared : Analytics
    object settings_merchantname_edit : Analytics
    object settings_merchantname_changed : Analytics
    object settings_currency_edit : Analytics
    object settings_currency_changed : Analytics
    object settings_pin_edit : Analytics
    object settings_pin_changed : Analytics
    object settings_paymenttarget_edit : Analytics
    object settings_paymenttarget_changed : Analytics
    object settings_paymenttarget_pairingcode_set : Analytics
    object settings_paymenttarget_xpub_set : Analytics
    object settings_paymenttarget_pubkey_set : Analytics
    object settings_paymenttarget_apikey_set : Analytics
    object tap_settings : Analytics
    object tap_link_wallet : Analytics
    object tap_link_localbitcoin : Analytics
    object tap_link_exchange : Analytics
    object tap_privacypolicy : Analytics
    object tap_serviceterms : Analytics
    object tap_termsofuse : Analytics
    object tap_about : Analytics
    object tap_transactions : Analytics
    object tx_id_explorer_launched : Analytics
    object tx_id_copied : Analytics

    // List of errors
    object error_db_write_tx : AnalyticsError
    object error_db_read_tx : AnalyticsError
    object error_db_read_address : AnalyticsError
    object error_db_unknown : AnalyticsError
    object error_rendering : AnalyticsError
    object error_syncing_xpub : AnalyticsError
    object error_rest_bitcoin_com_scan_address_funds : AnalyticsError
    object error_copy_to_clipboard : AnalyticsError
    object error_generate_address_from_xpub : AnalyticsError
    object error_download_invoice : AnalyticsError
    object error_connect_to_socket : AnalyticsError
    object error_generate_qr_code : AnalyticsError
    object error_convert_address_to_bch : AnalyticsError
    object error_format_currency : AnalyticsError {
        fun sendError(error: Exception, country: String?, currency: String?, locale: String?) =
                sendWithProperties("error" to error.message, "country" to country, "locale" to locale, "currency" to currency)
    }

    object error_parse_invoice : AnalyticsError {
        fun sendError(error: Exception, invoice: String?) =
                sendWithProperties("error" to error.message, "invoice" to invoice)
    }

    fun send() {
        sendWithProperties()
    }

    fun sendWithProperties(vararg pairs: Pair<String, Any?>) {
        try {
            // if (!AppUtil.isEmulator) {
                val a = Amplitude.getInstance()
                val eventName = javaClass.simpleName.replace('_', ' ')
                if (pairs.isEmpty()) {
                    a.logEvent(eventName)
                    Log.i(TAG, eventName)
                } else {
                    val properties = JSONObject()
                    for (p in pairs) properties.put(p.first, p.second)
                    a.logEvent(eventName, properties)
                    Log.i(TAG, "$eventName $properties")
            }
            // }
        } catch (e: Exception) {
            Log.e(TAG, "", e)
        }
    }

    companion object {
        val TAG = "BCR-Analytics"
        fun configure(app: Application, context: Context) {
            // if (!AppUtil.isEmulator)
                Amplitude.getInstance()
                        .trackSessionEvents(true)
                        .setFlushEventsOnClose(false)
                        .initialize(context, context.resources.getString(R.string.amplitude_api_key))
                        .enableForegroundTracking(app)

        }
    }
}

interface AnalyticsError : Analytics {
    fun sendError(error: Exception) = sendWithProperties("error" to error.message)
}
