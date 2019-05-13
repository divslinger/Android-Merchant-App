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
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bitcoin.merchant.app.R;
import com.google.bitcoin.uri.BitcoinCashURI;

import com.bitcoin.merchant.app.util.AppUtil;
import com.bitcoin.merchant.app.util.PrefsUtil;
import com.bitcoin.merchant.app.util.ToastCustom;
import info.blockchain.wallet.util.FormatsUtil;

public class SettingsSetReceivingAddressActivity extends PreferenceActivity {
    public static final String SCAN_RESULT = "SCAN_RESULT";
    private static final int CAMERA_PERMISSION = 1111;
    private static final boolean ENTERING_ADDRESS_BYPASSED = false;
    private static int ZBAR_SCANNER_REQUEST = 2026;
    private Preference newAddressPref = null;
    private static final String TAG = "SetReceivingAddress";

    public static boolean isReceivingAddressAvailable(Context ctx) {
        return getAddress(ctx).length() != 0;
    }

    private static String getAddress(Context setReceivingAddressActivity) {
        return PrefsUtil.getInstance(setReceivingAddressActivity).getValue(PrefsUtil.MERCHANT_KEY_MERCHANT_RECEIVER, "");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setTitle(R.string.app_name); // never shown
        boolean addressAvailable = isReceivingAddressAvailable(this);
        boolean walletInstalled = AppUtil.isWalletInstalled(this);
        addPreferencesFromResource(addressAvailable ? R.xml.settings_with_address_set
                : walletInstalled ? R.xml.settings_without_address
                : R.xml.settings_without_address_and_wallet);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        LinearLayout root = (LinearLayout) findViewById(android.R.id.list).getParent().getParent().getParent();
        Toolbar toolbar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root, false);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        root.addView(toolbar, 0);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        newAddressPref = findPreference("address");
        String address = getAddress(SettingsSetReceivingAddressActivity.this);
        newAddressPref.setSummary(convertToBitcoinCash(address));
        if (!walletInstalled) {
            Preference walletUrlPref = findPreference("wallet");
            if (walletUrlPref != null) {
                walletUrlPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                    public boolean onPreferenceClick(Preference preference) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://wallet.bitcoin.com"));
                        startActivity(intent);
                        return true;
                    }
                });
            }
        }
        if (!addressAvailable) {
            if (walletInstalled) {
                askHowToAddNewAddress();
            }
            newAddressPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    askHowToAddNewAddress();
                    return true;
                }
            });
        } else {
            final Preference forgetPref = findPreference("forget");
            forgetPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    confirmAddressRemoval();
                    return true;
                }
            });
        }
    }

    private String convertToBitcoinCash(String address) {
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
        final TextView tvReceiverHelp = new TextView(SettingsSetReceivingAddressActivity.this);
        tvReceiverHelp.setText(SettingsSetReceivingAddressActivity.this.getText(R.string.options_add_payment_address_text));
        tvReceiverHelp.setPadding(50, 10, 50, 10);
        new AlertDialog.Builder(SettingsSetReceivingAddressActivity.this)
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
        Intent intent = new Intent(SettingsSetReceivingAddressActivity.this, ScanQRCodeActivity.class);
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
            final EditText etReceiver = new EditText(SettingsSetReceivingAddressActivity.this);
            etReceiver.setSingleLine(true);
            etReceiver.setText(getAddress(SettingsSetReceivingAddressActivity.this));
            showDialogToEnterAddress(etReceiver);
        }
    }

    private void confirmAddressRemoval() {
        final TextView tvForgetHelp = new TextView(SettingsSetReceivingAddressActivity.this);
        tvForgetHelp.setText(SettingsSetReceivingAddressActivity.this.getText(R.string.options_forget_payment_address_text));
        tvForgetHelp.setPadding(50, 10, 50, 10);
        new AlertDialog.Builder(SettingsSetReceivingAddressActivity.this)
                .setTitle(R.string.options_forget_payment_address)
                .setView(tvForgetHelp)
                .setCancelable(false)
                .setPositiveButton(R.string.prompt_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                        PrefsUtil.getInstance(SettingsSetReceivingAddressActivity.this).setValue(PrefsUtil.MERCHANT_KEY_MERCHANT_RECEIVER, "");
                        SettingsSetReceivingAddressActivity.this.finish();
                    }
                })
                .setNegativeButton(R.string.prompt_ko, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void showDialogToEnterAddress(final EditText etReceiver) {
        new AlertDialog.Builder(SettingsSetReceivingAddressActivity.this)
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
            ToastCustom.makeText(SettingsSetReceivingAddressActivity.this, getString(R.string.unrecognized_xpub), ToastCustom.LENGTH_SHORT, ToastCustom.TYPE_ERROR);
        }
    }

    private void setNewAddress(String receiver) {
        newAddressPref.setSummary(convertToBitcoinCash(receiver));
        PrefsUtil.getInstance(SettingsSetReceivingAddressActivity.this).setValue(PrefsUtil.MERCHANT_KEY_MERCHANT_RECEIVER, receiver);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
        }
        return false;
    }
}
