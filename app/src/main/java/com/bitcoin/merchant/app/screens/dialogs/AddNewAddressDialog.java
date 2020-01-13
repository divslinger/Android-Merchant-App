package com.bitcoin.merchant.app.screens.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.EditText;
import android.widget.TextView;

import com.bitcoin.merchant.app.R;
import com.bitcoin.merchant.app.screens.SettingsFragment;
import com.bitcoin.merchant.app.util.AppUtil;

public class AddNewAddressDialog {
    private static final boolean ENTERING_ADDRESS_BYPASSED = false;
    private final Activity ctx;
    private final SettingsFragment settingsController;

    public AddNewAddressDialog(SettingsFragment f) {
        this.settingsController = f;
        this.ctx = f.activity;
    }

    public void show() {
        final TextView tvReceiverHelp = new TextView(ctx);
        tvReceiverHelp.setText(ctx.getString(R.string.options_explain_payment_address));
        tvReceiverHelp.setPadding(50, 10, 50, 10);
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx)
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
                        settingsController.requestToOpenCamera();
                    }
                });
        if (! ctx.isFinishing()) {
            builder.show();
        }
    }


    private void showDialogToEnterAddress(final EditText etReceiver) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx)
                .setTitle(R.string.options_add_payment_address)
                .setView(etReceiver)
                .setCancelable(false)
                .setPositiveButton(R.string.prompt_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        settingsController.validateThenSetNewAddress(etReceiver.getText().toString().trim());
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.prompt_ko, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                });
        if (! ctx.isFinishing()) {
            builder.show();
        }
    }

    private void enterAddressUsingInputField() {
        if (ENTERING_ADDRESS_BYPASSED) {
            settingsController.setNewAddress("1MxRuANd5CmHWcveTwQaAJ36sStEQ5QM5k");
        } else {
            final EditText etReceiver = new EditText(ctx);
            etReceiver.setSingleLine(true);
            etReceiver.setText(AppUtil.convertToBitcoinCash(AppUtil.getReceivingAddress(ctx)));
            showDialogToEnterAddress(etReceiver);
        }
    }
}
