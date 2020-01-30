package com.bitcoin.merchant.app.screens

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.bitcoin.merchant.app.R
import com.bitcoin.merchant.app.screens.features.ToolbarAwareFragment
import com.bitcoin.merchant.app.util.AppUtil
import com.bitcoin.merchant.app.util.PrefsUtil
import com.bitcoin.merchant.app.screens.dialogs.ToastHelper
import java.util.*

class PinCodeFragment : ToolbarAwareFragment() {
    private var userEnteredPIN = ""
    private var userEnteredPINConfirm: String? = null
    private lateinit var titleView: TextView
    private lateinit var pinBoxArray: Array<TextView>
    private var doCreate = false
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val rootView = inflater.inflate(R.layout.fragment_pin, container, false)
        val args = arguments
        if (args != null) {
            doCreate = args.getBoolean(EXTRA_DO_CREATE)
        }
        titleView = rootView.findViewById(R.id.titleBox)
        if (doCreate) {
            titleView.setText(R.string.create_pin)
        } else {
            titleView.setText(R.string.enter_pin)
        }
        pinBoxArray = arrayOf(
                rootView.findViewById(R.id.pinBox0),
                rootView.findViewById(R.id.pinBox1),
                rootView.findViewById(R.id.pinBox2),
                rootView.findViewById(R.id.pinBox3)
        )
        val digitListener = View.OnClickListener { v -> digitPressed((v as TextView).text.toString()) }
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
        val buttonDeleteBack = rootView.findViewById<Button>(R.id.buttonDeleteBack)
        buttonDeleteBack.setOnClickListener { deletePressed() }
        setToolbarVisible(false)
        return rootView
    }

    fun digitPressed(num: String) {
        if (userEnteredPIN.length == pinBoxArray.size) {
            return
        }
        // Append tapped #
        userEnteredPIN = userEnteredPIN + num
        pinBoxArray[userEnteredPIN.length - 1].setBackgroundResource(R.drawable.passcode_blob)
        // Perform appropriate action if PIN_LENGTH has been reached
        if (userEnteredPIN.length == pinBoxArray.size) {
            if (!doCreate) {
                validatePin()
            } else if (userEnteredPINConfirm == null) {
                newPinHasBeenEnteredOnce()
            } else if (userEnteredPINConfirm == userEnteredPIN) {
                newPinHasBeenConfirmed()
            } else {
                pinCodesMismatchedDuringCreation()
            }
        }
    }

    private fun newPinHasBeenConfirmed() {
        val hashed = userEnteredPIN
        PrefsUtil.getInstance(activity).setValue(PrefsUtil.MERCHANT_KEY_PIN, hashed)
        PrefsUtil.getInstance(activity).setValue(PrefsUtil.MERCHANT_KEY_ACCOUNT_INDEX, 0)
        nav.navigate(R.id.nav_to_settings_screen)
    }

    private fun validatePin() {
        val hashed = userEnteredPIN
        val stored: String = PrefsUtil.getInstance(activity).getValue(PrefsUtil.MERCHANT_KEY_PIN, "")
        if (stored == hashed) {
            nav.navigate(R.id.nav_to_settings_screen)
        } else {
            delayAction(Runnable {
                ToastHelper.makeText(activity, getString(R.string.pin_code_enter_error), ToastHelper.LENGTH_SHORT, ToastHelper.TYPE_ERROR)
                clearPinBoxes()
                userEnteredPIN = ""
                userEnteredPINConfirm = null
            }, 750)
        }
    }

    private fun newPinHasBeenEnteredOnce() { // request confirmation
        val action = Runnable {
            titleView.setText(R.string.confirm_pin)
            clearPinBoxes()
            userEnteredPINConfirm = userEnteredPIN
            userEnteredPIN = ""
        }
        delayAction(action, 200)
    }

    private fun delayAction(action: Runnable, delay: Int) {
        val timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                activity.runOnUiThread(action)
                timer.cancel()
            }
        }, delay.toLong())
    }

    private fun pinCodesMismatchedDuringCreation() {
        ToastHelper.makeText(activity, getString(R.string.pin_code_create_error), ToastHelper.LENGTH_SHORT, ToastHelper.TYPE_ERROR)
        clearPinBoxes()
        userEnteredPIN = ""
        userEnteredPINConfirm = null
        titleView.setText(R.string.create_pin)
    }

    fun deletePressed() {
        clearPinBoxes()
        userEnteredPIN = ""
    }

    private fun clearPinBoxes() {
        if (userEnteredPIN.length > 0) {
            for (aPinBoxArray in pinBoxArray) {
                aPinBoxArray.background = null //reset pin buttons blank
            }
        }
    }

    override fun canFragmentBeDiscardedWhenInBackground(): Boolean {
        return AppUtil.getPaymentTarget(activity).isValid && !doCreate
    }

    companion object {
        const val EXTRA_DO_CREATE = "doCreate"
        fun isPinMissing(ctx: Context): Boolean {
            val pin: String = PrefsUtil.getInstance(ctx).getValue(PrefsUtil.MERCHANT_KEY_PIN, "")
            return pin == ""
        }
    }
}