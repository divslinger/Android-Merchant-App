package com.bitcoin.merchant.app.network;

import android.content.ContentValues;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bitcoin.merchant.app.MainActivity;
import com.bitcoin.merchant.app.application.CashRegisterApplication;
import com.bitcoin.merchant.app.model.PaymentReceived;
import com.bitcoin.merchant.app.database.PaymentRecord;
import com.bitcoin.merchant.app.model.rest.Utxo;
import com.bitcoin.merchant.app.model.rest.Utxos;
import com.bitcoin.merchant.app.util.AppUtil;
import com.bitcoin.merchant.app.util.PrefsUtil;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public class QueryUtxoTask extends DownloadTask<Utxos> {
    public static final String TAG = "QueryUtxoTask";
    private final CashRegisterApplication app;
    private final QueryUtxoType query;
    private final QueryUtxoType nextQuery;
    private final boolean memPool;
    private final boolean checkExpectedPayment;
    private final List<Utxo> utxosWithoutTimeButWithBlockHeight = new ArrayList<>();
    private final List<Utxo> utxosWithUpdatedConfirmations = new ArrayList<>();

    public QueryUtxoTask(CashRegisterApplication app, QueryUtxoType... queries) {
        super(app);
        this.app = app;
        query = queries[0];
        memPool = query == QueryUtxoType.UNCONFIRMED;
        checkExpectedPayment = memPool;
        nextQuery = queries.length > 1 ? queries[1] : null;
    }

    public static PaymentReceived createPayment(Utxo utxo, boolean checkExpectedPayment, String address) {
        ExpectedAmounts expected = checkExpectedPayment
                ? ExpectedPayments.getInstance().getExpectedAmounts(address)
                : ExpectedAmounts.UNDEFINED;
        // use current time for expected payments
        long ts = checkExpectedPayment ? System.currentTimeMillis() / 1000 : utxo.ts;
        return new PaymentReceived(address, utxo.satoshis, utxo.txid, ts, (int) utxo.confirmations, expected);
    }

    @Override
    protected Class<Utxos> getReturnClass() {
        return Utxos.class;
    }

    @Override
    protected String getUrl() {
        if (!AppUtil.hasValidReceiver(app) || AppUtil.isValidXPub(app)) {
            return null;
        }
        String address = PrefsUtil.getInstance(app).getValue(PrefsUtil.MERCHANT_KEY_MERCHANT_RECEIVER, "");
        String url = memPool
                ? "https://rest.bitcoin.com/v2/address/unconfirmed/"
                : "https://rest.bitcoin.com/v2/address/utxo/";
        return url + address;
    }

    private void removeKnownTxAndFindTxWithMissingTime(Utxos utxos) {
        List<Utxo> newUtxos = new ArrayList<>();
        for (Utxo utxo : utxos.utxos) {
            try {
                ContentValues values = app.getDb().getPaymentFromTx(utxo.txid);
                if (values == null) {
                    newUtxos.add(utxo);
                }
                // when they are in the memPool, we assume that time is now
                if (!memPool) {
                    // are we missing the time & do we have the height to query it ?
                    if ((utxo.ts == 0) && (utxo.height > 0)) {
                        if (values != null) {
                            PaymentRecord record = new PaymentRecord(values);
                            if (record.timeInSec == 0) {
                                // known TX but we miss the correct time
                                utxosWithoutTimeButWithBlockHeight.add(utxo);
                            }
                        } else {
                            // unknown TX but we miss the correct time
                            utxosWithoutTimeButWithBlockHeight.add(utxo);
                        }
                    }
                    if (values != null) {
                        int confirmations = new PaymentRecord(values).confirmations;
                        if (confirmations < 2 && confirmations < utxo.confirmations) {
                            utxosWithUpdatedConfirmations.add(utxo);
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "", e);
            }
        }
        utxos.utxos = newUtxos.toArray(new Utxo[0]);
    }

    @Override
    protected void onDownloaded(Utxos result) {
        if ((result != null) && (result.utxos != null)) {
            Log.i("QueryUtxoTask", result.utxos.length + " TX founds");
            removeKnownTxAndFindTxWithMissingTime(result);
            for (Utxo utxo : result.utxos) {
                recordPayment(createPayment(utxo, checkExpectedPayment, result.legacyAddress));
            }
            for (Utxo utxo : utxosWithUpdatedConfirmations) {
                updatePayment(createPayment(utxo, checkExpectedPayment, result.legacyAddress));
            }
        }
        if (nextQuery != null) {
            new QueryUtxoTask(app, nextQuery).execute();
        }
        if ((utxosWithoutTimeButWithBlockHeight.size() > 0) && (result != null)) {
            new QueryTxTimeTask(app, query, checkExpectedPayment, result.legacyAddress, utxosWithoutTimeButWithBlockHeight).execute();
        } else {
            if (query == QueryUtxoType.ALL) {
                LocalBroadcastManager.getInstance(app).sendBroadcast(new Intent(MainActivity.ACTION_QUERY_ALL_UXTO_FINISHED));
            }
        }
    }

    private void recordPayment(PaymentReceived payment) {
        Intent intent = new Intent(MainActivity.ACTION_INTENT_RECORD_TX);
        payment.toIntent(intent);
        LocalBroadcastManager.getInstance(app).sendBroadcast(intent);
    }

    private void updatePayment(PaymentReceived payment) {
        Intent intent = new Intent(MainActivity.ACTION_INTENT_UPDATE_TX);
        payment.toIntent(intent);
        LocalBroadcastManager.getInstance(app).sendBroadcast(intent);
    }
}
