package com.bitcoin.merchant.app.util

import android.R
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface

object DialogUtil {
    fun show(activity: Activity, title: String?, message: String?, runner: () -> Unit) {
        show(activity, title, message, activity.getString(R.string.ok), runner)
    }

    fun show(activity: Activity, title: String?, message: String?, positiveText: String?, runner: () -> Unit) {
        activity.runOnUiThread {
            val builder = AlertDialog.Builder(activity)
            builder.setTitle(title)
                    .setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton(positiveText) { dialog: DialogInterface, _: Int ->
                        dialog.dismiss()
                        runner.invoke()
                    }
            if (!activity.isFinishing) {
                builder.create().show()
            }
        }
    }
}