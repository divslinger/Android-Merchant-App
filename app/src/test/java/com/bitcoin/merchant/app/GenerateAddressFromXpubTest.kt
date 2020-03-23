package com.bitcoin.merchant.app

import com.bitcoin.merchant.app.util.WalletUtil
import org.junit.Assert.assertEquals
import org.bitcoinj.core.ECKey
import org.bitcoinj.crypto.ChildNumber
import org.bitcoinj.crypto.HDKeyDerivation
import org.bitcoinj.params.MainNetParams
import org.junit.Test

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

    @Test
    fun verifyDifferentIndexesAreDifferentAddresses() {
        val xpub = "xpub6CUGRUonZSQ4TWtTMmzXdrXDtypWKiKrhko4egpiMZbpiaQL2jkwSB1icqYh2cfDfVxdx4df189oLKnC5fSwqPfgyP3hooxujYzAu3fDVmz"
        val index1 = 0
        val index2 = 1
        val accountKey = WalletUtil.createMasterPubKeyFromXPub(xpub)
        val dk1 = HDKeyDerivation.deriveChildKey(accountKey, ChildNumber(index1, false))
        val ecKey1 = ECKey.fromPublicOnly(dk1.pubKey)
        val legacyAddress1 = ecKey1.toAddress(MainNetParams.get()).toBase58()
        val dk2 = HDKeyDerivation.deriveChildKey(accountKey, ChildNumber(index2, false))
        val ecKey2 = ECKey.fromPublicOnly(dk2.pubKey)
        val legacyAddress2 = ecKey2.toAddress(MainNetParams.get()).toBase58()
        println(legacyAddress1)
        println(legacyAddress2)
        assert(legacyAddress1 != legacyAddress2)
    }
}