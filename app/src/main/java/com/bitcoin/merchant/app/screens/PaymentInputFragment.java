package com.bitcoin.merchant.app.screens;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bitcoin.merchant.app.R;
import com.bitcoin.merchant.app.screens.features.ToolbarAwareFragment;
import com.bitcoin.merchant.app.util.AmountUtil;
import com.bitcoin.merchant.app.util.AppUtil;
import com.bitcoin.merchant.app.util.MonetaryUtil;
import com.bitcoin.merchant.app.util.SnackCustom;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

public class PaymentInputFragment extends ToolbarAwareFragment {
    private static final String TAG = "PaymentInputFragment";
    public static final String ACTION_INTENT_RESET_AMOUNT = "RESET_AMOUNT";
    public static String AMOUNT_PAYABLE_FIAT = "AMOUNT_PAYABLE_FIAT";
    public double amountPayableFiat;
    private int allowedDecimalPlaces = 2;
    private View rootView;
    private TextView tvCurrencySymbol;
    private TextView tvAmount;
    protected BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            if (ACTION_INTENT_RESET_AMOUNT.equals(intent.getAction())) {
                if (tvAmount != null) {
                    tvAmount.setText("0");
                }
            }
        }
    };
    private Button buttonDecimal;
    private TextView tvBch;
    private NumberFormat nf;
    private String strDecimal;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        rootView = inflater.inflate(R.layout.fragment_input_amount, container, false);
        nf = NumberFormat.getInstance(Locale.getDefault());
        tvCurrencySymbol = rootView.findViewById(R.id.tv_currency_symbol);
        tvAmount = rootView.findViewById(R.id.tv_fiat_amount);
        Button ivCharge = rootView.findViewById(R.id.iv_charge);
        ivCharge.setOnClickListener(v -> chargeClicked());
        tvBch = rootView.findViewById(R.id.tv_bch);
        initializeButtons();
        updateAmounts();
        initDecimalButton();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_INTENT_RESET_AMOUNT);
        LocalBroadcastManager.getInstance(activity.getApplicationContext()).registerReceiver(receiver, filter);
        tvCurrencySymbol.setText(getCurrencySymbol());
        setToolbarAsMenuButton();
        clearToolbarTitle();
        return rootView;
    }

    @Override
    public void onDestroyView() {
        if (activity != null) {
            LocalBroadcastManager.getInstance(activity.getApplicationContext()).unregisterReceiver(receiver);
        }
        super.onDestroyView();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Ensure PIN & BCH address are correctly configured
        if (PinCodeFragment.isPinMissing(activity)) {
            Bundle args = new Bundle();
            args.putBoolean(PinCodeFragment.EXTRA_DO_CREATE, true);
            getNav().navigate(R.id.pin_code_screen, args);
        } else if (!AppUtil.getPaymentTarget(activity).isValid()) {
            getNav().navigate(R.id.nav_to_settings_screen_bypass_security);
        } else {
            if (AppUtil.getActiveInvoice(activity) != null) {
                getNav().navigate(R.id.nav_to_payment_request_screen);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateAmounts();
        initDecimalButton();
        tvCurrencySymbol.setText(getCurrencySymbol());
    }

    private void initDecimalButton() {
        strDecimal = Character.toString(MonetaryUtil.getInstance().getDecimalFormatSymbols().getDecimalSeparator());
        try {
            Currency instance = Currency.getInstance(AppUtil.getCurrency(getContext()));
            allowedDecimalPlaces = instance.getDefaultFractionDigits();
            boolean enabled = allowedDecimalPlaces > 0;
            View buttonView = rootView.findViewById(R.id.buttonDecimal);
            buttonView.setEnabled(enabled);
            buttonDecimal.setText(enabled ? strDecimal : "");
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
    }

    private String getCurrencySymbol() {
        String currency = AppUtil.getCurrency(getContext());
        String fiat = new AmountUtil(activity).formatFiat(0.0);
        String symbol = fiat.replaceAll("[\\s\\d.,]+", "");
        // It seems that replaceAll does not handle right-to-left languages like arabic correctly
        // For this reason, we consider that it is only safe to extract single character symbol
        return symbol.length() == 1 ? symbol : currency;
    }

    private void initializeButtons() {
        View.OnClickListener digitListener = v -> {
            digitPressed(((Button) v).getText().toString());
            updateAmounts();
        };
        rootView.findViewById(R.id.button0).setOnClickListener(digitListener);
        rootView.findViewById(R.id.button1).setOnClickListener(digitListener);
        rootView.findViewById(R.id.button2).setOnClickListener(digitListener);
        rootView.findViewById(R.id.button3).setOnClickListener(digitListener);
        rootView.findViewById(R.id.button4).setOnClickListener(digitListener);
        rootView.findViewById(R.id.button5).setOnClickListener(digitListener);
        rootView.findViewById(R.id.button6).setOnClickListener(digitListener);
        rootView.findViewById(R.id.button7).setOnClickListener(digitListener);
        rootView.findViewById(R.id.button8).setOnClickListener(digitListener);
        rootView.findViewById(R.id.button9).setOnClickListener(digitListener);
        buttonDecimal = rootView.findViewById(R.id.buttonDecimal);
        buttonDecimal.setOnClickListener(v -> {
            decimalPressed();
            updateAmounts();
        });
        Button buttonDeleteBack = rootView.findViewById(R.id.buttonDeleteBack);
        buttonDeleteBack.setOnClickListener(v -> {
            backspacePressed();
            updateAmounts();
        });
    }

    private boolean validateAmount() {
        try {
            Double value = nf.parse(tvAmount.getText().toString()).doubleValue();
            return !value.isInfinite() && !value.isNaN() && value > 0.0;
        } catch (Exception e) {
            return false;
        }
    }

    public void chargeClicked() {
        if (activity == null) {
            return;
        }
        if (!AppUtil.getPaymentTarget(activity).isValid()) {
            SnackCustom.make(activity, getView(), activity.getText(R.string.obligatory_receiver), activity.getResources().getString(R.string.prompt_ok), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getNav().navigate(R.id.nav_to_settings_screen_bypass_security);
                }
            });
            return;
        }
        if (validateAmount()) {
            updateAmounts();
            Bundle extras = new Bundle();
            extras.putDouble(PaymentInputFragment.AMOUNT_PAYABLE_FIAT, amountPayableFiat);
            getNav().navigate(R.id.nav_to_payment_request_screen, extras);
        } else {
            SnackCustom.make(activity, getView(), activity.getText(R.string.invalid_amount), activity.getResources().getString(R.string.prompt_ok), null);
        }
    }

    private void backspacePressed() {
        String amountText = tvAmount.getText().toString();
        if (amountText.length() > 1) {
            tvAmount.setText(amountText.substring(0, amountText.length() - 1));
        } else {
            tvAmount.setText("0");
        }
    }

    private void decimalPressed() {
        String amountText = tvAmount.getText().toString();
        if (amountText.contains(strDecimal)) {
            return; // Don't allow multiple decimal separators
        }
        double amount;
        try {
            amount = nf.parse(amountText).doubleValue();
        } catch (Exception pe) {
            amount = 0.0;
        }
        if (amount == 0.0) {
            tvAmount.setText("0" + strDecimal);
        } else {
            tvAmount.append(strDecimal);
        }
    }

    private void digitPressed(String digit) {
        String amountText = tvAmount.getText().toString();
        if (amountText.equals("0")) {
            tvAmount.setText(digit);
            return;
        }
        int i = amountText.indexOf(strDecimal);
        if (i >= 0) {
            String decimalPart = amountText.substring(i + 1);
            if (decimalPart.length() >= allowedDecimalPlaces) {
                return;
            }
        }
        tvAmount.append(digit);
    }

    private void updateAmounts() {
        if (tvAmount == null) return;
        try {
            double amount = nf.parse(tvAmount.getText().toString()).doubleValue();
            amountPayableFiat = amount;
        } catch (Exception e) {
            amountPayableFiat = 0.0;
            Log.e(TAG, "", e);
        }
        if (amountPayableFiat == 0.0) {
            tvBch.setText("Enter an amount");
        }
    }
}
