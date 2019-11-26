package com.bitcoin.merchant.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;

import com.bitcoin.merchant.app.util.PrefsUtil;
import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import info.blockchain.wallet.crypto.AESUtil;
import info.blockchain.wallet.util.CharSequenceX;

public class DBControllerV3 extends SQLiteOpenHelper {
    private static final String TAG = "DBControllerV3";
    private static final String DB = "paymentsV3.db";
    private static final String TABLE = "payment";
    private static CharSequenceX pw;
    private final Context context;
    private String salt;

    public DBControllerV3(Context context) {
        super(context, DB, null, 1);
        this.context = context;
        this.salt = Build.MANUFACTURER + Build.BRAND + Build.MODEL + Build.DEVICE + Build.PRODUCT + Build.SERIAL;
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

    public void insertPayment(ContentValues record) throws Exception {
        SQLiteDatabase database = null;
        try {
            database = this.getWritableDatabase();
            database.insert(TABLE, null, record);
        } finally {
            closeAll(database, null);
        }
    }

    public ContentValues getPaymentFromTx(String tx) {
        ContentValues vals = null;
        SQLiteDatabase database = null;
        Cursor c = null;
        try {
            String selectQuery = "SELECT * FROM payment WHERE tx = ?";
            database = this.getReadableDatabase();
            c = database.rawQuery(selectQuery, new String[]{tx});
            if (c.moveToFirst()) {
                vals = parseRecord(c);
            }
        } finally {
            closeAll(database, c);
        }
        return vals;
    }

    public ArrayList<ContentValues> getAllPayments() throws Exception {
        ArrayList<ContentValues> data = new ArrayList<>();
        SQLiteDatabase database = null;
        Cursor c = null;
        try {
            String selectQuery = "SELECT * FROM payment ORDER BY ts DESC";
            database = this.getReadableDatabase();
            c = database.rawQuery(selectQuery, null);
            if (c.moveToFirst()) {
                do {
                    ContentValues vals = parseRecord(c);
                    data.add(vals);
                } while (c.moveToNext());
            }
        } finally {
            closeAll(database, c);
        }
        // decrypt and overwrite decrypted
        formatDecryptAndResave(data);
        return data;
    }

    private ContentValues parseRecord(Cursor c) {
        ContentValues vals = new ContentValues();
        vals.put("_id", c.getString(0));
        vals.put("ts", c.getLong(1));
        vals.put("iad", c.getString(2));
        vals.put("amt", c.getString(3));
        vals.put("famt", c.getString(4));
        vals.put("cfm", c.getString(5));
        vals.put("msg", c.getString(6));
        vals.put("tx", c.getString(7));
        return vals;
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
                    decrypt(vals, "iad");
                    decrypt(vals, "amt");
                    decrypt(vals, "famt");
                    decrypt(vals, "cfm");
                    decrypt(vals, "msg");
                    decrypt(vals, "tx");
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

    public void updateRecord(ContentValues values) {
        SQLiteDatabase database = null;
        try {
            database = this.getWritableDatabase();
            int result = database.update(TABLE, values, "_id=" + values.get("_id"), null);
            Log.i(TAG, "resaved record:" + values.get("_id") + ", update:" + result);
        } finally {
            closeAll(database, null);
        }
    }

    private String decryptValue(String value) {
        if (pw == null) {
            pw = new CharSequenceX(PrefsUtil.MERCHANT_KEY_PIN + salt);
        }
        return AESUtil.decrypt(value, pw, AESUtil.PinPbkdf2Iterations);
    }

    private void decrypt(ContentValues vals, String name) {
        String value = vals.getAsString(name);
        if (value != null) {
            vals.put(name, decryptValue(value));
        }
    }

    private void formatValues(ContentValues vals) {
        long amt = Long.parseLong(vals.getAsString("amt"));
        int cfm = Integer.parseInt(vals.getAsString("cfm"));
        vals.put("amt", amt);
        vals.put("cfm", cfm);
    }

    public Set<String> getAllAddresses() throws Exception {
        Set<String> data = new HashSet<>();
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            String selectQuery = "SELECT iad FROM payment";
            database = this.getReadableDatabase();
            cursor = database.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {
                do {
                    String decrypt = decryptValue(cursor.getString(0));
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
