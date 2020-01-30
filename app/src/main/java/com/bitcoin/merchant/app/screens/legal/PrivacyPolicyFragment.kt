package com.bitcoin.merchant.app.screens.legal

import android.webkit.WebView
import com.bitcoin.merchant.app.R

class PrivacyPolicyFragment : LegalTextFragment() {
    companion object {
        val URL = "https://www.bitcoin.com/privacy-policy/"
    }
    override fun setupWebView(webView: WebView) {
        setToolbarTitle(R.string.menu_privacy_policy)
        webView.loadUrl(URL)
    }
}