package com.bitcoin.merchant.app.screens.legal

import android.webkit.WebView
import com.bitcoin.merchant.app.R

class ServiceTermsFragment : LegalTextFragment() {
    override fun setupWebView(webView: WebView) {
        setToolbarTitle(R.string.menu_service_terms)
        webView.loadUrl(resources.getString(R.string.url_service_terms))
    }
}