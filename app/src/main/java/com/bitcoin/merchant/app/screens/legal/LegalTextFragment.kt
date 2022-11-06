package com.bitcoin.merchant.app.screens.legal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import com.bitcoin.merchant.app.R
import com.bitcoin.merchant.app.screens.features.ToolbarAwareFragment

abstract class LegalTextFragment : ToolbarAwareFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_legal, container, false)
        setupWebView(view.findViewById(R.id.webview))
        setToolbarAsBackButton()
        return view
    }

    protected open fun setupWebView(webView: WebView) {
        webView.loadUrl("")
    }

    override fun canFragmentBeDiscardedWhenInBackground(): Boolean {
        return true
    }
}