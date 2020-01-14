package com.bitcoin.merchant.app.screens.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bitcoin.merchant.app.R;
import com.bitcoin.merchant.app.currency.CountryCurrency;
import com.bitcoin.merchant.app.currency.CurrencyHelper;
import com.bitcoin.merchant.app.screens.PaymentInputFragment;
import com.bitcoin.merchant.app.screens.SettingsFragment;
import com.bitcoin.merchant.app.util.PrefsUtil;

import java.util.List;

public class CurrencySelectionDialog {
    private final Activity ctx;
    private final SettingsFragment settingsController;

    public CurrencySelectionDialog(SettingsFragment f) {
        this.settingsController = f;
        this.ctx = f.activity;
    }

    public boolean show() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        final List<CountryCurrency> currencies = CurrencyHelper.getInstance(ctx).getCountryCurrencies();
        ListAdapter adapter = new ArrayAdapterWithIcon(ctx, currencies);
        builder.setAdapter(adapter, (dialog, which) -> {
            CountryCurrency cc = currencies.get(which);
            String locale = cc.countryLocales.getFirstSupportedLocale();
            if (locale == null) {
                Toast.makeText(ctx, ctx.getResources().getString(R.string.not_supported), Toast.LENGTH_SHORT).show();
                return;
            }
            dialog.dismiss();
            save(cc, locale);
        });
        builder.setCancelable(true);
        AlertDialog alert = builder.create();
        alert.setCanceledOnTouchOutside(true);
        if (! ctx.isFinishing()) {
            alert.show();
        }
        return true;
    }

    private void save(CountryCurrency cc, String locale) {
        PrefsUtil prefsUtil = PrefsUtil.getInstance(ctx);
        prefsUtil.setValue(PrefsUtil.MERCHANT_KEY_CURRENCY, cc.currencyCode);
        prefsUtil.setValue(PrefsUtil.MERCHANT_KEY_COUNTRY, cc.countryLocales.countryCode);
        prefsUtil.setValue(PrefsUtil.MERCHANT_KEY_LOCALE, locale);
        Intent intent = new Intent(PaymentInputFragment.ACTION_INTENT_RESET_AMOUNT);
        LocalBroadcastManager.getInstance(ctx).sendBroadcast(intent);
        settingsController.setCurrencySummary(cc);
    }

    private class ArrayAdapterWithIcon extends ArrayAdapter<CountryCurrency> {
        private List<CountryCurrency> cc;

        public ArrayAdapterWithIcon(Context context, List<CountryCurrency> items) {
            super(context, R.layout.select_currency_dialog_item, items);
            this.cc = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            TextView textView = view.findViewById(R.id.select_currency_text);
            textView.setCompoundDrawablesRelativeWithIntrinsicBounds(cc.get(position).image, 0, 0, 0);
            float dimension = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getContext().getResources().getDisplayMetrics());
            textView.setCompoundDrawablePadding((int) dimension);
            return view;
        }
    }
}
