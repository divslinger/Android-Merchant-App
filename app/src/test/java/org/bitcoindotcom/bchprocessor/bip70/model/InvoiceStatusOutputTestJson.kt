package org.bitcoindotcom.bchprocessor.bip70.model

import org.bitcoindotcom.bchprocessor.bip70.GsonHelper
import org.junit.Assert.assertEquals
import org.junit.Test

internal class InvoiceStatusOutputTestJson {
    @Test
    fun checkInvoiceStatusJsonDate() {
        val script = "76a91434caf2ed1f3dc31530ccd216a2a11d99697949bb88ac"
        val amount = 44910L
        val address = "15p9G7cpWwsvZTjWvsT74F9szU2cbaiCFM"
        val type = "P2PKH"
        val jsonString = javaClass.getResource("/InvoiceStatusOutput.json")?.readText()
        println(jsonString)
        val o = GsonHelper.gson.fromJson(jsonString, InvoiceStatusOutput::class.java);
        println(o)
        assertEquals(o.script, script)
        assertEquals(o.amount, amount)
        assertEquals(o.address, address)
        assertEquals(o.type, type)
    }
}