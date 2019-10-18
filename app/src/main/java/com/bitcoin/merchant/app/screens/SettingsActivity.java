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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bitcoin.merchant.app.R;
import com.bitcoin.merchant.app.currency.CountryCurrency;
import com.bitcoin.merchant.app.currency.CurrencyExchange;
import com.bitcoin.merchant.app.screens.dialogs.AddNewAddressDialog;
import com.bitcoin.merchant.app.screens.dialogs.CurrencySelectionDialog;
import com.bitcoin.merchant.app.screens.dialogs.MerchantNameEditorDialog;
import com.bitcoin.merchant.app.util.AddressUtil;
import com.bitcoin.merchant.app.util.AppUtil;
import com.bitcoin.merchant.app.util.PrefsUtil;
import com.bitcoin.merchant.app.util.ToastCustom;
import com.google.bitcoin.uri.BitcoinCashURI;

import de.tobibrandt.bitcoincash.BitcoinCashAddressFormatter;
import info.blockchain.wallet.util.FormatsUtil;

public class SettingsActivity extends PreferenceActivity {
    public static final String SCAN_RESULT = "SCAN_RESULT";
    private static final String TAG = "SettingsActivity";
    private static final int CAMERA_PERMISSION = 1111;
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
        if (!AppUtil.isReceivingAddressAvailable(ctx) && AppUtil.isWalletAppInstalled(ctx)) {
            new AddNewAddressDialog(ctx).show();
        }
    }

    private void addOptionName(final SettingsActivity ctx) {
        final Preference p = findPreference("name");
        p.setSummary(PrefsUtil.getInstance(ctx).getValue(PrefsUtil.MERCHANT_KEY_MERCHANT_NAME, "..."));
        p.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                return new MerchantNameEditorDialog(ctx).show(p);
            }
        });
    }

    private void addOptionCurrency(final SettingsActivity ctx) {
        final Preference p = findPreference("fiat");
        setCurrencySummary(p, ctx);
        p.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                return new CurrencySelectionDialog(ctx).show(p);
            }
        });
    }

    private void addOptionAddress(final SettingsActivity ctx) {
        newAddressPref = findPreference("address");
        String summary = "";
        if (AppUtil.isReceivingAddressAvailable(ctx)) {
            summary = AppUtil.convertToBitcoinCash(AppUtil.getReceivingAddress(ctx));
        } else {
            summary = "...\n\n" + getString(R.string.options_explain_payment_address);
        }
        newAddressPref.setSummary(summary);
        newAddressPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                new AddNewAddressDialog(ctx).show();
                return true;
            }
        });
    }

    private void addOptionDownloadWallet(SettingsActivity ctx) {
        boolean walletInstalled = AppUtil.isWalletAppInstalled(ctx);
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

    private boolean changePin() {
        Intent intent = new Intent(SettingsActivity.this, PinActivity.class);
        intent.putExtra("create", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        return false;
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

    public void setCurrencySummary(Preference fiatPref, CountryCurrency countryCurrency) {
        fiatPref.setIcon(countryCurrency.image);
        fiatPref.setSummary(countryCurrency.toString());
    }

    private void backButton() {
        if (!AppUtil.isReceivingAddressAvailable(this)) {
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                String text = "Please grant camera permission to use the QR Scanner";
                Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void requestToOpenCamera() {
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
            System.out.println("ADDRESS SCANNED: " + data.getStringExtra(SCAN_RESULT));
            validateThenSetNewAddress(data.getStringExtra(SCAN_RESULT));
        } else {
            Log.v(TAG, "requestCode:" + requestCode + ", resultCode:" + resultCode);
        }
    }

    public void setNewAddress(String receiver) {
        newAddressPref.setSummary(AppUtil.convertToBitcoinCash(receiver));
        AppUtil.setReceivingAddress(this, receiver);
    }

    public void validateThenSetNewAddress(String address) {
        final SettingsActivity ctx = SettingsActivity.this;
        //Is this a valid xpub, legacy, or cashaddr?
        if (AppUtil.isValidAddress(address)) {
            /*
            If it's not a valid xpub, we can assume it's a Bitcoin Cash address since the address is valid from the previous if statement.
             */
            if (!FormatsUtil.getInstance().isValidXpub(address)) {
                //legacy or cashaddr logic.
                this.validateCashaddrOrLegacyAddress(address);
            } else {
                //xpub logic
                this.saveXpubAsDestinationAddress(address);
                this.beginSyncingXpubWallet(ctx);
            }
        } else {
            //If it is not valid, then display to the user that they did not enter a valid xpub, or legacy/cashaddr address.
            ToastCustom.makeText(ctx, ctx.getString(R.string.unrecognized_xpub), ToastCustom.LENGTH_SHORT, ToastCustom.TYPE_ERROR);
        }
    }

    private void validateCashaddrOrLegacyAddress(String address) {
        //First we check if it's a cashaddr
        if (AddressUtil.isValidCashAddr(address)) {
            //Then we check if it has the bitcoincash: prefix, if it doesn't then we add it
            String cashAddrPrefix = BitcoinCashAddressFormatter.MAIN_NET_PREFIX + ":";
            if (!address.startsWith(cashAddrPrefix))
                address = cashAddrPrefix + address;
        }
        //Then we set the address.
        setNewAddress(address);
    }

    private void saveXpubAsDestinationAddress(String xpub)
    {
        setNewAddress(xpub);
    }

    private void beginSyncingXpubWallet(final SettingsActivity ctx) {
        /*
        When a merchant sets an xpub as their address in the settings, we want to sync the wallet up to the freshest address so users won't be sending to older addresses.
        We do this by polling Bitcoin.com's REST API for the address history of all addresses up until we find a fresh address.

        Due to Android forcing networking to be on a separate thread, we do this in a thread.
         */
        new Thread() {
            @Override
            public void run() {
                AppUtil util = AppUtil.getInstance(ctx);
                try {
                    boolean synced = util.getWallet().syncXpub();
                    if (synced) {
                        ctx.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastCustom.makeText(ctx, ctx.getString(R.string.synced_xpub), ToastCustom.LENGTH_SHORT, ToastCustom.TYPE_OK);
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.e(TAG, "", e);
                }
            }
        }.start();
        ToastCustom.makeText(ctx, ctx.getString(R.string.syncing_xpub), ToastCustom.LENGTH_SHORT, ToastCustom.TYPE_GENERAL);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}
