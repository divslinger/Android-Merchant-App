package com.bitcoin.merchant.app.screens;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.bitcoin.merchant.app.currency.CurrencyExchange;
import com.bitcoin.merchant.app.database.DBControllerV3;
import com.bitcoin.merchant.app.util.AmountUtil;
import com.bitcoin.merchant.app.util.AppUtil;
import com.crashlytics.android.Crashlytics;

import static com.bitcoin.merchant.app.MainActivity.TAG;

public class PaymentProcessor {
    private final Context context;

    public PaymentProcessor(Context context) {
        this.context = context;
    }

    public ContentValues receive(PaymentReceived p) {
        long bchAmount = (p.bchReceived == -1L) ? p.bchExpected : p.bchReceived;
        if (bchAmount != p.bchExpected) {
            bchAmount *= -1L;
        }
        Double currencyPrice = CurrencyExchange.getInstance(context).getCurrencyPrice(AppUtil.getCurrency(context));
        double amountPayableFiat = (Math.abs((double) bchAmount) / 1e8) * currencyPrice;
        String fiatAmount = (p.bchReceived == -1L) ? p.fiatExpected : new AmountUtil(context).formatFiat(amountPayableFiat);
        try {
            AppUtil util = AppUtil.getInstance(context);
            if (util.isValidXPub()) {
                util.getWallet().addUsedAddress(p.addr);
            }
            ContentValues vals = new DBControllerV3(context).insertPayment(
                    System.currentTimeMillis() / 1000,
                    p.addr,
                    bchAmount,
                    fiatAmount,
                    -1, // confirmations
                    "", // note, message
                    p.txHash
            );
            return vals;
        } catch (Exception e) {
            Log.e(TAG, "insertPayment", e);
            Crashlytics.logException(e);
            return null;
        }
    }
}
