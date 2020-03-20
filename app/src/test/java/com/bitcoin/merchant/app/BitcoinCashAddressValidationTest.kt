package com.bitcoin.merchant.app

import com.bitcoin.merchant.app.util.AddressUtil
import org.junit.Test

internal class BitcoinCashAddressValidationTest {
    @Test
    fun validateCashAddr() {
        val address = "bitcoincash:qrjc9yecwkldlhzys3euqz68f78s2wjxw5h6j9rqpq"
        assert(AddressUtil.isValidCashAddr(address))
    }

    @Test
    fun validateCashAddrNoPrefix() {
        val address = "qrjc9yecwkldlhzys3euqz68f78s2wjxw5h6j9rqpq"
        assert(AddressUtil.isValidCashAddr(address))
    }

    @Test
    fun validateLegacy() {
        val address = "1MvYASoHjqynMaMnP7SBmenyEWiLsTqoU6"
        assert(AddressUtil.isValidLegacy(address))
    }

    @Test
    fun validateInvalidCashAddr() {
        val address = "bitcoincash:qrjc9yecwkldlhzys3euqz68f9rqpq"
        assert(!AddressUtil.isValidCashAddr(address))
    }

    @Test
    fun validateInvalidLegacy() {
        val address = "1MvYASoHjqynMaMnP7SBmenyEWi"
        assert(!AddressUtil.isValidCashAddr(address))
    }
}