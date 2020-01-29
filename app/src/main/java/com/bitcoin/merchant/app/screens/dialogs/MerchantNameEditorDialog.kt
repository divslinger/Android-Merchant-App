package com.bitcoin.merchant.app.screens.dialogs

import android.app.Activity
import android.app.AlertDialog
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.widget.EditText
import android.widget.TextView
import com.bitcoin.merchant.app.R
import com.bitcoin.merchant.app.util.PrefsUtil

class MerchantNameEditorDialog(private val ctx: Activity) {
    fun show(namePref: TextView): Boolean {
        val etName = EditText(ctx)
        etName.isSingleLine = true
        val maxLength = 70
        val fArray = arrayOfNulls<InputFilter>(1)
        fArray[0] = LengthFilter(maxLength)
        etName.filters = fArray
        etName.setText(PrefsUtil.getInstance(ctx).getValue(PrefsUtil.MERCHANT_KEY_MERCHANT_NAME, ""))
        val builder = AlertDialog.Builder(ctx)
                .setTitle(R.string.settings_merchant_name)
                .setView(etName)
                .setCancelable(false)
                .setPositiveButton(R.string.prompt_ok) { dialog, whichButton ->
                    val name = etName.text.toString()
                    if (name.length > 0) {
                        PrefsUtil.getInstance(ctx).setValue(PrefsUtil.MERCHANT_KEY_MERCHANT_NAME, name)
                        namePref.text = name
                    }
                    dialog.dismiss()
                }
                .setNegativeButton(R.string.prompt_ko) { dialog, whichButton -> dialog.dismiss() }
        if (!ctx.isFinishing) {
            builder.show()
        }
        return true
    }

}