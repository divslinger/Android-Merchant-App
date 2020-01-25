package com.bitcoin.merchant.app

import com.bitcoin.merchant.app.util.WalletUtil
import org.bitcoinj.core.ECKey
import org.bitcoinj.crypto.ChildNumber
import org.bitcoinj.crypto.HDKeyDerivation
import org.bitcoinj.params.MainNetParams
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class GenerateAddressFromXpubTest {
    @Test
    fun createDeterministicKeyFromXpub() {
        val xpub = "xpub6CUGRUonZSQ4TWtTMmzXdrXDtypWKiKrhko4egpiMZbpiaQL2jkwSB1icqYh2cfDfVxdx4df189oLKnC5fSwqPfgyP3hooxujYzAu3fDVmz"
        val account = WalletUtil.createMasterPubKeyFromXPub(xpub)
        println(account.publicKeyAsHex)
        assertEquals(account.publicKeyAsHex, "0340bc6a46ca1adac64cc9599c8a6004afd7782c20d2d568a101e3a81029517358")
    }

    @Test
    fun generateReceiveAddressFromXpub() {
        val xpub = "xpub6CUGRUonZSQ4TWtTMmzXdrXDtypWKiKrhko4egpiMZbpiaQL2jkwSB1icqYh2cfDfVxdx4df189oLKnC5fSwqPfgyP3hooxujYzAu3fDVmz"
        val index = 0
        val accountKey = WalletUtil.createMasterPubKeyFromXPub(xpub)
        val dk = HDKeyDerivation.deriveChildKey(accountKey, ChildNumber(index, false))
        val ecKey = ECKey.fromPublicOnly(dk.pubKey)
        val legacyAddress = ecKey.toAddress(MainNetParams.get()).toBase58()
        println(legacyAddress)
        assertEquals(legacyAddress, "1HZhTawZTGXaphD2Ut1qhfC2Sij1WKQydE")
    }
}