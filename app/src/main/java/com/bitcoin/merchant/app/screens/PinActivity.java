package com.bitcoin.merchant.app.screens;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.bitcoin.merchant.app.MainActivity;
import com.bitcoin.merchant.app.R;
import com.bitcoin.merchant.app.util.AppUtil;
import com.bitcoin.merchant.app.util.OSUtil;
import com.bitcoin.merchant.app.util.PrefsUtil;
import com.bitcoin.merchant.app.util.ToastCustom;

import java.util.Timer;
import java.util.TimerTask;

public class PinActivity extends Activity {
    String userEnteredPIN = "";
    String userEnteredPINConfirm;
    private TextView titleView;
    private TextView[] pinBoxArray;
    private boolean doCreate ;

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
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.getBoolean("create")) {
                doCreate = true;
            }
        }
        titleView = findViewById(R.id.titleBox);
        if (doCreate) {
            titleView.setText(R.string.create_pin);
        } else {
            titleView.setText(R.string.enter_pin);
        }
        pinBoxArray = new TextView[]{
                findViewById(R.id.pinBox0),
                findViewById(R.id.pinBox1),
                findViewById(R.id.pinBox2),
                findViewById(R.id.pinBox3)
        };
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(!doCreate) {
                finish();
            } else {
                if(!isPinMissing(this)) {
                    /*
                    If PIN is already set, and we are in doCreate, then we can assume they are changing their PIN.
                    Since they are attempting to change their PIN, we can return to SettingsActivity.
                     */
                    Intent intent = new Intent(PinActivity.this, SettingsActivity.class);
                    this.startActivity(intent);
                    //Then we finish this activity.
                    this.finish();
                } else {
                    //Allow the user to exit app even when creating PIN on first launch. This will simply return to home screen.
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_HOME);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    this.startActivity(intent);
                }
            }
            return true;
        }

        return false;
    }

    public void padClicked(String num) {
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
        PrefsUtil.getInstance(PinActivity.this).setValue(PrefsUtil.MERCHANT_KEY_PIN, hashed);
        PrefsUtil.getInstance(PinActivity.this).setValue(PrefsUtil.MERCHANT_KEY_ACCOUNT_INDEX, 0);
        //To get the change PIN activity to return to the SettingsActivity, we create a new SettingsActivity intent
        Intent intent = new Intent(PinActivity.this, SettingsActivity.class);
        this.startActivity(intent);
        //Then we finish the PinActivity
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
            delayAction(new Runnable() {
                @Override
                public void run() {
                    ToastCustom.makeText(PinActivity.this, getString(R.string.pin_code_enter_error), ToastCustom.LENGTH_SHORT, ToastCustom.TYPE_ERROR);
                    clearPinBoxes();
                    userEnteredPIN = "";
                    userEnteredPINConfirm = null;
                }
            }, 750);
        }
    }

    private void createNewPin() {
        // End of Create -  Change to Confirm
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
                PinActivity.this.runOnUiThread(action);
                timer.cancel();
            }
        }, delay);
    }

    private void pinCreationError() {
        //End of Confirm - Pin Mismatch
        ToastCustom.makeText(PinActivity.this, getString(R.string.pin_code_create_error), ToastCustom.LENGTH_SHORT, ToastCustom.TYPE_ERROR);
        clearPinBoxes();
        userEnteredPIN = "";
        userEnteredPINConfirm = null;
        titleView.setText(R.string.create_pin);
    }

    public void cancelClicked() {
        clearPinBoxes();
        userEnteredPIN = "";
    }

    private void clearPinBoxes() {
        if (userEnteredPIN.length() > 0) {
            for (TextView aPinBoxArray : pinBoxArray) {
                aPinBoxArray.setBackground(null);//reset pin buttons blank
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (AppUtil.isReceivingAddressAvailable(this) && !doCreate)
            goToCashRegisterScreenWhenInBackground();
    }

    private void goToCashRegisterScreenWhenInBackground() {
        this.onBackPressed();
    }

    public void clickPinButton(View view) {
        Button viewButton = (Button) view;
        if (view.getId() != R.id.buttonDeleteBack)
            padClicked(viewButton.getText().toString());
        else
            cancelClicked();
    }
}
