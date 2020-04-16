package com.bitcoin.merchant.app.currency;

import android.util.Log;

import java.util.Locale;

public class CountryLocales {
    public String country;
    public String locales;

    public CountryLocales() {
    }

    public CountryLocales(String country, String locales) {
        this.country = country;
        this.locales = locales;
    }

    public String getFirstSupportedLocale() {
        for (String id : locales.split(",")) {
            try {
                Locale locale = Locale.forLanguageTag(id);
                if (locale != null) {
                    return id;
                }
            } catch (Exception e) {
                Log.v("Currency", "Locale:" + id + " not supported");
            }
        }
        return null;
    }

    public Locale getLocale() {
        String l = getFirstSupportedLocale();
        return l != null ? Locale.forLanguageTag(l) : Locale.getDefault();
    }

    @Override
    public String toString() {
        return "CountryLocales{" +
                "country='" + country + '\'' +
                ", locales='" + locales + '\'' +
                '}';
    }
}
