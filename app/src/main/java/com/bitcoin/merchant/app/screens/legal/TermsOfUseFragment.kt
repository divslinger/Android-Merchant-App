package com.bitcoin.merchant.app.screens.legal

import android.webkit.WebView
import com.bitcoin.merchant.app.R

class TermsOfUseFragment : LegalTextFragment() {
    companion object {
        const val URL = "https://www.bitcoin.com/legal/"
    }
    override fun setupWebView(webView: WebView) {
        setToolbarTitle(R.string.menu_terms_of_use)
        webView.loadUrl(URL)
    }
}