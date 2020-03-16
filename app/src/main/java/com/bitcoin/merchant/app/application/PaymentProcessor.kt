package com.bitcoin.merchant.app.application

import android.util.Log
import com.bitcoin.merchant.app.MainActivity
import com.bitcoin.merchant.app.database.DBControllerV3
import com.bitcoin.merchant.app.database.PaymentRecord
import com.bitcoin.merchant.app.model.Analytics
import com.bitcoin.merchant.app.util.Settings
import org.bitcoindotcom.bchprocessor.bip70.model.InvoiceStatus

class PaymentProcessor(private val app: CashRegisterApplication, private val db: DBControllerV3) {
    fun recordInDatabase(i: InvoiceStatus, fiatFormatted: String?) {
        val bch = i.totalAmountInSatoshi
        try {
            if (Settings.getPaymentTarget(app).isXPub) {
                for ((_, _, address) in i.outputs) {
                    app.wallet.addUsedAddress(address)
                }
            }
            val message = ""
            val confirmations = 0
            val addr = i.firstAddress
            val timeInSec = System.currentTimeMillis() / 1000
            db.insertPayment(PaymentRecord(timeInSec, addr, bch, fiatFormatted, confirmations, message, i.txId))
        } catch (e: Exception) {
            Analytics.error_db_write_tx.sendError(e)
            Log.e(MainActivity.TAG, "recordInDatabase $i", e)
        }
    }
}