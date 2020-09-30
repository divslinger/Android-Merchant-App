package com.bitcoin.merchant.app.screens.dialogs

import android.app.AlertDialog
import android.content.ClipboardManager
import android.content.Context
import android.widget.EditText
import android.widget.TextView
import com.bitcoin.merchant.app.MainActivity
import com.bitcoin.merchant.app.R
import com.bitcoin.merchant.app.model.PaymentTarget
import com.bitcoin.merchant.app.screens.SettingsFragment
import com.bitcoin.merchant.app.util.Settings

class ToggleMultiterminalDialog(private val settingsController: SettingsFragment) {
    private val ctx: MainActivity = settingsController.activity
    fun show() {
        val tvReceiverHelp = TextView(ctx)
        tvReceiverHelp.text = ctx.getString(R.string.options_explain_multiterminal)
        tvReceiverHelp.setPadding(50, 10, 50, 10)
        val builder = AlertDialog.Builder(ctx)
                .setTitle(R.string.options_multiterminal)
                .setView(tvReceiverHelp)
                .setCancelable(true)
                .setNeutralButton(R.string.button_cancel) { dialog, _ ->
                    dialog.dismiss()
                }
                .setPositiveButton(R.string.enable) { dialog, _ ->
                    Settings.setMultiterminal(ctx, true)
                    settingsController.setMultiterminal(true)
                    dialog.dismiss()
                }
                .setNegativeButton(R.string.disable) { dialog, _ ->
                    Settings.setMultiterminal(ctx, false)
                    settingsController.setMultiterminal(false)
                    dialog.dismiss()
                }
        if (!ctx.isFinishing) {
            builder.show()
        }
    }
}