package com.bitcoin.merchant.app.screens.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.EditText;
import android.widget.TextView;

import com.bitcoin.merchant.app.R;
import com.bitcoin.merchant.app.screens.SettingsFragment;
import com.bitcoin.merchant.app.util.AppUtil;
import com.bitcoin.merchant.app.util.PaymentTarget;

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
                .setPositiveButton(R.string.paste, (dialog, whichButton) -> {
                    enterAddressUsingInputField();
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.scan, (dialog, whichButton) -> {
                    dialog.dismiss();
                    settingsController.requestToOpenCamera();
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
                .setPositiveButton(R.string.prompt_ok, (dialog, whichButton) -> {
                    settingsController.validateThenSetReceiverKey(etReceiver.getText().toString().trim());
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.prompt_ko, (dialog, whichButton) -> dialog.dismiss());
        if (! ctx.isFinishing()) {
            builder.show();
        }
    }

    private void enterAddressUsingInputField() {
        if (ENTERING_ADDRESS_BYPASSED) {
            settingsController.setAndDisplayPaymentTarget(PaymentTarget.Companion.parse("1MxRuANd5CmHWcveTwQaAJ36sStEQ5QM5k"));
        } else {
            final EditText editText = new EditText(ctx);
            editText.setSingleLine(true);
            editText.setText(AppUtil.getPaymentTarget(ctx).getBchAddress());
            showDialogToEnterAddress(editText);
        }
    }
}
