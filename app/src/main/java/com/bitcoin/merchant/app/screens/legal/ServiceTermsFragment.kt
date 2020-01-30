package com.bitcoin.merchant.app.screens.legal

import android.webkit.WebView
import com.bitcoin.merchant.app.R

class ServiceTermsFragment : LegalTextFragment() {
    companion object {
        const val URL = "https://www.bitcoin.com/bitcoin-cash-register/service-terms/"
    }

    override fun setupWebView(webView: WebView) {
        setToolbarTitle(R.string.menu_service_terms)
        webView.loadUrl(URL)
    }
}