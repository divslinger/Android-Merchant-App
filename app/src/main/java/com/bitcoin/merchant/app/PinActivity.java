package com.bitcoin.merchant.app;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import info.blockchain.merchant.util.OSUtil;
import info.blockchain.merchant.util.PrefsUtil;
import info.blockchain.merchant.util.ToastCustom;
//import android.util.Log;

public class PinActivity extends Activity {
    String userEnteredPIN = "";
    String userEnteredPINConfirm = null;
    private TextView titleView = null;
    private TextView[] pinBoxArray = null;
    private boolean doCreate = false;
    private TextView explanationView;

    public static boolean isPinMissing(Context ctx) {
        String pin = PrefsUtil.getInstance(ctx).getValue(PrefsUtil.MERCHANT_KEY_PIN, "");
        return pin.equals("");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_pin);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setTitle(R.string.action_pincode);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.getBoolean("create")) {
                doCreate = true;
            }
        }
        titleView = findViewById(R.id.titleBox);
        explanationView = findViewById(R.id.enter_pin_explanation);
        if (doCreate) {
            titleView.setText(R.string.create_pin);
            explanationView.setText(R.string.please_create_pin);
        } else {
            titleView.setText(R.string.enter_pin);
            explanationView.setText("");
        }
        pinBoxArray = new TextView[] {
            findViewById(R.id.pinBox0),
            findViewById(R.id.pinBox1),
            findViewById(R.id.pinBox2),
            findViewById(R.id.pinBox3)
        };
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && !doCreate) {
            finish();
            return true;
        }
        return false;
    }

    public void padClicked(View view) {
        if (userEnteredPIN.length() == pinBoxArray.length) {
            return;
        }
        // Append tapped #
        userEnteredPIN = userEnteredPIN + view.getTag().toString().substring(0, 1);
        pinBoxArray[userEnteredPIN.length() - 1].setBackgroundResource(R.drawable.ic_launcher);
        // Perform appropriate action if PIN_LENGTH has been reached
        if (userEnteredPIN.length() == pinBoxArray.length) {
            if (!doCreate) {
                validatePin();
            } else if (userEnteredPINConfirm == null) {
                createNewPin();
            } else if (userEnteredPINConfirm.equals(userEnteredPIN)) {
                confirmNewPin();
            } else {
                pinCreationError();
            }
        }
    }

    private void confirmNewPin() {
        // End of Confirm - Pin is confirmed
        String hashed = OSUtil.getSha256(userEnteredPIN);
//      ToastCustom.makeText(PinActivity.this, hashed, ToastCustom.LENGTH_SHORT, ToastCustom.TYPE_OK);
        PrefsUtil.getInstance(PinActivity.this).setValue(PrefsUtil.MERCHANT_KEY_PIN, hashed);
        PrefsUtil.getInstance(PinActivity.this).setValue(PrefsUtil.MERCHANT_KEY_ACCOUNT_INDEX, 0);
        setResult(RESULT_OK);
        finish();
    }

    private void validatePin() {
        // Validate
        String hashed = OSUtil.getSha256(userEnteredPIN);
        String stored = PrefsUtil.getInstance(PinActivity.this).getValue(PrefsUtil.MERCHANT_KEY_PIN, "");
        if (stored.equals(hashed)) {
            setResult(RESULT_OK);
            finish();
        } else {
            ToastCustom.makeText(PinActivity.this, getString(R.string.pin_code_enter_error), ToastCustom.LENGTH_SHORT, ToastCustom.TYPE_ERROR);
            clearPinBoxes();
            userEnteredPIN = "";
            userEnteredPINConfirm = null;
        }
    }

    private void createNewPin() {
        // End of Create -  Change to Confirm
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                PinActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        titleView.setText(R.string.confirm_pin);
                        explanationView.setText("");
                        clearPinBoxes();
                        userEnteredPINConfirm = userEnteredPIN;
                        userEnteredPIN = "";
                    }
                });
            }
        }, 200);
    }

    private void pinCreationError() {
        //End of Confirm - Pin Mismatch
        ToastCustom.makeText(PinActivity.this, getString(R.string.pin_code_create_error), ToastCustom.LENGTH_SHORT, ToastCustom.TYPE_ERROR);
        clearPinBoxes();
        userEnteredPIN = "";
        userEnteredPINConfirm = null;
        titleView.setText(R.string.create_pin);
    }

    public void cancelClicked(View view) {
        clearPinBoxes();
        userEnteredPIN = "";
    }

    private void clearPinBoxes() {
        if (userEnteredPIN.length() > 0) {
            for (TextView aPinBoxArray : pinBoxArray) {
                aPinBoxArray.setBackgroundResource(R.drawable.rounded_view_blue_white_border);//reset pin buttons blank
            }
        }
    }
}
