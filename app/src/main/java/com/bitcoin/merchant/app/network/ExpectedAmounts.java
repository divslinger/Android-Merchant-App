package com.bitcoin.merchant.app.network;

public class ExpectedAmounts {
    public static final ExpectedAmounts UNDEFINED = new ExpectedAmounts(0, null);
    public final long bch;
    public final String fiat;

    public ExpectedAmounts(long bch, String fiat) {
        this.bch = bch;
        this.fiat = fiat;
    }

    @Override
    public String toString() {
        return "ExpectedAmounts{" +
                "bch=" + bch +
                ", fiat='" + fiat + '\'' +
                '}';
    }
}
