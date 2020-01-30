package com.bitcoin.merchant.app.screens

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bitcoin.merchant.app.R
import com.bitcoin.merchant.app.ScanQRCodeActivity
import com.bitcoin.merchant.app.model.CountryCurrencyLocale
import com.bitcoin.merchant.app.screens.features.ToolbarAwareFragment
import com.bitcoin.merchant.app.util.AppUtil
import com.bitcoin.merchant.app.model.PaymentTarget
import com.bitcoin.merchant.app.screens.dialogs.*
import com.bitcoin.merchant.app.util.PrefsUtil

class SettingsFragment : ToolbarAwareFragment() {
    private lateinit var rootView: View
    private lateinit var lvMerchantName: LinearLayout
    private lateinit var lvPaymentAddress: LinearLayout
    private lateinit var lvLocalCurrency: LinearLayout
    private lateinit var lvPinCode: LinearLayout
    private lateinit var btnLocalBitcoin: RelativeLayout
    private lateinit var btnThePit: RelativeLayout
    private var isScanning = false
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        rootView = inflater.inflate(R.layout.fragment_settings, container, false)
        lvMerchantName = rootView.findViewById(R.id.lv_merchant_name)
        lvPaymentAddress = rootView.findViewById(R.id.lv_payment_address)
        lvLocalCurrency = rootView.findViewById(R.id.lv_fiat_currency)
        lvPinCode = rootView.findViewById(R.id.lv_pin_code)
        btnLocalBitcoin = rootView.findViewById(R.id.localbch_ad)
        btnThePit = rootView.findViewById(R.id.bce_ad)
        addOptionName()
        addOptionCurrency()
        addOptionAddress()
        addOptionPin()
        btnLocalBitcoin.setOnClickListener { openUrl("https://local.bitcoin.com") }
        btnThePit.setOnClickListener { openUrl("https://exchange.bitcoin.com") }
        setToolbarAsBackButton()
        setToolbarTitle(R.string.menu_settings)
        return rootView
    }

    private fun openUrl(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    private fun addOptionName() {
        val merchantName: String = PrefsUtil.getInstance(activity).getValue(PrefsUtil.MERCHANT_KEY_MERCHANT_NAME, "...")
        val tvMerchantName = rootView.findViewById<TextView>(R.id.et_merchant_name)
        tvMerchantName.text = merchantName
        lvMerchantName.setOnClickListener { MerchantNameEditorDialog(activity).show(tvMerchantName) }
    }

    private fun addOptionCurrency() {
        setCurrencySummary(AppUtil.getCountryCurrencyLocale(activity))
        lvLocalCurrency.setOnClickListener { CurrencySelectionDialog(this@SettingsFragment).show() }
    }

    private fun addOptionAddress() {
        val tvPaymentAddress = rootView.findViewById<TextView>(R.id.et_payment_address)
        val paymentTarget = AppUtil.getPaymentTarget(activity)
        val summary = if (paymentTarget.type === PaymentTarget.Type.INVALID) "...\n\n" + getString(R.string.options_explain_payment_address) else paymentTarget.bchAddress
        tvPaymentAddress.text = summary
        lvPaymentAddress.setOnClickListener { AddNewAddressDialog(this@SettingsFragment).show() }
    }

    private fun addOptionPin() {
        val tvPinCode = rootView.findViewById<TextView>(R.id.et_pin_code)
        tvPinCode.text = "####"
        lvPinCode.setOnClickListener { changePin() }
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
        get() = if (!AppUtil.getPaymentTarget(activity).isValid) {
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                isScanning = false
                val text = "Please grant camera permission to use the QR Scanner"
                Toast.makeText(activity, text, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun requestToOpenCamera() {
        isScanning = true
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION)
        } else {
            openCamera()
        }
    }

    private fun openCamera() {
        val intent = Intent(activity, ScanQRCodeActivity::class.java)
        startActivityForResult(intent, ZBAR_SCANNER_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == ZBAR_SCANNER_REQUEST && data != null) {
            Log.v(TAG, "requestCode:" + requestCode + ", resultCode:" + resultCode + ", Intent:" + data.getStringExtra(SCAN_RESULT))
            println("ADDRESS SCANNED: " + data.getStringExtra(SCAN_RESULT))
            validateThenSetReceiverKey(data.getStringExtra(SCAN_RESULT))
            isScanning = false
        } else {
            Log.v(TAG, "requestCode:$requestCode, resultCode:$resultCode")
        }
    }

    fun setAndDisplayPaymentTarget(target: PaymentTarget) {
        val v = rootView.findViewById<TextView>(R.id.et_payment_address)
        v.text = target.bchAddress
        AppUtil.setPaymentTarget(activity, target)
    }

    fun validateThenSetReceiverKey(address: String?) {
        val paymentTarget = PaymentTarget.parse(address)
        if (paymentTarget.isValid) {
            setAndDisplayPaymentTarget(paymentTarget)
            if (paymentTarget.isXPub) {
                beginSyncingXpubWallet()
            }
        } else {
            //If it is not valid, then display to the user that they did not enter a valid xpub, or legacy/cashaddr address.
            val text = activity.getString(R.string.unrecognized_xpub)
            SnackHelper.show(activity, rootView, text, error = true)
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
                        val text = activity.getString(R.string.synced_xpub)
                        SnackHelper.show(activity, rootView, text)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "", e)
                }
            }
        }.start()
        val text = activity.getString(R.string.syncing_xpub)
        SnackHelper.show(activity, rootView, text)
    }

    override fun canFragmentBeDiscardedWhenInBackground(): Boolean {
        return AppUtil.getPaymentTarget(activity).isValid && !isScanning
    }

    companion object {
        const val SCAN_RESULT = "SCAN_RESULT"
        private const val TAG = "SettingsActivity"
        private const val CAMERA_PERMISSION = 1111
        private const val ZBAR_SCANNER_REQUEST = 2026
    }
}