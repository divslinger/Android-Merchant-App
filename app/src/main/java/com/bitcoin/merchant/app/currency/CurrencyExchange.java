package com.bitcoin.merchant.app.currency;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import info.blockchain.merchant.util.AppUtil;

public class CurrencyExchange {
    private static final Object LOCK = new Object();
    public static final int MINIMUM_INTERVAL_BETWEEN_UPDATE_IN_MS = 3 * 60 * 1000;
    private static CurrencyExchange instance = null;
    private static Map<String, String> symbolByTicker;
    private static Map<String, Double> priceByTicker = new TreeMap<>();
    private static Map<String, String> nameByTicker = new TreeMap<>();
    private static List<String> tickers = new ArrayList<>();
    private static Context context = null;
    private static volatile long lastUpdate;

    private CurrencyExchange() {
    }

    public static synchronized CurrencyExchange getInstance(Context ctx) {
        context = ctx;
        if (instance == null) {
            instance = new CurrencyExchange();
            symbolByTicker = AppUtil.readFromJsonFile(ctx, "currency_symbols.json", TreeMap.class);
            tickers = AppUtil.readFromJsonFile(ctx, "currency_tickers.json", ArrayList.class);
            loadFromStore();
        }
        requestUpdatedExchangeRates();
        return instance;
    }

    private static void requestUpdatedExchangeRates() {
        if (isUpToDate()) return;
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                checkCurrencyUpdate();
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private static boolean isUpToDate() {
        long now = System.currentTimeMillis();
        return (now - lastUpdate) < MINIMUM_INTERVAL_BETWEEN_UPDATE_IN_MS;
    }

    private static void checkCurrencyUpdate() {
        if (isUpToDate()) return;
        try {
            CurrencyRate[] rates = getUrlAsJson("https://www.bitcoin.com/special/rates.json", CurrencyRate[].class);
            double bchRate = findBchRate(rates);
            for (CurrencyRate currency : rates) {
                String ticker = currency.code;
                if (!currency.name.toLowerCase().contains("bitcoin")) {
                    BigDecimal bchValue = new BigDecimal(currency.rate * bchRate).setScale(2, BigDecimal.ROUND_CEILING);
                    double price = bchValue.doubleValue();
                    synchronized (LOCK) {
                        priceByTicker.put(ticker, price);
                        nameByTicker.put(ticker, currency.name);
                    }
                    // System.out.println(symbolByTicker.get(ticker) + " " + currency.name + " => " + bchValue.toPlainString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                loadFromStore();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            return;
        }
        try {
            lastUpdate = System.currentTimeMillis();
            saveToStore();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static double findBchRate(CurrencyRate[] rates) {
        double bchRate = 0;
        for (CurrencyRate rate : rates) {
            if ("BCH".equals(rate.code)) {
                bchRate = rate.rate;
                break;
            }
        }
        return bchRate;
    }

    private static void loadFromStore() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        long defaultPrice = Double.doubleToLongBits(0.0);
        for (String ticker : tickers) {
            priceByTicker.put(ticker, Double.longBitsToDouble(prefs.getLong(ticker, defaultPrice)));
            nameByTicker.put(ticker, prefs.getString(ticker + "-NAME", null));
        }
    }

    private static void saveToStore() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        for (String ticker : priceByTicker.keySet()) {
            editor.putLong(ticker, Double.doubleToRawLongBits(priceByTicker.get(ticker)));
            editor.putString(ticker + "-NAME", nameByTicker.get(ticker));
        }
        editor.commit();
    }

    public static <T> T getUrlAsJson(String url, Class<T> c) {
        try {
            InputStream i = new URL(url).openStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(i));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine).append('\n');
            }
            in.close();
            return new Gson().fromJson(response.toString(), c);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isTickerSupported(String ticker) {
        return (ticker != null) && (ticker.length() > 0) && tickers.contains(ticker);
    }

    public Double getCurrencyPrice(String ticker) {
        Double price = priceByTicker.get(ticker);
        return price == null ? 0 : price;
    }

    public String getCurrencySymbol(String ticker) {
        return symbolByTicker.get(ticker);
    }

    public CurrencyRate[] getCurrencies() {
        synchronized (LOCK) {
            CurrencyRate[] currencies = new CurrencyRate[priceByTicker.size()];
            int i = 0;
            for (String ticker : priceByTicker.keySet()) {
                String symbol = symbolByTicker.get(ticker);
                currencies[i++] = new CurrencyRate(ticker, nameByTicker.get(ticker), priceByTicker.get(ticker), symbol);
            }
            return currencies;
        }
    }
}
