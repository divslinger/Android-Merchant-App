package com.bitcoin.merchant.app.util

import android.content.Context
import android.os.Looper
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import com.bitcoin.merchant.app.R

object ToastCustom {
    const val TYPE_ERROR = "TYPE_ERROR"
    const val TYPE_GENERAL = "TYPE_GENERAL"
    const val TYPE_OK = "TYPE_OK"
    const val LENGTH_SHORT = 0
    const val LENGTH_LONG = 1
    private var toast: Toast? = null
    fun makeText(context: Context, text: CharSequence?, duration: Int, type: String?) {
        Thread(Runnable {
            Looper.prepare()
            toast?.cancel()
            val toast = Toast.makeText(context, text, duration)
            val inflate = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val v = inflate.inflate(R.layout.transient_notification, null)
            val tv = v.findViewById<TextView>(R.id.message)
            tv.text = text
            if (type == TYPE_ERROR) {
                tv.background = context.resources.getDrawable(R.drawable.rounded_view_toast_error)
                tv.setTextColor(context.resources.getColor(R.color.toast_error_text))
            } else if (type == TYPE_GENERAL) {
                tv.background = context.resources.getDrawable(R.drawable.rounded_view_toast_warning)
                tv.setTextColor(context.resources.getColor(R.color.toast_warning_text))
            } else if (type == TYPE_OK) {
                tv.background = context.resources.getDrawable(R.drawable.rounded_view_toast_info)
                tv.setTextColor(context.resources.getColor(R.color.toast_info_text))
            }
            toast.setView(v)
            toast.show()
            this.toast = toast;
            Looper.loop()
        }).start()
    }
}