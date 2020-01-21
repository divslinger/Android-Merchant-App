package com.bitcoin.merchant.app.screens.legal;

import android.webkit.WebView;

import com.bitcoin.merchant.app.R;

public class TermsOfUseFragment extends LegalTextFragment {
    protected void setupWebView(WebView webView) {
        setToolbarTitle(R.string.menu_terms_of_use);
        webView.loadUrl("https://www.bitcoin.com/legal/");
    }
}