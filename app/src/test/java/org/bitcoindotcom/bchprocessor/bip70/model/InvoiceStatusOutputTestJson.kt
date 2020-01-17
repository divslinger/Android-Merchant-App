package org.bitcoindotcom.bchprocessor.bip70.model

import com.bitcoin.merchant.app.util.GsonUtil
import org.junit.Assert
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
        val o = GsonUtil.gson.fromJson(jsonString, InvoiceStatusOutput::class.java);
        println(o)
        Assert.assertEquals(o.script, script)
        Assert.assertEquals(o.amount, amount)
        Assert.assertEquals(o.address, address)
        Assert.assertEquals(o.type, type)
    }
}