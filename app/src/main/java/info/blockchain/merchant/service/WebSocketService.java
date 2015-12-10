package info.blockchain.merchant.service;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import java.util.Timer;
import java.util.TimerTask;

public class WebSocketService extends android.app.Service	{

    private static final long checkIfNotConnectedDelay = 5000L;

    private WebSocketHandler webSocketHandler = null;

    Timer timer = new Timer();

    private final Handler handler = new Handler();

    private NotificationManager nm;
    private static final int NOTIFICATION_ID_CONNECTED = 0;
    public static boolean isRunning = false;

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

        isRunning = true;

        nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

        IntentFilter filter = new IntentFilter(ACTION_INTENT_SUBSCRIBE_TO_ADDRESS);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(receiver, filter);

        webSocketHandler = new WebSocketHandler(getApplicationContext());

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        connectToWebsocketIfNotConnected();
                    }
                });
            }
        }, 1000, checkIfNotConnectedDelay);

    }

    public void connectToWebsocketIfNotConnected()
    {
        try {
            if(webSocketHandler != null && !webSocketHandler.isConnected()) {
                webSocketHandler.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {

        timer.cancel();

        try {
            if(webSocketHandler != null)	{
                webSocketHandler.stop();
                LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(receiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy()
    {
        isRunning = false;

        stop();

        handler.removeCallbacksAndMessages(null);

        handler.postDelayed(new Runnable()
        {
            public void run()
            {
                nm.cancel(NOTIFICATION_ID_CONNECTED);
            }
        }, 2000);

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
}