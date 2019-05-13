package com.bitcoin.merchant.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.bitcoin.merchant.app.util.OSUtil;
import com.bitcoin.merchant.app.util.PrefsUtil;
import info.blockchain.wallet.crypto.AESUtil;
import info.blockchain.wallet.util.CharSequenceX;

public class DBControllerV3 extends SQLiteOpenHelper {
    private static final String DB = "paymentsV3.db";
    private static final String TABLE = "payment";
    private static Context context = null;

    public DBControllerV3(Context context) {
        super(context, DB, null, 1);
        this.context = context;
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

    public void insertPayment(long ts, String address, long amount, String fiat_amount, int confirmed, String message, String tx) {
        CharSequenceX pw = new CharSequenceX(PrefsUtil.MERCHANT_KEY_PIN + OSUtil.getInstance(context).getFootprint());
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("ts", ts);
        values.put("iad", AESUtil.encrypt(address, pw, AESUtil.PinPbkdf2Iterations));
        values.put("amt", AESUtil.encrypt(Long.toString(amount), pw, AESUtil.PinPbkdf2Iterations));
        values.put("famt", AESUtil.encrypt(fiat_amount, pw, AESUtil.PinPbkdf2Iterations));
        values.put("cfm", AESUtil.encrypt(Integer.toString(confirmed), pw, AESUtil.PinPbkdf2Iterations));
        values.put("msg", AESUtil.encrypt(message, pw, AESUtil.PinPbkdf2Iterations));
        values.put("tx", AESUtil.encrypt(tx != null ? tx : "", pw, AESUtil.PinPbkdf2Iterations));
        database.insert(TABLE, null, values);
        database.close();
    }

    public ArrayList<ContentValues> getAllPayments() {
        CharSequenceX pw = new CharSequenceX(PrefsUtil.MERCHANT_KEY_PIN + OSUtil.getInstance(context).getFootprint());
        ArrayList<ContentValues> data;
        data = new ArrayList<>();
        String selectQuery = "SELECT * FROM payment ORDER BY ts DESC";
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
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
        cursor.close();
        database.close();
        return data;
    }

    public Set<String> getAllAddresses() {
        CharSequenceX pw = new CharSequenceX(PrefsUtil.MERCHANT_KEY_PIN + OSUtil.getInstance(context).getFootprint());
        Set<String> data = new HashSet<>();
        String selectQuery = "SELECT iad FROM payment";
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                String decrypt = AESUtil.decrypt(cursor.getString(0), pw, AESUtil.PinPbkdf2Iterations);
                data.add(decrypt);
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        return data;
    }
}
