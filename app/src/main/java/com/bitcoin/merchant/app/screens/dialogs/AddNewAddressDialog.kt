package com.bitcoin.merchant.app.screens.dialogs

import android.app.Activity
import android.app.AlertDialog
import android.widget.EditText
import android.widget.TextView
import com.bitcoin.merchant.app.R
import com.bitcoin.merchant.app.screens.SettingsFragment
import com.bitcoin.merchant.app.util.AppUtil
import com.bitcoin.merchant.app.model.PaymentTarget

class AddNewAddressDialog(private val settingsController: SettingsFragment) {
    companion object {
        private const val ENTERING_ADDRESS_BYPASSED = false
    }

    private val ctx: Activity = settingsController.activity
    fun show() {
        val tvReceiverHelp = TextView(ctx)
        tvReceiverHelp.text = ctx.getString(R.string.options_explain_payment_address)
        tvReceiverHelp.setPadding(50, 10, 50, 10)
        val builder = AlertDialog.Builder(ctx)
                .setTitle(R.string.options_add_payment_address)
                .setView(tvReceiverHelp)
                .setCancelable(true)
                .setPositiveButton(R.string.paste) { dialog, _ ->
                    enterAddressUsingInputField()
                    dialog.dismiss()
                }
                .setNegativeButton(R.string.scan) { dialog, _ ->
                    dialog.dismiss()
                    settingsController.requestToOpenCamera()
                }
        if (!ctx.isFinishing) {
            builder.show()
        }
    }

    private fun showDialogToEnterAddress(etReceiver: EditText) {
        val builder = AlertDialog.Builder(ctx)
                .setTitle(R.string.options_add_payment_address)
                .setView(etReceiver)
                .setCancelable(false)
                .setPositiveButton(R.string.prompt_ok) { dialog, _ ->
                    settingsController.validateThenSetReceiverKey(etReceiver.text.toString().trim { it <= ' ' })
                    dialog.dismiss()
                }
                .setNegativeButton(R.string.prompt_ko) { dialog, _ -> dialog.dismiss() }
        if (!ctx.isFinishing) {
            builder.show()
        }
    }

    private fun enterAddressUsingInputField() {
        if (ENTERING_ADDRESS_BYPASSED) {
            settingsController.setAndDisplayPaymentTarget(PaymentTarget.parse("1MxRuANd5CmHWcveTwQaAJ36sStEQ5QM5k"))
        } else {
            val editText = EditText(ctx)
            editText.isSingleLine = true
            editText.setText(AppUtil.getPaymentTarget(ctx).bchAddress)
            showDialogToEnterAddress(editText)
        }
    }
}