package com.bitcoin.merchant.app.util

import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.WindowManager
import org.bitcoindotcom.bchprocessor.bip70.GsonHelper.gson
import java.io.BufferedReader
import java.io.InputStreamReader

object AppUtil {
    fun <T> readFromJsonFile(ctx: Context, fileName: String, classOfT: Class<T>): T {
        return gson.fromJson(readFromfile(fileName, ctx), classOfT)
    }

    private fun readFromfile(fileName: String, context: Context): String {
        BufferedReader(InputStreamReader(context.resources.assets.open(fileName))).use {
            return it.readText()
        }
    }

    fun setStatusBarColor(activity: Activity, color: Int) {
        val window = activity.window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = activity.resources.getColor(color)
    }

    val isEmulator: Boolean
        get() = Build.PRODUCT != null && Build.PRODUCT.toLowerCase().contains("sdk")
}