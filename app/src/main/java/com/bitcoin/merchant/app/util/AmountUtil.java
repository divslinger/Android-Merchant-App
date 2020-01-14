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
        String currencySymbol = cc.symbol;
        currencySymbol = currencySymbol != null ? currencySymbol : currency;
        if (fiat != null) {
            String currencySign = "\u00a4";
            fiat = fiat.replace(currencySign, currencySymbol);
        } else {
            fiat = currencySymbol + " " + MonetaryUtil.getInstance().getFiatDecimalFormat().format(amountFiat);
        }
        return fiat;
    }

    public String formatBch(double amountBch) {
        return MonetaryUtil.getInstance().getBchDecimalFormat().format(amountBch) + " " + DEFAULT_CURRENCY_BCH;
    }

/*
    private DecimalFormat df;
    private DecimalFormatSymbols dfs;
    dfs = new DecimalFormatSymbols();
    df = new DecimalFormat("#.########", dfs);
    // TODO
    private static final double bitcoinLimit = 21_000_000.0;
    private double toBch(double amount) throws ParseException {
        Double currencyPrice = CurrencyExchange.getInstance(activity).getCurrencyPrice(getCurrency());
        MonetaryUtil util = MonetaryUtil.getInstance();
        return (currencyPrice == 0.0d) ? 0.0d : nf.parse(util.getBchDecimalFormat().format(amount / currencyPrice)).doubleValue();
    }

    private void checkBitcoinLimit() {
        double currentValue = 0.0;
        try {
            currentValue = nf.parse(tvAmount.getText().toString()).doubleValue();
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
        double bchValue = 0.0;
        try {
            bchValue = toBch(currentValue);
        } catch (ParseException e) {
            Log.e(TAG, "", e);
        }
        // TODO
        if (bchValue > bitcoinLimit) {
            Double currencyPrice = CurrencyExchange.getInstance(activity).getCurrencyPrice(getCurrency());
            tvAmount.setText(MonetaryUtil.getInstance().getFiatDecimalFormat().format(bitcoinLimit * currencyPrice));
            ToastCustom.makeText(activity, getResources().getString(R.string.invalid_amount), ToastCustom.LENGTH_SHORT, ToastCustom.TYPE_ERROR);
        }
    }

    private String formatFiat(double bch) {
        Double currencyPrice = CurrencyExchange.getInstance(app).getCurrencyPrice(AppUtil.getCurrency(app));
        double fiat = (Math.abs(bch) / 1e8) * currencyPrice;
        return new AmountUtil(app).formatFiat(fiat);
    }

*/

}
