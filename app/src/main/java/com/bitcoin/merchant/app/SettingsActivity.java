package com.bitcoin.merchant.app;

import android.app.AlertDialog;
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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import info.blockchain.merchant.util.PrefsUtil;

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
        final boolean addressAvailable = SetReceivingAddressActivity.isReceivingAddressAvailable(ctx);
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
        fiatPref.setSummary(PrefsUtil.getInstance(SettingsActivity.this).getValue(PrefsUtil.MERCHANT_KEY_CURRENCY, "USD"));
        fiatPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                return selectCurrency(fiatPref);
            }
        });
        Preference pinPref = findPreference("pin");
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
        Intent intent = new Intent(SettingsActivity.this, SetReceivingAddressActivity.class);
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
        final CurrencyExchange ce = CurrencyExchange.getInstance(this);
        final Currency[] currencies = ce.getCurrencies();
        String ticker = PrefsUtil.getInstance(SettingsActivity.this).getValue(PrefsUtil.MERCHANT_KEY_CURRENCY, "USD");
        int sel = -1;
        for (int i = 0; i < currencies.length; i++) {
            if (currencies[i].code.equals(ticker)) {
                sel = i;
                break;
            }
        }
        if (sel == -1) {
            sel = currencies.length - 1;    // set to USD
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
        builder.setTitle(R.string.options_local_currency);
        String texts[] = new String[currencies.length];
        for (int i = 0; i < texts.length; i++) {
            texts[i] = currencies[i].toString();
        }
        builder.setSingleChoiceItems(texts, sel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                String ticker = currencies[which].code;
                PrefsUtil.getInstance(SettingsActivity.this).setValue(PrefsUtil.MERCHANT_KEY_CURRENCY, ticker);
                fiatPref.setSummary(PrefsUtil.getInstance(SettingsActivity.this).getValue(PrefsUtil.MERCHANT_KEY_CURRENCY, ticker));
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
        return true;
    }

    private void backButton() {
        if (!SetReceivingAddressActivity.isReceivingAddressAvailable(this)) {
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
        final boolean addressAvailable = SetReceivingAddressActivity.isReceivingAddressAvailable(this);
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
