package com.bitcoin.merchant.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bitcoin.merchant.app.network.WebSocketHandler;
import com.bitcoin.merchant.app.network.WebSocketListener;
import com.bitcoin.merchant.app.screens.AboutActivity;
import com.bitcoin.merchant.app.screens.PinActivity;
import com.bitcoin.merchant.app.screens.SettingsActivity;
import com.bitcoin.merchant.app.screens.TabsPagerAdapter;
import com.bitcoin.merchant.app.util.PrefsUtil;

public class MainActivity extends AppCompatActivity implements NfcAdapter.CreateNdefMessageCallback, NfcAdapter.OnNdefPushCompleteCallback, WebSocketListener, NavigationView.OnNavigationItemSelectedListener {
    public static final String TAG = "MainActivity";
    private static final String APP_PACKAGE = "com.bitcoin.merchant.app";
    public static final String ACTION_INTENT_SUBSCRIBE_TO_ADDRESS = APP_PACKAGE + "MainActivity.SUBSCRIBE_TO_ADDRESS";
    public static final String ACTION_INTENT_INCOMING_TX = APP_PACKAGE + "MainActivity.ACTION_INTENT_INCOMING_TX";
    public static final String ACTION_INTENT_RECONNECT = APP_PACKAGE + "MainActivity.ACTION_INTENT_RECONNECT";
    public static int SETTINGS_ACTIVITY = 1;
    private static int PIN_ACTIVITY = 2;
    private static int RESET_PIN_ACTIVITY = 3;
    private static int ABOUT_ACTIVITY = 4;
    DrawerLayout mDrawerLayout;
    private WebSocketHandler webSocketHandler = null;
    protected BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            if (ACTION_INTENT_SUBSCRIBE_TO_ADDRESS.equals(intent.getAction())) {
                webSocketHandler.subscribeToAddress(intent.getStringExtra("address"));
            }
            //Connection re-established
            if (ACTION_INTENT_RECONNECT.equals(intent.getAction())) {
                if (webSocketHandler != null && !webSocketHandler.isConnected()) {
                    webSocketHandler.start();
                }
            }
        }
    };
    //Navigation Drawer
    private Toolbar toolbar = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setToolbar();
        setNavigationDrawer();
        initTableLayout();
        if (PinActivity.isPinMissing(this)) {
            createPIN();
        } else if (!SettingsActivity.isReceivingAddressAvailable(this)) {
            goToSettings(false);
        }
        startWebsockets();
    }

    private void startWebsockets() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_INTENT_SUBSCRIBE_TO_ADDRESS);
        filter.addAction(ACTION_INTENT_RECONNECT);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(receiver, filter);
        webSocketHandler = new WebSocketHandler();
        webSocketHandler.addListener(this);
        webSocketHandler.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setMerchantName();
    }

    @Override
    protected void onNewIntent(Intent intent) {
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
        // TODO
        NdefRecord rtdUriRecord = NdefRecord.createUri("market://details?id=" + APP_PACKAGE);
        return new NdefMessage(rtdUriRecord);
    }

    @Override
    protected void onDestroy() {
        //Stop websockets
        webSocketHandler.stop();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(receiver);
        super.onDestroy();
    }

    private void initTableLayout() {
        String[] tabs = new String[]{getResources().getString(R.string.tab_payment), getResources().getString(R.string.tab_history)};
        TabLayout tabLayout = findViewById(R.id.tabs);
        ViewPager viewPager = findViewById(R.id.pager);
        PagerAdapter mAdapter = new TabsPagerAdapter(getSupportFragmentManager(), tabs);
        viewPager.setAdapter(mAdapter);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabTextColors(getResources().getColor(R.color.white_50), getResources().getColor(R.color.white));
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setTabsFromPagerAdapter(mAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v(TAG, "requestCode:" + requestCode + ", resultCode:" + resultCode + ", Intent:" + data);
        if (requestCode == SETTINGS_ACTIVITY && resultCode == RESULT_OK) {
            ;
        } else if (requestCode == PIN_ACTIVITY && resultCode == RESULT_OK) {
            showSettings();
        } else if (requestCode == RESET_PIN_ACTIVITY && resultCode == RESULT_OK) {
            createPIN();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void showSettings() {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivityForResult(intent, SETTINGS_ACTIVITY);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        boolean ret = super.dispatchTouchEvent(event);
        View view = this.getCurrentFocus();
        if (view instanceof EditText) {
            View w = this.getCurrentFocus();
            int scrcoords[] = new int[2];
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

    private void goToSettings(final boolean create) {
        if (create) {
            showSettings();
        } else {
            enterPIN();
        }
    }

    private void createPIN() {
        Intent intent = new Intent(MainActivity.this, PinActivity.class);
        intent.putExtra("create", true);
        startActivityForResult(intent, PIN_ACTIVITY);
    }

    private void enterPIN() {
        Intent intent = new Intent(MainActivity.this, PinActivity.class);
        intent.putExtra("create", false);
        startActivityForResult(intent, PIN_ACTIVITY);
    }

    private void doAbout() {
        Intent intent = new Intent(MainActivity.this, AboutActivity.class);
        startActivityForResult(intent, ABOUT_ACTIVITY);
    }

    private void setMerchantName() {
        //Update Merchant name
        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        TextView tvName = headerView.findViewById(R.id.drawer_title);
        String drawerTitle = PrefsUtil.getInstance(this).getValue(PrefsUtil.MERCHANT_KEY_MERCHANT_NAME, getResources().getString(R.string.app_name));
        tvName.setText(drawerTitle);
        toolbar.setTitle(drawerTitle);
    }

    @Override
    public void onIncomingPayment(String addr, long paymentAmount, String txHash) {
        //New incoming payment - broadcast message
        Intent intent = new Intent(MainActivity.ACTION_INTENT_INCOMING_TX);
        intent.putExtra("payment_address", addr);
        intent.putExtra("payment_amount", paymentAmount);
        intent.putExtra("payment_tx_hash", txHash);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public void setToolbar() {
        toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_menu_white_24dp));
        setSupportActionBar(toolbar);
    }

    private void setNavigationDrawer() {
        mDrawerLayout = findViewById(R.id.drawer_layout);
        setMerchantName();
        // listen for navigation events
        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        if (isNavDrawerOpen()) {
            closeNavDrawer();
        } else {
            super.onBackPressed();
        }
    }

    protected boolean isNavDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(GravityCompat.START);
    }

    protected void closeNavDrawer() {
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    @Override
    public boolean onNavigationItemSelected(final MenuItem menuItem) {
        // allow some time after closing the drawer before performing real navigation
        // so the user can see what is happening
        mDrawerLayout.closeDrawer(GravityCompat.START);
        Handler mDrawerActionHandler = new Handler();
        mDrawerActionHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                switch (menuItem.getItemId()) {
                    case R.id.action_settings:
                        goToSettings(false);
                        break;
                    case R.id.action_about:
                        doAbout();
                        break;
                }
            }
        }, 250);
        return false;
    }
}
