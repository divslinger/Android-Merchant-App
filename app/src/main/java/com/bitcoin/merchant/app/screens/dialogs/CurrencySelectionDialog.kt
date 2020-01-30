package com.bitcoin.merchant.app.screens.dialogs

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListAdapter
import android.widget.TextView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bitcoin.merchant.app.R
import com.bitcoin.merchant.app.model.CountryCurrency
import com.bitcoin.merchant.app.screens.PaymentInputFragment
import com.bitcoin.merchant.app.screens.SettingsFragment
import com.bitcoin.merchant.app.util.PrefsUtil

class CurrencySelectionDialog(private val settingsController: SettingsFragment) {
    private val ctx = settingsController.activity
    fun show(): Boolean {
        val builder = AlertDialog.Builder(ctx)
        val currencies: List<CountryCurrency> = CountryCurrency.getAll(ctx);
        val adapter: ListAdapter = ArrayAdapterWithIcon(ctx, currencies)
        builder.setAdapter(adapter) { dialog: DialogInterface, which: Int ->
            save(currencies[which])
            dialog.dismiss()
        }
        builder.setCancelable(true)
        val alert = builder.create()
        alert.setCanceledOnTouchOutside(true)
        if (!ctx.isFinishing) {
            alert.show()
        }
        return true
    }

    private fun save(cc: CountryCurrency) {
        val prefsUtil: PrefsUtil = PrefsUtil.getInstance(ctx)
        prefsUtil.setValue(PrefsUtil.MERCHANT_KEY_CURRENCY, cc.currency)
        prefsUtil.setValue(PrefsUtil.MERCHANT_KEY_COUNTRY, cc.iso)
        prefsUtil.setValue(PrefsUtil.MERCHANT_KEY_LANG_LOCALE, cc.lang)
        val intent = Intent(PaymentInputFragment.ACTION_INTENT_RESET_AMOUNT)
        LocalBroadcastManager.getInstance(ctx).sendBroadcast(intent)
        settingsController.setCurrencySummary(cc)
    }

    private inner class ArrayAdapterWithIcon(context: Context, private val cc: List<CountryCurrency>) : ArrayAdapter<CountryCurrency?>(context, R.layout.select_currency_dialog_item, cc) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = super.getView(position, convertView, parent)
            val textView = view.findViewById<TextView>(R.id.select_currency_text)
            textView.setCompoundDrawablesRelativeWithIntrinsicBounds(cc[position].image, 0, 0, 0)
            val dimension = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, context.resources.displayMetrics)
            textView.compoundDrawablePadding = dimension.toInt()
            return view
        }

    }
}