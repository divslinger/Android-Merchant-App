package com.bitcoin.merchant.app;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bitcoin.merchant.app.database.PaymentRecord;
import com.bitcoin.merchant.app.model.PaymentReceived;
import com.bitcoin.merchant.app.network.ExpectedAmounts;
import com.bitcoin.merchant.app.network.ExpectedPayments;
import com.bitcoin.merchant.app.network.NetworkStateReceiver;
import com.bitcoin.merchant.app.network.QueryUtxoTask;
import com.bitcoin.merchant.app.network.QueryUtxoType;
import com.bitcoin.merchant.app.network.websocket.TxWebSocketHandler;
import com.bitcoin.merchant.app.network.websocket.WebSocketListener;
import com.bitcoin.merchant.app.network.websocket.impl.bitcoincom.BitcoinComSocketHandler;
import com.bitcoin.merchant.app.network.websocket.impl.blockchaininfo.BlockchainInfoSocketSocketHandler;
import com.bitcoin.merchant.app.screens.PaymentInputFragment;
import com.bitcoin.merchant.app.screens.TransactionsHistoryFragment;
import com.bitcoin.merchant.app.screens.features.ToolbarAwareFragment;
import com.bitcoin.merchant.app.util.AppUtil;
import com.bitcoin.merchant.app.util.PaymentProcessor;
import com.bitcoin.merchant.app.util.PrefsUtil;
import com.crashlytics.android.Crashlytics;
import com.google.android.material.navigation.NavigationView;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity
        implements NfcAdapter.CreateNdefMessageCallback, NfcAdapter.OnNdefPushCompleteCallback,
        WebSocketListener {
    public static final String TAG = "MainActivity";
    private static final String APP_PACKAGE = "com.bitcoin.merchant.app";
    public static final String ACTION_INTENT_SUBSCRIBE_TO_ADDRESS = APP_PACKAGE + "MainActivity.SUBSCRIBE_TO_ADDRESS";
    public static final String ACTION_INTENT_RECORD_TX = APP_PACKAGE + "MainActivity.ACTION_INTENT_RECORD_TX";
    public static final String ACTION_INTENT_UPDATE_TX = APP_PACKAGE + "MainActivity.ACTION_INTENT_UPDATE_TX";
    public static final String ACTION_INTENT_EXPECTED_PAYMENT_RECEIVED = APP_PACKAGE + "MainActivity.ACTION_INTENT_EXPECTED_PAYMENT_RECEIVED";
    public static final String ACTION_INTENT_RECONNECT = APP_PACKAGE + "MainActivity.ACTION_INTENT_RECONNECT";
    public static final String ACTION_INTENT_SHOW_HISTORY = APP_PACKAGE + "MainActivity.ACTION_INTENT_SHOW_HISTORY";
    public static final String ACTION_QUERY_MISSING_TX_IN_MEMPOOL = APP_PACKAGE + "MainActivity.ACTION_QUERY_MISSING_TX_IN_MEMPOOL";
    public static final String ACTION_QUERY_MISSING_TX_THEN_ALL_UTXO = APP_PACKAGE + "MainActivity.ACTION_QUERY_MISSING_TX_THEN_ALL_UTXO";
    public static final String ACTION_QUERY_ALL_UXTO = APP_PACKAGE + "MainActivity.ACTION_QUERY_ALL_UXTO";
    public static final String ACTION_QUERY_ALL_UXTO_FINISHED = APP_PACKAGE + "MainActivity.ACTION_QUERY_ALL_UXTO_FINISHED";
    DrawerLayout mDrawerLayout;
    private TxWebSocketHandler bitcoinDotComSocket = null;
    private TxWebSocketHandler blockchainDotInfoSocket = null;
    private NetworkStateReceiver networkStateReceiver;
    protected BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            if (ACTION_INTENT_SUBSCRIBE_TO_ADDRESS.equals(intent.getAction())) {
                bitcoinDotComSocket.subscribeToAddress(intent.getStringExtra("address"));
                blockchainDotInfoSocket.subscribeToAddress(intent.getStringExtra("address"));
            }
            if (ACTION_INTENT_RECONNECT.equals(intent.getAction())
                    || ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                reconnectIfNecessary();
            }
            if (ACTION_INTENT_RECORD_TX.equals(intent.getAction())) {
                recordNewTx(new PaymentReceived(intent));
            }
            if (ACTION_INTENT_UPDATE_TX.equals(intent.getAction())) {
                updateExistingTx(new PaymentReceived(intent));
            }
            if (ACTION_INTENT_SHOW_HISTORY.equals(intent.getAction())) {
                getNav().navigate(R.id.transactions_screen);
            }
            if (ACTION_QUERY_MISSING_TX_IN_MEMPOOL.equals(intent.getAction())) {
                new QueryUtxoTask(MainActivity.this, QueryUtxoType.UNCONFIRMED).execute();
            }
            if (ACTION_QUERY_MISSING_TX_THEN_ALL_UTXO.equals(intent.getAction())) {
                new QueryUtxoTask(MainActivity.this, QueryUtxoType.UNCONFIRMED, QueryUtxoType.ALL).execute();
            }
            if (ACTION_QUERY_ALL_UXTO.equals(intent.getAction())) {
                new QueryUtxoTask(MainActivity.this, QueryUtxoType.ALL).execute();
            }
        }
    };
    private Toolbar toolbar;

    public Toolbar getToolbar() {
        return toolbar;
    }

    public static NavController getNav(Activity activity) {
        return Navigation.findNavController(activity, R.id.main_nav_controller);
    }

    private NavController getNav() {
        return getNav(this);
    }

    private void reconnectIfNecessary() {
        final ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo ni = connectivityManager.getActiveNetworkInfo();
        if (ni != null && ni.isConnectedOrConnecting()) {
            if (bitcoinDotComSocket != null && !bitcoinDotComSocket.isConnected()) {
                bitcoinDotComSocket.start();
            }
            if (blockchainDotInfoSocket != null && !blockchainDotInfoSocket.isConnected()) {
                blockchainDotInfoSocket.start();
            }
        }
    }

    private void recordNewTx(PaymentReceived payment) {
        Log.i(TAG, "record potential new Tx:" + payment);
        PaymentProcessor processor = new PaymentProcessor(this);
        if (processor.isAlreadyRecorded(payment)) {
            Log.i(TAG, "TX was already in DB: " + payment);
            return; // already in the DB, nothing to do
        }
        ContentValues tx = processor.recordInDatabase(payment);
        boolean paymentExpected = payment.bchExpected > 0;
        if (paymentExpected) {
            if (payment.bchReceived >= payment.bchExpected) {
                ExpectedPayments.getInstance().removePayment(payment.addr);
            }
            if (payment.confirmations <= 2) {
                Intent intent = new Intent(MainActivity.ACTION_INTENT_EXPECTED_PAYMENT_RECEIVED);
                payment.toIntent(intent);
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            }
        }
        addTxToHistory(tx);
        if (paymentExpected) {
            soundAlert();
        }
    }

    private void updateExistingTx(PaymentReceived payment) {
        Log.i(TAG, "update existing Tx:" + payment);
        PaymentProcessor processor = new PaymentProcessor(this);
        ContentValues values = processor.getExistingRecord(payment);
        if (values != null) {
            PaymentRecord record = new PaymentRecord(values);
            record.confirmations = Math.max(payment.confirmations, record.confirmations);
            if ((record.timeInSec == 0) && (payment.timeInSec > 0)) {
                record.timeInSec = payment.timeInSec;
            }
            // Reuse existing content to preserve the db id
            record.toContentValues(values);
            processor.updateInDatabase(values);
            updateTxToHistory(values);
        } else {
            Log.e(TAG, "TX not found in DB: " + payment.txHash);
        }
    }

    private void addTxToHistory(ContentValues tx) {
        TransactionsHistoryFragment f = null; // TODO
        // f.addTx(tx);
    }

    private void updateTxToHistory(ContentValues tx) {
        TransactionsHistoryFragment f = null; // TODO
        // f.updateTx(tx);
    }

    public void soundAlert() {
        AudioManager audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null && audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
            MediaPlayer mp;
            mp = MediaPlayer.create(this, R.raw.alert);
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.reset();
                    mp.release();
                }
            });
            mp.start();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!AppUtil.isEmulator()) {
            Fabric.with(this, new Crashlytics());
        }
        setContentView(R.layout.activity_main);
        setToolbar();
        setTitle(""); // clear "Bitcoin Cash Register" from toolBar when opens on Payment Input screen
        setNavigationDrawer();
        startWebsockets();
        // scan for missing funds at least one
        if (!PrefsUtil.getInstance(this).getValue(PrefsUtil.MERCHANT_KEY_SCANNED_ALL_MISSING_FUNDS, false)) {
            PrefsUtil.getInstance(this).setValue(PrefsUtil.MERCHANT_KEY_SCANNED_ALL_MISSING_FUNDS, true);
            new QueryUtxoTask(MainActivity.this, QueryUtxoType.ALL).execute();
        } else {
            new QueryUtxoTask(MainActivity.this, QueryUtxoType.UNCONFIRMED).execute();
        }
        mDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View view, float v) {
                if (v > 0) {
                    AppUtil.setStatusBarColor(MainActivity.this, R.color.bitcoindotcom_green);
                } else {
                    AppUtil.setStatusBarColor(MainActivity.this, R.color.gray);
                }
            }

            @Override
            public void onDrawerOpened(@NonNull View view) {
                AppUtil.setStatusBarColor(MainActivity.this, R.color.bitcoindotcom_green);
            }

            @Override
            public void onDrawerClosed(@NonNull View view) {
                AppUtil.setStatusBarColor(MainActivity.this, R.color.gray);
            }

            @Override
            public void onDrawerStateChanged(int i) {
            }
        });
        System.out.println("Stored address: " + AppUtil.getReceivingAddress(this));
    }

    /**
     * Only used for debugging purposes
     */
    private void resetPaymentTime(String tx) {
        PaymentProcessor processor = new PaymentProcessor(this);
        ContentValues values = processor.getExistingRecord(new PaymentReceived("", 0, tx, 0, 0, ExpectedAmounts.UNDEFINED));
        if (values != null) {
            PaymentRecord record = new PaymentRecord(values);
            record.timeInSec = 0;
            record.toContentValues(values);
            processor.updateInDatabase(values);
        }
    }

    private void startWebsockets() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_INTENT_SUBSCRIBE_TO_ADDRESS);
        filter.addAction(ACTION_INTENT_RECONNECT);
        filter.addAction(ACTION_INTENT_RECORD_TX);
        filter.addAction(ACTION_INTENT_UPDATE_TX);
        filter.addAction(ACTION_INTENT_SHOW_HISTORY);
        filter.addAction(ACTION_QUERY_MISSING_TX_IN_MEMPOOL);
        filter.addAction(ACTION_QUERY_MISSING_TX_THEN_ALL_UTXO);
        filter.addAction(ACTION_QUERY_ALL_UXTO);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(receiver, filter);
        filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        networkStateReceiver = new NetworkStateReceiver();
        registerReceiver(networkStateReceiver, filter);
        bitcoinDotComSocket = new BitcoinComSocketHandler();
        bitcoinDotComSocket.setListener(this);
        bitcoinDotComSocket.start();
        blockchainDotInfoSocket = new BlockchainInfoSocketSocketHandler();
        blockchainDotInfoSocket.setListener(this);
        blockchainDotInfoSocket.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setMerchantName();
        ToolbarAwareFragment fragment = getVisibleFragment();
        // Note: The navigation starts the payment_input_screen due to config in navigation.xml
        if (fragment != null && !fragment.canFragmentBeDiscardedWhenInBackground()) {
            return; // keep current screen, do not pop any screen
        }
        // Remove all screens from the stack until we reach the payment_input_screen
        getNav().popBackStack(R.id.payment_input_screen, false);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    public void onNdefPushComplete(NfcEvent event) {
        final String eventString = "onNdefPushComplete\n" + event.toString();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), eventString, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        NdefRecord rtdUriRecord = NdefRecord.createUri("market://details?id=" + APP_PACKAGE);
        return new NdefMessage(rtdUriRecord);
    }

    @Override
    protected void onDestroy() {
        bitcoinDotComSocket.stop();
        blockchainDotInfoSocket.stop();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(receiver);
        unregisterReceiver(networkStateReceiver);
        super.onDestroy();
    }

    public void openMenuDrawer() {
        mDrawerLayout.openDrawer(GravityCompat.START);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        boolean ret = super.dispatchTouchEvent(event);
        View view = this.getCurrentFocus();
        if (view instanceof EditText) {
            View w = this.getCurrentFocus();
            int[] scrcoords = new int[2];
            w.getLocationOnScreen(scrcoords);
            float x = event.getRawX() + w.getLeft() - scrcoords[0];
            float y = event.getRawY() + w.getTop() - scrcoords[1];
            if (event.getAction() == MotionEvent.ACTION_UP && (x < w.getLeft() || x >= w.getRight() || y < w.getTop() || y > w.getBottom())) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);
            }
        }
        return ret;
    }

    private void setMerchantName() {
        NavigationView navigationView = findViewById(R.id.navigation_view);
        View headerView = navigationView.getHeaderView(0);
        TextView tvName = headerView.findViewById(R.id.drawer_title);
        String drawerTitle = PrefsUtil.getInstance(this).getValue(PrefsUtil.MERCHANT_KEY_MERCHANT_NAME, "");
        tvName.setText(drawerTitle);
    }

    @Override
    public void onIncomingPayment(PaymentReceived p) {
        Intent intent = new Intent(MainActivity.ACTION_INTENT_RECORD_TX);
        p.toIntent(intent);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public void setToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void setNavigationDrawer() {
        mDrawerLayout = findViewById(R.id.drawer_layout);
        setMerchantName();
        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                menuButtonPressed(menuItem);
                return false;
            }
        });
    }

    public ToolbarAwareFragment getVisibleFragment() {
        Fragment navHostFragment = getSupportFragmentManager().getPrimaryNavigationFragment();
        if (navHostFragment == null) {
            return null;
        }
        for (Fragment fragment : navHostFragment.getChildFragmentManager().getFragments()) {
            if (fragment instanceof ToolbarAwareFragment && fragment.isVisible()) {
                return (ToolbarAwareFragment) fragment;
            }
        }
        return null;
    }

    @Override
    public void onBackPressed() {
        if (isNavDrawerOpen()) {
            closeNavDrawer();
        }
        ToolbarAwareFragment fragment = getVisibleFragment();
        if (!fragment.isBackAllowed()) {
            return;
        }
        super.onBackPressed();
    }

    protected boolean isNavDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(GravityCompat.START);
    }

    protected void closeNavDrawer() {
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    private void menuButtonPressed(final MenuItem menuItem) {
        // allow some time after closing the drawer before performing real navigation
        // so the user can see what is happening
        mDrawerLayout.closeDrawer(GravityCompat.START);
        Handler mDrawerActionHandler = new Handler();
        mDrawerActionHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                switch (menuItem.getItemId()) {
                    case R.id.action_transactions:
                        getNav().navigate(R.id.nav_to_transactions_screen);
                        break;
                    case R.id.action_settings:
                        getNav().navigate(R.id.nav_to_settings_screen);
                        break;
                    case R.id.action_about:
                        getNav().navigate(R.id.nav_to_about_screen);
                        break;
                }
            }
        }, 250);
    }
}
