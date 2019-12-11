package com.bitcoin.merchant.app.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

public class MonetaryUtil {
    private static MonetaryUtil instance;
    private static NumberFormat bchFormat;
    private static DecimalFormat dfBch = new DecimalFormat("######0.0#######");
    private static DecimalFormatSymbols symbols;

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

    public String getDisplayAmountWithFormatting(long value) {
        DecimalFormat df = new DecimalFormat("#");
        df.setMinimumIntegerDigits(1);
        df.setMinimumFractionDigits(1);
        df.setMaximumFractionDigits(8);
        String strAmount = bchFormat.format(value / 1e8);
        int i = strAmount.indexOf('.');
        if (i != -1) {
            String integerPart = strAmount.substring(0, i);
            String decimalParts = strAmount.substring(i + 1);
            StringBuilder s = new StringBuilder(integerPart + ".");
            int length = decimalParts.length();
            for (int j = 0; j < 8; j++) {
                if (j == 3 || j == 6) {
                    s.append(" ");
                }
                s.append(j < length ? decimalParts.charAt(j) : '0');
            }
            strAmount = s.toString();
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
