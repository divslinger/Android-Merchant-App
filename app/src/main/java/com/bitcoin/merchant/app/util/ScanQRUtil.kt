package com.bitcoin.merchant.app.util

import android.app.Activity
import com.google.zxing.BarcodeFormat
import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.CaptureActivity

class ScanQRUtil {
    fun startQRScan(activity: Activity, requestCode: Int) {
        IntentIntegrator(activity).setPrompt("Scan QR")
                .setBeepEnabled(false)
                .setDesiredBarcodeFormats(BarcodeFormat.QR_CODE.name)
                .setOrientationLocked(true)
                .setCameraId(0)
                .setCaptureActivity(CaptureActivity::class.java)
                .setRequestCode(requestCode)
                .initiateScan()
    }
}