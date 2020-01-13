package com.bitcoin.merchant.app.screens;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bitcoin.merchant.app.R;
import com.bitcoin.merchant.app.screens.features.ToolbarAwareFragment;
import com.bitcoin.merchant.app.util.AppUtil;
import com.bitcoin.merchant.app.util.PrefsUtil;
import com.bitcoin.merchant.app.util.ToastCustom;

import java.util.Timer;
import java.util.TimerTask;

public class PinCodeFragment extends ToolbarAwareFragment {
    public static final String EXTRA_DO_CREATE = "doCreate";
    private String userEnteredPIN = "";
    private String userEnteredPINConfirm;
    private TextView titleView;
    private TextView[] pinBoxArray;
    private boolean doCreate;

    public static boolean isPinMissing(Context ctx) {
        String pin = PrefsUtil.getInstance(ctx).getValue(PrefsUtil.MERCHANT_KEY_PIN, "");
        return pin.equals("");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_pin, container, false);
        Bundle args = getArguments();
        if (args != null) {
            doCreate = args.getBoolean(EXTRA_DO_CREATE);
        }
        titleView = rootView.findViewById(R.id.titleBox);
        if (doCreate) {
            titleView.setText(R.string.create_pin);
        } else {
            titleView.setText(R.string.enter_pin);
        }
        pinBoxArray = new TextView[]{
                rootView.findViewById(R.id.pinBox0),
                rootView.findViewById(R.id.pinBox1),
                rootView.findViewById(R.id.pinBox2),
                rootView.findViewById(R.id.pinBox3)
        };
        View.OnClickListener digitListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                digitPressed(((TextView)v).getText().toString());
            }
        };
        rootView.findViewById(R.id.button0).setOnClickListener(digitListener);
        rootView.findViewById(R.id.button1).setOnClickListener(digitListener);
        rootView.findViewById(R.id.button2).setOnClickListener(digitListener);
        rootView.findViewById(R.id.button3).setOnClickListener(digitListener);
        rootView.findViewById(R.id.button4).setOnClickListener(digitListener);
        rootView.findViewById(R.id.button5).setOnClickListener(digitListener);
        rootView.findViewById(R.id.button6).setOnClickListener(digitListener);
        rootView.findViewById(R.id.button7).setOnClickListener(digitListener);
        rootView.findViewById(R.id.button8).setOnClickListener(digitListener);
        rootView.findViewById(R.id.button9).setOnClickListener(digitListener);
        Button buttonDeleteBack = rootView.findViewById(R.id.buttonDeleteBack);
        buttonDeleteBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deletePressed();
            }
        });
        setToolbarVisible(false);
        return rootView;
    }

    public void digitPressed(String num) {
        if (userEnteredPIN.length() == pinBoxArray.length) {
            return;
        }
        // Append tapped #
        userEnteredPIN = userEnteredPIN + num;
        pinBoxArray[userEnteredPIN.length() - 1].setBackgroundResource(R.drawable.passcode_blob);
        // Perform appropriate action if PIN_LENGTH has been reached
        if (userEnteredPIN.length() == pinBoxArray.length) {
            if (!doCreate) {
                validatePin();
            } else if (userEnteredPINConfirm == null) {
                newPinHasBeenEnteredOnce();
            } else if (userEnteredPINConfirm.equals(userEnteredPIN)) {
                newPinHasBeenConfirmed();
            } else {
                pinCodesMismatchedDuringCreation();
            }
        }
    }

    private void newPinHasBeenConfirmed() {
        String hashed = userEnteredPIN;
        PrefsUtil.getInstance(getContext()).setValue(PrefsUtil.MERCHANT_KEY_PIN, hashed);
        PrefsUtil.getInstance(getContext()).setValue(PrefsUtil.MERCHANT_KEY_ACCOUNT_INDEX, 0);
        getNav().navigate(R.id.nav_to_settings_screen);
    }

    private void validatePin() {
        String hashed = userEnteredPIN;
        String stored = PrefsUtil.getInstance(getContext()).getValue(PrefsUtil.MERCHANT_KEY_PIN, "");
        if (stored.equals(hashed)) {
            getNav().navigate(R.id.nav_to_settings_screen);
        } else {
            delayAction(new Runnable() {
                @Override
                public void run() {
                    ToastCustom.makeText(getContext(), getString(R.string.pin_code_enter_error), ToastCustom.LENGTH_SHORT, ToastCustom.TYPE_ERROR);
                    clearPinBoxes();
                    userEnteredPIN = "";
                    userEnteredPINConfirm = null;
                }
            }, 750);
        }
    }

    private void newPinHasBeenEnteredOnce() {
        // request confirmation
        Runnable action = new Runnable() {
            @Override
            public void run() {
                titleView.setText(R.string.confirm_pin);
                clearPinBoxes();
                userEnteredPINConfirm = userEnteredPIN;
                userEnteredPIN = "";
            }
        };
        delayAction(action, 200);
    }

    private void delayAction(final Runnable action, int delay) {
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                activity.runOnUiThread(action);
                timer.cancel();
            }
        }, delay);
    }

    private void pinCodesMismatchedDuringCreation() {
        ToastCustom.makeText(activity, getString(R.string.pin_code_create_error), ToastCustom.LENGTH_SHORT, ToastCustom.TYPE_ERROR);
        clearPinBoxes();
        userEnteredPIN = "";
        userEnteredPINConfirm = null;
        titleView.setText(R.string.create_pin);
    }

    public void deletePressed() {
        clearPinBoxes();
        userEnteredPIN = "";
    }

    private void clearPinBoxes() {
        if (userEnteredPIN.length() > 0) {
            for (TextView aPinBoxArray : pinBoxArray) {
                aPinBoxArray.setBackground(null); //reset pin buttons blank
            }
        }
    }

    @Override
    public boolean canFragmentBeDiscardedWhenInBackground() {
        return AppUtil.isReceivingAddressAvailable(getContext()) && !doCreate;
    }
}
