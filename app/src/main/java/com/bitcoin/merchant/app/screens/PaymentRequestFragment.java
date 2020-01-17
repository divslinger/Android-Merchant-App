package com.bitcoin.merchant.app.screens;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bitcoin.merchant.app.R;
import com.bitcoin.merchant.app.screens.features.ToolbarAwareFragment;
import com.bitcoin.merchant.app.util.AmountUtil;
import com.bitcoin.merchant.app.util.AppUtil;
import com.bitcoin.merchant.app.util.DialogUtil;
import com.bitcoin.merchant.app.util.PaymentTarget;
import com.bitcoin.merchant.app.util.ToastCustom;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.android.Contents;
import com.google.zxing.client.android.encode.QRCodeEncoder;

import org.bitcoindotcom.bchprocessor.Action;
import org.bitcoindotcom.bchprocessor.bip70.Bip70Manager;
import org.bitcoindotcom.bchprocessor.bip70.Bip70PayService;
import org.bitcoindotcom.bchprocessor.bip70.model.InvoiceRequest;
import org.bitcoindotcom.bchprocessor.bip70.model.InvoiceStatus;

import java.net.SocketTimeoutException;

import retrofit2.Response;

import static com.bitcoin.merchant.app.MainActivity.TAG;

public class PaymentRequestFragment extends ToolbarAwareFragment {
    private LinearLayout waitingLayout;
    private LinearLayout receivedLayout;
    private TextView tvFiatAmount;
    private TextView tvBtcAmount;
    private ImageView ivReceivingQr;
    private LinearLayout progressLayout;
    private Button ivCancel;
    private Button ivDone;
    private String lastProcessedInvoicePaymentId;
    private String qrCodeUri;
    private Bip70Manager bip70Manager;
    private Bip70PayService bip70PayService;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            if (Action.INVOICE_PAYMENT_ACKNOWLEDGED.equals(intent.getAction())) {
                acknowledgePayment(InvoiceStatus.fromJson(intent.getStringExtra(Action.PARAM_INVOICE_STATUS)));
            }
            if (Action.INVOICE_PAYMENT_EXPIRED.equals(intent.getAction())) {
                expirePayment(InvoiceStatus.fromJson(intent.getStringExtra(Action.PARAM_INVOICE_STATUS)));
            }
            if (Action.NETWORK_RECONNECT.equals(intent.getAction())) {
                reconnectIfNecessary();
            }
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                if (intent.getExtras() != null) {
                    reconnectIfNecessary();
                }
            }
        }

        private void reconnectIfNecessary() {
            getBip70Manager().reconnectIfNecessary();
        }
    };
    private String fiatFormatted;

    private void expirePayment(InvoiceStatus invoiceStatus) {
        if (markInvoiceAsProcessed(invoiceStatus)) {
            return;
        }
        cancelPayment();
    }

    private void acknowledgePayment(InvoiceStatus i) {
        if (markInvoiceAsProcessed(i)) {
            return;
        }
        Log.i(TAG, "record new Tx:" + i);
        getApp().getPaymentProcessor().recordInDatabase(i, fiatFormatted);
        showCheckMark();
        soundAlert();
    }

    /**
     * @return true if it was already processed, false otherwise
     */
    private boolean markInvoiceAsProcessed(InvoiceStatus invoiceStatus) {
        // Check that it has not yet been processed to avoid redundant processing
        String paymentId = invoiceStatus.getPaymentId();
        if (paymentId != null && paymentId.equals(lastProcessedInvoicePaymentId)) {
            Log.i(TAG, "Already processed invoice:" + invoiceStatus);
            return true;
        }
        lastProcessedInvoicePaymentId = paymentId;
        return false;
    }

    public Bip70Manager getBip70Manager() {
        return bip70Manager;
    }

    private void soundAlert() {
        AudioManager audioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null && audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
            MediaPlayer mp = MediaPlayer.create(activity, R.raw.alert);
            mp.setOnCompletionListener(player -> {
                player.reset();
                player.release();
            });
            mp.start();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_request_payment, container, false);
        initViews(v);
        setToolbarVisible(false);
        registerReceiver();
        bip70PayService = Bip70PayService.Companion.create(getResources().getString(R.string.bip70_bitcoin_com_host));
        bip70Manager = new Bip70Manager(getApp());
        Bundle args = getArguments();
        double amountFiat = args.getDouble(PaymentInputFragment.AMOUNT_PAYABLE_FIAT, 0.0);
        AmountUtil f = new AmountUtil(activity);
        fiatFormatted = f.formatFiat(amountFiat);
        tvFiatAmount.setText(fiatFormatted);
        InvoiceRequest InvoiceRequest = createInvoice(amountFiat, AppUtil.getCurrency(activity));
        if (InvoiceRequest == null) {
            ToastCustom.makeText(activity, getText(R.string.unable_to_generate_address), ToastCustom.LENGTH_LONG, ToastCustom.TYPE_ERROR);
            cancelPayment();
        } else {
            generateInvoiceAndWaitForPayment(InvoiceRequest);
        }
        return v;
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(Action.INVOICE_PAYMENT_ACKNOWLEDGED);
        filter.addAction(Action.INVOICE_PAYMENT_EXPIRED);
        filter.addAction(Action.NETWORK_RECONNECT);
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(activity.getApplicationContext());
        broadcastManager.registerReceiver(receiver, filter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (bip70Manager != null) {
            bip70Manager.stopSocket();
        }
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(receiver);
    }

    private void initViews(View v) {
        tvFiatAmount = v.findViewById(R.id.tv_fiat_amount);
        tvBtcAmount = v.findViewById(R.id.tv_btc_amount);
        ivReceivingQr = v.findViewById(R.id.qr);
        progressLayout = v.findViewById(R.id.progressLayout);
        waitingLayout = v.findViewById(R.id.layout_waiting);
        receivedLayout = v.findViewById(R.id.layout_complete);
        ivCancel = v.findViewById(R.id.iv_cancel);
        ivDone = v.findViewById(R.id.iv_done);
        showGeneratingQrCodeProgress(true);
        ivCancel.setOnClickListener(view -> cancelPayment());
        ivReceivingQr.setOnClickListener(view -> copyQrCodeToClipboard());
        waitingLayout.setVisibility(View.VISIBLE);
        receivedLayout.setVisibility(View.GONE);
    }

    private void showGeneratingQrCodeProgress(boolean enabled) {
        this.progressLayout.setVisibility(enabled ? View.VISIBLE : View.GONE);
        this.ivReceivingQr.setVisibility(enabled ? View.GONE : View.VISIBLE);
    }

    private void cancelPayment() {
        Log.d(TAG, "Canceling payment...");
        activity.onBackPressed();
    }

    private void copyQrCodeToClipboard() {
        try {
            ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null) {
                ClipData clip = ClipData.newPlainText(qrCodeUri, qrCodeUri);
                clipboard.setPrimaryClip(clip);
                Log.i(TAG, "Copied to clipboard: " + qrCodeUri);
            }
        } catch (Exception e) {
            Log.i(TAG, "Failed to copy to clipboard: " + qrCodeUri);
        }
    }

    private InvoiceRequest createInvoice(double amountFiat, String currency) {
        PaymentTarget paymentTarget = AppUtil.getPaymentTarget(activity);
        InvoiceRequest i = new InvoiceRequest("" + amountFiat, currency);
        switch (paymentTarget.getType()) {
            case INVALID:
                return null;
            case API_KEY:
                i.setApiKey(paymentTarget.getTarget());
                break;
            case ADDRESS:
                i.setAddress(paymentTarget.getLegacyAddress());
                break;
            case XPUB:
                try {
                    i.setAddress(getApp().getWallet().generateAddressFromXPub());
                    Log.i(TAG, "BCH-address(xPub) to receive: " + i.getAddress());
                } catch (Exception e) {
                    Log.e(TAG, "", e);
                    return null;
                }
                break;
        }
        return i;
    }

    @SuppressLint("StaticFieldLeak")
    private void generateInvoiceAndWaitForPayment(final InvoiceRequest invoiceRequest) {
        new AsyncTask<InvoiceRequest, Void, Pair<InvoiceStatus, Bitmap>>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                showGeneratingQrCodeProgress(true);
            }

            @Override
            protected Pair<InvoiceStatus, Bitmap> doInBackground(InvoiceRequest... bip70InvoiceRequests) {
                InvoiceRequest request = bip70InvoiceRequests[0];
                InvoiceStatus invoice = null;
                Bitmap bitmap = null;
                try {
                    Response<InvoiceStatus> response = bip70PayService.createInvoice(request).execute();
                    invoice = response.body();
                    if (invoice == null) {
                        throw new Exception("HTTP status:" + response.code() + " message:" + response.message());
                    }
                    qrCodeUri = invoice.getWalletUri();
                    // TODO display icon showing if we are connected or not
                    // connect the socket first before showing the bitmap
                    getBip70Manager().startWebsockets(invoice.getPaymentId());
                    bitmap = generateQrCode(qrCodeUri);
                } catch (Exception e) {
                    if (!(e instanceof SocketTimeoutException)) {
                        Log.e(TAG, "", e);
                    }
                    String title = "Error during invoice creation";
                    DialogUtil.show(activity, title, e.getMessage(),
                            () -> cancelPayment());
                }
                return new Pair(invoice, bitmap);
            }

            private Bitmap generateQrCode(String url) throws Exception {
                int qrCodeDimension = 260;
                Log.d(TAG, "paymentUrl:" + url);
                QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(url, null, Contents.Type.TEXT, BarcodeFormat.QR_CODE.toString(), qrCodeDimension);
                return qrCodeEncoder.encodeAsBitmap();
            }

            @Override
            protected void onPostExecute(Pair<InvoiceStatus, Bitmap> pair) {
                super.onPostExecute(pair);
                showGeneratingQrCodeProgress(false);
                InvoiceStatus i = pair.first;
                Bitmap bitmap = pair.second;
                if (i != null && bitmap != null) {
                    AmountUtil f = new AmountUtil(activity);
                    tvBtcAmount.setText(f.formatBch(i.getTotalBchAmount()));
                    ivReceivingQr.setImageBitmap(bitmap);
                }
            }
        }.execute(invoiceRequest);
    }

    private void showCheckMark() {
        waitingLayout.setVisibility(View.GONE);
        receivedLayout.setVisibility(View.VISIBLE);
        AppUtil.setStatusBarColor(activity, R.color.bitcoindotcom_green);
        ivDone.setOnClickListener(v -> {
            AppUtil.setStatusBarColor(activity, R.color.gray);
            activity.onBackPressed();
        });
    }

    public boolean isBackAllowed() {
        LocalBroadcastManager.getInstance(activity).sendBroadcast(new Intent(PaymentInputFragment.ACTION_INTENT_RESET_AMOUNT));
        return true;
    }
}
