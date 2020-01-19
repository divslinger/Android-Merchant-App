package com.bitcoin.merchant.app.util;

import android.content.Context;
import android.util.Log;

import com.bitcoin.merchant.app.currency.CountryCurrency;
import com.bitcoin.merchant.app.currency.CurrencyHelper;

import java.text.NumberFormat;
import java.util.Currency;

public class AmountUtil {
    public static final String TAG = "AmountUtil";
    public static final String DEFAULT_CURRENCY_BCH = "BCH";
    private final Context context;

    public AmountUtil(Context context) {
        this.context = context;
    }

    public String formatFiat(double amountFiat) {
        String currency = AppUtil.getCurrency(context);
        String country = AppUtil.getCountry(context);
        String locale = AppUtil.getLocale(context);
        CountryCurrency cc = CurrencyHelper.getInstance(context).getCountryCurrency(currency, country, locale);
        String fiat = null;
        try {
            NumberFormat formatter = NumberFormat.getCurrencyInstance(cc.countryLocales.getLocale());
            Currency instance = Currency.getInstance(currency);
            formatter.setCurrency(instance);
            formatter.setMaximumFractionDigits(instance.getDefaultFractionDigits());
            fiat = formatter.format(amountFiat);
        } catch (Exception e) {
            Log.d(TAG, "Locale not supported for " + currency + " failed to format to fiat: " + amountFiat);
        }
        if (fiat != null) {
            String currencySign = "\u00a4";
            fiat = fiat.replace(currencySign, currency);
        } else {
            fiat = currency + " " + MonetaryUtil.getInstance().getFiatDecimalFormat().format(amountFiat);
        }
        return fiat;
    }

    public String formatBch(double amountBch) {
        return MonetaryUtil.getInstance().getBchDecimalFormat().format(amountBch) + " " + DEFAULT_CURRENCY_BCH;
    }
}
