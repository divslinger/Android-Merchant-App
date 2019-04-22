package com.bitcoin.merchant.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.bitcoin.uri.BitcoinCashURI;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.android.Contents;
import com.google.zxing.client.android.encode.QRCodeEncoder;

import org.bitcoinj.core.Coin;

import java.math.BigInteger;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import info.blockchain.api.receive.Receive;
import info.blockchain.api.receive.ReceiveResponse;
import info.blockchain.merchant.api.APIFactory;
import info.blockchain.merchant.db.DBControllerV3;
import info.blockchain.merchant.service.ExpectedIncoming;
import info.blockchain.merchant.tabsswipe.PaymentFragment;
import info.blockchain.merchant.util.AppUtil;
import info.blockchain.merchant.util.MonetaryUtil;
import info.blockchain.merchant.util.PrefsUtil;
import info.blockchain.merchant.util.ToastCustom;

public class ReceiveTxActivity extends Activity implements View.OnClickListener {
    private TextView tvMerchantName = null;
    private TextView tvFiatAmount = null;
    private TextView tvBtcAmount = null;
    private ImageView ivReceivingQr = null;
    private LinearLayout progressLayout = null;
    private ImageView ivCancel = null;
    private ImageView ivCheck = null;
    private TextView tvStatus = null;
    private String receivingAddress = null;
    protected BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            //Catch incoming tx
            if (MainActivity.ACTION_INTENT_INCOMING_TX.equals(intent.getAction())) {
                soundAlert();
                final String addr = intent.getStringExtra("payment_address");
                final long paymentAmount = intent.getLongExtra("payment_amount", 0L);
                final String paymentTxHash = intent.getStringExtra("payment_tx_hash");
                // underpayment
                Long expectedAmount = ExpectedIncoming.getInstance().getBTC().get(addr);
                if (paymentAmount < expectedAmount) {
                    underpayment(addr, paymentAmount, paymentTxHash, expectedAmount);
                } else if (paymentAmount > expectedAmount) {
                    // overpayment
                    onPaymentReceived(addr, paymentAmount, paymentTxHash);
                } else {
                    // expected amount
                    onPaymentReceived(addr, -1L, paymentTxHash);
                }
            }
        }
    };

    private void underpayment(final String addr, final long paymentAmount, final String paymentTxHash, Long expectedAmount) {
        final long remainder = expectedAmount - paymentAmount;
        ToastCustom.makeText(ReceiveTxActivity.this, "Remainder:" + remainder, ToastCustom.LENGTH_LONG, ToastCustom.TYPE_ERROR);
        final double btcAmount = Double.valueOf(remainder / 1e8);
        String strCurrency = PrefsUtil.getInstance(ReceiveTxActivity.this).getValue(PrefsUtil.MERCHANT_KEY_CURRENCY, PaymentFragment.DEFAULT_CURRENCY_FIAT);
        Double currencyPrice = CurrencyExchange.getInstance(ReceiveTxActivity.this).getCurrencyPrice(strCurrency);
        NumberFormat nf = NumberFormat.getInstance(Locale.getDefault());
        double fiatAmount;
        try {
            fiatAmount = nf.parse(MonetaryUtil.getInstance().getFiatDecimalFormat().format(btcAmount * currencyPrice)).doubleValue();
        } catch (ParseException pe) {
            fiatAmount = 0.0;
        }
        final double _fiatAmount = fiatAmount;
        StringBuilder sb = new StringBuilder();
        sb.append(ReceiveTxActivity.this.getText(R.string.insufficient_payment));
        sb.append("\n");
        sb.append(ReceiveTxActivity.this.getText(R.string.re_payment_requested));
        sb.append(": ");
        sb.append(MonetaryUtil.getInstance(ReceiveTxActivity.this).getDisplayAmount(expectedAmount));
        sb.append(" " + PaymentFragment.DEFAULT_CURRENCY_BCH);
        sb.append("\n");
        sb.append(ReceiveTxActivity.this.getText(R.string.re_payment_received));
        sb.append(": ");
        sb.append(MonetaryUtil.getInstance(ReceiveTxActivity.this).getDisplayAmount(paymentAmount));
        sb.append(" " + PaymentFragment.DEFAULT_CURRENCY_BCH);
        sb.append("\n");
        sb.append(ReceiveTxActivity.this.getText(R.string.insufficient_payment_continue));
        AlertDialog.Builder builder = new AlertDialog.Builder(ReceiveTxActivity.this, R.style.AppTheme);
        builder.setTitle(R.string.app_name);
        builder.setMessage(sb.toString()).setCancelable(false);
        AlertDialog alert = builder.create();
        alert.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.prompt_ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                onPaymentReceived(addr, paymentAmount, paymentTxHash);
                Intent intent = new Intent(ReceiveTxActivity.this, ReceiveTxActivity.class);
                intent.putExtra(PaymentFragment.AMOUNT_PAYABLE_FIAT, _fiatAmount);
                intent.putExtra(PaymentFragment.AMOUNT_PAYABLE_BTC, btcAmount);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
        alert.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.prompt_ko), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                onPaymentReceived(addr, paymentAmount, paymentTxHash);
            }
        });
        alert.show();
    }

    private BigInteger bamount = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_receive);
        initViews();
        //Register receiver (Listen for incoming tx)
        IntentFilter filter = new IntentFilter(MainActivity.ACTION_INTENT_INCOMING_TX);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(receiver, filter);
        //Incoming intent value
        double amountFiat = this.getIntent().getDoubleExtra(PaymentFragment.AMOUNT_PAYABLE_FIAT, 0.0);
        double amountBtc = this.getIntent().getDoubleExtra(PaymentFragment.AMOUNT_PAYABLE_BTC, 0.0);
        tvFiatAmount.setText(getCurrencySymbol() + " " + MonetaryUtil.getInstance().getFiatDecimalFormat().format(amountFiat));
        tvBtcAmount.setText(MonetaryUtil.getInstance().getBTCDecimalFormat().format(amountBtc) + " " + PaymentFragment.DEFAULT_CURRENCY_BCH);
        getReceiveAddress(amountBtc, tvFiatAmount.getText().toString());
    }

    @Override
    protected void onDestroy() {
        //Unregister receiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_cancel:
                onBackPressed();
                break;
        }
    }

    private void initViews() {
        tvMerchantName = findViewById(R.id.tv_merchant_name);
        tvMerchantName.setText(PrefsUtil.getInstance(this).getValue(PrefsUtil.MERCHANT_KEY_MERCHANT_NAME, ""));
        tvFiatAmount = findViewById(R.id.tv_fiat_amount);
        tvBtcAmount = findViewById(R.id.tv_btc_amount);
        ivReceivingQr = findViewById(R.id.qr);
        progressLayout = findViewById(R.id.progressLayout);
        ivCancel = findViewById(R.id.iv_cancel);
        ivCheck = findViewById(R.id.iv_check);
        tvStatus = findViewById(R.id.tv_status);
        ivReceivingQr.setVisibility(View.GONE);
        ivCheck.setVisibility(View.GONE);
        progressLayout.setVisibility(View.VISIBLE);
        ivCancel.setOnClickListener(this);
    }

    private void displayQRCode(long lamount) {
        String uri = BitcoinCashURI.toCashAddress(receivingAddress);
        try {
            bamount = MonetaryUtil.getInstance(this).getUndenominatedAmount(lamount);
            if (bamount.compareTo(BigInteger.valueOf(2100000000000000L)) == 1) {
                ToastCustom.makeText(this, "Invalid amount", ToastCustom.LENGTH_LONG, ToastCustom.TYPE_ERROR);
                return;
            }
            if (!bamount.equals(BigInteger.ZERO)) {
                generateQRCode(BitcoinCashURI.toURI(receivingAddress, Coin.valueOf(bamount.longValue()), "", ""));
                write2NFC(BitcoinCashURI.toURI(receivingAddress, Coin.valueOf(bamount.longValue()), "", ""));
            } else {
                generateQRCode(uri);
                write2NFC(uri);
            }
        } catch (NumberFormatException e) {
            generateQRCode(uri);
            write2NFC(uri);
        }
    }

    private void generateQRCode(final String uri) {
        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                //Show generating QR message
                ivReceivingQr.setVisibility(View.GONE);
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
                ivReceivingQr.setImageBitmap(bitmap);
            }
        }.execute();
    }

    private String getCurrencySymbol() {
        String strCurrencySymbol = "$";
        String value = PrefsUtil.getInstance(ReceiveTxActivity.this).getValue(PrefsUtil.MERCHANT_KEY_CURRENCY, PaymentFragment.DEFAULT_CURRENCY_FIAT);
        String currencySymbol = CurrencyExchange.getInstance(this).getCurrencySymbol(value);
        if (currencySymbol != null) {
            strCurrencySymbol = currencySymbol.substring(0, 1);
        }
        return strCurrencySymbol;
    }

    private void getReceiveAddress(final double amountBtc, final String strFiat) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressLayout.setVisibility(View.VISIBLE);
            }

            @Override
            protected String doInBackground(Void... params) {
                //Generate new address/QR code for receive
                String address = PrefsUtil.getInstance(ReceiveTxActivity.this).getValue(PrefsUtil.MERCHANT_KEY_MERCHANT_RECEIVER, "");
                if (AppUtil.getInstance(ReceiveTxActivity.this).isV2API()) {
                    try {
                        ReceiveResponse response = new Receive(APIFactory.getInstance().getAPIKey()).receive(address, APIFactory.getInstance().getCallback());
                        receivingAddress = response.getReceivingAddress();
                    } catch (Exception e) {
                        receivingAddress = null;
                        e.getMessage();
                        e.printStackTrace();
                    }
                } else {
                    receivingAddress = address;
                }
                if (receivingAddress == null) {
                    ToastCustom.makeText(ReceiveTxActivity.this, getText(R.string.unable_to_generate_address), ToastCustom.LENGTH_LONG, ToastCustom.TYPE_ERROR);
                    return null;
                }
                //Subscribe to websocket to new address
                Intent intent = new Intent(MainActivity.ACTION_INTENT_SUBSCRIBE_TO_ADDRESS);
                intent.putExtra("address", receivingAddress);
                LocalBroadcastManager.getInstance(ReceiveTxActivity.this).sendBroadcast(intent);
                long lAmount = getLongAmount(amountBtc);
                ExpectedIncoming.getInstance().getBTC().put(receivingAddress, lAmount);
                ExpectedIncoming.getInstance().getFiat().put(receivingAddress, strFiat);
                displayQRCode(lAmount);
                return receivingAddress;
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                progressLayout.setVisibility(View.GONE);
            }
        }.execute();
    }

    private long getLongAmount(double amountPayable) {
        double value = Math.round(amountPayable * 100000000.0);
        long longValue = (Double.valueOf(value)).longValue();
        return longValue;
    }

    public void soundAlert() {
        AudioManager audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null && audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
            MediaPlayer mp;
            mp = MediaPlayer.create(this, R.raw.alert);
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.reset();
                    mp.release();
                }
            });
            mp.start();
        }
    }

    private void onPaymentReceived(String addr, long bchPaymentAmount, String paymentTxHash) {
        ivCancel.setVisibility(View.GONE);
        ivReceivingQr.setVisibility(View.GONE);
        ivCheck.setVisibility(View.VISIBLE);
        ivCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                ReceiveTxActivity.this.setResult(RESULT_OK, intent);
                ReceiveTxActivity.this.finish();
            }
        });
        tvStatus.setText(getResources().getText(R.string.payment_received));
        tvStatus.setTextColor(getResources().getColor(R.color.blockchain_receive_green));
        tvBtcAmount.setVisibility(View.GONE);
        tvFiatAmount.setVisibility(View.GONE);
        Long bchExpectedAmount = ExpectedIncoming.getInstance().getBTC().get(addr);
        long bchAmount = (bchPaymentAmount == -1L) ? bchExpectedAmount : bchPaymentAmount;
        if (bchAmount != bchExpectedAmount) {
            bchAmount *= -1L;
        }
        String strCurrency = PrefsUtil.getInstance(ReceiveTxActivity.this).getValue(PrefsUtil.MERCHANT_KEY_CURRENCY, PaymentFragment.DEFAULT_CURRENCY_FIAT);
        Double currencyPrice = CurrencyExchange.getInstance(ReceiveTxActivity.this).getCurrencyPrice(strCurrency);
        double amountPayableFiat = (Math.abs((double) bchAmount) / 1e8) * currencyPrice;
        String fiatAmount = (bchPaymentAmount == -1L) ? ExpectedIncoming.getInstance().getFiat().get(addr) : getCurrencySymbol() + " " + MonetaryUtil.getInstance().getFiatDecimalFormat().format(amountPayableFiat);
        DBControllerV3 pdb = new DBControllerV3(ReceiveTxActivity.this);
        pdb.insertPayment(
                System.currentTimeMillis() / 1000,
                receivingAddress,
                bchAmount,
                fiatAmount,
                -1, // confirmations
                "", // note, message
                paymentTxHash
        );
        pdb.close();
        if (bchPaymentAmount > bchExpectedAmount) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ReceiveTxActivity.this);
            TextView title = new TextView(ReceiveTxActivity.this);
            title.setPadding(20, 60, 20, 20);
            title.setText(R.string.app_name);
            title.setGravity(Gravity.CENTER);
            title.setTextSize(20);
            builder.setCustomTitle(title);
            builder.setMessage(R.string.overpaid_amount).setCancelable(false);
            AlertDialog alert = builder.create();
            alert.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.prompt_ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
            alert.show();
        }
        setResult(RESULT_OK);
    }

    private void write2NFC(final String uri) {
        NfcAdapter nfc = NfcAdapter.getDefaultAdapter(ReceiveTxActivity.this);
        if (nfc != null && nfc.isNdefPushEnabled()) {
            nfc.setNdefPushMessageCallback(new NfcAdapter.CreateNdefMessageCallback() {
                @Override
                public NdefMessage createNdefMessage(NfcEvent event) {
                    NdefRecord uriRecord = NdefRecord.createUri(uri);
                    return new NdefMessage(new NdefRecord[]{uriRecord});
                }
            }, ReceiveTxActivity.this);
        }
    }
}
