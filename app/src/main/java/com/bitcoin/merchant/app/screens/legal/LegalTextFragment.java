package com.bitcoin.merchant.app.screens.legal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bitcoin.merchant.app.R;
import com.bitcoin.merchant.app.screens.features.ToolbarAwareFragment;

public abstract class LegalTextFragment extends ToolbarAwareFragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_legal, container, false);
        setupWebView(view.findViewById(R.id.webview));
        setToolbarAsBackButton();
        return view;
    }

    protected void setupWebView(WebView webView) {
        webView.loadUrl("https://www.bitcoin.com/");
    }

    @Override
    public boolean canFragmentBeDiscardedWhenInBackground() {
        return true;
    }
}
