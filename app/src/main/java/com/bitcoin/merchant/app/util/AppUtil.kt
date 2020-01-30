package com.bitcoin.merchant.app.util

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.WindowManager
import com.bitcoin.merchant.app.model.CountryCurrency
import com.bitcoin.merchant.app.model.PaymentTarget
import org.bitcoindotcom.bchprocessor.bip70.GsonHelper.gson
import org.bitcoindotcom.bchprocessor.bip70.model.InvoiceStatus
import java.io.BufferedReader
import java.io.InputStreamReader

object AppUtil {
    const val TAG = "AppUtil"
    const val DEFAULT_CURRENCY_FIAT = "USD"
    fun getCurrency(context: Context): String {
        var currency: String = PrefsUtil.getInstance(context).getValue(PrefsUtil.MERCHANT_KEY_CURRENCY, "")
        if (currency.isEmpty()) { // auto-detect currency
            currency = CountryCurrency.findCurrencyFromLocale(context)
            if (currency.isEmpty()) {
                currency = DEFAULT_CURRENCY_FIAT
            }
            // save to avoid further auto-detection
            PrefsUtil.getInstance(context).setValue(PrefsUtil.MERCHANT_KEY_CURRENCY, currency)
        }
        return currency
    }

    // TODO check usage null=>""
    fun getCountryIso(context: Context): String {
        return PrefsUtil.getInstance(context).getValue(PrefsUtil.MERCHANT_KEY_COUNTRY, "")
    }

    // TODO check usage null=>""
    fun getLocale(context: Context): String {
        return PrefsUtil.getInstance(context).getValue(PrefsUtil.MERCHANT_KEY_LANG_LOCALE, "")
    }

    fun <T> readFromJsonFile(ctx: Context, fileName: String, classOfT: Class<T>): T {
        return gson.fromJson(readFromfile(fileName, ctx), classOfT)
    }

    private fun readFromfile(fileName: String, context: Context): String {
        val b = StringBuilder()
        var input: BufferedReader? = null
        try {
            input = BufferedReader(InputStreamReader(context.resources.assets.open(fileName)))
            var line: String?
            while (input.readLine().also { line = it } != null) {
                b.append(line)
            }
        } catch (e: Exception) {
            e.message
        } finally {
            try {
                input?.close()
            } catch (e2: Exception) {
                e2.message
            }
        }
        return b.toString()
    }

    fun getPaymentTarget(context: Context): PaymentTarget {
        val value: String = PrefsUtil.getInstance(context).getValue(PrefsUtil.MERCHANT_KEY_MERCHANT_RECEIVER, "")
        return PaymentTarget.parse(value)
    }

    fun setPaymentTarget(context: Context, target: PaymentTarget) {
        PrefsUtil.getInstance(context).setValue(PrefsUtil.MERCHANT_KEY_MERCHANT_RECEIVER, target.target)
    }

    fun setStatusBarColor(activity: Activity, color: Int) {
        val window = activity.window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = activity.resources.getColor(color)
    }

    val isEmulator: Boolean
        get() = Build.PRODUCT != null && Build.PRODUCT.toLowerCase().contains("sdk")

    fun getActiveInvoice(context: Context): InvoiceStatus? {
        val invoiceJson: String = PrefsUtil.getInstance(context).getValue(PrefsUtil.MERCHANT_KEY_PERSIST_INVOICE, "")
        val invoice = if (invoiceJson.isEmpty()) null else InvoiceStatus.fromJson(invoiceJson)
        if (invoice != null) {
            Log.i(TAG, "Loading active invoice...")
        }
        return invoice
    }

    fun setActiveInvoice(context: Context, i: InvoiceStatus) {
        if (i.isInitialized) {
            Log.i(TAG, "Saving active invoice...")
            PrefsUtil.getInstance(context).setValue(PrefsUtil.MERCHANT_KEY_PERSIST_INVOICE, gson.toJson(i))
        }
    }

    fun deleteActiveInvoice(context: Context) {
        PrefsUtil.getInstance(context).setValue(PrefsUtil.MERCHANT_KEY_PERSIST_INVOICE, "")
    }
}