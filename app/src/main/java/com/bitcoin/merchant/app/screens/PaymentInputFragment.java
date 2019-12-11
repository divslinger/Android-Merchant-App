package com.bitcoin.merchant.app.screens;

import android.app.Activity;
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

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bitcoin.merchant.app.MainActivity;
import com.bitcoin.merchant.app.R;
import com.bitcoin.merchant.app.currency.CountryCurrency;
import com.bitcoin.merchant.app.currency.CurrencyExchange;
import com.bitcoin.merchant.app.util.AppUtil;
import com.bitcoin.merchant.app.util.MonetaryUtil;
import com.bitcoin.merchant.app.util.SnackCustom;
import com.bitcoin.merchant.app.util.ToastCustom;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

public class PaymentInputFragment extends Fragment  {
    private static final String TAG = "PaymentInputFragment";
    public static final String DEFAULT_CURRENCY_BCH = "BCH";
    public static final int RECEIVE_RESULT = 1122;
    public static final String ACTION_INTENT_RESET_AMOUNT = "RESET_AMOUNT";
    private static final double bitcoinLimit = 21_000_000.0;
    public static String AMOUNT_PAYABLE_FIAT = "AMOUNT_PAYABLE_FIAT";
    public static String AMOUNT_PAYABLE_BTC = "AMOUNT_PAYABLE_BTC";
    public double amountPayableFiat;
    public double amountPayableBch;
    private int allowedDecimalPlaces = 2;
    private View rootView = null;
    private TextView tvCurrencySymbol = null;
    private TextView tvAmount = null;
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
    private Button buttonDecimal = null;
    private TextView tvBch = null;
    private NumberFormat nf = null;
    private DecimalFormat df = null;
    private DecimalFormatSymbols dfs = null;
    private String strDecimal = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_payment, container, false);
        nf = NumberFormat.getInstance(Locale.getDefault());
        dfs = new DecimalFormatSymbols();
        df = new DecimalFormat("#.########", dfs);
        tvCurrencySymbol = rootView.findViewById(R.id.tv_currency_symbol);
        tvAmount = rootView.findViewById(R.id.tv_fiat_amount);
        Button ivCharge = rootView.findViewById(R.id.iv_charge);
        ivCharge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chargeClicked();
            }
        });
        tvBch = rootView.findViewById(R.id.tv_bch);
        initializeButtons();
        updateAmounts();
        initDecimalButton();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_INTENT_RESET_AMOUNT);
        LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver(receiver, filter);
        tvCurrencySymbol.setText(getCurrencySymbol());
        return rootView;
    }

    @Override
    public void onDestroyView() {
        FragmentActivity activity = getActivity();
        if (activity != null) {
            LocalBroadcastManager.getInstance(activity.getApplicationContext()).unregisterReceiver(receiver);
        }
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateAmounts();
        initDecimalButton();
        tvCurrencySymbol.setText(getCurrencySymbol());
        FragmentActivity activity = getActivity();
        if (activity != null) {
            // Query mempool, in case the previous TX was not received by the socket listeners
            // It will be unnecessary and can be removed after the switch to BIP-70 in the next big release.
            // Performing the request at the resume time of the application is critical to prevent the following issue:
            // When clicking on the checkout button, the device queries the mempool/unconfirmed tx for a missed tx,
            // Unfortunately, if two devices use the same pubKey/address, then it would erroneously detect
            // the last payment made on the other device as an instant payment for the new payment request
            // The next line attempts to detect the missed payments on appResume and prior to the checkout operation
            LocalBroadcastManager.getInstance(activity).sendBroadcast(new Intent(MainActivity.ACTION_QUERY_MISSING_TX_IN_MEMPOOL));
        }
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

    private String getCurrency() {
        return AppUtil.getCurrency(getActivity());
    }

    private String getCurrencySymbol() {
        String currency = AppUtil.getCurrency(getContext());
        String country = AppUtil.getCountry(getContext());
        String locale = AppUtil.getLocale(getContext());
        CountryCurrency cc = CurrencyExchange.getInstance(getContext()).getCountryCurrency(currency, country, locale);
        if (cc != null) {
            if (cc.currencyRate.symbol != null) {
                return cc.currencyRate.symbol;
            } else {
                /* The currency symbols displayed on the payment request screen are not able to be grabbed from the Currency class, or from CountryCurrency.
                   A prime example of this is country AOA. NumberFormat gives Kz for its symbol, yet you can't grab it from any object.
                   Here we format a fake balance (0) then remove all digits, periods, commas, etc. to get the symbol.

                   It's kinda hacky but it works.
                 */
                NumberFormat formatter = NumberFormat.getCurrencyInstance(cc.countryLocales.getLocale());
                Currency instance = Currency.getInstance(currency);
                formatter.setCurrency(instance);
                formatter.setMaximumFractionDigits(instance.getDefaultFractionDigits());
                String formatted = formatter.format(0);
                return formatted.replaceAll("[\\d., ]", "");
            }
        }
        return "";
    }


    private void initializeButtons() {
        View.OnClickListener digitListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                digitPressed(((Button) v).getText().toString());
                updateAmounts();
            }
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
        buttonDecimal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                decimalPressed();
                updateAmounts();
            }
        });
        Button buttonDeleteBack = rootView.findViewById(R.id.buttonDeleteBack);
        buttonDeleteBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backspacePressed();
                updateAmounts();
            }
        });
    }

    private boolean validateAmount() {
        try {
            Double value = nf.parse(tvAmount.getText().toString()).doubleValue();
            return !value.isInfinite() && !value.isNaN() && value > 0.0
                    && getCurrencyPrice() > 0.0;
        } catch (Exception e) {
            return false;
        }
    }

    public void chargeClicked() {
        final FragmentActivity activity = getActivity();
        if (activity == null) {
            return;
        }
        if (!AppUtil.getInstance(activity).hasValidReceiver()) {
            SnackCustom.make(activity, getView(), getActivity().getText(R.string.obligatory_receiver), getActivity().getResources().getString(R.string.prompt_ok), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(activity, SettingsActivity.class);
                    startActivityForResult(intent, MainActivity.SETTINGS_ACTIVITY);
                }
            });
            return;
        }
        if (validateAmount()) {
            updateAmounts();
            Intent intent = new Intent(activity, PaymentRequestActivity.class);
            intent.putExtra(AMOUNT_PAYABLE_FIAT, amountPayableFiat);
            intent.putExtra(AMOUNT_PAYABLE_BTC, amountPayableBch);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivityForResult(intent, RECEIVE_RESULT);
        } else {
            SnackCustom.make(activity, getView(), getActivity().getText(R.string.invalid_amount), getActivity().getResources().getString(R.string.prompt_ok), null);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //reset amount after receive
        if (requestCode == RECEIVE_RESULT && resultCode == Activity.RESULT_OK) {
            tvAmount.setText("0");
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
        checkBitcoinLimit();
    }

    private void checkBitcoinLimit() {
        double bchValue = 0.0;
        try {
            double amount = nf.parse(tvAmount.getText().toString()).doubleValue();
            bchValue = toBch(amount);
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
        if (bchValue > bitcoinLimit) {
            Double currencyPrice = getCurrencyPrice();
            tvAmount.setText(MonetaryUtil.getInstance().getFiatDecimalFormat().format(bitcoinLimit * currencyPrice));
            ToastCustom.makeText(getActivity(), getResources().getString(R.string.invalid_amount), ToastCustom.LENGTH_SHORT, ToastCustom.TYPE_ERROR);
        }
    }

    private void updateAmounts() {
        if (tvAmount == null) return;
        try {
            amountPayableFiat = nf.parse(tvAmount.getText().toString()).doubleValue();
            amountPayableBch = toBch(amountPayableFiat);
        } catch (Exception e) {
            amountPayableFiat = 0.0;
            amountPayableBch = 0.0;
            Log.e(TAG, "", e);
        }
        if (amountPayableFiat == 0.0) {
            tvBch.setText(R.string.enter_an_amount);
        } else {
            tvBch.setText(df.format(amountPayableBch) + " BCH");
        }
    }

    private double toBch(double amount) {
        Double currencyPrice = getCurrencyPrice();
        return (currencyPrice == 0.0d) ? 0.0d
                : new BigDecimal(amount).divide(new BigDecimal(currencyPrice), 8, RoundingMode.HALF_EVEN).doubleValue();
    }

    private Double getCurrencyPrice() {
        return CurrencyExchange.getInstance(getActivity()).getCurrencyPrice(getCurrency());
    }
}
