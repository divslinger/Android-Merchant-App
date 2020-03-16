package com.bitcoin.merchant.app.model

import android.util.Log
import com.bitcoin.merchant.app.util.AddressUtil
import org.apache.commons.lang3.StringUtils
import org.bitcoinj.core.Xpub

data class PaymentTarget(val type: Type, val target: String) {
    val TAG = "PaymentTarget"
    val isValid = type != Type.INVALID
    val isXPub = type == Type.XPUB
    val isApiKey = type == Type.API_KEY
    val isAddress = type == Type.ADDRESS

    /**
     * Get legacy format if it's an address, otherwise returns default target.
     */
    val legacyAddress: String = target

    /**
     * Converts legacy to BCH address, otherwise returns default target.
     */
    val bchAddress: String
        get() {
            if (type == Type.ADDRESS) {
                try {
                    return AddressUtil.toCashAddress(target)
                } catch (e: Exception) {
                    Analytics.error_convert_address_to_bch.sendError(e)
                    Log.e(TAG, "", e)
                }
            }
            return target
        }

    enum class Type {
        INVALID, XPUB, ADDRESS, API_KEY
    }

    companion object {
        private val REGEX_API_KEY = Regex("[a-z]{40}")

        private fun isApiKey(value: String): Boolean {
            return !StringUtils.isEmpty(value)
                    && REGEX_API_KEY.matches(value)
        }

        @JvmStatic
        fun parse(value: String?): PaymentTarget {
            if (value == null || StringUtils.isEmpty(value))
                return PaymentTarget(Type.INVALID, "")
            if (Xpub.isValid(value))
                return PaymentTarget(Type.XPUB, value)
            if (AddressUtil.isValidLegacy(value))
                return PaymentTarget(Type.ADDRESS, value)
            if (AddressUtil.isValidCashAddr(value))
                return PaymentTarget(Type.ADDRESS, AddressUtil.toLegacyAddress(value))
            if (isApiKey(value))
                return PaymentTarget(Type.API_KEY, value)
            return PaymentTarget(Type.INVALID, "")
        }
    }
}
