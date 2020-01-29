package com.bitcoin.merchant.app.util

import android.content.Context
import android.preference.PreferenceManager
import info.blockchain.wallet.util.PersistantPrefs

class PrefsUtil private constructor() : PersistantPrefs {
    override fun getValue(name: String, value: String?): String {
        // TODO
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getString(name, value ?: "") ?: ""
    }

    override fun setValue(name: String, value: String?): Boolean {
        val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
        editor.putString(name, value ?: "")
        return editor.commit()
    }

    override fun getValue(name: String, value: Int): Int {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getInt(name, 0)
    }

    override fun setValue(name: String, value: Int): Boolean {
        val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
        editor.putInt(name, if (value < 0) 0 else value)
        return editor.commit()
    }

    override fun setValue(name: String, value: Long): Boolean {
        val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
        editor.putLong(name, if (value < 0L) 0L else value)
        return editor.commit()
    }

    override fun getValue(name: String, value: Long): Long {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val result: Long
        result = try {
            prefs.getLong(name, 0L)
        } catch (e: Exception) {
            prefs.getInt(name, 0).toLong()
        }
        return result
    }

    override fun getValue(name: String, value: Boolean): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(name, value)
    }

    override fun setValue(name: String, value: Boolean): Boolean {
        val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
        editor.putBoolean(name, value)
        return editor.commit()
    }

    override fun has(name: String): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.contains(name)
    }

    override fun removeValue(name: String): Boolean {
        val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
        editor.remove(name)
        return editor.commit()
    }

    override fun clear(): Boolean {
        val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
        editor.clear()
        return editor.commit()
    }

    companion object {
        const val MERCHANT_KEY_PIN = "pin"
        const val MERCHANT_KEY_CURRENCY = "currency"
        const val MERCHANT_KEY_COUNTRY = "country"
        const val MERCHANT_KEY_LANG_LOCALE = "locale"
        const val MERCHANT_KEY_MERCHANT_NAME = "receiving_name"
        const val MERCHANT_KEY_MERCHANT_RECEIVER = "receiving_address"
        const val MERCHANT_KEY_XPUB_INDEX = "xpub_index"
        const val MERCHANT_KEY_ACCOUNT_INDEX = "account_idx"
        const val MERCHANT_KEY_EULA = "eula"
        const val MERCHANT_KEY_PERSIST_INVOICE = "persist_invoice"
        private lateinit var context: Context
        private lateinit var instance: PrefsUtil
        fun getInstance(ctx: Context): PrefsUtil {
            context = ctx
            if (!::instance.isInitialized) {
                instance = PrefsUtil()
            }
            return instance
        }
    }
}