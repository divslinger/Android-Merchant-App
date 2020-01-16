package com.bitcoin.merchant.app.application;

import android.content.ContentValues;
import android.util.Log;

import com.bitcoin.merchant.app.database.DBControllerV3;
import com.bitcoin.merchant.app.database.PaymentRecord;
import com.bitcoin.merchant.app.util.AppUtil;
import com.crashlytics.android.Crashlytics;

import org.bitcoindotcom.bchprocessor.bip70.model.InvoiceStatus;
import org.bitcoindotcom.bchprocessor.bip70.model.InvoiceStatusOutput;

import static com.bitcoin.merchant.app.MainActivity.TAG;

public class PaymentProcessor {
    private final CashRegisterApplication app;
    private final DBControllerV3 db;

    public PaymentProcessor(CashRegisterApplication app) {
        this.app = app;
        this.db = app.getDb();
    }

    public ContentValues recordInDatabase(InvoiceStatus i, String fiatFormatted) {
        long bch = i.getTotalBchAmount();
        try {
            if (AppUtil.getPaymentTarget(app).isXPub()) {
                for (InvoiceStatusOutput output : i.outputs) {
                    app.getWallet().addUsedAddress(output.address);
                }
            }
            String message = "";
            int confirmations = 0;
            String addr = i.getFirstAddress();
            long time = System.currentTimeMillis();
            PaymentRecord r = new PaymentRecord(time, addr, bch, fiatFormatted, confirmations, message, i.txId);
            ContentValues values = r.toContentValues();
            db.insertPayment(values);
            return values;
        } catch (Exception e) {
            Log.e(TAG, "recordInDatabase" + i, e);
            Crashlytics.logException(e);
            return null;
        }
    }
}
