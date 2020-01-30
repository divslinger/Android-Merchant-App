package com.bitcoin.merchant.app.screens.dialogs

import android.content.Context
import android.graphics.Color
import android.view.View
import com.bitcoin.merchant.app.R
import com.google.android.material.snackbar.Snackbar

object SnackHelper {
    fun make(context: Context, view: View, text: CharSequence, action: String?, listener: View.OnClickListener?) {
        val snack = Snackbar.make(view, text, Snackbar.LENGTH_LONG).setAction(action, listener)
        val sview = snack.view
        sview.setBackgroundColor(Color.parseColor("#C00000"))
        snack.setActionTextColor(context.resources.getColor(R.color.accent_material_dark))
        snack.show()
    }
}