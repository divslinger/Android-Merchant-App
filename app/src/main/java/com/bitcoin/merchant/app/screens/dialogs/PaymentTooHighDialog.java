package com.bitcoin.merchant.app.screens.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.Gravity;
import android.widget.TextView;

import com.bitcoin.merchant.app.R;

public class PaymentTooHighDialog {
    private final Activity context;

    public PaymentTooHighDialog(Activity context) {
        this.context = context;
    }

    public void showOverpayment() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        TextView title = new TextView(context);
        title.setPadding(20, 60, 20, 20);
        title.setText(R.string.app_name);
        title.setGravity(Gravity.CENTER);
        title.setTextSize(20);
        builder.setCustomTitle(title);
        builder.setMessage(R.string.overpaid_amount).setCancelable(false);
        AlertDialog alert = builder.create();
        alert.setButton(AlertDialog.BUTTON_POSITIVE, context.getString(R.string.prompt_ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        if (! context.isFinishing()) {
            alert.show();
        }
    }
}
