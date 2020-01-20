package com.bitcoin.merchant.app.screens.legal;

import android.webkit.WebView;

import com.bitcoin.merchant.app.R;

public class PrivacyPolicyFragment extends LegalTextFragment {
    protected void setupWebView(WebView webView) {
        setToolbarTitle(R.string.menu_privacy_policy);
        webView.loadUrl("https://www.bitcoin.com/privacy-policy/");
    }
}