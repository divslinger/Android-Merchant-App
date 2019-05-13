package com.bitcoin.merchant.app.screens;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.bitcoin.merchant.app.BuildConfig;
import com.bitcoin.merchant.app.R;
import com.bitcoin.merchant.app.util.AppUtil;
//import android.util.Log;

public class AboutActivity extends Activity {
    private TextView tvAbout = null;
    private TextView bRate = null;
    private TextView bSupport = null;
    private TextView bDownload = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.setContentView(R.layout.activity_about);
        tvAbout = findViewById(R.id.about);
        tvAbout.setText(getString(R.string.about, BuildConfig.VERSION_NAME, "2019"));
        bRate = findViewById(R.id.rate_us);
        bRate.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                String appPackageName = getPackageName();
                Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName));
                marketIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                startActivity(marketIntent);
            }
        });
        bSupport = findViewById(R.id.support);
        bSupport.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "support@bitcoin.com", null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "BCH Merchant Tech Support");
                emailIntent.putExtra(Intent.EXTRA_TEXT,
                        "Dear Bitcoin.com Support," +
                                "\n\n" +
                                "" +
                                "\n\n" +
                                "--\n" +
                                "App: " + getString(R.string.app_name) + " \n" +
                                "System: " + Build.MANUFACTURER + "\n" +
                                "Model: " + Build.MODEL + "\n" +
                                "Version: " + Build.VERSION.RELEASE);
                startActivity(Intent.createChooser(emailIntent, AboutActivity.this.getResources().getText(R.string.email_chooser)));
            }
        });
        bDownload = findViewById(R.id.free_wallet);
        if (hasWallet()) {
            bDownload.setVisibility(View.GONE);
        } else {
            bDownload.setOnClickListener(new Button.OnClickListener() {
                public void onClick(View v) {
                    Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + AppUtil.PACKAGE_BITCOIN_DOT_COM_WALLET));
                    startActivity(marketIntent);
                }
            });
        }
    }

    private boolean hasWallet() {
        return AppUtil.isWalletInstalled(this);
    }
}
