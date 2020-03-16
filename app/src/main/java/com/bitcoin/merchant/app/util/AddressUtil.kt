package com.bitcoin.merchant.app.util

import org.bitcoinj.core.Address
import org.bitcoinj.core.AddressFormatException
import org.bitcoinj.core.CashAddressFactory
import org.bitcoinj.core.SlpAddress
import org.bitcoinj.params.MainNetParams

object AddressUtil {
    fun isValidCashAddr(cashAddress: String): Boolean {
        return try {
            Address.fromCashAddr(MainNetParams.get(), cashAddress)
            true
        } catch (e: AddressFormatException) {
            false
        }
    }

    fun isValidLegacy(legacyAddress: String): Boolean {
        return try {
            Address.fromBase58(MainNetParams.get(), legacyAddress)
            true
        } catch (e: AddressFormatException) {
            false
        }
    }

    fun toCashAddress(legacy: String): String {
        return CashAddressFactory.create().getFromBase58(MainNetParams.get(), legacy).toString()
    }

    fun toLegacyAddress(address: String): String {
        return CashAddressFactory.create().getFromFormattedAddress(MainNetParams.get(), address).toBase58()
    }

    fun toSimpleLedgerAddress(address: String): String {
        return SlpAddress.fromCashAddr(MainNetParams.get(), address).toString()
    }

    fun fromSimpleLedgerAddress(address: String): String {
        return SlpAddress(MainNetParams.get(), address).toCashAddress()
    }
}