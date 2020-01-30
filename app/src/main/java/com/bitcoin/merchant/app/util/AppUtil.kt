package com.bitcoin.merchant.app.util

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.WindowManager
import com.bitcoin.merchant.app.model.CountryCurrencyLocale
import com.bitcoin.merchant.app.model.PaymentTarget
import org.bitcoindotcom.bchprocessor.bip70.GsonHelper.gson
import org.bitcoindotcom.bchprocessor.bip70.model.InvoiceStatus
import java.io.BufferedReader
import java.io.InputStreamReader

object AppUtil {
    const val TAG = "AppUtil"
    fun getCountryCurrencyLocale(context: Context): CountryCurrencyLocale {
        val p = PrefsUtil.getInstance(context)
        val currency = p.getValue(PrefsUtil.MERCHANT_KEY_CURRENCY, "")
        val country = p.getValue(PrefsUtil.MERCHANT_KEY_COUNTRY, "")
        val locale = p.getValue(PrefsUtil.MERCHANT_KEY_LANG_LOCALE, "")
        return if (currency.isEmpty() || country.isEmpty()) {
            // detect & save to avoid further auto-detection
            setCountryCurrencyLocale(context, CountryCurrencyLocale.getFromLocale(context))
        } else CountryCurrencyLocale.get(context, currency, country, locale)
    }

    fun setCountryCurrencyLocale(ctx: Context, ccl: CountryCurrencyLocale) : CountryCurrencyLocale {
        val p = PrefsUtil.getInstance(ctx)
        p.setValue(PrefsUtil.MERCHANT_KEY_CURRENCY, ccl.currency)
        p.setValue(PrefsUtil.MERCHANT_KEY_COUNTRY, ccl.iso)
        p.setValue(PrefsUtil.MERCHANT_KEY_LANG_LOCALE, ccl.lang)
        return ccl
    }

    fun <T> readFromJsonFile(ctx: Context, fileName: String, classOfT: Class<T>): T {
        return gson.fromJson(readFromfile(fileName, ctx), classOfT)
    }

    private fun readFromfile(fileName: String, context: Context): String {
        BufferedReader(InputStreamReader(context.resources.assets.open(fileName))).use {
            return it.readText()
        }
    }

    fun getPaymentTarget(context: Context): PaymentTarget {
        val value = PrefsUtil.getInstance(context).getValue(PrefsUtil.MERCHANT_KEY_MERCHANT_RECEIVER, "")
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
        val invoiceJson = PrefsUtil.getInstance(context).getValue(PrefsUtil.MERCHANT_KEY_PERSIST_INVOICE, "")
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