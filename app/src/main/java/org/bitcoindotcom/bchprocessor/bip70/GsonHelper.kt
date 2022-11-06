package org.bitcoindotcom.bchprocessor.bip70

import com.bitcoin.merchant.app.util.AppUtil
import com.google.gson.Gson
import com.google.gson.GsonBuilder

object GsonHelper {
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
