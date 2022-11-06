package com.bitcoin.merchant.app

import com.bitcoin.merchant.app.model.PaymentTarget
import com.bitcoin.merchant.app.util.AddressUtil
import org.junit.Test

internal class BitcoinComPayAPIKeyValidationTest {
    @Test
    fun validateApiKey() {
        val apiKey1 = "dtgmfljtkcbwwvkbegpakhwseymimpalanmqjtae"
        assert(PaymentTarget.parse(apiKey1).isApiKey)
        val apiKey2 = "bvcdndeyaropfdlcjeutwghghkyuomespvrctayf"
        assert(PaymentTarget.parse(apiKey2).isApiKey)
    }
}