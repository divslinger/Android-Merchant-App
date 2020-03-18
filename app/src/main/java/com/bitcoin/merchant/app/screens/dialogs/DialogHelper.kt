package com.bitcoin.merchant.app.screens.dialogs

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface

object DialogHelper {
    fun show(activity: Activity, title: String?, message: String?, runner: () -> Unit) {
        activity.runOnUiThread {
            val builder = AlertDialog.Builder(activity)
            builder.setTitle(title)
                    .setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton(activity.getString(android.R.string.ok)) { dialog: DialogInterface, _: Int ->
                        dialog.dismiss()
                        runner.invoke()
                    }
            if (!activity.isFinishing) {
                builder.create().show()
            }
        }
    }
}