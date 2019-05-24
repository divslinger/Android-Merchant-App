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

    public void insertPayment(long ts, String address, long amount, String fiat_amount, int confirmed, String message, String tx)
            throws Exception {
        SQLiteDatabase database = null;
        try {
            database = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("ts", ts);
            values.put("iad", AESUtil.encrypt(address, pw, AESUtil.PinPbkdf2Iterations));
            values.put("amt", AESUtil.encrypt(Long.toString(amount), pw, AESUtil.PinPbkdf2Iterations));
            values.put("famt", AESUtil.encrypt(fiat_amount, pw, AESUtil.PinPbkdf2Iterations));
            values.put("cfm", AESUtil.encrypt(Integer.toString(confirmed), pw, AESUtil.PinPbkdf2Iterations));
            values.put("msg", AESUtil.encrypt(message, pw, AESUtil.PinPbkdf2Iterations));
            values.put("tx", AESUtil.encrypt(tx != null ? tx : "", pw, AESUtil.PinPbkdf2Iterations));
            database.insert(TABLE, null, values);
        } finally {
            closeAll(database, null);
        }
    }

    public ArrayList<ContentValues> getAllPayments()
            throws Exception {
        ArrayList<ContentValues> data = new ArrayList<>();
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            String selectQuery = "SELECT * FROM payment ORDER BY ts DESC";
            database = this.getReadableDatabase();
            cursor = database.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {
                do {
                    ContentValues vals = new ContentValues();
                    vals.put("_id", cursor.getString(0));
                    vals.put("ts", cursor.getLong(1));
                    vals.put("iad", AESUtil.decrypt(cursor.getString(2), pw, AESUtil.PinPbkdf2Iterations));
                    vals.put("amt", Long.parseLong(AESUtil.decrypt(cursor.getString(3), pw, AESUtil.PinPbkdf2Iterations)));
                    vals.put("famt", AESUtil.decrypt(cursor.getString(4), pw, AESUtil.PinPbkdf2Iterations));
                    vals.put("cfm", Integer.parseInt(AESUtil.decrypt(cursor.getString(5), pw, AESUtil.PinPbkdf2Iterations)));
                    vals.put("msg", AESUtil.decrypt(cursor.getString(6), pw, AESUtil.PinPbkdf2Iterations));
                    vals.put("tx", AESUtil.decrypt(cursor.getString(7), pw, AESUtil.PinPbkdf2Iterations));
                    data.add(vals);
                } while (cursor.moveToNext());
            }
        } finally {
            closeAll(database, cursor);
        }
        if (FAKE_TX_USED) {
            for (int i = 0; i < 10; i++) {
                ContentValues vals = new ContentValues();
                vals.put("_id", "" + i);
                vals.put("ts", new Date().getTime()+"");
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
