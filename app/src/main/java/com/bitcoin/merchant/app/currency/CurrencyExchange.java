package com.bitcoin.merchant.app.currency;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class CurrencyExchange {
    public static final int MINIMUM_INTERVAL_BETWEEN_UPDATE_IN_MS = 3 * 60 * 1000;
    public static final String TAG = "CurrencyExchange";
    private static CurrencyExchange instance;
    private final Context context;
    private final Map<String, CurrencyRate> tickerToRate = Collections.synchronizedMap(new TreeMap<String, CurrencyRate>());
    private final CurrencyToLocales currencyToLocales;
    private final Map<String, String> tickerToSymbol;
    private final Map<String, String> countryToName;
    private final Map<String, String> countryToCurrency;
    private volatile long lastUpdate;

    private CurrencyExchange(Context context) {
        this.context = context;
        tickerToSymbol = CountryJsonUtil.readFromJsonFile(context, "currency_symbols.json", TreeMap.class);
        countryToName = CountryJsonUtil.readFromJsonFile(context, "country_to_name.json", TreeMap.class);
        countryToCurrency = CountryJsonUtil.readFromJsonFile(context, "country_to_currency.json", TreeMap.class);
        currencyToLocales = CountryJsonUtil.readFromJsonFile(context, "currency_to_locales.json", CurrencyToLocales.class);
        CurrencyRate[] btcRates = CountryJsonUtil.readFromJsonFile(context, "example_rates.json", CurrencyRate[].class);
        tickerToRate.putAll(CurrencyRate.convertFromBtcToBch(btcRates, tickerToSymbol));
        loadFromStore();
    }

    public static synchronized CurrencyExchange getInstance(Context ctx) {
        if (instance == null) {
            instance = new CurrencyExchange(ctx);
        }
        instance.requestUpdatedExchangeRates();
        return instance;
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

    public List<CountryCurrency> getCountryCurrencies() {
        List<CountryCurrency> cc = new ArrayList<>();
        for (String country : countryToName.keySet()) {
            String currency = countryToCurrency.get(country);
            CurrencyRate cr = getCurrencyRate(currency);
            if (cr != null && isCountrySupported(country)) {
                CountryLocales[] countryLocalesList = currencyToLocales.get(currency);
                if (countryLocalesList != null) {
                    for (CountryLocales countryLocales : countryLocalesList) {
                        if (countryLocales.country.equals(country)) {
                            cc.add(new CountryCurrency(countryLocales, getCountryName(country), cr));
                            break;
                        }
                    }
                }
            }
        }
        Collections.sort(cc, CountryCurrency.BY_NAME);
        return cc;
    }

    private boolean isCountrySupported(String country) {
        return (country != null) && (country.length() > 0)
                && CountryCurrency.isSupported(country)
                && countryToName.containsKey(country);
    }

    private CountryLocales getCountryLocalesForCurrency(String currency, String country, String locale) {
        if (StringUtils.isEmpty(country) || StringUtils.isEmpty(locale)) {
            CountryLocales[] countryLocales = currencyToLocales.get(currency);
            if ((countryLocales == null) || (countryLocales.length == 0)) {
                return null;
            }
            return countryLocales[0];
        }
        return new CountryLocales(country, locale);
    }

    // WARNING: country & locale can be null due to legacy reasons
    public CountryCurrency getCountryCurrency(String currency, String country, String locale) {
        CurrencyRate cr = getCurrencyRate(currency);
        if (cr == null) {
            return null;
        }
        CountryLocales countryLocales = getCountryLocalesForCurrency(currency, country, locale);
        if (countryLocales == null) {
            return null;
        }
        return new CountryCurrency(countryLocales, getCountryName(countryLocales.country), cr);
    }

    private void requestUpdatedExchangeRates() {
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

    private boolean isUpToDate() {
        long now = System.currentTimeMillis();
        return (now - lastUpdate) < MINIMUM_INTERVAL_BETWEEN_UPDATE_IN_MS;
    }

    private void checkCurrencyUpdate() {
        if (isUpToDate()) {
            return;
        }
        try {
            CurrencyRate[] rates = getUrlAsJson("https://www.bitcoin.com/special/rates.json", CurrencyRate[].class);
            tickerToRate.putAll(CurrencyRate.convertFromBtcToBch(rates, tickerToSymbol));
            lastUpdate = System.currentTimeMillis();
            saveToStore();
            Log.i("CurrencyExchange", "rates updated 1 BCH=$" + tickerToRate.get("USD").rate);
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
    }

    private ArrayList<String> getTickers() {
        return new ArrayList<>(tickerToRate.keySet());
    }

    private void loadFromStore() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        long defaultPrice = Double.doubleToLongBits(0.0);
        for (String ticker : getTickers()) {
            String name = prefs.getString(ticker + "-NAME", null);
            double price = Double.longBitsToDouble(prefs.getLong(ticker, defaultPrice));
            tickerToRate.put(ticker, new CurrencyRate(ticker, name, price, tickerToSymbol.get(ticker)));
        }
    }

    private void saveToStore() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        for (String ticker : getTickers()) {
            CurrencyRate cr = tickerToRate.get(ticker);
            editor.putLong(ticker, Double.doubleToRawLongBits(cr.rate));
            editor.putString(ticker + "-NAME", cr.name);
        }
        editor.commit();
    }

    public boolean isTickerSupported(String ticker) {
        return getCurrencyRate(ticker) != null;
    }

    public String getCountryName(String countryCode) {
        String name = countryToName.get(countryCode);
        return name == null ? "" : name;
    }

    public Double getCurrencyPrice(String ticker) {
        CurrencyRate rate = getCurrencyRate(ticker);
        Double price = (rate == null) ? null : rate.rate;
        return (price == null) ? 0 : price;
    }

    public String getCurrencySymbol(String ticker) {
        return tickerToSymbol.get(ticker);
    }

    public CurrencyRate getCurrencyRate(String ticker) {
        return (ticker != null) && (ticker.length() > 0) ? tickerToRate.get(ticker) : null;
    }

    private static class CurrencyToLocales extends TreeMap<String, CountryLocales[]> {
    }
}
