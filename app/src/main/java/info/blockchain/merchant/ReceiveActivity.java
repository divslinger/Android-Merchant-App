package info.blockchain.merchant;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
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
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
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

import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.bip44.Account;
import org.bitcoinj.core.bip44.Address;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.uri.BitcoinURI;

import java.math.BigInteger;
import java.text.DecimalFormat;

import info.blockchain.merchant.api.APIFactory;
import info.blockchain.merchant.db.DBController;
import info.blockchain.merchant.tabsswipe.PaymentFragment;
import info.blockchain.merchant.util.AppUtil;
import info.blockchain.merchant.util.MonetaryUtil;
import info.blockchain.merchant.util.PrefsUtil;
import info.blockchain.merchant.util.ToastCustom;

public class ReceiveActivity extends Activity implements View.OnClickListener{

    private TextView tvMerchantName = null;
    private TextView tvFiatAmount = null;
    private TextView tvBtcAmount = null;
    private ImageView ivReceivingQr = null;
    private ProgressBar progressBar = null;
    private LinearLayout progressLayout = null;
    private ImageView ivCancel = null;
    private ImageView ivCheck = null;
    private TextView tvStatus = null;

    private String receivingAddress = null;

    private static final int ADDRESS_LOOKAHEAD = 20;

    private DecimalFormat dfBtc = new DecimalFormat("######0.0######");
    private DecimalFormat dfFiat = new DecimalFormat("######0.00");
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
        tvFiatAmount.setText(getCurrencySymbol()+" "+ dfFiat.format(amountFiat));
        tvBtcAmount.setText(dfBtc.format(amountBtc) + " " + PaymentFragment.DEFAULT_CURRENCY_BTC);

        //Generate new address/QR code for receive
        if(AppUtil.getInstance(ReceiveActivity.this).isV2API())    {
            receivingAddress = getV2ReceiveAddress();
        }
        else    {
            receivingAddress = PrefsUtil.getInstance(ReceiveActivity.this).getValue(PrefsUtil.MERCHANT_KEY_MERCHANT_RECEIVER, "");
        }

        if(receivingAddress == null)    {
            ToastCustom.makeText(ReceiveActivity.this, getText(R.string.unable_to_generate_address), ToastCustom.LENGTH_LONG, ToastCustom.TYPE_ERROR);
            finish();
        }
        else    {
            ToastCustom.makeText(ReceiveActivity.this, receivingAddress, ToastCustom.LENGTH_LONG, ToastCustom.TYPE_OK);
        }

