package com.bitcoin.merchant.app.screens;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
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
import com.bitcoin.merchant.app.util.AppUtil;
import com.bitcoin.merchant.app.util.PrefsUtil;
import com.bitcoin.merchant.app.util.ToastCustom;
import com.google.bitcoin.uri.BitcoinCashURI;

import java.util.List;

import info.blockchain.wallet.util.FormatsUtil;

public class SettingsActivity extends PreferenceActivity {
    public static final String SCAN_RESULT = "SCAN_RESULT";
    private static final String TAG = "SettingsActivity";
    private static final int CAMERA_PERMISSION = 1111;
    private static final boolean ENTERING_ADDRESS_BYPASSED = false;
    private static int ZBAR_SCANNER_REQUEST = 2026;
    private Preference newAddressPref = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "onCreate");
        SettingsActivity ctx = SettingsActivity.this;
        super.onCreate(savedInstanceState);
        setTitle(R.string.app_name); // never shown
        addPreferencesFromResource(R.xml.settings);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        LinearLayout root = (LinearLayout) findViewById(android.R.id.list).getParent().getParent().getParent();
        Toolbar toolbar = (Toolbar) LayoutInflater.from(ctx).inflate(R.layout.settings_toolbar, root, false);
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
        addOptionName(ctx);
        addOptionCurrency(ctx);
        addOptionAddress(ctx);
        addOptionDownloadWallet(ctx);
        addOptionPin(ctx);
        if (!isReceivingAddressAvailable(ctx) && AppUtil.isWalletInstalled(ctx)) {
            askHowToAddNewAddress();
        }
    }

    private void addOptionName(SettingsActivity ctx) {
        final Preference p = findPreference("name");
        p.setSummary(PrefsUtil.getInstance(ctx).getValue(PrefsUtil.MERCHANT_KEY_MERCHANT_NAME, "..."));
        p.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                return selectMerchantName(p);
            }
        });
    }

    private void addOptionCurrency(SettingsActivity ctx) {
        final Preference p = findPreference("fiat");
        setCurrencySummary(p, ctx);
        p.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                return selectCurrency(p);
            }
        });
    }

    private void addOptionAddress(SettingsActivity ctx) {
        newAddressPref = findPreference("address");
        String summary = "";
        if (isReceivingAddressAvailable(ctx)) {
            summary = convertToBitcoinCash(getAddress(ctx));
        } else {
            summary = "...\n\n" + getString(R.string.options_explain_payment_address);
        }
        newAddressPref.setSummary(summary);
        newAddressPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                askHowToAddNewAddress();
                return true;
            }
        });
    }

    private void addOptionDownloadWallet(SettingsActivity ctx) {
        boolean walletInstalled = AppUtil.isWalletInstalled(ctx);
        Preference p = findPreference("download_wallet");
        if (walletInstalled) {
            getPreferenceScreen().removePreference(p);
        } else {
            p.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://wallet.bitcoin.com"));
                    startActivity(intent);
                    return true;
                }
            });
        }
    }

    private void addOptionPin(SettingsActivity ctx) {
        Preference p = findPreference("pin");
        p.setSummary("####");
        p.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                return changePin();
            }
        });
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
            float dimension = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getContext().getResources().getDisplayMetrics());
            textView.setCompoundDrawablePadding((int) dimension);
            return view;
        }
    }

    private void backButton() {
        if (!isReceivingAddressAvailable(this)) {
            notifyUserThatAddressIsRequiredToReceivePayments();
        } else {
            finish();
        }
    }

    private void notifyUserThatAddressIsRequiredToReceivePayments() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
        builder.setTitle(R.string.options_payment_address)
                .setMessage(R.string.obligatory_receiver)
                .setCancelable(false)
                .setPositiveButton(R.string.prompt_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            backButton();
        }
        return false;
    }

    public static boolean isReceivingAddressAvailable(Context ctx) {
        return getAddress(ctx).length() != 0;
    }

    private static String getAddress(Context setReceivingAddressActivity) {
        return PrefsUtil.getInstance(setReceivingAddressActivity).getValue(PrefsUtil.MERCHANT_KEY_MERCHANT_RECEIVER, "");
    }

    private static String convertToBitcoinCash(String address) {
        FormatsUtil f = FormatsUtil.getInstance();
        if (address != null && address.length() > 0 &&
                (!f.isValidXpub(address) && f.isValidBitcoinAddress(address))) {
            try {
                address = BitcoinCashURI.toCashAddress(address);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return address;
    }

    private void askHowToAddNewAddress() {
        final TextView tvReceiverHelp = new TextView(this);
        tvReceiverHelp.setText(this.getText(R.string.options_add_payment_address_text));
        tvReceiverHelp.setPadding(50, 10, 50, 10);
        new AlertDialog.Builder(this)
                .setTitle(R.string.options_add_payment_address)
                .setView(tvReceiverHelp)
                .setCancelable(true)
                .setPositiveButton(R.string.paste, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        enterAddressUsingInputField();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.scan, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                        requestToOpenCamera();
                    }
                }).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case CAMERA_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera();
                } else {
                    String text = "Please grant camera permission to use the QR Scanner";
                    Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void requestToOpenCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION);
        } else {
            openCamera();
        }
    }

    private void openCamera() {
        Intent intent = new Intent(this, ScanQRCodeActivity.class);
        // intent.putExtra(ZBarConstants.SCAN_MODES, new int[]{Symbol.QRCODE});
        startActivityForResult(intent, ZBAR_SCANNER_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((resultCode == Activity.RESULT_OK) && (requestCode == ZBAR_SCANNER_REQUEST) && (data != null)) {
            Log.v(TAG, "requestCode:" + requestCode + ", resultCode:" + resultCode + ", Intent:" + data.getStringExtra(SCAN_RESULT));
            String address = BitcoinCashURI.toLegacyAddress(data.getStringExtra(SCAN_RESULT));
            if (AppUtil.isValidAddress(address)) {
                setNewAddress(address);
            } else {
                ToastCustom.makeText(this, getString(R.string.unrecognized_xpub), ToastCustom.LENGTH_SHORT, ToastCustom.TYPE_ERROR);
            }
        } else {
            Log.v(TAG, "requestCode:" + requestCode + ", resultCode:" + resultCode);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void enterAddressUsingInputField() {
        if (ENTERING_ADDRESS_BYPASSED) {
            setNewAddress("1MxRuANd5CmHWcveTwQaAJ36sStEQ5QM5k");
        } else {
            final EditText etReceiver = new EditText(this);
            etReceiver.setSingleLine(true);
            etReceiver.setText(getAddress(this));
            showDialogToEnterAddress(etReceiver);
        }
    }

    private void showDialogToEnterAddress(final EditText etReceiver) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.options_add_payment_address)
                .setView(etReceiver)
                .setCancelable(false)
                .setPositiveButton(R.string.prompt_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        validateThenSetNewAddress(etReceiver.getText().toString().trim());
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.prompt_ko, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void validateThenSetNewAddress(String address) {
        address = BitcoinCashURI.toLegacyAddress(address);
        if (AppUtil.isValidAddress(address)) {
            setNewAddress(address);
        } else {
            ToastCustom.makeText(this, getString(R.string.unrecognized_xpub), ToastCustom.LENGTH_SHORT, ToastCustom.TYPE_ERROR);
        }
    }

    private void setNewAddress(String receiver) {
        newAddressPref.setSummary(convertToBitcoinCash(receiver));
        PrefsUtil.getInstance(this).setValue(PrefsUtil.MERCHANT_KEY_MERCHANT_RECEIVER, receiver);
    }
}
