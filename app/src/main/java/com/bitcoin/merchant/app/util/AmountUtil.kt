package com.bitcoin.merchant.app.util

import android.content.Context
import android.util.Log
import java.text.NumberFormat
import java.util.*

class AmountUtil(private val context: Context) {
    fun formatFiat(amountFiat: Double): String {
        val ccl = Settings.getCountryCurrencyLocale(context)
        var fiat: String? = null
        try {
            val formatter = NumberFormat.getCurrencyInstance(ccl.locale)
            val instance = Currency.getInstance(ccl.currency)
            formatter.currency = instance
            formatter.maximumFractionDigits = instance.defaultFractionDigits
            fiat = formatter.format(amountFiat)
        } catch (e: Exception) {
            Log.d(TAG, "Locale not supported for $ccl.currency failed to format to fiat: $amountFiat")
        }
        fiat = if (fiat != null) {
            val currencySign = "\u00a4"
            fiat.replace(currencySign, ccl.currency)
        } else {
            ccl.currency + " " + MonetaryUtil.instance.fiatDecimalFormat.format(amountFiat)
        }
        return fiat
    }

    fun formatBch(amountBch: Double): String {
        return MonetaryUtil.instance.bchDecimalFormat.format(amountBch) + " " + DEFAULT_CURRENCY_BCH
    }

    companion object {
        const val TAG = "AmountUtil"
        const val DEFAULT_CURRENCY_BCH = "BCH"
    }
}