        //Subscribe to websocket to new address
        Intent intent = new Intent(MainActivity.ACTION_INTENT_SUBSCRIBE_TO_ADDRESS);
        intent.putExtra("address",receivingAddress);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        long lAmount = getLongAmount(amountBtc);
        displayQRCode(lAmount);
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
            case R.id.iv_cancel:onBackPressed();break;
        }
    }

    private void initViews(){
        tvMerchantName = (TextView)findViewById(R.id.tv_merchant_name);
        tvMerchantName.setText(PrefsUtil.getInstance(this).getValue(PrefsUtil.MERCHANT_KEY_MERCHANT_NAME, ""));
        tvFiatAmount = (TextView)findViewById(R.id.tv_fiat_amount);
        tvBtcAmount = (TextView)findViewById(R.id.tv_btc_amount);
        ivReceivingQr = (ImageView)findViewById(R.id.qr);
        progressBar = (ProgressBar)findViewById(R.id.progress);
        progressLayout = (LinearLayout)findViewById(R.id.progressLayout);
        ivCancel = (ImageView)findViewById(R.id.iv_cancel);
        ivCheck = (ImageView)findViewById(R.id.iv_check);
        tvStatus = (TextView)findViewById(R.id.tv_status);

        ivReceivingQr.setVisibility(View.GONE);
        ivCheck.setVisibility(View.GONE);
        progressLayout.setVisibility(View.VISIBLE);

        ivCancel.setOnClickListener(this);
    }

    private void displayQRCode(long lamount) {

        try {

            bamount = MonetaryUtil.getInstance(this).getUndenominatedAmount(lamount);
            if(bamount.compareTo(BigInteger.valueOf(2100000000000000L)) == 1)    {
                ToastCustom.makeText(this, "Invalid amount", ToastCustom.LENGTH_LONG, ToastCustom.TYPE_ERROR);
                return;
            }
            if(!bamount.equals(BigInteger.ZERO)) {
                generateQRCode(BitcoinURI.convertToBitcoinURI(receivingAddress, Coin.valueOf(bamount.longValue()), "", ""));
                write2NFC(BitcoinURI.convertToBitcoinURI(receivingAddress, Coin.valueOf(bamount.longValue()), "", ""));
            }
            else {
                generateQRCode("bitcoin:" + receivingAddress);
                write2NFC("bitcoin:" + receivingAddress);
            }
        }
        catch(NumberFormatException e) {
            generateQRCode("bitcoin:" + receivingAddress);
            write2NFC("bitcoin:" + receivingAddress);
        }
    }

    private void generateQRCode(final String uri) {

        new AsyncTask<Void, Void, Bitmap>(){

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

        if(CurrencyExchange.getInstance(this).getCurrencySymbol(PrefsUtil.getInstance(ReceiveActivity.this).getValue(PrefsUtil.MERCHANT_KEY_CURRENCY, PaymentFragment.DEFAULT_CURRENCY_FIAT)) != null) {
            strCurrencySymbol = CurrencyExchange.getInstance(this).getCurrencySymbol(PrefsUtil.getInstance(ReceiveActivity.this).getValue(PrefsUtil.MERCHANT_KEY_CURRENCY, PaymentFragment.DEFAULT_CURRENCY_FIAT)).substring(0, 1);
        }

        return strCurrencySymbol;
    }

    private String getV2ReceiveAddress() {

        String receivingAddress = null;

        try {
            Account account = new Account(MainNetParams.get(), PrefsUtil.getInstance(ReceiveActivity.this).getValue(PrefsUtil.MERCHANT_KEY_MERCHANT_RECEIVER, ""), 0);

            int idx = PrefsUtil.getInstance(ReceiveActivity.this).getValue(PrefsUtil.MERCHANT_KEY_ACCOUNT_INDEX, APIFactory.getInstance(ReceiveActivity.this).getAccountIndex());
            if(idx - APIFactory.getInstance(ReceiveActivity.this).getAccountIndex() >= ADDRESS_LOOKAHEAD)    {
                idx = APIFactory.getInstance(ReceiveActivity.this).getAccountIndex() + (ADDRESS_LOOKAHEAD - 1);
            }
            Address addr = account.getReceive().getAddressAt(idx);
            receivingAddress = addr.getAddressString();
            PrefsUtil.getInstance(ReceiveActivity.this).setValue(PrefsUtil.MERCHANT_KEY_ACCOUNT_INDEX, idx + 1);
        }
        catch(AddressFormatException afe) {
            return null;
        }

        return receivingAddress;
    }

    private long getLongAmount(double amountPayable) {

        double value = Math.round(amountPayable * 100000000.0);
        long longValue = (Double.valueOf(value)).longValue();

        return longValue;
    }

    protected BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {

        //Catch incoming tx
        if (MainActivity.ACTION_INTENT_INCOMING_TX.equals(intent.getAction())) {
            soundAlert();
            onPaymentReceived();
        }
        }
    };

    public void soundAlert(){
        AudioManager audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager!=null && audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
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

    private void onPaymentReceived(){

        ivCancel.setVisibility(View.GONE);
        ivReceivingQr.setVisibility(View.GONE);
        ivCheck.setVisibility(View.VISIBLE);
        ivCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        tvStatus.setText(getResources().getText(R.string.payment_received));
        tvStatus.setTextColor(getResources().getColor(R.color.blockchain_receive_green));
        tvBtcAmount.setVisibility(View.GONE);
        tvFiatAmount.setVisibility(View.GONE);

        DBController pdb = new DBController(ReceiveActivity.this);
        pdb.insertPayment(
                System.currentTimeMillis() / 1000,          // timestamp, Unix time
                receivingAddress,                           // receiving address
                bamount.longValue(),                        // BTC amount
                tvFiatAmount.getText().toString(),          // fiat amount
                -1,                                         // confirmations
                ""                                          // note, message
        );
        pdb.close();

        setResult(RESULT_OK);
    }

    private void write2NFC(final String uri) {

        if (Build.VERSION.SDK_INT < 16){
            return;
        }

        NfcAdapter nfc = NfcAdapter.getDefaultAdapter(ReceiveActivity.this);
        if (nfc != null && nfc.isNdefPushEnabled() ) {
            nfc.setNdefPushMessageCallback(new NfcAdapter.CreateNdefMessageCallback() {
                @Override
                public NdefMessage createNdefMessage(NfcEvent event) {
                    NdefRecord uriRecord = NdefRecord.createUri(uri);
                    return new NdefMessage(new NdefRecord[]{ uriRecord });
                }
            }, ReceiveActivity.this);
        }

    }

}
