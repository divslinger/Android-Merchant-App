package com.bitcoin.merchant.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import com.bitcoin.merchant.app.screens.SettingsFragment
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView
import me.dm7.barcodescanner.zxing.ZXingScannerView.ResultHandler

class ScanQRCodeActivity : Activity(), ResultHandler {
    private lateinit var mScannerView: ZXingScannerView
    public override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        mScannerView = ZXingScannerView(this) // Programmatically initialize the scanner view
        setContentView(mScannerView) // Set the scanner view as the content view
    }

    public override fun onResume() {
        super.onResume()
        mScannerView.setResultHandler(this) // Register ourselves as a handler for scan results.
        mScannerView.startCamera() // Start camera on resume
    }

    public override fun onPause() {
        super.onPause()
        mScannerView.stopCamera() // Stop camera on pause
    }

    override fun handleResult(rawResult: Result) {
        Log.v(TAG, rawResult.text)
        Log.v(TAG, rawResult.barcodeFormat.toString()) // Prints the scan format (qrcode, pdf417 etc.)
        val symData = rawResult.text
        if (!TextUtils.isEmpty(symData)) {
            val dataIntent = Intent()
            dataIntent.putExtra(SettingsFragment.SCAN_RESULT, symData)
            setResult(RESULT_OK, dataIntent)
            finish()
        } else { // If you would like to resume scanning, call this method below:
            mScannerView.resumeCameraPreview(this)
        }
    }

    companion object {
        private const val TAG = "Scanner"
    }
}