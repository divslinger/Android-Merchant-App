package org.bitcoindotcom.bchprocessor.bip70.model

import com.bitcoin.merchant.app.util.GsonUtil
import org.junit.Assert
import org.junit.Test

internal class InvoiceRequestTest {
    @Test
    fun checkParams() {
        val amount = 5.toString()
        val fiat = "USD"
        val ir = InvoiceRequest(amount, fiat);
        println(ir)
        Assert.assertEquals(amount, ir.amount)
        Assert.assertEquals(fiat, ir.fiat)
    }

    @Test
    fun checkJsonAddress() {
        val jsonString = javaClass.getResource("/InvoiceRequestAddress.json")?.readText()
        println(jsonString)
        val o = GsonUtil.gson.fromJson(jsonString, InvoiceRequest::class.java);
        println(o)
        Assert.assertEquals(o.address, "1MxRuANd5CmHWcveTwQaAJ36sStEQ5QM5k")
    }

    @Test
    fun checkJsonApiKey() {
        val jsonString = javaClass.getResource("/InvoiceRequestApiKey.json")?.readText()
        println(jsonString)
        val o = GsonUtil.gson.fromJson(jsonString, InvoiceRequest::class.java);
        println(o)
        Assert.assertEquals(o.apiKey, "sexqvmkxafvzhzfageoojrkchdekfwmuqpfqywsf")
    }
}