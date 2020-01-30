package com.bitcoin.merchant.app.screens.dialogs

import android.app.Activity
import android.view.View
import com.bitcoin.merchant.app.R
import com.google.android.material.snackbar.Snackbar

object SnackHelper {
    fun show(activity: Activity, view: View, text: CharSequence,
             action: String? = null,
             error: Boolean = false,
             listener: View.OnClickListener? = null) {
        activity.runOnUiThread {
            val snack = Snackbar.make(view, text, Snackbar.LENGTH_LONG)
                    .setAction(action, listener)
            val colBack = if (error) R.color.snack_error_background else R.color.snack_info_background
            val colText = if (error) R.color.snack_error_text else R.color.snack_info_text
            snack.view.setBackgroundColor(activity.resources.getColor(colBack))
            snack.setActionTextColor(activity.resources.getColor(colText))
            snack.show()
        }
    }
}