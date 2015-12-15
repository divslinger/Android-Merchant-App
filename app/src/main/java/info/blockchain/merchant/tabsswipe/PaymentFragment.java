package info.blockchain.merchant.tabsswipe;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import info.blockchain.merchant.CurrencyExchange;
import info.blockchain.merchant.R;
import info.blockchain.merchant.ReceiveActivity;
import info.blockchain.merchant.api.APIFactory;
import info.blockchain.merchant.util.AppUtil;
import info.blockchain.merchant.util.PrefsUtil;
import info.blockchain.merchant.util.ToastCustom;

public class PaymentFragment extends Fragment implements View.OnClickListener {

    private View rootView = null;
    private TextView tvAmount = null;
    private ImageView ivCharge = null;
    private TextView tvCurrency = null;
    private LinearLayout llAmountContainer = null;
    public static String AMOUNT_PAYABLE_FIAT = "AMOUNT_PAYABLE_FIAT";
    public static String AMOUNT_PAYABLE_BTC = "AMOUNT_PAYABLE_BTC";

    public double amountPayableFiat = 0.0;
    public double amountPayableBtc = 0.0;

    private final int DECIMAL_PLACES_FIAT = 2;
    private final int DECIMAL_PLACES_BTC = 8;
    public static final String DEFAULT_CURRENCY_FIAT = "USD";
    public static final String DEFAULT_CURRENCY_BTC = "BTC";

    private boolean isBtc = false;
    private int allowedDecimalPlaces = DECIMAL_PLACES_FIAT;
    private DecimalFormat dfBtc = new DecimalFormat("######0.0######");
    private DecimalFormat dfFiat = new DecimalFormat("######0.00");
    private final double bitcoinLimit = 21000000.0;

    private static Timer timer = null;

