package org.bitcoindotcom.bchprocessor.bip70.model

import org.bitcoindotcom.bchprocessor.bip70.GsonHelper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class InvoiceStatusTestJson {
    @Test
    fun checkDate() {
        val time = "2019-10-05T21:44:34.009Z";
        val expires = "2019-01-09T01:02:03.456Z";
        val partialJson = """{"time":"$time", "expires":"$expires"}"""
        val invoiceStatus = GsonHelper.gson.fromJson(partialJson, InvoiceStatus::class.java)
        println(invoiceStatus)
        val fullJson = GsonHelper.gson.toJsonTree(invoiceStatus).asJsonObject;
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
        val o = GsonHelper.gson.fromJson(jsonString, InvoiceStatus::class.java);
        println(o)
        assertEquals(o.outputs.size, 2)
        assertTrue(o.isPaid)
        assertEquals(o.outputs[0].script, "76a91434caf2ed1f3dc31530ccd216a2a11d99697949bb88ac")
        assertEquals(o.outputs[0].amount, 44910L)
        assertEquals(o.outputs[0].address, "15p9G7cpWwsvZTjWvsT74F9szU2cbaiCFM")
        assertEquals(o.outputs[0].type, "P2PKH")
        assertEquals(o.outputs[1].script, "AABBCCDDEEFF")
        assertEquals(o.outputs[1].amount, 5090L)
        assertEquals(o.outputs[1].address, "333333333333333333333333333333CFM")
        assertEquals(o.outputs[1].type, "P2SH")
        assertEquals(o.network, "main")
        assertEquals(o.currency, "BCH")
        assertEquals(o.status, "paid")
        assertEquals(o.merchantId, "24826146-2b90-48c5-9fd2-6e94cf40f2b0")
        assertEquals(o.paymentAsset, "BCH")
        assertEquals(o.memo, "Payment request for invoice C9kVtnhDYdiGig5Q2rXJ44")
        assertEquals(o.fiatSymbol, "USD")
        assertEquals(o.fiatRate, 222.67, 0.0)
        assertEquals(o.fiatTotal, 0.1, 0.0)
        assertEquals(o.paymentUrl, "https://pay.bitcoin.com/i/C9kVtnhDYdiGig5Q2rXJ44")
        assertEquals(o.paymentId, "C9kVtnhDYdiGig5Q2rXJ44")
        assertEquals(o.txId, "a71a8c008ff5dba3d5cc5929485d2860b836f559c782effeef0daec666db7ea7")
    }
}