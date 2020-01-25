package org.bitcoindotcom.bchprocessor.bip70.model

import com.bitcoin.merchant.app.util.GsonUtil
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class InvoiceStatusTestJson {
    @Test
    fun checkDate() {
        val time = "2019-10-05T21:44:34.009Z";
        val expires = "2019-01-09T01:02:03.456Z";
        val partialJson = """{"time":"$time", "expires":"$expires"}"""
        val invoiceStatus = GsonUtil.gson.fromJson(partialJson, InvoiceStatus::class.java)
        println(invoiceStatus)
        val fullJson = GsonUtil.gson.toJsonTree(invoiceStatus).asJsonObject;
        println(fullJson)
        println(time)
        assertEquals(fullJson["time"].asString, time)
        println(expires)
        assertEquals(fullJson["expires"].asString, expires)
    }

    @Test
    fun checkJson() {
        val jsonString = javaClass.getResource("/InvoiceStatus.json")?.readText()
        println(jsonString)
        val o = GsonUtil.gson.fromJson(jsonString, InvoiceStatus::class.java);
        println(o)
        assertEquals(o.outputs.size, 2)
        assertTrue(o.isPaid)
        assertEquals(o.fiatSymbol, "USD")
        assertEquals(o.fiatRate, 222.67, 0.0)
        assertEquals(o.fiatTotal, 0.1, 0.0)
        assertEquals(o.paymentUrl, "https://pay.bitcoin.com/i/C9kVtnhDYdiGig5Q2rXJ44")
        assertEquals(o.paymentId, "C9kVtnhDYdiGig5Q2rXJ44")
        assertEquals(o.txId, "a71a8c008ff5dba3d5cc5929485d2860b836f559c782effeef0daec666db7ea7")
    }
}