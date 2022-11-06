package com.bitcoin.merchant.app.database

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Build
import android.util.Log
import com.bitcoin.merchant.app.application.CashRegisterApplication
import com.bitcoin.merchant.app.model.Analytics
import java.util.*

fun ContentValues.toPaymentRecord(): PaymentRecord {
    return PaymentRecord(timeInSec = getAsLong("ts"),
            tx = getAsString("tx"),
            address = getAsString("iad"),
            bchAmount = getAsLong("amt"),
            fiatAmount = getAsString("famt"),
            confirmations = getAsInteger("cfm"),
            message = getAsString("msg"))
}

private fun PaymentRecord.toContentValues(): ContentValues {
    val c = ContentValues()
    c.put("ts", timeInSec)
    c.put("tx", tx)
    c.put("iad", address)
    c.put("amt", bchAmount.toString())
    c.put("famt", fiatAmount)
    c.put("cfm", confirmations.toString())
    c.put("msg", message)
    return c
}

class DBControllerV3(app: CashRegisterApplication?) : SQLiteOpenHelper(app, DB, null, 1) {
    private val salt: String = Build.MANUFACTURER + Build.BRAND + Build.MODEL + Build.DEVICE + Build.PRODUCT + Build.SERIAL
    override fun onCreate(database: SQLiteDatabase) {
        val query: String
        query = "CREATE TABLE " + TABLE + " ( " +
                "_id INTEGER PRIMARY KEY, " +
                "ts integer, " +
                "iad text, " +
                "amt text, " +
                "famt text, " +
                "cfm text, " +
                "msg text," +
                "tx text" +
                ")"
        database.execSQL(query)
    }

    override fun onUpgrade(database: SQLiteDatabase, version_old: Int, current_version: Int) {
        database.execSQL("DROP TABLE IF EXISTS $TABLE")
        onCreate(database)
    }

    fun paymentAlreadyRecorded(tx: String): Boolean {
        return getPaymentFromTx(tx) != null
    }

    @Throws(Exception::class)
    fun insertPayment(record: PaymentRecord) {
        insertPayment(record.toContentValues())
    }

    @Throws(Exception::class)
    fun insertPayment(record: ContentValues) {
        var database: SQLiteDatabase? = null
        try {
            database = this.writableDatabase
            database.insert(TABLE, null, record)
        } finally {
            closeAll(database, null)
        }
    }

    fun getPaymentFromTx(tx: String): ContentValues? {
        var vals: ContentValues? = null
        var database: SQLiteDatabase? = null
        var c: Cursor? = null
        try {
            val selectQuery = "SELECT * FROM payment WHERE tx = ?"
            database = this.readableDatabase
            c = database.rawQuery(selectQuery, arrayOf(tx))
            if (c.moveToFirst()) {
                vals = parseTx(c)
            }
        } finally {
            closeAll(database, c)
        }
        return vals
    }

    @get:Throws(Exception::class)
    val allPayments: ArrayList<ContentValues>
        get() {
            val data = ArrayList<ContentValues>()
            var database: SQLiteDatabase? = null
            var c: Cursor? = null
            try {
                val selectQuery = "SELECT * FROM payment ORDER BY ts DESC"
                database = this.readableDatabase
                c = database.rawQuery(selectQuery, null)
                if (c.moveToFirst()) {
                    do {
                        parseTx(c)?.let { data.add(it) }
                    } while (c.moveToNext())
                }
            } finally {
                closeAll(database, c)
            }
            return data
        }

    private fun parseTx(c: Cursor): ContentValues? {
        val cv = ContentValues()
        return try {
            cv.put("_id", c.getString(0))
            cv.put("ts", c.getLong(1))
            cv.put("iad", c.getString(2))
            cv.put("amt", c.getString(3).toLong())
            cv.put("famt", c.getString(4))
            cv.put("cfm", c.getString(5).toInt())
            cv.put("msg", c.getString(6))
            cv.put("tx", c.getString(7))
            cv
        } catch (e: Exception) {
            Log.v(TAG, "invalid DB TX found, probably an obsolete encrypted record: $cv")
            null
        }
    }

    @get:Throws(Exception::class)
    val allAddresses: Set<String>
        get() {
            val data: MutableSet<String> = HashSet()
            var database: SQLiteDatabase? = null
            var cursor: Cursor? = null
            try {
                val selectQuery = "SELECT iad FROM payment"
                database = this.readableDatabase
                cursor = database.rawQuery(selectQuery, null)
                if (cursor.moveToFirst()) {
                    do {
                        data.add(cursor.getString(0))
                    } while (cursor.moveToNext())
                }
            } finally {
                closeAll(database, cursor)
            }
            return data
        }

    private fun closeAll(database: SQLiteDatabase?, cursor: Cursor?) {
        try {
            cursor?.close()
            database?.close()
            close()
        } catch (e: Exception) {
            Analytics.error_db_unknown.sendError(e)
            Log.e(TAG, "closeAll", e)
        }
    }

    companion object {
        private const val TAG = "BCR-DBControllerV3"
        private const val DB = "paymentsV3.db"
        private const val TABLE = "payment"
    }
}