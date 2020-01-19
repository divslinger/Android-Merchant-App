package com.bitcoin.merchant.app.currency;

import android.content.Context;

import com.bitcoin.merchant.app.util.AppUtil;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class CurrencyHelper {
    public static final String TAG = "CurrencyExchange";
    private static CurrencyHelper instance;
    private final CurrencyToLocales currencyToLocales;
    private final Map<String, String> countryToName;
    private final Map<String, String> countryToCurrency;

    // @Serialized with GSON
    private static class CurrencyToLocales extends TreeMap<String, CountryLocales[]> {
    }

    private CurrencyHelper(Context context) {
        countryToName = AppUtil.readFromJsonFile(context, "country_to_name.json", TreeMap.class);
        countryToCurrency = AppUtil.readFromJsonFile(context, "country_to_currency.json", TreeMap.class);
        currencyToLocales = AppUtil.readFromJsonFile(context, "currency_to_locales.json", CurrencyToLocales.class);
    }

    public static synchronized CurrencyHelper getInstance(Context ctx) {
        if (instance == null) {
            instance = new CurrencyHelper(ctx);
        }
        return instance;
    }

    public List<CountryCurrency> getCountryCurrencies() {
        List<CountryCurrency> ccList = new ArrayList<>();
        for (String country : countryToName.keySet()) {
            String currency = countryToCurrency.get(country);
            if (isCountrySupported(country) && (currency != null)) {
                CountryLocales[] countryLocalesList = currencyToLocales.get(currency);
                if (countryLocalesList != null) {
                    for (CountryLocales countryLocales : countryLocalesList) {
                        if (countryLocales.countryCode.equals(country)) {
                            ccList.add(createCountryCurrency(country, countryLocales, currency));
                            break;
                        }
                    }
                }
            }
        }
        Collections.sort(ccList, CountryCurrency.BY_NAME);
        return ccList;
    }

    private CountryCurrency createCountryCurrency(String countryCode, CountryLocales countryLocales, String currencyCode) {
        return new CountryCurrency(countryLocales, getCountryName(countryCode), currencyCode);
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
        CountryLocales countryLocales = getCountryLocalesForCurrency(currency, country, locale);
        if (countryLocales == null) {
            return null;
        }
        if (StringUtils.isEmpty(country) && StringUtils.isEmpty(countryLocales.countryCode)) {
            return null;
        }
        return createCountryCurrency(country == null ? countryLocales.countryCode : country, countryLocales, currency);
    }

    boolean isCurrencySupported(String ticker) {
        return countryToCurrency.values().contains(ticker);
    }

    Map<String, String> getCountryToCurrency() {
        return countryToCurrency;
    }

    private String getCountryName(String countryCode) {
        String name = countryToName.get(countryCode);
        return name == null ? "" : name;
    }
}
