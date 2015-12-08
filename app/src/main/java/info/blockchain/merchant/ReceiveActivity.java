package info.blockchain.merchant;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.android.Contents;
import com.google.zxing.client.android.encode.QRCodeEncoder;

import org.bitcoinj.core.Coin;
import org.bitcoinj.uri.BitcoinURI;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.Locale;

import info.blockchain.merchant.tabsswipe.PaymentFragment;
import info.blockchain.merchant.util.MonetaryUtil;
import info.blockchain.merchant.util.PrefsUtil;
import info.blockchain.merchant.util.ToastCustom;

public class ReceiveActivity extends Activity implements View.OnClickListener{

    private TextView tvFiatAmount = null;
    private TextView tvBtcAmount = null;
    private TextView tvReceivingAddress = null;
    private ImageView ivReceivingQr = null;
    private ProgressBar progressBar = null;
    private LinearLayout progressLayout = null;

    private String receivingAddress = null;
    private DecimalFormat decimalFormat2 = null;
    private DecimalFormat decimalFormat8 = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_receive);

        initViews();

        //Incoming intent value
        double amountPayable = this.getIntent().getDoubleExtra(PaymentFragment.AMOUNT_PAYABLE, 0.0);
        decimalFormat2 = new DecimalFormat("######0.00");
        decimalFormat8 = new DecimalFormat("######0.00000000");
        tvFiatAmount.setText(getCurrencySymbol()+" "+decimalFormat2.format(amountPayable));
        tvBtcAmount.setText(decimalFormat8.format(getBtcAmountFromFiat(amountPayable, getCurrencySymbol()))+" BTC");

        //Generate new address/QR code for receive
        receivingAddress = getHDReceiveAddress();
        long lAmount = getLongAmount(amountPayable, getCurrency());
        displayQRCode(lAmount);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.tv_cancel:onBackPressed();break;
        }
    }

    private void initViews(){
        tvFiatAmount = (TextView)findViewById(R.id.tv_fiat_amount);
        tvBtcAmount = (TextView)findViewById(R.id.tv_btc_amount);
        ivReceivingQr = (ImageView)findViewById(R.id.qr);
        progressBar = (ProgressBar)findViewById(R.id.progress);
        progressLayout = (LinearLayout)findViewById(R.id.progressLayout);
        tvReceivingAddress = (TextView)findViewById(R.id.tvAddress);

        ivReceivingQr.setVisibility(View.GONE);
        progressLayout.setVisibility(View.VISIBLE);

        findViewById(R.id.tv_cancel).setOnClickListener(this);
    }

    private String getCurrency(){
        return PrefsUtil.getInstance(this).getValue(PrefsUtil.MERCHANT_KEY_CURRENCY, "USD");
    }

    private double getBtcAmountFromFiat(double fiat, String strCurrency){

        Locale locale = new Locale("en", "US");
        Locale.setDefault(locale);

        double result = 0;

        if(CurrencyExchange.getInstance(this).getCurrencyPrice(strCurrency) != 0.0) {
            result = fiat / CurrencyExchange.getInstance(this).getCurrencyPrice(strCurrency);
        }
        else {
            result = fiat / CurrencyExchange.getInstance(this).getCurrencyPrice("USD");
        }

        return result;
    }

    private void displayQRCode(long lamount) {

        BigInteger bamount = null;
        try {

            bamount = MonetaryUtil.getInstance(this).getUndenominatedAmount(lamount);
            if(bamount.compareTo(BigInteger.valueOf(2100000000000000L)) == 1)    {
                ToastCustom.makeText(this, "Invalid amount", ToastCustom.LENGTH_LONG, ToastCustom.TYPE_ERROR);
                return;
            }
            if(!bamount.equals(BigInteger.ZERO)) {
                generateQRCode(BitcoinURI.convertToBitcoinURI(receivingAddress, Coin.valueOf(bamount.longValue()), "", ""));
            }
            else {
                generateQRCode("bitcoin:" + receivingAddress);
            }
        }
        catch(NumberFormatException e) {
            generateQRCode("bitcoin:" + receivingAddress);
        }

    }

    private void generateQRCode(final String uri) {

        new AsyncTask<Void, Void, Bitmap>(){

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                //Show generating QR message
                ivReceivingQr.setVisibility(View.GONE);
                tvReceivingAddress.setText(R.string.generating_qr);
                progressLayout.setVisibility(View.VISIBLE);
            }

            @Override
            protected Bitmap doInBackground(Void... params) {

                Bitmap bitmap = null;
                int qrCodeDimension = 260;

                QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(uri, null, Contents.Type.TEXT, BarcodeFormat.QR_CODE.toString(), qrCodeDimension);

                try {
                    bitmap = qrCodeEncoder.encodeAsBitmap();
                } catch (WriterException e) {
                    e.printStackTrace();
                }

                return bitmap;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                super.onPostExecute(bitmap);
                progressLayout.setVisibility(View.GONE);
                ivReceivingQr.setVisibility(View.VISIBLE);
                tvReceivingAddress.setVisibility(View.VISIBLE);
                tvReceivingAddress.setText(receivingAddress);
                ivReceivingQr.setImageBitmap(bitmap);
            }
        }.execute();
    }

    private String getCurrencySymbol() {

        String strCurrencySymbol = "$";

        if(CurrencyExchange.getInstance(this).getCurrencySymbol(PrefsUtil.getInstance(ReceiveActivity.this).getValue(PrefsUtil.MERCHANT_KEY_CURRENCY, "USD")) != null) {
            strCurrencySymbol = CurrencyExchange.getInstance(this).getCurrencySymbol(PrefsUtil.getInstance(ReceiveActivity.this).getValue(PrefsUtil.MERCHANT_KEY_CURRENCY, "USD")).substring(0, 1);
        }

        return strCurrencySymbol;
    }

    private String getHDReceiveAddress() {

        // stub address
        String receivingAddress = "15ZtXsKk6P9UMquo9qs1oTX7okRday9pv6";

        return receivingAddress;
    }

    private long getLongAmount(double amountPayable, String strCurrency) {

        Locale locale = new Locale("en", "US");
        Locale.setDefault(locale);

        double amount = 0;

        if(CurrencyExchange.getInstance(this).getCurrencyPrice(strCurrency) != 0.0) {
            amount = amountPayable / CurrencyExchange.getInstance(this).getCurrencyPrice(strCurrency);
        }
        else {
            amount = amountPayable / CurrencyExchange.getInstance(this).getCurrencyPrice("USD");
        }

        double value = Math.round(amount * 100000000.0);
        long longValue = (Double.valueOf(value)).longValue();

        return longValue;
    }
}
