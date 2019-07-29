package com.bitcoin.merchant.app.database;

import android.content.ContentValues;

import java.util.Objects;

public class PaymentRecord {
    public long timeInSec; // can be 0
    public final String address;
    public final long bchAmount; // can negative
    public final String fiatAmount; // can be null
    public int confirmations; // can be -1
    public final String message;
    public final String tx; // can be null or empty

    public PaymentRecord(long timeInSec, String address, long bchAmount, String fiatAmount, int confirmations, String message, String tx) {
        this.timeInSec = timeInSec;
        this.address = address;
        this.bchAmount = bchAmount;
        this.fiatAmount = fiatAmount;
        this.confirmations = confirmations;
        this.message = message;
        this.tx = tx;
    }

    public PaymentRecord(ContentValues vals) {
        this.timeInSec = vals.getAsLong("ts");
        this.tx = vals.getAsString("tx");
        this.address = vals.getAsString("iad");
        this.bchAmount = vals.getAsLong("amt");
        this.fiatAmount = vals.getAsString("famt");
        this.confirmations = vals.getAsInteger("cfm");
        this.message = vals.getAsString("message");
    }

    public ContentValues toContentValues() {
        ContentValues c = new ContentValues();
        toContentValues(c);
        return c;
    }

    public void toContentValues(ContentValues c) {
        c.put("ts", timeInSec);
        c.put("tx", tx);
        c.put("iad", address);
        c.put("amt", Long.toString(bchAmount));
        c.put("famt", fiatAmount);
        c.put("cfm", Integer.toString(confirmations));
        c.put("msg", message);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaymentRecord txRecord = (PaymentRecord) o;
        return (tx != null) && tx.equals(txRecord.tx);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tx);
    }

    @Override
    public String toString() {
        return "TxRecord{" +
                "tx='" + tx + '\'' +
                ", bchAmount=" + bchAmount +
                ", fiatAmount='" + fiatAmount + '\'' +
                ", address='" + address + '\'' +
                ", timeInSec=" + timeInSec +
                ", confirmations=" + confirmations +
                ", message='" + message + '\'' +
                '}';
    }
}
