package com.bitcoin.merchant.app.screens;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.bitcoin.merchant.app.R;
import com.bitcoin.merchant.app.currency.CountryCurrency;
import com.bitcoin.merchant.app.currency.CurrencyExchange;

import java.util.List;

import com.bitcoin.merchant.app.util.AppUtil;
import com.bitcoin.merchant.app.util.PrefsUtil;

public class SettingsActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.app_name); // never shown
        addPreferencesFromResource(R.xml.settings);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        LinearLayout root = (LinearLayout) findViewById(android.R.id.list).getParent().getParent().getParent();
        Toolbar toolbar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root, false);
        toolbar.setTitle(R.string.action_settings);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        root.addView(toolbar, 0);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backButton();
            }
        });
        final Preference receivePref = findPreference("receiveAPI");
        SettingsActivity ctx = SettingsActivity.this;
        final boolean addressAvailable = SettingsSetReceivingAddressActivity.isReceivingAddressAvailable(ctx);
        receivePref.setSummary(SettingsActivity.this.getText(addressAvailable ? R.string.on : R.string.off));
        receivePref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                goToReceivingPayments();
                return true;
            }
        });
        final Preference namePref = findPreference("name");
        namePref.setSummary(PrefsUtil.getInstance(SettingsActivity.this).getValue(PrefsUtil.MERCHANT_KEY_MERCHANT_NAME, ""));
        namePref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                return selectMerchantName(namePref);
            }
        });
        final Preference fiatPref = findPreference("fiat");
        setCurrencySummary(fiatPref, SettingsActivity.this);
        fiatPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                return selectCurrency(fiatPref);
            }
        });
        Preference pinPref = findPreference("pin");
        pinPref.setSummary("####");
        pinPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                return changePin();
            }
        });
        if (!addressAvailable) {
            goToReceivingPayments();
        }
    }

    private void goToReceivingPayments() {
        Intent intent = new Intent(SettingsActivity.this, SettingsSetReceivingAddressActivity.class);
        startActivity(intent);
    }

    private boolean selectMerchantName(final Preference namePref) {
        final EditText etName = new EditText(SettingsActivity.this);
        etName.setSingleLine(true);
        int maxLength = 70;
        InputFilter[] fArray = new InputFilter[1];
        fArray[0] = new InputFilter.LengthFilter(maxLength);
        etName.setFilters(fArray);
        etName.setText(PrefsUtil.getInstance(SettingsActivity.this).getValue(PrefsUtil.MERCHANT_KEY_MERCHANT_NAME, ""));
        new AlertDialog.Builder(SettingsActivity.this)
                .setTitle(R.string.receive_coins_fragment_name)
                .setView(etName)
                .setCancelable(false)
                .setPositiveButton(R.string.prompt_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String name = etName.getText().toString();
                        if (name.length() > 0) {
                            PrefsUtil.getInstance(SettingsActivity.this).setValue(PrefsUtil.MERCHANT_KEY_MERCHANT_NAME, name);
                            namePref.setSummary(name);
                        }
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.prompt_ko, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                }).show();
        return true;
    }

    private boolean changePin() {
        Intent intent = new Intent(SettingsActivity.this, PinActivity.class);
        intent.putExtra("create", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        return false;
    }

    private boolean selectCurrency(final Preference fiatPref) {
        final SettingsActivity activity = SettingsActivity.this;
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.options_local_currency);
        final List<CountryCurrency> currencies = CurrencyExchange.getInstance(activity).getCountryCurrencies();
        ListAdapter adapter = new ArrayAdapterWithIcon(activity, currencies);
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                CountryCurrency cc = currencies.get(which);
                String locale = cc.countryLocales.getFirstSupportedLocale();
                if (locale == null) {
                    Toast.makeText(activity, "Not supported", Toast.LENGTH_SHORT).show();
                    return;
                }
                dialog.dismiss();
                PrefsUtil prefsUtil = PrefsUtil.getInstance(activity);
                prefsUtil.setValue(PrefsUtil.MERCHANT_KEY_CURRENCY, cc.currencyRate.code);
                prefsUtil.setValue(PrefsUtil.MERCHANT_KEY_COUNTRY, cc.countryLocales.country);
                prefsUtil.setValue(PrefsUtil.MERCHANT_KEY_LOCALE, locale);
                setCurrencySummary(fiatPref, cc);
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
        return true;
    }

    private void setCurrencySummary(Preference fiatPref, Context context) {
        String currency = AppUtil.getCurrency(context);
        String country = AppUtil.getCountry(context);
        String locale = AppUtil.getLocale(context);
        CountryCurrency cc = CurrencyExchange.getInstance(context).getCountryCurrency(currency, country, locale);
        if (cc != null) {
            setCurrencySummary(fiatPref, cc);
        }
    }

    private void setCurrencySummary(Preference fiatPref, CountryCurrency countryCurrency) {
        fiatPref.setIcon(countryCurrency.image);
        fiatPref.setSummary(countryCurrency.toString());
    }

    private class ArrayAdapterWithIcon extends ArrayAdapter<CountryCurrency> {
        private List<CountryCurrency> cc;

        public ArrayAdapterWithIcon(Context context, List<CountryCurrency> items) {
            super(context, android.R.layout.select_dialog_item, items);
            this.cc = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            TextView textView = view.findViewById(android.R.id.text1);
            textView.setCompoundDrawablesRelativeWithIntrinsicBounds(cc.get(position).image, 0, 0, 0);
            float dimension = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getContext().getResources().getDisplayMetrics());
            textView.setCompoundDrawablePadding((int) dimension);
            return view;
        }
    }

    private void backButton() {
        if (!SettingsSetReceivingAddressActivity.isReceivingAddressAvailable(this)) {
            // ToastCustom.makeText(SettingsActivity.this, getString(R.string.obligatory_receiver), ToastCustom.LENGTH_SHORT, ToastCustom.TYPE_ERROR);
            forceToEnterReceivingPaymentAddress();
        } else {
            finish();
        }
    }

    private void forceToEnterReceivingPaymentAddress() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
        builder.setTitle(R.string.options_payment_address)
                .setMessage(R.string.obligatory_receiver)
                .setCancelable(false)
                .setPositiveButton(R.string.prompt_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                        goToReceivingPayments();
                    }
                });
        builder.create().show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        final Preference receivePref = findPreference("receiveAPI");
        final boolean addressAvailable = SettingsSetReceivingAddressActivity.isReceivingAddressAvailable(this);
        receivePref.setSummary(addressAvailable ? (String) SettingsActivity.this.getText(R.string.on) : (String) SettingsActivity.this.getText(R.string.off));
        receivePref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                goToReceivingPayments();
                return true;
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            backButton();
        }
        return false;
    }
}
