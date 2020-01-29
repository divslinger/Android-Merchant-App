package com.bitcoin.merchant.app.screens

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.bitcoin.merchant.app.BuildConfig
import com.bitcoin.merchant.app.R
import com.bitcoin.merchant.app.screens.features.ToolbarAwareFragment

class AboutFragment : ToolbarAwareFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_about, container, false)
        val about = view.findViewById<TextView>(R.id.about_screen)
        about.text = BuildConfig.VERSION_NAME + " - 2020"
        view.findViewById<View>(R.id.about_logo).setOnClickListener { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.bitcoin.com/"))) }
        setToolbarAsBackButton()
        setToolbarTitle(R.string.menu_about)
        return view
    }

    override fun canFragmentBeDiscardedWhenInBackground(): Boolean {
        return true
    }
}