package com.bitcoin.merchant.app.currency

import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.WindowManager
import org.bitcoindotcom.bchprocessor.bip70.GsonHelper.gson
import java.io.BufferedReader
import java.io.InputStreamReader

class CountryJsonUtil {
    companion object {
        @JvmStatic
        fun <T> readFromJsonFile(ctx: Context, fileName: String, classOfT: Class<T>): T {
            return gson.fromJson(readFromfile(fileName, ctx), classOfT)
        }

        private fun readFromfile(fileName: String, context: Context): String {
            BufferedReader(InputStreamReader(context.resources.assets.open(fileName))).use {
                return it.readText()
            }
        }
    }
}