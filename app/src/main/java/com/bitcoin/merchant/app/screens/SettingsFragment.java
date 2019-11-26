package com.bitcoin.merchant.app.screens;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bitcoin.merchant.app.R;
import com.bitcoin.merchant.app.ScanQRCodeActivity;
import com.bitcoin.merchant.app.currency.CountryCurrency;
import com.bitcoin.merchant.app.currency.CurrencyExchange;
import com.bitcoin.merchant.app.screens.dialogs.AddNewAddressDialog;
import com.bitcoin.merchant.app.screens.dialogs.CurrencySelectionDialog;
import com.bitcoin.merchant.app.screens.dialogs.MerchantNameEditorDialog;
import com.bitcoin.merchant.app.screens.features.ToolbarAwareFragment;
import com.bitcoin.merchant.app.util.AddressUtil;
import com.bitcoin.merchant.app.util.AppUtil;
import com.bitcoin.merchant.app.util.PrefsUtil;
import com.bitcoin.merchant.app.util.ToastCustom;

import de.tobibrandt.bitcoincash.BitcoinCashAddressFormatter;
import info.blockchain.wallet.util.FormatsUtil;

public class SettingsFragment extends ToolbarAwareFragment {
    public static final String SCAN_RESULT = "SCAN_RESULT";
    private static final String TAG = "SettingsActivity";
    private static final int CAMERA_PERMISSION = 1111;
    private static int ZBAR_SCANNER_REQUEST = 2026;
    private LinearLayout lvMerchantName;
    private LinearLayout lvPaymentAddress;
    private LinearLayout lvLocalCurrency;
    private LinearLayout lvPinCode;
    private Button btnSave;
    private RelativeLayout btnLocalBitcoin;
    private RelativeLayout btnThePit;
    private boolean isScanning;
    private View rootView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        lvMerchantName = rootView.findViewById(R.id.lv_merchant_name);
        lvPaymentAddress = rootView.findViewById(R.id.lv_payment_address);
        lvLocalCurrency = rootView.findViewById(R.id.lv_fiat_currency);
        lvPinCode = rootView.findViewById(R.id.lv_pin_code);
        btnSave = rootView.findViewById(R.id.btn_save);
        btnLocalBitcoin = rootView.findViewById(R.id.localbch_ad);
        btnThePit = rootView.findViewById(R.id.bce_ad);
        addOptionName();
        addOptionCurrency();
        addOptionAddress();
        addOptionPin();
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.onBackPressed();
            }
        });
        btnLocalBitcoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUrl("https://local.bitcoin.com");
            }
        });
        btnThePit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUrl("https://exchange.bitcoin.com");
            }
        });
        setToolbarAsBackButton();
        setToolbarTitle(R.string.menu_settings);
        return rootView;
    }

    private void openUrl(String url) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    private void addOptionName() {
        String merchantName = PrefsUtil.getInstance(activity).getValue(PrefsUtil.MERCHANT_KEY_MERCHANT_NAME, "...");
        final TextView tvMerchantName = rootView.findViewById(R.id.et_merchant_name);
        tvMerchantName.setText(merchantName);
        lvMerchantName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MerchantNameEditorDialog(activity).show(tvMerchantName);
            }
        });
    }

    private void addOptionCurrency() {
        setCurrencySummary(activity);
        lvLocalCurrency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new CurrencySelectionDialog(SettingsFragment.this).show();
            }
        });
    }

    private void addOptionAddress() {
        final TextView tvPaymentAddress = rootView.findViewById(R.id.et_payment_address);
        String summary = AppUtil.isReceivingAddressAvailable(activity)
                ? AppUtil.convertToBitcoinCash(AppUtil.getReceivingAddress(activity))
                : "...\n\n" + getString(R.string.options_explain_payment_address);
        tvPaymentAddress.setText(summary);
        lvPaymentAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AddNewAddressDialog(SettingsFragment.this).show();
            }
        });
    }

    private void addOptionPin() {
        final TextView tvPinCode = rootView.findViewById(R.id.et_pin_code);
        tvPinCode.setText("####");
        lvPinCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePin();
            }
        });
    }

    private void changePin() {
        Bundle args = new Bundle();
        args.putBoolean(PinCodeFragment.EXTRA_DO_CREATE, true);
        getNav().navigate(R.id.nav_to_pin_code_screen, args);
    }

    private void setCurrencySummary(Context context) {
        String currency = AppUtil.getCurrency(context);
        String country = AppUtil.getCountry(context);
        String locale = AppUtil.getLocale(context);
        CountryCurrency cc = CurrencyExchange.getInstance(context).getCountryCurrency(currency, country, locale);
        if (cc != null) {
            setCurrencySummary(cc);
        }
    }

    public void setCurrencySummary(CountryCurrency countryCurrency) {
        TextView currencyString = rootView.findViewById(R.id.et_local_currency);
        currencyString.setText(countryCurrency.toString());
    }

    @Override
    public boolean isBackAllowed() {
        if (!AppUtil.isReceivingAddressAvailable(activity)) {
            notifyUserThatAddressIsRequiredToReceivePayments();
            return false; // forbid
        } else {
            return true;
        }
    }

    private void notifyUserThatAddressIsRequiredToReceivePayments() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
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
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                this.isScanning = false;
                String text = "Please grant camera permission to use the QR Scanner";
                Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void requestToOpenCamera() {
        this.isScanning = true;
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION);
        } else {
            openCamera();
        }
    }

    private void openCamera() {
        Intent intent = new Intent(activity, ScanQRCodeActivity.class);
        startActivityForResult(intent, ZBAR_SCANNER_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((resultCode == Activity.RESULT_OK) && (requestCode == ZBAR_SCANNER_REQUEST) && (data != null)) {
            Log.v(TAG, "requestCode:" + requestCode + ", resultCode:" + resultCode + ", Intent:" + data.getStringExtra(SCAN_RESULT));
            System.out.println("ADDRESS SCANNED: " + data.getStringExtra(SCAN_RESULT));
            validateThenSetNewAddress(data.getStringExtra(SCAN_RESULT));
            this.isScanning = false;
        } else {
            Log.v(TAG, "requestCode:" + requestCode + ", resultCode:" + resultCode);
        }
    }

    public void setNewAddress(String receiver) {
        TextView v = rootView.findViewById(R.id.et_payment_address);
        v.setText(AppUtil.convertToBitcoinCash(receiver));
        AppUtil.setReceivingAddress(activity, receiver);
    }

    public void validateThenSetNewAddress(String address) {
        //Is this a valid xpub, legacy, or cashaddr?
        if (AppUtil.isValidAddress(address)) {
            //If it's not an xpub, we can assume it's a BCH address since the address is valid from the previous if statement.
            if (!FormatsUtil.getInstance().isValidXpub(address)) {
                //legacy or cashaddr logic.
                this.validateCashaddrOrLegacyAddress(address);
            } else {
                //xpub logic
                this.saveXpubAsDestinationAddress(address);
                this.beginSyncingXpubWallet();
            }
        } else {
            //If it is not valid, then display to the user that they did not enter a valid xpub, or legacy/cashaddr address.
            ToastCustom.makeText(activity, activity.getString(R.string.unrecognized_xpub), ToastCustom.LENGTH_SHORT, ToastCustom.TYPE_ERROR);
        }
    }

    private void validateCashaddrOrLegacyAddress(String address) {
        if (AddressUtil.isValidCashAddr(address)) {
            String cashAddrPrefix = BitcoinCashAddressFormatter.MAIN_NET_PREFIX + ":";
            if (!address.startsWith(cashAddrPrefix))
                address = cashAddrPrefix + address;
        }
        setNewAddress(address);
    }

    private void saveXpubAsDestinationAddress(String xpub) {
        setNewAddress(xpub);
    }

    private void beginSyncingXpubWallet() {
        // When a merchant sets an xpub as their address in the settings,
        // sync the wallet up to the freshest address so users won't be sending to older addresses.
        // We do this by polling Bitcoin.com's REST API until we find a fresh address.
        new Thread() {
            @Override
            public void run() {
                AppUtil util = AppUtil.get();
                try {
                    boolean synced = util.getWallet(activity).syncXpub();
                    if (synced) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastCustom.makeText(activity, activity.getString(R.string.synced_xpub), ToastCustom.LENGTH_SHORT, ToastCustom.TYPE_OK);
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.e(TAG, "", e);
                }
            }
        }.start();
        ToastCustom.makeText(activity, activity.getString(R.string.syncing_xpub), ToastCustom.LENGTH_SHORT, ToastCustom.TYPE_GENERAL);
    }

    @Override
    public boolean canFragmentBeDiscardedWhenInBackground() {
        return AppUtil.isReceivingAddressAvailable(activity) && !this.isScanning;
    }
}
