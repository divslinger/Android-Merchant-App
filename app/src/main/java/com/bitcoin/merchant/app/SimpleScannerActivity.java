package com.bitcoin.merchant.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static com.bitcoin.merchant.app.SetReceivingAddressActivity.SCAN_RESULT;

public class SimpleScannerActivity extends Activity implements ZXingScannerView.ResultHandler {
    private static final String TAG = "Scanner";
    private ZXingScannerView mScannerView;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        setContentView(mScannerView);                // Set the scanner view as the content view
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void handleResult(Result rawResult) {
        Log.v(TAG, rawResult.getText());
        Log.v(TAG, rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode, pdf417 etc.)
        String symData = rawResult.getText();
        if (!TextUtils.isEmpty(symData)) {
            Intent dataIntent = new Intent();
            dataIntent.putExtra(SCAN_RESULT, symData);
            setResult(Activity.RESULT_OK, dataIntent);
            finish();
        } else {
            // If you would like to resume scanning, call this method below:
            mScannerView.resumeCameraPreview(this);
        }
    }
}