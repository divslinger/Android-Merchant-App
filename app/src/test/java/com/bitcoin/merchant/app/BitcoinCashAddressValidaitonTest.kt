package com.bitcoin.merchant.app

import com.bitcoin.merchant.app.util.AddressUtil
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class BitcoinCashAddressValidaitonTest {
    @Test
    fun validateCashAddr() {
        val address = "bitcoincash:qrjc9yecwkldlhzys3euqz68f78s2wjxw5h6j9rqpq"
        assertEquals(address, "bitcoincash:qrjc9yecwkldlhzys3euqz68f78s2wjxw5h6j9rqpq")
        assertEquals(AddressUtil.isValidCashAddr(address), true)
    }

    @Test
    fun validateCashAddrNoPrefix() {
        val address = "qrjc9yecwkldlhzys3euqz68f78s2wjxw5h6j9rqpq"
        assertEquals(address, "qrjc9yecwkldlhzys3euqz68f78s2wjxw5h6j9rqpq")
        assertEquals(AddressUtil.isValidCashAddr(address), true)
    }

    @Test
    fun validateLegacy() {
        val address = "1MvYASoHjqynMaMnP7SBmenyEWiLsTqoU6"
        assertEquals(address, "1MvYASoHjqynMaMnP7SBmenyEWiLsTqoU6")
        assertEquals(AddressUtil.isValidLegacy(address), true)
    }

    @Test
    fun validateInvalidCashAddr() {
        val address = "bitcoincash:qrjc9yecwkldlhzys3euqz68f9rqpq"
        assertEquals(address, "bitcoincash:qrjc9yecwkldlhzys3euqz68f9rqpq")
        assertEquals(AddressUtil.isValidCashAddr(address), false)
    }

    @Test
    fun validateInvalidLegacy() {
        val address = "1MvYASoHjqynMaMnP7SBmenyEWi"
        assertEquals(address, "1MvYASoHjqynMaMnP7SBmenyEWi")
        assertEquals(AddressUtil.isValidCashAddr(address), false)
    }
}