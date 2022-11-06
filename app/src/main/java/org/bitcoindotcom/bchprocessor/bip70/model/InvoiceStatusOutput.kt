package org.bitcoindotcom.bchprocessor.bip70.model

import com.google.gson.annotations.SerializedName

data class InvoiceStatusOutput(
        @SerializedName("script") var script: String? = null,
        @SerializedName("amount") var amount: Long = 0,
        @SerializedName("address") var address: String = "",
        @SerializedName("type") var type: String? = null
)