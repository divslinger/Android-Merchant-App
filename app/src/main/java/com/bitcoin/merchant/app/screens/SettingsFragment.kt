package com.bitcoin.merchant.app.screens

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bitcoin.merchant.app.Action
import com.bitcoin.merchant.app.R
import com.bitcoin.merchant.app.model.Analytics
import com.bitcoin.merchant.app.model.CountryCurrencyLocale
import com.bitcoin.merchant.app.model.PaymentTarget
import com.bitcoin.merchant.app.screens.dialogs.*
import com.bitcoin.merchant.app.screens.features.ToolbarAwareFragment
import com.bitcoin.merchant.app.util.Settings

class SettingsFragment : ToolbarAwareFragment() {
    private val TAG = "BCR-Settings"
    private lateinit var rootView: View
    private lateinit var lvMerchantName: LinearLayout
    private lateinit var lvPaymentAddress: LinearLayout
    private lateinit var lvLocalCurrency: LinearLayout
    private lateinit var lvPinCode: LinearLayout
    private lateinit var lvBip70: LinearLayout
    private lateinit var btnWallet: RelativeLayout
    private lateinit var btnLocalBitcoin: RelativeLayout

    private val paymentTargetReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (Action.SET_PAYMENT_TARGET == intent.action) {
                if (intent.extras != null) {
                    setPaymentTargetFromScan(intent.getStringExtra(Action.PARAM_PAYMENT_TARGET))
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        rootView = inflater.inflate(R.layout.fragment_settings, container, false)
        lvMerchantName = rootView.findViewById(R.id.lv_merchant_name)
        lvPaymentAddress = rootView.findViewById(R.id.lv_payment_address)
        lvLocalCurrency = rootView.findViewById(R.id.lv_fiat_currency)
        lvPinCode = rootView.findViewById(R.id.lv_pin_code)
        lvBip70 = rootView.findViewById(R.id.lv_bip70)
        btnWallet = rootView.findViewById(R.id.wallet_ad)
        btnLocalBitcoin = rootView.findViewById(R.id.localbch_ad)
        addOptionName()
        addOptionCurrency()
        addOptionAddress()
        addOptionPin()
        addOptionMultiterminal()
        btnWallet.setOnClickListener {
            Analytics.tap_link_wallet.send()
            openUrl(activity.getString(R.string.url_wallet_bitcoin_com))
        }
        btnLocalBitcoin.setOnClickListener {
            Analytics.tap_link_localbitcoin.send()
            openUrl(activity.getString(R.string.url_local_bitcoin_com))
        }
        setToolbarAsBackButton()
        setToolbarTitle(R.string.menu_settings)
        LocalBroadcastManager.getInstance(activity).registerReceiver(paymentTargetReceiver, IntentFilter(Action.SET_PAYMENT_TARGET))
        return rootView
    }

    override fun onDestroyView() {
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(paymentTargetReceiver)
        super.onDestroyView()
    }

    private fun openUrl(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    private fun addOptionName() {
        val merchantName: String = Settings.getMerchantName(activity)
        val tvMerchantName = rootView.findViewById<TextView>(R.id.et_merchant_name)
        tvMerchantName.text = if (merchantName.isNotEmpty()) merchantName else "..."
        lvMerchantName.setOnClickListener {
            Analytics.settings_merchantname_edit.send()
            MerchantNameEditorDialog(activity).show(tvMerchantName)
        }
    }

    private fun addOptionCurrency() {
        setCurrencySummary(Settings.getCountryCurrencyLocale(activity))
        lvLocalCurrency.setOnClickListener {
            Analytics.settings_currency_edit.send()
            CurrencySelectionDialog(this@SettingsFragment).show()
        }
    }

    private fun addOptionAddress() {
        val tvPaymentAddress = rootView.findViewById<TextView>(R.id.et_payment_address)
        val paymentTarget = Settings.getPaymentTarget(activity)
        val summary = if (paymentTarget.type === PaymentTarget.Type.INVALID)
            "...\n\n" + getString(R.string.options_explain_payment_address)
        else paymentTarget.bchAddress
        tvPaymentAddress.text = summary
        lvPaymentAddress.setOnClickListener {
            Analytics.settings_paymenttarget_edit.send()
            AddNewAddressDialog(this@SettingsFragment).show()
        }
    }

    private fun addOptionPin() {
        val tvPinCode = rootView.findViewById<TextView>(R.id.et_pin_code)
        tvPinCode.text = "####"
        lvPinCode.setOnClickListener {
            Analytics.settings_pin_edit.send()
            changePin()
        }
    }

    private fun addOptionMultiterminal() {
        val tvMultiterminal = rootView.findViewById<TextView>(R.id.tv_bip70)
        val multiterminal = Settings.getMultiterminal(activity)
        val summary = if (multiterminal)
            getString(R.string.enabled)
        else getString(R.string.disabled)
        tvMultiterminal.text = summary
        lvBip70.setOnClickListener {
            ToggleMultiterminalDialog(this@SettingsFragment).show()
        }
    }

    private fun changePin() {
        val args = Bundle()
        args.putBoolean(PinCodeFragment.EXTRA_DO_CREATE, true)
        nav.navigate(R.id.nav_to_pin_code_screen, args)
    }

    fun setCurrencySummary(countryCurrency: CountryCurrencyLocale) {
        val currencyView = rootView.findViewById<TextView>(R.id.et_local_currency)
        currencyView.text = countryCurrency.toString()
    }

    override val isBackAllowed: Boolean
        get() = if (!Settings.getPaymentTarget(activity).isValid) {
            notifyUserThatAddressIsRequiredToReceivePayments()
            false // forbid
        } else {
            true
        }

    private fun notifyUserThatAddressIsRequiredToReceivePayments() {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.options_payment_address)
                .setMessage(R.string.obligatory_receiver)
                .setCancelable(false)
                .setPositiveButton(R.string.prompt_ok) { dialog, _ -> dialog.dismiss() }
        builder.create().show()
    }

    fun setAndDisplayPaymentTarget(target: PaymentTarget) {
        val v = rootView.findViewById<TextView>(R.id.et_payment_address)
        v.text = target.bchAddress
        if (Settings.getPaymentTarget(activity) != target) {
            Settings.setPaymentTarget(activity, target)
            Analytics.settings_paymenttarget_changed.send()
            when (target.type) {
                PaymentTarget.Type.ADDRESS -> Analytics.settings_paymenttarget_pubkey_set.send()
                PaymentTarget.Type.XPUB -> Analytics.settings_paymenttarget_xpub_set.send()
                PaymentTarget.Type.API_KEY -> Analytics.settings_paymenttarget_apikey_set.send()
                // PaymentTarget.Type.PAIRING_CODE -> Analytics.settings_paymenttarget_pairingcode_set.send()
                PaymentTarget.Type.INVALID -> {
                    // no action
                }
            }
            SnackHelper.show(activity, activity.getString(R.string.notify_changes_have_been_saved))
        }
    }

    fun setMultiterminal(enabled: Boolean) {
        val v = rootView.findViewById<TextView>(R.id.tv_bip70)
        v.text = if(enabled) getString(R.string.enabled) else getString(R.string.disabled)
        SnackHelper.show(activity, activity.getString(R.string.notify_changes_have_been_saved))
    }

    fun validateThenSetPaymentTarget(address: String?) {
        val paymentTarget = PaymentTarget.parse(address)
        if (paymentTarget.isValid) {
            setAndDisplayPaymentTarget(paymentTarget)
            if (paymentTarget.isXPub) {
                beginSyncingXpubWallet()
            }
        } else {
            // If it is not valid, then display to the user that they did not enter a valid xpub, or legacy/cashaddr address.
            SnackHelper.show(activity, activity.getString(R.string.unrecognized_xpub), error = true)
        }
    }

    private fun beginSyncingXpubWallet() {
        // When a merchant sets an xpub as their address in the settings,
        // sync the wallet up to the freshest address so users won't be sending to older addresses.
        // We do this by polling Bitcoin.com's REST API until we find a fresh address.
        object : Thread() {
            override fun run() {
                try {
                    val synced = app.wallet.syncXpub()
                    if (synced) {
                        SnackHelper.show(activity, activity.getString(R.string.synced_xpub))
                    } else {
                        val errorMessage = activity.getString(R.string.error) + ": " + activity.getString(R.string.syncing_xpub)
                        SnackHelper.show(activity, errorMessage, error = true)
                    }
                } catch (e: Exception) {
                    // analytics already covered in callee
                    Log.e(TAG, "", e)
                }
            }
        }.start()
        SnackHelper.show(activity, activity.getString(R.string.syncing_xpub))
    }

    fun setPaymentTargetFromScan(target: String?) {
        if (target != null) {
            validateThenSetPaymentTarget(target)
        }
        app.qrCodeScanner.isScanning = false
    }

    override fun canFragmentBeDiscardedWhenInBackground(): Boolean {
        return Settings.getPaymentTarget(activity).isValid && !app.qrCodeScanner.isScanning
    }
}