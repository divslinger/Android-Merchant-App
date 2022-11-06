package com.bitcoin.merchant.app.util

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.zxing.BarcodeFormat
import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.CaptureActivity

class ScanQRUtil {
    var isScanning = false

    fun onRequestPermissionsResult(activity: Activity, requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera(activity)
            } else {
                isScanning = false
                val text = "Please grant camera permission to use the QR Scanner"
                Toast.makeText(activity, text, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun requestToOpenCamera(activity: Activity) {
        isScanning = true
        if (Build.VERSION.SDK_INT >= 23
                && ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION)
        } else {
            openCamera(activity)
        }
    }

    private fun openCamera(activity: Activity) {
        IntentIntegrator(activity).setPrompt("Scan QR")
                .setBeepEnabled(false)
                .setDesiredBarcodeFormats(BarcodeFormat.QR_CODE.name)
                .setOrientationLocked(true)
                .setCameraId(0)
                .setCaptureActivity(CaptureActivity::class.java)
                .setRequestCode(ZXING_SCAN_REQUEST)
                .initiateScan()
    }

    companion object {
        private const val CAMERA_PERMISSION = 1111
        const val ZXING_SCAN_REQUEST = 2026
        const val ZXING_SCAN_RESULT = "SCAN_RESULT"
    }
}