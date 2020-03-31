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
    fun validateP2SHCashAddr() {
        val address = "bitcoincash:pp67j94cfvnfg727etymlst9jts3uhfdkurqvtj2un"
        assert(AddressUtil.isValidCashAddr(address))
    }

    @Test
    fun validateP2SHLegacy() {
        val address = "3CSUDH5yW1KHJmMDHfCCWShWgJkbVnfvnJ"
        assert(AddressUtil.isValidLegacy(address))
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

    @Test
    fun validateInvalidCashAddrUsingTest() {
        val address = "bchtest:qzgmyjle755g2v5kptrg02asx5f8k8fg55xlze46jr"
        assert(!AddressUtil.isValidCashAddr(address))
    }

    @Test
    fun validateInvalidCashAddrUsingTestNoPrefix() {
        val address = "qzgmyjle755g2v5kptrg02asx5f8k8fg55xlze46jr"
        assert(!AddressUtil.isValidCashAddr(address))
    }

    @Test
    fun validateInvalidLegacyUsingTest() {
        val address = "mtoKs9V381UAhUia3d7Vb9GNak8Qvmcsme"
        assert(!AddressUtil.isValidLegacy(address))
    }
}