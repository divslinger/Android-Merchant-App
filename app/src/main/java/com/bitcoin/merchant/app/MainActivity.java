package com.bitcoin.merchant.app;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
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

import com.bitcoin.merchant.app.application.CashRegisterApplication;
import com.bitcoin.merchant.app.application.NetworkStateReceiver;
import com.bitcoin.merchant.app.screens.features.ToolbarAwareFragment;
import com.bitcoin.merchant.app.util.AppUtil;
import com.bitcoin.merchant.app.util.PrefsUtil;
import com.crashlytics.android.Crashlytics;
import com.google.android.material.navigation.NavigationView;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity
        implements NfcAdapter.CreateNdefMessageCallback, NfcAdapter.OnNdefPushCompleteCallback {
    public static final String TAG = "MainActivity";
    private static final String APP_PACKAGE = "com.bitcoin.merchant.app";
    public static final String ACTION_INTENT_SHOW_HISTORY = APP_PACKAGE + "MainActivity.ACTION_INTENT_SHOW_HISTORY";
    DrawerLayout mDrawerLayout;
    private NetworkStateReceiver networkStateReceiver;
    protected BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            if (ACTION_INTENT_SHOW_HISTORY.equals(intent.getAction())) {
                getNav().navigate(R.id.transactions_screen);
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


    public CashRegisterApplication getApp() {
        return (CashRegisterApplication) getApplication();
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
        Log.d(TAG, "Stored " + AppUtil.getPaymentTarget(this));
    }

    private void startWebsockets() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_INTENT_SHOW_HISTORY);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(receiver, filter);
        filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        networkStateReceiver = new NetworkStateReceiver();
        registerReceiver(networkStateReceiver, filter);
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
