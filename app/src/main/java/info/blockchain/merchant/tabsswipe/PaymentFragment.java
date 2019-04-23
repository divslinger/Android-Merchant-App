package info.blockchain.merchant.tabsswipe;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bitcoin.merchant.app.currency.CurrencyExchange;
import com.bitcoin.merchant.app.MainActivity;
import com.bitcoin.merchant.app.R;
import com.bitcoin.merchant.app.ReceiveTxActivity;
import com.bitcoin.merchant.app.SettingsActivity;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.Timer;

import info.blockchain.merchant.util.AppUtil;
import info.blockchain.merchant.util.MonetaryUtil;
import info.blockchain.merchant.util.PrefsUtil;
import info.blockchain.merchant.util.SnackCustom;
import info.blockchain.merchant.util.ToastCustom;
//import android.util.Log;

public class PaymentFragment extends Fragment implements View.OnClickListener {
    public static final String DEFAULT_CURRENCY_BCH = "BCH";
    public static final int RECEIVE_RESULT = 1122;
    public static String AMOUNT_PAYABLE_FIAT = "AMOUNT_PAYABLE_FIAT";
    public static String AMOUNT_PAYABLE_BTC = "AMOUNT_PAYABLE_BTC";
    private static Timer timer = null;
    private final int DECIMAL_PLACES_FIAT = 2;
    private final int DECIMAL_PLACES_BTC = 8;
    private final double bitcoinLimit = 21000000.0;
    public double amountPayableFiat = 0.0;
    public double amountPayableBtc = 0.0;
    private View rootView = null;
    private TextView tvAmount = null;
    private ImageView ivCharge = null;
    private TextView tvCurrency = null;
    private LinearLayout llAmountContainer = null;
    private boolean isBch = false;
    private int allowedDecimalPlaces = DECIMAL_PLACES_FIAT;
    private NumberFormat nf = null;
    private String strDecimal = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_payment, container, false);
        nf = NumberFormat.getInstance(Locale.getDefault());
        strDecimal = Character.toString(MonetaryUtil.getInstance().getDecimalFormatSymbols().getDecimalSeparator());
        ((TextView) rootView.findViewById(R.id.decimal)).setText(strDecimal);
        tvAmount = rootView.findViewById(R.id.tv_fiat_amount);
        ivCharge = rootView.findViewById(R.id.iv_charge);
        ivCharge.setOnClickListener(this);
        tvCurrency = rootView.findViewById(R.id.tv_currency);
        llAmountContainer = rootView.findViewById(R.id.amount_container);
        llAmountContainer.setOnClickListener(this);
        initPadClickListeners();
        initValues();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        isBch = PrefsUtil.getInstance(getActivity()).getValue(PrefsUtil.MERCHANT_KEY_CURRENCY_DISPLAY, false);
        initValues();
    }

    private void initValues() {
        isBch = PrefsUtil.getInstance(getActivity()).getValue(PrefsUtil.MERCHANT_KEY_CURRENCY_DISPLAY, false);
        if (tvCurrency != null) {
            if (isBch) {
                tvCurrency.setText(DEFAULT_CURRENCY_BCH);
                allowedDecimalPlaces = DECIMAL_PLACES_BTC;
            } else {
                tvCurrency.setText(getCurrency());
                allowedDecimalPlaces = DECIMAL_PLACES_FIAT;
            }
        }
        updateAmounts();
    }

    private String getCurrency() {
        return AppUtil.getCurrency(getActivity());
    }

    private void initPadClickListeners() {
        rootView.findViewById(R.id.button0).setOnClickListener(this);
        rootView.findViewById(R.id.button1).setOnClickListener(this);
        rootView.findViewById(R.id.button2).setOnClickListener(this);
        rootView.findViewById(R.id.button3).setOnClickListener(this);
        rootView.findViewById(R.id.button4).setOnClickListener(this);
        rootView.findViewById(R.id.button5).setOnClickListener(this);
        rootView.findViewById(R.id.button6).setOnClickListener(this);
        rootView.findViewById(R.id.button7).setOnClickListener(this);
        rootView.findViewById(R.id.button8).setOnClickListener(this);
        rootView.findViewById(R.id.button9).setOnClickListener(this);
        rootView.findViewById(R.id.button0).setOnClickListener(this);
        rootView.findViewById(R.id.button10).setOnClickListener(this);
        rootView.findViewById(R.id.buttonDeleteBack).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button1:
                padClicked(v.getTag().toString().substring(0, 1));
                break;
            case R.id.button2:
                padClicked(v.getTag().toString().substring(0, 1));
                break;
            case R.id.button3:
                padClicked(v.getTag().toString().substring(0, 1));
                break;
            case R.id.button4:
                padClicked(v.getTag().toString().substring(0, 1));
                break;
            case R.id.button5:
                padClicked(v.getTag().toString().substring(0, 1));
                break;
            case R.id.button6:
                padClicked(v.getTag().toString().substring(0, 1));
                break;
            case R.id.button7:
                padClicked(v.getTag().toString().substring(0, 1));
                break;
            case R.id.button8:
                padClicked(v.getTag().toString().substring(0, 1));
                break;
            case R.id.button9:
                padClicked(v.getTag().toString().substring(0, 1));
                break;
            case R.id.button10:
                padClicked(strDecimal);
                break;
            case R.id.button0:
                padClicked(v.getTag().toString().substring(0, 1));
                break;
            case R.id.buttonDeleteBack:
                padClicked(null);
                break;
            case R.id.amount_container:
                toggleAmount();
                break;
            case R.id.iv_charge:
                chargeClicked();
                return;
        }
        updateAmounts();
    }

    private boolean validateAmount() {
        try {
            Double value = Double.valueOf(nf.parse(tvAmount.getText().toString()).doubleValue());
            if (!value.isInfinite() && !value.isNaN() && value > 0.0) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
//            e.printStackTrace();
            return false;
        }
    }

    public void chargeClicked() {
        if (!AppUtil.getInstance(getActivity()).hasValidReceiver()) {
            SnackCustom.make(getActivity(), getView(), getActivity().getText(R.string.no_valid_receiver), getActivity().getResources().getString(R.string.prompt_ok), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), SettingsActivity.class);
                    startActivityForResult(intent, MainActivity.SETTINGS_ACTIVITY);
                }
            });
            return;
        }
        if (validateAmount()) {
            updateAmounts();
            Intent intent = new Intent(getActivity(), ReceiveTxActivity.class);
            intent.putExtra(AMOUNT_PAYABLE_FIAT, amountPayableFiat);
            intent.putExtra(AMOUNT_PAYABLE_BTC, amountPayableBtc);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivityForResult(intent, RECEIVE_RESULT);
        } else {
            SnackCustom.make(getActivity(), getView(), getActivity().getText(R.string.invalid_amount), getActivity().getResources().getString(R.string.prompt_ok), null);
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

    public void padClicked(String pad) {
        // Back clicked
        if (pad == null) {
            String e1 = tvAmount.getText().toString();
            if (e1.length() > 1) {
                tvAmount.setText(e1.substring(0, e1.length() - 1));
            } else {
                tvAmount.setText("0");
            }
            return;
        }
        String amountText = tvAmount.getText().toString();
        if (amountText.length() == 1 && amountText.charAt(0) == '0' && !pad.equals(strDecimal)) {
            tvAmount.setText(pad);
            return;
        }
        //initial input
        double amount = 0.0;
        try {
            amount = nf.parse(amountText).doubleValue();
        } catch (ParseException pe) {
            amount = 0.0;
        }
        if (amount == 0.0 && pad.equals(strDecimal)) {
            tvAmount.setText("0" + strDecimal);
            return;
        }
        //Don't allow multiple decimal separators
        if (amountText.contains(strDecimal) && pad.equals(strDecimal)) {
            return;
        }
        //Don't allow multiple leading 0's
        if (amountText.equals("0") && pad.equals("0")) {
            return;
        }
        //Get decimal places
        if (amountText.contains(strDecimal)) {
            int decimalPlaces = 0;
            amountText += pad;
            String[] result = amountText.split(strDecimal);
            if (result.length >= 2)
                decimalPlaces = result[1].length();
            if (decimalPlaces > allowedDecimalPlaces) return;
        }
        if (pad != null) {
            // Append tapped #
            tvAmount.append(pad);
        }
        //Check that we don't exceed bitcoin limit
        checkBitcoinLimit();
    }

    private void checkBitcoinLimit() {
        double currentValue = 0.0;
        try {
            currentValue = nf.parse(tvAmount.getText().toString()).doubleValue();
        } catch (ParseException pe) {
            pe.printStackTrace();
        }
        if (isBch) {
            if (currentValue > bitcoinLimit) {
                tvAmount.setText(MonetaryUtil.getInstance().getBTCDecimalFormat().format(bitcoinLimit));
                ToastCustom.makeText(getActivity(), getResources().getString(R.string.btc_limit_reached), ToastCustom.LENGTH_SHORT, ToastCustom.TYPE_ERROR);
            }
        } else {
            Double currencyPrice = CurrencyExchange.getInstance(getActivity()).getCurrencyPrice(getCurrency());
            double btcValue = 0.0;
            try {
                btcValue = nf.parse(MonetaryUtil.getInstance().getBTCDecimalFormat().format(currentValue / currencyPrice)).doubleValue();
            } catch (ParseException pe) {
                pe.printStackTrace();
            }
            if (btcValue > bitcoinLimit) {
                tvAmount.setText(MonetaryUtil.getInstance().getFiatDecimalFormat().format(bitcoinLimit * currencyPrice));
                ToastCustom.makeText(getActivity(), getResources().getString(R.string.btc_limit_reached), ToastCustom.LENGTH_SHORT, ToastCustom.TYPE_ERROR);
            }
        }
    }

    private void toggleAmount() {
        if (isBch) {
            tvAmount.setText(MonetaryUtil.getInstance().getFiatDecimalFormat().format(amountPayableFiat));
            tvCurrency.setText(getCurrency());
            allowedDecimalPlaces = DECIMAL_PLACES_FIAT;
            PrefsUtil.getInstance(getActivity()).setValue(PrefsUtil.MERCHANT_KEY_CURRENCY_DISPLAY, false);
        } else {
            tvAmount.setText(MonetaryUtil.getInstance().getBTCDecimalFormat().format(amountPayableBtc));
            tvCurrency.setText(DEFAULT_CURRENCY_BCH);
            allowedDecimalPlaces = DECIMAL_PLACES_BTC;
            PrefsUtil.getInstance(getActivity()).setValue(PrefsUtil.MERCHANT_KEY_CURRENCY_DISPLAY, true);
        }
        isBch = !isBch;
    }

    private void updateAmounts() {
        if (tvAmount == null) return;
        try {
            double amount = nf.parse(tvAmount.getText().toString()).doubleValue();
            Double currencyPrice = CurrencyExchange.getInstance(getActivity()).getCurrencyPrice(getCurrency());
            MonetaryUtil util = MonetaryUtil.getInstance();
            if (isBch) {
                amountPayableFiat = nf.parse(util.getFiatDecimalFormat().format(amount * currencyPrice)).doubleValue();
                amountPayableBtc = amount;
            } else {
                amountPayableFiat = amount;
                amountPayableBtc = (currencyPrice == 0.0d) ? 0.0d : nf.parse(util.getBTCDecimalFormat().format(amount / currencyPrice)).doubleValue();
            }
        } catch (ParseException pe) {
            amountPayableFiat = 0.0;
            amountPayableBtc = 0.0;
            pe.printStackTrace();
        }
    }
}
