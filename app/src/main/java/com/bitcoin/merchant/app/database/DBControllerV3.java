package com.bitcoin.merchant.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.bitcoin.merchant.app.util.OSUtil;
import com.bitcoin.merchant.app.util.PrefsUtil;
import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import info.blockchain.wallet.crypto.AESUtil;
import info.blockchain.wallet.util.CharSequenceX;

public class DBControllerV3 extends SQLiteOpenHelper {
    private static final String TAG = "DBControllerV3";
    private static final String DB = "paymentsV3.db";
    private static final String TABLE = "payment";
    private static final Boolean FAKE_TX_USED = false;
    private final CharSequenceX pw;

    public DBControllerV3(Context context) {
        super(context, DB, null, 1);
        this.pw = new CharSequenceX(PrefsUtil.MERCHANT_KEY_PIN + OSUtil.getInstance(context).getFootprint());
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        String query;
        query = "CREATE TABLE " + TABLE + " ( " +
                "_id INTEGER PRIMARY KEY, " +
                "ts integer, " +
                "iad text, " +
                "amt text, " +
                "famt text, " +
                "cfm text, " +
                "msg text," +
                "tx text" +
                ")";
        database.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int version_old, int current_version) {
        String query;
        query = "DROP TABLE IF EXISTS " + TABLE;
        database.execSQL(query);
        onCreate(database);
    }

    public ContentValues insertPayment(long ts, String address, long amount, String fiat_amount, int confirmed, String message, String tx)
            throws Exception {
        SQLiteDatabase database = null;
        try {
            tx = tx != null ? tx : "";
            database = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("ts", ts);
            values.put("iad", address);
            values.put("amt", Long.toString(amount));
            values.put("famt", fiat_amount);
            values.put("cfm", Integer.toString(confirmed));
            values.put("msg", message);
            values.put("tx", tx);
            database.insert(TABLE, null, values);
        } finally {
            closeAll(database, null);
        }
        ContentValues vals = new ContentValues();
        vals.put("ts", ts);
        vals.put("iad", address);
        vals.put("amt", Long.toString(amount));
        vals.put("famt", fiat_amount);
        vals.put("cfm", Integer.toString(confirmed));
        vals.put("msg", message);
        vals.put("tx", tx);
        return vals;
    }

    public ArrayList<ContentValues> getAllPayments()
            throws Exception {
        ArrayList<ContentValues> data = new ArrayList<>();
        SQLiteDatabase database = null;
        Cursor c = null;
        try {
            String selectQuery = "SELECT * FROM payment ORDER BY ts DESC";
            database = this.getReadableDatabase();
            c = database.rawQuery(selectQuery, null);
            if (c.moveToFirst()) {
                do {
                    ContentValues vals = new ContentValues();
                    vals.put("_id", c.getString(0));
                    vals.put("ts", c.getLong(1));
                    vals.put("iad", c.getString(2));
                    vals.put("amt", c.getString(3));
                    vals.put("famt", c.getString(4));
                    vals.put("cfm", c.getString(5));
                    vals.put("msg", c.getString(6));
                    vals.put("tx", c.getString(7));
                    data.add(vals);
                } while (c.moveToNext());
            }
        } finally {
            closeAll(database, c);
        }
        // decrypt and overwrite decrypted
        formatDecryptAndResave(data);
        if (FAKE_TX_USED) {
            for (int i = 0; i < 10; i++) {
                ContentValues vals = new ContentValues();
                vals.put("_id", "" + i);
                vals.put("ts", new Date().getTime() + "");
                vals.put("iad", "1MxRuANd5CmHWcveTwQaAJ36sStEQ5QM5k");
                vals.put("amt", 123456789L);
                vals.put("famt", "$3.55");
                vals.put("cfm", "1");
                vals.put("msg", "N/A");
                vals.put("tx", "-");
                data.add(vals);
            }
        }
        return data;
    }

    private void formatDecryptAndResave(ArrayList<ContentValues> data) {
        SQLiteDatabase database = null;
        try {
            for (int i = 0; i < data.size(); i++) {
                ContentValues vals = data.get(i);
                try {
                    formatValues(vals);
                } catch (Exception e) {
                    // decrypt
                    vals.put("iad", AESUtil.decrypt(vals.getAsString("iad"), pw, AESUtil.PinPbkdf2Iterations));
                    vals.put("amt", AESUtil.decrypt(vals.getAsString("amt"), pw, AESUtil.PinPbkdf2Iterations));
                    vals.put("famt", AESUtil.decrypt(vals.getAsString("famt"), pw, AESUtil.PinPbkdf2Iterations));
                    vals.put("cfm", AESUtil.decrypt(vals.getAsString("cfm"), pw, AESUtil.PinPbkdf2Iterations));
                    vals.put("msg", AESUtil.decrypt(vals.getAsString("msg"), pw, AESUtil.PinPbkdf2Iterations));
                    vals.put("tx", AESUtil.decrypt(vals.getAsString("tx"), pw, AESUtil.PinPbkdf2Iterations));
                    Log.i(TAG, "decrypted record:" + vals.get("_id"));
                    // resave
                    if (database == null) {
                        database = this.getWritableDatabase();
                    }
                    int result = database.update(TABLE, vals, "_id=" + vals.get("_id"), null);
                    Log.i(TAG, "resaved record:" + vals.get("_id") + ", update:" + result);
                    // format
                    formatValues(vals);
                }
            }
        } finally {
            closeAll(database, null);
        }
    }

    private void formatValues(ContentValues vals) {
        long amt = Long.parseLong(vals.getAsString("amt"));
        int cfm = Integer.parseInt(vals.getAsString("cfm"));
        vals.put("amt", amt);
        vals.put("cfm", cfm);
    }

    public Set<String> getAllAddresses()
            throws Exception {
        Set<String> data = new HashSet<>();
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            String selectQuery = "SELECT iad FROM payment";
            database = this.getReadableDatabase();
            cursor = database.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {
                do {
                    String decrypt = AESUtil.decrypt(cursor.getString(0), pw, AESUtil.PinPbkdf2Iterations);
                    data.add(decrypt);
                } while (cursor.moveToNext());
            }
        } finally {
            closeAll(database, cursor);
        }
        return data;
    }

    private void closeAll(SQLiteDatabase database, Cursor cursor) {
        try {
            if (cursor != null) {
                cursor.close();
            }
            if (database != null) {
                database.close();
            }
            close();
        } catch (Exception e) {
            Log.e(TAG, "closeAll", e);
            Crashlytics.logException(e);
        }
    }
}
