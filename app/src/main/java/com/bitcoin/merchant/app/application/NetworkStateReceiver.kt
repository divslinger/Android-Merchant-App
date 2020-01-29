package com.bitcoin.merchant.app.application

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import org.bitcoindotcom.bchprocessor.Action

/**
 * NB: this class is referenced in the AndroidManifest.xml to listen to connectivity/WIFI changes.
 * Created by riaanvos on 11/12/15.
 */
class NetworkStateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.extras != null) {
            LocalBroadcastManager.getInstance(context).sendBroadcastSync(Intent(Action.NETWORK_RECONNECT))
        }
    }
}