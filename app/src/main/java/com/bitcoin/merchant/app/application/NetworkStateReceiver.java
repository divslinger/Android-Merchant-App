package com.bitcoin.merchant.app.application;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.bitcoindotcom.bchprocessor.Action;

/**
 * Created by riaanvos on 11/12/15.
 */
public class NetworkStateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (intent.getExtras() != null) {
            LocalBroadcastManager.getInstance(context).sendBroadcastSync(new Intent(Action.NETWORK_RECONNECT));
        }
    }
}