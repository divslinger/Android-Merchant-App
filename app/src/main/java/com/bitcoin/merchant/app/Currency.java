package com.bitcoin.merchant.app;

public class Currency {
    public String code;
    public String name;
    public Double rate;
    public String symbol; // no in json

    public Currency() {
    }

    public Currency(String code, String name, Double rate, String symbol) {
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
