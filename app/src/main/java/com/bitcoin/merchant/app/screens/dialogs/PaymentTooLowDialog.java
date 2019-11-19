package com.bitcoin.merchant.app.screens.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;

import com.bitcoin.merchant.app.R;
import com.bitcoin.merchant.app.currency.CurrencyExchange;
import com.bitcoin.merchant.app.screens.PaymentInputFragment;
import com.bitcoin.merchant.app.screens.PaymentRequestActivity;
import com.bitcoin.merchant.app.util.AmountUtil;
import com.bitcoin.merchant.app.util.AppUtil;
import com.bitcoin.merchant.app.util.MonetaryUtil;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public class PaymentTooLowDialog {
    private final Activity context;

    public PaymentTooLowDialog(Activity context) {
        this.context = context;
    }

    public void showUnderpayment(final long paymentAmountInSatoshis, Long expectedAmountInSatoshis,
                                 final Runnable closingAction) {
        final double bchExpectedAmount = expectedAmountInSatoshis / 1e8;
        final double bchPaymentAmount = paymentAmountInSatoshis / 1e8;
        final double bchRemainder = bchExpectedAmount - bchPaymentAmount;
        Double currencyPrice = CurrencyExchange.getInstance(context).getCurrencyPrice(AppUtil.getCurrency(context));
        NumberFormat nf = NumberFormat.getInstance(Locale.getDefault());
        double fiatAmount;
        try {
            fiatAmount = nf.parse(MonetaryUtil.getInstance().getFiatDecimalFormat().format(bchRemainder * currencyPrice)).doubleValue();
        } catch (ParseException pe) {
            fiatAmount = 0.0;
        }
        final double _fiatRemainder = fiatAmount;
        StringBuilder sb = new StringBuilder();
        sb.append(context.getText(R.string.removed_by_bip70_insufficient_payment));
        sb.append("\n");
        sb.append(context.getText(R.string.removed_by_bip70_re_payment_requested));
        sb.append(" ");
        AmountUtil f = new AmountUtil(context);
        sb.append(f.formatBch(bchExpectedAmount));
        sb.append("\n");
        sb.append(context.getText(R.string.removed_by_bip70_re_payment_received));
        sb.append(" ");
        sb.append(f.formatBch(bchPaymentAmount));
        sb.append("\n");
        sb.append(context.getText(R.string.removed_by_bip70_re_payment_remainder));
        sb.append(" ");
        sb.append(f.formatBch(bchRemainder));
        sb.append("\n");
        sb.append(context.getText(R.string.removed_by_bip70_re_payment_remainder));
        sb.append(" ");
        sb.append(f.formatFiat(fiatAmount));
        sb.append("\n");
        sb.append(context.getText(R.string.removed_by_bip70_insufficient_payment_continue));
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AppTheme);
        builder.setTitle(R.string.app_name);
        builder.setMessage(sb.toString()).setCancelable(false);
        AlertDialog alert = builder.create();
        alert.setButton(AlertDialog.BUTTON_POSITIVE, context.getString(R.string.prompt_ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                closingAction.run();
                Intent intent = new Intent(context, PaymentRequestActivity.class);
                intent.putExtra(PaymentInputFragment.AMOUNT_PAYABLE_FIAT, _fiatRemainder);
                intent.putExtra(PaymentInputFragment.AMOUNT_PAYABLE_BTC, bchRemainder);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(intent);
            }
        });
        alert.setButton(AlertDialog.BUTTON_NEGATIVE, context.getString(R.string.prompt_ko), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                closingAction.run();
            }
        });
        if (! context.isFinishing()) {
            alert.show();
        }
    }
}