    public static final int RECEIVE_RESULT = 1122;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_payment, container, false);

        tvAmount = (TextView)rootView.findViewById(R.id.tv_fiat_amount);
        ivCharge = (ImageView)rootView.findViewById(R.id.iv_charge);
        ivCharge.setOnClickListener(this);
        tvCurrency = (TextView)rootView.findViewById(R.id.tv_currency);
        llAmountContainer = (LinearLayout)rootView.findViewById(R.id.amount_container);
        llAmountContainer.setOnClickListener(this);

        initPadClickListeners();

        initValues();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        if(PrefsUtil.getInstance(getActivity()).getValue(PrefsUtil.MERCHANT_KEY_ACCOUNT_INDEX, 0L) > APIFactory.getInstance(getActivity()).getAccountIndex())    {
            if(timer == null) {
                timer = new Timer();

                timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try	{
                                    APIFactory.getInstance(getActivity()).getXPUB();
                                    PrefsUtil.getInstance(getActivity()).setValue(PrefsUtil.MERCHANT_KEY_ACCOUNT_INDEX, APIFactory.getInstance(getActivity()).getAccountIndex());
                                }
                                catch(Exception e)	{
                                    System.out.println(e.getMessage());
                                    e.printStackTrace();
                                }
                            }
                        }).start();

                    }
                }, 1000, 60000);
            }
        }
        else if(timer != null)    {
            timer.cancel();
            timer = null;
        }
        else    {
            ;
        }

        isBtc = PrefsUtil.getInstance(getActivity()).getValue(PrefsUtil.MERCHANT_KEY_CURRENCY_DISPLAY, false);

        initValues();
    }

    private void initValues(){

        isBtc = PrefsUtil.getInstance(getActivity()).getValue(PrefsUtil.MERCHANT_KEY_CURRENCY_DISPLAY, false);

        if(tvCurrency != null)    {
            if(isBtc)    {
                tvCurrency.setText(DEFAULT_CURRENCY_BTC);
                allowedDecimalPlaces = DECIMAL_PLACES_BTC;
            }
            else    {
                tvCurrency.setText(PrefsUtil.getInstance(getActivity()).getValue(PrefsUtil.MERCHANT_KEY_CURRENCY, DEFAULT_CURRENCY_FIAT));
                allowedDecimalPlaces = DECIMAL_PLACES_FIAT;
            }
        }
        updateAmounts();
    }

    private void initPadClickListeners(){
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
            case R.id.button1:padClicked(v.getTag().toString().substring(0, 1));break;
            case R.id.button2:padClicked(v.getTag().toString().substring(0, 1));break;
            case R.id.button3:padClicked(v.getTag().toString().substring(0, 1));break;
            case R.id.button4:padClicked(v.getTag().toString().substring(0, 1));break;
            case R.id.button5:padClicked(v.getTag().toString().substring(0, 1));break;
            case R.id.button6:padClicked(v.getTag().toString().substring(0, 1));break;
            case R.id.button7:padClicked(v.getTag().toString().substring(0, 1));break;
            case R.id.button8:padClicked(v.getTag().toString().substring(0, 1));break;
            case R.id.button9:padClicked(v.getTag().toString().substring(0, 1));break;
            case R.id.button10:padClicked(".");break;
            case R.id.button0:padClicked(v.getTag().toString().substring(0, 1));break;
            case R.id.buttonDeleteBack:padClicked(null);break;

            case R.id.amount_container: toggleAmount();break;

            case R.id.iv_charge:chargeClicked(); return;
        }

        updateAmounts();
    }

    private boolean validateAmount(){
        try {
            double textParsable = Double.parseDouble(tvAmount.getText().toString());
            if (textParsable > 0.0) {
                return true;
            }else{
                return false;
            }
        }catch(Exception e){
            return false;
        }
    }

    public void chargeClicked() {

        if(!AppUtil.getInstance(getActivity()).hasValidReceiver())    {
            ToastCustom.makeText(getActivity(), getActivity().getText(R.string.no_valid_receiver), ToastCustom.LENGTH_LONG, ToastCustom.TYPE_ERROR);
            return;
        }

        if(validateAmount()) {
            updateAmounts();
            Intent intent = new Intent(getActivity(), ReceiveActivity.class);
            intent.putExtra(AMOUNT_PAYABLE_FIAT, amountPayableFiat);
            intent.putExtra(AMOUNT_PAYABLE_BTC, amountPayableBtc);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivityForResult(intent, RECEIVE_RESULT);
        }else{
            ToastCustom.makeText(getActivity(),getResources().getString(R.string.invalid_amount),ToastCustom.LENGTH_SHORT,ToastCustom.TYPE_ERROR);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //reset amount after receive
        if(requestCode == RECEIVE_RESULT && resultCode == Activity.RESULT_OK) {
            tvAmount.setText("0");
        }
    }

    public void padClicked(String pad) {

        // Back clicked
        if(pad == null){
            String e1 = tvAmount.getText().toString();
            if (e1.length() > 1) {
                tvAmount.setText(e1.substring(0, e1.length() - 1));
            }else {
                tvAmount.setText("0");
            }
            return;
        }

        String amountText = tvAmount.getText().toString();

        //initial input
        if((!isBtc && amountText.equals("0.00")) || (isBtc && amountText.equals("0.00000000")) || amountText.equals("0") || amountText.equals("")){

            if(pad.equals(".")){
                tvAmount.setText("0.");
                return;
            }else{
                tvAmount.setText(pad);
                return;
            }
        }

        //Don't allow multiple decimal separators
        if(amountText.contains(".") && pad.equals(".")){
            return;
        }

        //Don't allow multiple leading 0's
        if(amountText.equals("0") && pad.equals("0")){
            return;
        }

        //Get decimal places
        if(amountText.contains(".")) {

            int decimalPlaces = 0;
            amountText += pad;
            String[] result = amountText.split("\\.");

            if(result.length >= 2)
                decimalPlaces = result[1].length();

            if(decimalPlaces > allowedDecimalPlaces)return;
        }

        if(pad!=null) {
            // Append tapped #
            tvAmount.append(pad);
        }

        //Check that we don't exceed bitcoin limit
        checkBitcoinLimit();
    }

    private void checkBitcoinLimit(){

        double currentValue = Double.parseDouble(tvAmount.getText().toString());
        if(isBtc){

            if(currentValue > bitcoinLimit) {
                tvAmount.setText(dfBtc.format(bitcoinLimit));
                ToastCustom.makeText(getActivity(), getResources().getString(R.string.btc_limit_reached), ToastCustom.LENGTH_SHORT, ToastCustom.TYPE_ERROR);
            }
        }else if(!isBtc){

            String strCurrency = PrefsUtil.getInstance(getActivity()).getValue(PrefsUtil.MERCHANT_KEY_CURRENCY, DEFAULT_CURRENCY_FIAT);
            Double currencyPrice = CurrencyExchange.getInstance(getActivity()).getCurrencyPrice(strCurrency);
            double btcValue = Double.parseDouble(dfBtc.format(currentValue / currencyPrice));
            if(btcValue > bitcoinLimit) {
                tvAmount.setText(dfFiat.format(bitcoinLimit * currencyPrice));
                ToastCustom.makeText(getActivity(), getResources().getString(R.string.btc_limit_reached), ToastCustom.LENGTH_SHORT, ToastCustom.TYPE_ERROR);
            }
        }
    }

    private void toggleAmount(){

        if(isBtc) {
            tvAmount.setText(dfFiat.format(amountPayableFiat));
            tvCurrency.setText(PrefsUtil.getInstance(getActivity()).getValue(PrefsUtil.MERCHANT_KEY_CURRENCY, DEFAULT_CURRENCY_FIAT));
            allowedDecimalPlaces = DECIMAL_PLACES_FIAT;
            PrefsUtil.getInstance(getActivity()).setValue(PrefsUtil.MERCHANT_KEY_CURRENCY_DISPLAY, false);
        }else {
            tvAmount.setText(dfBtc.format(amountPayableBtc));
            tvCurrency.setText(DEFAULT_CURRENCY_BTC);
            allowedDecimalPlaces = DECIMAL_PLACES_BTC;
            PrefsUtil.getInstance(getActivity()).setValue(PrefsUtil.MERCHANT_KEY_CURRENCY_DISPLAY, true);
        }

        isBtc = !isBtc;
    }

    private void updateAmounts(){

        if(tvAmount==null)return;

        double amount = Double.parseDouble(tvAmount.getText().toString());

        Locale locale = new Locale("en", "US");
        Locale.setDefault(locale);

        String strCurrency = PrefsUtil.getInstance(getActivity()).getValue(PrefsUtil.MERCHANT_KEY_CURRENCY, DEFAULT_CURRENCY_FIAT);
        Double currencyPrice = CurrencyExchange.getInstance(getActivity()).getCurrencyPrice(strCurrency);

        if(isBtc) {
            amountPayableFiat = Double.parseDouble(dfFiat.format(amount * currencyPrice));
            amountPayableBtc = amount;
        }else {
            amountPayableFiat = amount;
            amountPayableBtc = Double.parseDouble(dfBtc.format(amount / currencyPrice));
        }
    }
}

