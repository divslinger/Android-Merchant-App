package info.blockchain.merchant.tabsswipe;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.Locale;

import info.blockchain.merchant.CurrencyExchange;
import info.blockchain.merchant.R;
import info.blockchain.merchant.ReceiveActivity;
import info.blockchain.merchant.util.PrefsUtil;

public class PaymentFragment extends Fragment implements View.OnClickListener {

    private View rootView = null;
    private TextView tvAmount = null;
    private TextView tvCharge = null;
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
    private DecimalFormat dfBtc = new DecimalFormat("######0.00000000");
    private DecimalFormat dfFiat = new DecimalFormat("######0.00");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_payment, container, false);

        tvAmount = (TextView)rootView.findViewById(R.id.tv_fiat_amount);
        tvCharge = (TextView)rootView.findViewById(R.id.tv_charge);
        tvCurrency = (TextView)rootView.findViewById(R.id.tv_currency);
        llAmountContainer = (LinearLayout)rootView.findViewById(R.id.amount_container);
        llAmountContainer.setOnClickListener(this);

        //Start off with fiat
        isBtc = false;

        initPadClickListeners();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        initValues();
    }

    private void initValues(){
        if(tvCurrency != null && !isBtc)    {
            tvCurrency.setText(PrefsUtil.getInstance(getActivity()).getValue(PrefsUtil.MERCHANT_KEY_CURRENCY, DEFAULT_CURRENCY_FIAT));
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

            case R.id.tv_charge:chargeClicked(); return;
        }

        validateAmount();
    }

    private void validateAmount(){
        try {
            double textParsable = Double.parseDouble(tvAmount.getText().toString());
            if (textParsable > 0.0) {
                tvCharge.setOnClickListener(this);
                tvCharge.setTextColor(getResources().getColor(R.color.white));
            }else{
                tvCharge.setOnClickListener(null);
                tvCharge.setTextColor(getResources().getColor(R.color.white_50));
            }
        }catch(Exception e){
            tvCharge.setOnClickListener(null);
            tvCharge.setTextColor(getResources().getColor(R.color.white_50));
        }

        updateAmounts();
    }

    public void chargeClicked() {

        updateAmounts();
        Intent intent = new Intent(getActivity(), ReceiveActivity.class);
        intent.putExtra(AMOUNT_PAYABLE_FIAT,amountPayableFiat);
        intent.putExtra(AMOUNT_PAYABLE_BTC,amountPayableBtc);
        startActivity(intent);
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
    }

    private void toggleAmount(){

        if(isBtc) {
            tvAmount.setText(dfFiat.format(amountPayableFiat));
            tvCurrency.setText(PrefsUtil.getInstance(getActivity()).getValue(PrefsUtil.MERCHANT_KEY_CURRENCY, DEFAULT_CURRENCY_FIAT));
            allowedDecimalPlaces = DECIMAL_PLACES_FIAT;
        }else {
            tvAmount.setText(dfBtc.format(amountPayableBtc));
            tvCurrency.setText(DEFAULT_CURRENCY_BTC);
            allowedDecimalPlaces = DECIMAL_PLACES_BTC;
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

