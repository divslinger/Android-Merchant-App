package com.bitcoin.merchant.app.currency;

import android.content.Context;
import android.util.Log;

import com.bitcoin.merchant.app.MainActivity;
import com.crashlytics.android.Crashlytics;

import java.util.Currency;
import java.util.Locale;

public class CurrencyDetector {
    private static final String TAG = MainActivity.TAG;

    /**
     * It will return an empty string when not found or when currency is unknown.
     */
    public static String findCurrencyFromLocale(Context context) {
        Locale locale = Locale.getDefault();
        String currencyCode = "";
        String countryCode = "";
        CurrencyHelper helper = CurrencyHelper.getInstance(context);
        try {
            countryCode = locale.getCountry();
            Log.i(TAG, "Currency Locale.country: " + countryCode);
            Currency currency = Currency.getInstance(locale);
            currencyCode = currency.getCurrencyCode();
            Log.i(TAG, "Currency Code: " + currencyCode + " for locale: " + locale.getDisplayName());
            Log.i(TAG, "Currency Symbol: " + currency.getSymbol());
            Log.i(TAG, "Currency Default Fraction Digits: " + currency.getDefaultFractionDigits());
        } catch (Exception e) {
            Log.e(TAG, "Currency", e);
            Crashlytics.logException(e);
            // check if currency can be determined from the country code
            if (countryCode.length() >= 2) {
                currencyCode = helper.getCountryToCurrency().get(countryCode.trim().substring(0, 2).toUpperCase());
            }
        }
        return helper.isCurrencySupported(currencyCode) ? currencyCode : "";
    }
}
