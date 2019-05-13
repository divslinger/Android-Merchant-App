package com.bitcoin.merchant.app.util;

import android.content.Context;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

public class MonetaryUtil {
    public static final int UNIT_BTC = 0;
    public static final int MILLI_BTC = 1;
    public static final int MICRO_BTC = 2;
    public static final long MILLI_LONG = 1000L;
    public static final long MICRO_LONG = 1000000L;
    public static final double BTC_DEC = 1e8;
    private static Context context = null;
    private static MonetaryUtil instance = null;
    private static NumberFormat bchFormat = null;
    private static DecimalFormat dfBch = new DecimalFormat("######0.0######");
    private static DecimalFormatSymbols symbols = null;

    private MonetaryUtil() {
    }

    public static MonetaryUtil getInstance() {
        if (instance == null) {
            bchFormat = NumberFormat.getInstance(Locale.getDefault());
            bchFormat.setMaximumFractionDigits(8);
            bchFormat.setMinimumFractionDigits(1);
            symbols = new DecimalFormatSymbols();
            dfBch.setDecimalFormatSymbols(symbols);
            instance = new MonetaryUtil();
        }
        return instance;
    }

    public static MonetaryUtil getInstance(Context ctx) {
        context = ctx;
        return getInstance();
    }

    public NumberFormat getBchFormat() {
        return bchFormat;
    }

    public BigInteger getUndenominatedAmount(long value) {
        BigInteger amount;
        int unit = PrefsUtil.getInstance(context).getValue(PrefsUtil.KEY_BTC_UNITS, MonetaryUtil.UNIT_BTC);
        switch (unit) {
            case MonetaryUtil.MICRO_BTC:
                amount = BigInteger.valueOf(value / MICRO_LONG);
                break;
            case MonetaryUtil.MILLI_BTC:
                amount = BigInteger.valueOf(value / MILLI_LONG);
                break;
            default:
                amount = BigInteger.valueOf(value);
                break;
        }
        return amount;
    }

    public String getDisplayAmountWithFormatting(long value) {
        String strAmount;
        DecimalFormat df = new DecimalFormat("#");
        df.setMinimumIntegerDigits(1);
        df.setMinimumFractionDigits(1);
        df.setMaximumFractionDigits(8);
        int unit = PrefsUtil.getInstance(context).getValue(PrefsUtil.KEY_BTC_UNITS, MonetaryUtil.UNIT_BTC);
        switch (unit) {
            case MonetaryUtil.MICRO_BTC:
                strAmount = df.format(((double) (value * MICRO_LONG)) / BTC_DEC);
                break;
            case MonetaryUtil.MILLI_BTC:
                strAmount = df.format(((double) (value * MILLI_LONG)) / BTC_DEC);
                break;
            default:
                strAmount = MonetaryUtil.getInstance().getBchFormat().format(value / BTC_DEC);
                break;
        }
        return strAmount;
    }

    public DecimalFormat getBchDecimalFormat() {
        return dfBch;
    }

    public DecimalFormat getFiatDecimalFormat() {
        DecimalFormat f = new DecimalFormat("######0.00");
        f.setDecimalFormatSymbols(symbols);
        return f;
    }

    public DecimalFormatSymbols getDecimalFormatSymbols() {
        return symbols;
    }
}
