package com.bitcoin.merchant.app.screens

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bitcoin.merchant.app.R
import com.bitcoin.merchant.app.currency.CurrencyExchange
import com.bitcoin.merchant.app.model.Analytics
import com.bitcoin.merchant.app.screens.dialogs.SnackHelper
import com.bitcoin.merchant.app.screens.features.ToolbarAwareFragment
import com.bitcoin.merchant.app.util.AmountUtil
import com.bitcoin.merchant.app.util.MonetaryUtil
import com.bitcoin.merchant.app.util.QrCodeUtil
import com.bitcoin.merchant.app.util.Settings
import kotlinx.coroutines.*
import org.bitcoindotcom.bchprocessor.bip70.Bip70Manager
import org.bitcoindotcom.bchprocessor.bip70.Bip70SocketHandler
import java.text.NumberFormat
import java.util.*

class PaymentInputFragment : ToolbarAwareFragment() {
    private val MAX_ALLOWED_NUMBER_OF_DIGIT = 12
    private var amountPayableFiat = 0.0
    private var allowedDecimalPlaces = 2
    private lateinit var rootView: View
    private lateinit var tvCurrencySymbol: TextView
    private lateinit var tvAmount: TextView
    private lateinit var buttonDecimal: Button
    private lateinit var tvBch: TextView
    private lateinit var nf: NumberFormat
    private var strDecimal: String = ""
    private var receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (ACTION_INTENT_RESET_AMOUNT == intent.action) {
                tvAmount.text = "0"
            }
        }
    }
    private var currencyExchange: CurrencyExchange? = null
    private val outOfDateRates: MutableLiveData<Boolean> = MutableLiveData(false)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        rootView = inflater.inflate(R.layout.fragment_input_amount, container, false)
        nf = NumberFormat.getInstance(Locale.getDefault())
        tvCurrencySymbol = rootView.findViewById(R.id.tv_currency_symbol)
        tvAmount = rootView.findViewById(R.id.tv_fiat_amount)
        val ivCharge = rootView.findViewById<Button>(R.id.iv_charge)
        ivCharge.setOnClickListener { chargeClicked() }
        tvBch = rootView.findViewById(R.id.tv_bch)
        initializeButtons()
        initDecimalButton()
        val filter = IntentFilter()
        filter.addAction(ACTION_INTENT_RESET_AMOUNT)
        LocalBroadcastManager.getInstance(activity).registerReceiver(receiver, filter)
        tvCurrencySymbol.text = currencySymbol
        setToolbarAsMenuButton()
        clearToolbarTitle()
        currencyExchange = CurrencyExchange.getInstance(context)
        runBlocking { currencyExchange?.forceExchangeRateUpdates() }
        //Initialize rates upon launch.
        getCurrencyPrice()

        outOfDateRates.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if(it == true) {
                GlobalScope.launch { currencyExchange?.forceExchangeRateUpdates() }
                SnackHelper.show(activity, activity.getString(R.string.out_of_date_fiat_rates), error = true)
            }
        })
        return rootView
    }

    override fun onDestroyView() {
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(receiver)
        super.onDestroyView()
    }

    override fun onStart() {
        super.onStart()
        // Ensure PIN & BCH address are correctly configured
        if (PinCodeFragment.isPinMissing(activity)) {
            val args = Bundle()
            args.putBoolean(PinCodeFragment.EXTRA_DO_CREATE, true)
            nav.navigate(R.id.pin_code_screen, args)
        } else if (!Settings.getPaymentTarget(activity).isValid) {
            nav.navigate(R.id.nav_to_settings_screen_bypass_security)
        }
    }

    override fun onResume() {
        super.onResume()
        updateAmounts()
        initDecimalButton()
        tvCurrencySymbol.text = currencySymbol
        // This code MUST be executed after MainActivity.onResume()
        // because it removes all screens above the PaymentInputFragment
        if (Settings.getActiveInvoice(activity) != null) {
            nav.navigate(R.id.nav_to_payment_request_screen)
        }
    }

    private fun getCurrencyPrice(): Double {
        return CurrencyExchange.getInstance(getActivity()).getCurrencyPrice(Settings.getCountryCurrencyLocale(activity).currency)
    }

    private fun initDecimalButton() {
        val ccl = Settings.getCountryCurrencyLocale(activity)
        try {
            strDecimal = MonetaryUtil.instance.decimalFormatSymbols.decimalSeparator.toString()
            allowedDecimalPlaces = ccl.decimals
            val enabled = allowedDecimalPlaces > 0
            val buttonView = rootView.findViewById<View>(R.id.buttonDecimal)
            buttonView.isEnabled = enabled
            buttonDecimal.text = if (enabled) strDecimal else ""
        } catch (e: Exception) {
            Analytics.error_format_currency.sendError(e, ccl.locale.country, ccl.currency, ccl.locale.displayName)
            Log.e(TAG, "", e)
        }
    }

    // It seems that replaceAll does not handle right-to-left languages like arabic correctly
    // For this reason, we consider that it is only safe to extract single character symbol
    private val currencySymbol: String
        get() {
            val currency = Settings.getCountryCurrencyLocale(activity).currency
            val fiat = AmountUtil(activity).formatFiat(0.0)
            val symbol = fiat.replace("[\\s\\d.,]+".toRegex(), "")
            // It seems that replaceAll does not handle right-to-left languages like arabic correctly
            // For this reason, we consider that it is only safe to extract single character symbol
            return if (symbol.length == 1) symbol else currency
        }

    private fun initializeButtons() {
        val digitListener = View.OnClickListener {
            checkConnectivity()
            digitPressed((it as Button).text.toString())
            outOfDateRates.value = currencyExchange?.isSeverelyOutOfDate
            updateAmounts()
        }
        rootView.findViewById<View>(R.id.button0).setOnClickListener(digitListener)
        rootView.findViewById<View>(R.id.button1).setOnClickListener(digitListener)
        rootView.findViewById<View>(R.id.button2).setOnClickListener(digitListener)
        rootView.findViewById<View>(R.id.button3).setOnClickListener(digitListener)
        rootView.findViewById<View>(R.id.button4).setOnClickListener(digitListener)
        rootView.findViewById<View>(R.id.button5).setOnClickListener(digitListener)
        rootView.findViewById<View>(R.id.button6).setOnClickListener(digitListener)
        rootView.findViewById<View>(R.id.button7).setOnClickListener(digitListener)
        rootView.findViewById<View>(R.id.button8).setOnClickListener(digitListener)
        rootView.findViewById<View>(R.id.button9).setOnClickListener(digitListener)
        buttonDecimal = rootView.findViewById(R.id.buttonDecimal)
        buttonDecimal.setOnClickListener {
            checkConnectivity()
            decimalPressed()
            updateAmounts()
        }
        val buttonDeleteBack = rootView.findViewById<Button>(R.id.buttonDeleteBack)
        buttonDeleteBack.setOnClickListener {
            checkConnectivity()
            backspacePressed()
            updateAmounts()
        }
        updateAmounts()
    }

    private fun checkConnectivity() {
        if (isActivityInitialized) {
            activity.restartSocketsWhenNeeded()
        }
    }

    private fun validateAmount(): Boolean {
        return try {
            val value: Double = getAmountFromUI()
            !value.isInfinite() && !value.isNaN() && value > 0.0
        } catch (e: Exception) {
            false
        }
    }

    private fun chargeClicked() {
        if (!isAdded) {
            return
        }
        val outOfDate = currencyExchange?.isSeverelyOutOfDate
        if(outOfDate == true) {
            outOfDateRates.value = outOfDate
            return
        }
        if (validateAmount()) {
            Analytics.invoice_checkout.send()
            updateAmounts()
            val extras = Bundle()
            extras.putDouble(AMOUNT_PAYABLE_FIAT, amountPayableFiat)
            nav.navigate(R.id.nav_to_payment_request_screen, extras)
        } else {
            SnackHelper.show(activity, activity.getText(R.string.invalid_amount),
                    activity.resources.getString(R.string.prompt_ok),
                    error = true)
        }
    }

    private fun backspacePressed() {
        val v = tvAmount.text.toString()
        tvAmount.text = if (v.length > 1) v.substring(0, v.length - 1) else "0"
    }

    private fun decimalPressed() {
        val amountText = tvAmount.text.toString()
        if (amountText.contains(strDecimal)) {
            return  // Don't allow multiple decimal separators
        }
        val amount = try {
            getAmountFromUI()
        } catch (pe: Exception) {
            0.0
        }
        if (amount == 0.0) {
            @SuppressLint("SetTextI18n")
            tvAmount.text = "0$strDecimal"
        } else {
            tvAmount.append(strDecimal)
        }
    }

    var warmedUp = false

    private fun digitPressed(digit: String) {
        if (!warmedUp) {
            warmedUp = true
            // the 2 next methods have no effect except saving about 500 ms of time
            // during the generation of the first invoice in PaymentRequestFragment
            // this provides a better user experience on some phones
            warmUpSocketConnection()
            warmUpQrCodeImageGeneration()
        }
        val amountText = tvAmount.text.toString()
        if (amountText == "0") {
            tvAmount.text = digit
            return
        }
        val i = amountText.indexOf(strDecimal)
        if (i >= 0) {
            val decimalPart = amountText.substring(i + 1)
            if (decimalPart.length >= allowedDecimalPlaces) {
                return
            }
        }
        if (amountText.length >= MAX_ALLOWED_NUMBER_OF_DIGIT) {
            return  // to avoid conversion error from server
        }
        tvAmount.append(digit)
    }

    private fun warmUpQrCodeImageGeneration() {
        viewLifecycleOwner.lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    QrCodeUtil.getBitmap("Test", 32)
                } catch (e: Exception) {
                    Log.e(TAG, "", e)
                }
            }
        }
    }

    private fun warmUpSocketConnection() {
        viewLifecycleOwner.lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    Bip70Manager(app)
                    Bip70SocketHandler(app, "")
                } catch (e: Exception) {
                    Log.e(TAG, "", e)
                }
            }
        }
    }

    private fun updateAmounts() {
        amountPayableFiat = try {
            getAmountFromUI()
        } catch (e: Exception) {
            Log.e(TAG, "", e)
            0.0
        }
        if (amountPayableFiat == 0.0) {
            tvBch.text = getString(R.string.payment_enter_an_amount)
        }
    }

    private fun getAmountFromUI() = (nf.parse(tvAmount.text.toString()) ?: 0.0).toDouble()

    companion object {
        private const val TAG = "BCR-PaymentInput"
        const val ACTION_INTENT_RESET_AMOUNT = "RESET_AMOUNT"
        var AMOUNT_PAYABLE_FIAT = "AMOUNT_PAYABLE_FIAT"
    }
}