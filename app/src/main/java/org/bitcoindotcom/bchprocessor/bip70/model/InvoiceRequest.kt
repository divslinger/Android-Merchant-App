package org.bitcoindotcom.bchprocessor.bip70.model

import com.google.gson.annotations.SerializedName
import java.util.*

data class InvoiceRequest(@SerializedName("fiatAmount") val amount: String,
                          @SerializedName("fiat") val fiat: String) {
    @SerializedName("webhook") @JvmField
    var webhook: String = "http://127.0.0.1/unused/webhook"
    @SerializedName("memo") @JvmField
    var memo: String = UUID.randomUUID().toString()
    @SerializedName("apiKey") @JvmField
    var apiKey: String? = null
    @SerializedName("address") @JvmField
    var address: String? = null
}