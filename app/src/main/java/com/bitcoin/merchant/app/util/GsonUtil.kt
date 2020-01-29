package com.bitcoin.merchant.app.util

import com.google.gson.Gson
import com.google.gson.GsonBuilder

object GsonUtil {
    private const val DATE_FORMAT_8601 = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    val gson = createInstance()

    private fun createInstance(): Gson {
        var builder = GsonBuilder()
        builder.setDateFormat(DATE_FORMAT_8601)
        if (AppUtil.isEmulator) {
            builder = builder.setPrettyPrinting()
        }
        return builder.create()
    }
}
