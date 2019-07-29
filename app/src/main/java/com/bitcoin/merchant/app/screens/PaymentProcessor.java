package com.bitcoin.merchant.app.screens;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.bitcoin.merchant.app.currency.CurrencyExchange;
import com.bitcoin.merchant.app.database.DBControllerV3;
import com.bitcoin.merchant.app.database.PaymentRecord;
import com.bitcoin.merchant.app.util.AmountUtil;
import com.bitcoin.merchant.app.util.AppUtil;
import com.crashlytics.android.Crashlytics;

import static com.bitcoin.merchant.app.MainActivity.TAG;

public class PaymentProcessor {
    private final Context context;
    private final DBControllerV3 db;

    public PaymentProcessor(Context context) {
        this.context = context;
        this.db = new DBControllerV3(context);
    }

    private String formatFiat(double bch) {
        Double currencyPrice = CurrencyExchange.getInstance(context).getCurrencyPrice(AppUtil.getCurrency(context));
        double fiat = (Math.abs(bch) / 1e8) * currencyPrice;
        return new AmountUtil(context).formatFiat(fiat);
    }

    public ContentValues getExistingRecord(PaymentReceived payment) {
        try {
            return db.getPaymentFromTx(payment.txHash);
        } catch (Exception e) {
            Log.e(TAG, "isAlreadyRecorded:" + payment, e);
            Crashlytics.logException(e);
            return null;
        }
    }

    public boolean isAlreadyRecorded(PaymentReceived payment) {
        return getExistingRecord(payment) != null;
    }

    public ContentValues recordInDatabase(PaymentReceived p) {
        long bch = p.bchReceived;
        String fiat = p.fiatExpected;
        if (bch != p.bchExpected) {
            if (p.bchExpected > 0) {
                bch = -bch;
            }
            fiat = formatFiat(bch);
        }
        try {
            AppUtil util = AppUtil.getInstance(context);
            if (util.isValidXPub()) {
                util.getWallet().addUsedAddress(p.addr);
            }
            String message = "";
            PaymentRecord r = new PaymentRecord(p.timeInSec, p.addr, bch, fiat,
                    p.confirmations, message, p.txHash);
            ContentValues values = r.toContentValues();
            db.insertPayment(values);
            return values;
        } catch (Exception e) {
            Log.e(TAG, "insertPayment", e);
            Crashlytics.logException(e);
            return null;
        }
    }

    public void updateInDatabase(ContentValues values) {
        try {
            db.updateRecord(values);
        } catch (Exception e) {
            Log.e(TAG, "updatePayment", e);
            Crashlytics.logException(e);
        }
    }
}
