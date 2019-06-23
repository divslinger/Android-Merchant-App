package com.bitcoin.merchant.app.network;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ExpectedIncoming {
    private static class Amounts {
        final long bch;
        final String fiat;

        public Amounts(long bch, String fiat) {
            this.bch = bch;
            this.fiat = fiat;
        }
    }

    private static final ExpectedIncoming instance = new ExpectedIncoming();
    private final Map<String, Amounts> addressToAmounts = new ConcurrentHashMap<>();

    private ExpectedIncoming() {
    }

    public static ExpectedIncoming getInstance() {
        return instance;
    }

    public void setExpectedAmounts(String receivingAddress, long bchAmount, String fiatAmount) {
        addressToAmounts.put(receivingAddress, new Amounts(bchAmount, fiatAmount));
    }

    public long getBchAmount(String receivingAddress) {
        Amounts amounts = addressToAmounts.get(receivingAddress);
        return (amounts == null) ? 0L : amounts.bch;
    }

    public String getFiatAmount(String receivingAddress) {
        Amounts amounts = addressToAmounts.get(receivingAddress);
        return (amounts == null) ? null : amounts.fiat;
    }

    public boolean isValidAddress(String receivingAddress) {
        return addressToAmounts.containsKey(receivingAddress);
    }
}
