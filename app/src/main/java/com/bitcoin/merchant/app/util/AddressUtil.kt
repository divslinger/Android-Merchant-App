package com.bitcoin.merchant.app.util

import com.github.kiulian.converter.AddressConverter
import com.github.kiulian.converter.b58.B58
import de.tobibrandt.bitcoincash.BitcoinCashAddressFormatter

object AddressUtil {
    fun isValidCashAddr(address: String?): Boolean {
        return BitcoinCashAddressFormatter.isValidCashAddress(address)
    }

    fun isValidLegacy(address: String): Boolean {
        return try {
            B58.decodeAndCheck(address)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun toCashAddress(legacy: String): String {
        return AddressConverter.toCashAddress(legacy)
    }
}