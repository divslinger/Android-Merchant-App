package com.bitcoin.merchant.app.currency;

public class CurrencyRate {
    public String code;
    public String name;
    public Double rate;
    public String symbol; // no in json

    public CurrencyRate() {
    }

    public CurrencyRate(String code, String name, Double rate, String symbol) {
        this.code = code;
        this.name = name;
        this.rate = rate;
        this.symbol = symbol;
    }

    @Override
    public String toString() {
        return code + " - " + (symbol == null ? "" : symbol + " - ") + name;
    }
}
