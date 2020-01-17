package org.bitcoindotcom.bchprocessor.bip70.model

import com.bitcoin.merchant.app.util.GsonUtil.gson
import com.google.gson.annotations.SerializedName
import java.util.*
import kotlin.collections.ArrayList

class InvoiceStatus(
        @SerializedName("paymentUrl") var paymentUrl: String = "",
        @SerializedName("paymentId") var paymentId: String? = null,
        @SerializedName("paymentAsset") var paymentAsset: String? = "BCH",
        @SerializedName("memo") var memo: String? = null,
        @SerializedName("time") var time: Date = Date(),
        @SerializedName("expires") var expires: Date = Date(),
        @SerializedName("status") var status: String = Status.expired.name,
        @SerializedName("network") var network: String? = "main",
        @SerializedName("currency") var currency: String? = "BCH",
        @SerializedName("fiatSymbol") var fiatSymbol: String = "USD",
        @SerializedName("fiatRate") var fiatRate: Double = 0.0,
        @SerializedName("fiatTotal") var fiatTotal: Double = 0.0,
        @SerializedName("txId") var txId: String? = null,
        @SerializedName("merchantId") var merchantId: String? = null,
        @SerializedName("outputs") var outputs: List<InvoiceStatusOutput> = ArrayList(0)) {

    val firstAddress: String?
        get() = if (outputs.size > 0) outputs[0].address else null

    val totalBchAmount: Long
        get() {
            var amount: Long = 0
            for (output in outputs) {
                amount += output.amount
            }
            return amount
        }

    val isPaid: Boolean
        get() = Status.paid.name == status

    val isOpen: Boolean
        get() = Status.open.name == status

    val isExpired: Boolean
        get() = Status.expired.name == status

    val walletUri: String
        get() = "bitcoincash:?r=$paymentUrl"

    internal enum class Status {
        open, paid, expired
    }

    companion object {
        @JvmStatic
        fun fromJson(message: String?): InvoiceStatus {
            return gson.fromJson(message, InvoiceStatus::class.java)
        }
    }
}