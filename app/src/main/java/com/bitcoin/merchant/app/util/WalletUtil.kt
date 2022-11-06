package com.bitcoin.merchant.app.util

import android.util.Log
import com.bitcoin.merchant.app.application.CashRegisterApplication
import com.bitcoin.merchant.app.database.DBControllerV3
import com.bitcoin.merchant.app.model.Analytics
import org.bitcoinj.core.AddressFormatException
import org.bitcoinj.core.Base58
import org.bitcoinj.core.ECKey
import org.bitcoinj.crypto.ChildNumber
import org.bitcoinj.crypto.DeterministicKey
import org.bitcoinj.crypto.HDKeyDerivation
import org.bitcoinj.params.MainNetParams
import org.json.JSONObject
import java.net.URL
import java.nio.ByteBuffer
import java.util.*

class WalletUtil(private val urlRestBitcoinCom: String, private val xPub: String, private val app: CashRegisterApplication) {
    private val accountKey: DeterministicKey
    // For performance reasons, we cache all used addresses (reported in May 2019 on Lenovo Tab E8)
    private val addressBank: AddressBank
    private var xpubIndex: Int = Settings.getXPubIndex(app, xPub)
    fun isSameXPub(xPub: String): Boolean {
        return this.xPub == xPub
    }

    fun addUsedAddress(address: String) {
        addressBank.addUsedAddress(address.trim { it <= ' ' })
    }

    private fun saveWallet(newIndex: Int) {
        Settings.setXPubIndex(app, xPub, newIndex)
        Log.d(TAG, "Saving new xpub index $newIndex")
    }

    /**
     * Gets the next address without checking balance on the network rest.bitcoin.com
     * to reduce delays & risks of failure when generating QR code
     */
    fun getAddressFromXPubAndMoveToNext(): String {
        val address = getAddressFromXpubKey(xpubIndex)
        moveToNextAddress()
        return address
    }

    fun syncXpub(): Boolean {
        return try {
            loopThroughXpubChildren()
            true
        } catch (e: Exception) {
            Analytics.error_syncing_xpub.sendError(e)
            false
        }
    }

    @Throws(Exception::class)
    private fun loopThroughXpubChildren(): String {
        var potentialAddress = getAddressFromXpubKey(xpubIndex)
        while (true) {
            if (!addressBank.isUsed(potentialAddress)) {
                if (doesAddressHaveHistoryOnBlockchain(potentialAddress)) {
                    addUsedAddress(potentialAddress)
                } else {
                    break // current address is unused
                }
            }
            // new address required
            moveToNextAddress()
            potentialAddress = getAddressFromXpubKey(xpubIndex)
        }
        return potentialAddress
    }

    private fun moveToNextAddress() {
        xpubIndex++
        Log.d(TAG, "Getting next xpub index $xpubIndex")
        saveWallet(xpubIndex)
    }

    @Throws(Exception::class)
    private fun doesAddressHaveHistoryOnBlockchain(address: String): Boolean {
        var doubleBackOff: Long = 1000
        while (true) {
            doubleBackOff *= try {
                val json = JSONObject(URL("$urlRestBitcoinCom/address/details/$address").readText())
                return json.getJSONArray("transactions").length() > 0
            } catch (e: Exception) {
                Analytics.error_rest_bitcoin_com_scan_address_funds.sendError(e)
                Log.e(TAG, "doesAddressHaveHistory", e)
                try {
                    Thread.sleep(doubleBackOff)
                } catch (ex: InterruptedException) {
                    // fail silently
                }
                2
            }
            if (doubleBackOff > 32_000)
                throw Exception()
        }
    }

    private fun getAddressFromXpubKey(index: Int): String {
        // This takes the accountKey from earlier, on the receive chain, and generates an address. For example, m/44'/145'/0'/0/{index}
        val dk = HDKeyDerivation.deriveChildKey(accountKey, ChildNumber(index, false))
        val ecKey = ECKey.fromPublicOnly(dk.pubKey)
        return ecKey.toAddress(MainNetParams.get()).toBase58()
    }

    override fun toString(): String {
        return "WalletUtil{xPub='$xPub', index=$xpubIndex}"
    }

    private inner class AddressBank(db: DBControllerV3) {
        val usedAddresses: MutableSet<String?>
        fun isUsed(address: String?): Boolean {
            return usedAddresses.contains(address)
        }

        fun addUsedAddress(address: String?) {
            usedAddresses.add(address)
        }

        init {
            var addresses: Set<String>
            try {
                addresses = db.allAddresses
                Log.d(TAG, "loaded ${addresses.size} addresses from TX history: $addresses")
            } catch (e: Exception) {
                Analytics.error_db_read_address.sendError(e)
                addresses = HashSet()
                Log.e(TAG, "Unable to load addresses from TX history", e)
            }
            usedAddresses = Collections.synchronizedSet(addresses)
        }
    }

    companion object {
        const val TAG = "BCR-WalletUtil"
        @Throws(AddressFormatException::class)
        fun createMasterPubKeyFromXPub(xpubstr: String?): DeterministicKey {
            val xpubBytes = Base58.decodeChecked(xpubstr)
            val bb = ByteBuffer.wrap(xpubBytes)
            val prefix = bb.int
            if (prefix != 0x0488B21E) {
                throw AddressFormatException("invalid xpub version")
            }
            val chain = ByteArray(32)
            val pub = ByteArray(33)
            bb.get()
            bb.int
            bb.int
            bb[chain]
            bb[pub]
            return HDKeyDerivation.createMasterPubKeyFromBytes(pub, chain)
        }
    }

    init {
        val key = createMasterPubKeyFromXPub(xPub)
        //This gets the receive chain from the xpub. If you want to generate change addresses, switch to 1 for the childNumber.
        accountKey = HDKeyDerivation.deriveChildKey(key, ChildNumber(0, false))
        addressBank = AddressBank(app.db)
    }
}