package org.bitcoindotcom.bchprocessor.bip70.model

import com.bitcoin.merchant.app.util.GsonUtil
import org.bitcoindotcom.bchprocessor.bip70.Bip70PayService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class InvoiceStatusNetworkTestCreateInvoice {
    @Test
    fun checkBip70PayServiceCreateInvoiceViaApiKey() {
        val ir = InvoiceRequest("5", "USD")
        ir.apiKey = "sexqvmkxafvzhzfageoojrkchdekfwmuqpfqywsf"
        println("${InvoiceRequest::class} = ${GsonUtil.gson.toJson(ir)}")
        println(Bip70PayService.create("https://pay.bitcoin.com")
                .createInvoice(ir).execute().body())
    }

    @Test
    fun checkBip70PayServiceCreateInvoiceViaBchAddress() {
        val ir = InvoiceRequest("5", "USD")
        ir.address = "bitcoincash:qrjautd36xzp2gm9phrgthal4fjp7e6ckcmmajrkcc"
        println("${InvoiceRequest::class} = ${GsonUtil.gson.toJson(ir)}")
        println(Bip70PayService.create("https://pay.bitcoin.com")
                .createInvoice(ir).execute().body())
    }

    @Test
    fun checkBip70PayServiceCreateInvoiceViaLegacyAddress() {
        val ir = InvoiceRequest("5", "USD")
        ir.address = "1MxRuANd5CmHWcveTwQaAJ36sStEQ5QM5k"
        println("${InvoiceRequest::class} = ${GsonUtil.gson.toJson(ir)}")
        println(Bip70PayService.create("https://pay.bitcoin.com")
                .createInvoice(ir).execute().body())
    }
}