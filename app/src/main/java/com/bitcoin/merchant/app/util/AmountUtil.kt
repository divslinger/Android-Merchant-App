package com.bitcoin.merchant.app.util

import android.content.Context
import android.util.Log
import com.bitcoin.merchant.app.model.Analytics
import java.text.NumberFormat
import java.util.*

class AmountUtil(private val context: Context) {
    fun formatFiat(amountFiat: Double): String {
        val ccl = Settings.getCountryCurrencyLocale(context)
        return try {
            val formatter = NumberFormat.getCurrencyInstance(ccl.locale)
            formatter.currency = Currency.getInstance(ccl.currency)
            formatter.maximumFractionDigits = ccl.decimals
            formatter.format(amountFiat).replace(CURRENCY_SIGN, ccl.currency)
        } catch (e: Exception) {
            Analytics.error_format_currency.sendError(e, ccl.locale.country, ccl.currency, ccl.locale.displayName)
            Log.d(TAG, "Locale not supported for $ccl.currency failed to format to fiat: $amountFiat")
            ccl.currency + " " + MonetaryUtil.instance.fiatDecimalFormat.format(amountFiat)
        }
    }

    fun formatBch(amountBch: Double): String {
        return MonetaryUtil.instance.bchDecimalFormat.format(amountBch) + " " + DEFAULT_CURRENCY_BCH
    }

    companion object {
        const val TAG = "BCR-AmountUtil"
        const val DEFAULT_CURRENCY_BCH = "BCH"
        const val CURRENCY_SIGN = "\u00a4"
    }
}