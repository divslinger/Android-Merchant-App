package com.bitcoin.merchant.app.screens.legal

import android.webkit.WebView
import com.bitcoin.merchant.app.R

class PrivacyPolicyFragment : LegalTextFragment() {
    override fun setupWebView(webView: WebView) {
        setToolbarTitle(R.string.menu_privacy_policy)
        webView.loadUrl("https://www.bitcoin.com/privacy-policy/")
    }
}