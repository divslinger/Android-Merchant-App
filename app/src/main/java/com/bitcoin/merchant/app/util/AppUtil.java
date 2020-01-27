package com.bitcoin.merchant.app.util;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.bitcoin.merchant.app.currency.CurrencyDetector;

import org.bitcoindotcom.bchprocessor.bip70.model.InvoiceStatus;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public abstract class AppUtil {
    public static final String TAG = "AppUtil";
    public static final String DEFAULT_CURRENCY_FIAT = "USD";

    private AppUtil() {
    }

    public static String getCurrency(Context context) {
        String currency = PrefsUtil.getInstance(context).getValue(PrefsUtil.MERCHANT_KEY_CURRENCY, "");
        if (currency.length() == 0) {
            // auto-detect currency
            currency = CurrencyDetector.findCurrencyFromLocale(context);
            if (currency.length() == 0) {
                currency = DEFAULT_CURRENCY_FIAT;
            }
            // save to avoid further auto-detection
            PrefsUtil.getInstance(context).setValue(PrefsUtil.MERCHANT_KEY_CURRENCY, currency);
        }
        return currency;
    }

    public static String getCountry(Context context) {
        return PrefsUtil.getInstance(context).getValue(PrefsUtil.MERCHANT_KEY_COUNTRY, null);
    }

    public static String getLocale(Context context) {
        return PrefsUtil.getInstance(context).getValue(PrefsUtil.MERCHANT_KEY_LOCALE, null);
    }

    public static <T> T readFromJsonFile(Context ctx, String fileName, Class<T> classOfT) {
        return GsonUtil.INSTANCE.getGson().fromJson(readFromfile(fileName, ctx), classOfT);
    }

    public static String readFromfile(String fileName, Context context) {
        StringBuilder b = new StringBuilder();
        BufferedReader input = null;
        try {
            input = new BufferedReader(new InputStreamReader(context.getResources().getAssets().open(fileName)));
            String line = "";
            while ((line = input.readLine()) != null) {
                b.append(line);
            }
        } catch (Exception e) {
            e.getMessage();
        } finally {
            try {
                if (input != null)
                    input.close();
            } catch (Exception e2) {
                e2.getMessage();
            }
        }
        return b.toString();
    }

    public static PaymentTarget getPaymentTarget(Context context) {
        String value = PrefsUtil.getInstance(context).getValue(PrefsUtil.MERCHANT_KEY_MERCHANT_RECEIVER, "");
        return PaymentTarget.parse(value);
    }

    public static void setPaymentTarget(Context context, PaymentTarget target) {
        PrefsUtil.getInstance(context).setValue(PrefsUtil.MERCHANT_KEY_MERCHANT_RECEIVER, target.getTarget());
    }

    public static void setStatusBarColor(Activity activity, int color) {
        Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(activity.getResources().getColor(color));
    }

    public static boolean isEmulator() {
        return Build.PRODUCT != null && Build.PRODUCT.toLowerCase().contains("sdk");
    }

    public static InvoiceStatus getActiveInvoice(Context context) {
        String invoiceJson = PrefsUtil.getInstance(context).getValue(PrefsUtil.MERCHANT_KEY_PERSIST_INVOICE, "");
        InvoiceStatus invoice = invoiceJson.isEmpty() ? null : InvoiceStatus.fromJson(invoiceJson);
        if (invoice != null) {
            Log.i(TAG, "Loading active invoice...");
        }
        return invoice;
    }

    public static void setActiveInvoice(Context context, InvoiceStatus i) {
        if (i.isInitialized()) {
            Log.i(TAG, "Saving active invoice...");
            PrefsUtil.getInstance(context).setValue(PrefsUtil.MERCHANT_KEY_PERSIST_INVOICE, GsonUtil.INSTANCE.getGson().toJson(i));
        }
    }

    public static void deleteActiveInvoice(Context context) {
        PrefsUtil.getInstance(context).setValue(PrefsUtil.MERCHANT_KEY_PERSIST_INVOICE, "");
    }
}
