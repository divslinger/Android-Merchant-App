package com.bitcoin.merchant.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.view.Window;
import android.view.WindowManager;

import com.bitcoin.merchant.app.currency.CurrencyHelper;

public class SplashActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.setContentView(R.layout.activity_splash);
        new Thread() {
            public void run() {
                Looper.getMainLooper().prepare();
                try {
                    sleep(300);
                } catch (InterruptedException ie) {
                }
                CurrencyHelper.getInstance(SplashActivity.this);
                Intent mainActivityIntent = new Intent(SplashActivity.this, MainActivity.class);
                mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(mainActivityIntent);
                Looper.getMainLooper().loop();
            }
        }.start();
    }
}
