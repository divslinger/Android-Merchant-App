package info.blockchain.merchant.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

public class WebSocketService extends android.app.Service implements WebSocketListener{

    private WebSocketHandler webSocketHandler = null;

    public static final String ACTION_INTENT_SUBSCRIBE_TO_ADDRESS = "info.blockchain.merchant.WebSocketService.SUBSCRIBE_TO_ADDRESS";
    public static final String ACTION_INTENT_INCOMING_TX = "info.blockchain.merchant.WebSocketService.ACTION_INTENT_INCOMING_TX";

    public class LocalBinder extends Binder
    {
        public WebSocketService getService()
        {
            return WebSocketService.this;
        }
    }

    private final IBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(final Intent intent)
    {
        return mBinder;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

        IntentFilter filter = new IntentFilter(ACTION_INTENT_SUBSCRIBE_TO_ADDRESS);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(receiver, filter);

        webSocketHandler = new WebSocketHandler();
        webSocketHandler.addListener(this);
        webSocketHandler.start();

    }

    @Override
    public void onDestroy()
    {
        webSocketHandler.stop();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(receiver);
        super.onDestroy();
    }

    protected BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {

        if (ACTION_INTENT_SUBSCRIBE_TO_ADDRESS.equals(intent.getAction())) {
            webSocketHandler.subscribeToAddress(intent.getStringExtra("address"));
        }
        }
    };

    @Override
    public void onIncomingPayment(long paymentAmount) {

        //New incoming payment - broadcast message
        Intent intent = new Intent(WebSocketService.ACTION_INTENT_INCOMING_TX);
        intent.putExtra("payment_amount",paymentAmount);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}