package org.bitcoindotcom.bchprocessor.bip70.model

import android.util.Log
import com.bitcoin.merchant.app.util.QrCodeUtil
import org.bitcoindotcom.bchprocessor.bip70.Bip70PayService
import org.junit.Test

internal class InvoiceAndQrCodeCreationPerformanceTest {
    @Test
    fun measureUSD() {
        val fiat = "USD"
        runTest(fiat)
    }

    @Test
    fun measureJPY() {
        val fiat = "JPY"
        runTest(fiat)
    }

    @Test
    fun measureEUR() {
        val fiat = "EUR"
        runTest(fiat)
    }

    private fun runTest(fiat: String) {
        println("Generating $fiat invoice...")
        var startMs = System.currentTimeMillis()
        val invoice = createInvoice("1999", fiat) ?: throw Exception()
        val invoiceMs = System.currentTimeMillis() - startMs
        startMs = System.currentTimeMillis()
        val bitmap = QrCodeUtil.getBitmap(invoice.walletUri, 280)
        val bitmapMs = System.currentTimeMillis() - startMs
        Log.i("BCR-JUNIT", "$fiat creation time invoice=$invoiceMs, qrCode=$bitmapMs")
    }

    private fun createInvoice(fiatAmount: String, fiat: String): InvoiceStatus? {
        val ir = InvoiceRequest(fiatAmount, fiat)
        ir.address = "bitcoincash:qrjautd36xzp2gm9phrgthal4fjp7e6ckcmmajrkcc"
        val i = Bip70PayService.create("https://pay.bitcoin.com").createInvoice(ir).execute().body()
        return i
    }
}