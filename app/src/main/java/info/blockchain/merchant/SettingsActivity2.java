package info.blockchain.merchant;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.dm.zbar.android.scanner.ZBarConstants;

import info.blockchain.merchant.util.AppUtil;
import info.blockchain.merchant.util.PrefsUtil;
import info.blockchain.merchant.util.ToastCustom;
import info.blockchain.wallet.util.FormatsUtil;

public class SettingsActivity2 extends PreferenceActivity	{

    private static int ZBAR_SCANNER_REQUEST = 2026;

    private Preference newPref = null;

    /** Called when the activity is first created. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            setTheme(android.R.style.Theme_Holo);
        }

        super.onCreate(savedInstanceState);
        setTitle(R.string.app_name);

        boolean status = false;
        Bundle extras = getIntent().getExtras();
        if(extras != null && extras.containsKey("status"))	{
            status = extras.getBoolean("status");
        }
        addPreferencesFromResource(status ? R.xml.settings_with_receive : R.xml.settings_no_receive);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        newPref = (Preference) findPreference("address");

        doPopUp(status);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(resultCode == Activity.RESULT_OK && requestCode == ZBAR_SCANNER_REQUEST)	{

            String scanResult = data.getStringExtra(ZBarConstants.SCAN_RESULT);

            if(scanResult.startsWith("bitcoin:"))    {
                scanResult = scanResult.substring(8);
            }

            if(FormatsUtil.getInstance().isValidXpub(scanResult) || FormatsUtil.getInstance().isValidBitcoinAddress(scanResult))    {
                PrefsUtil.getInstance(SettingsActivity2.this).setValue(PrefsUtil.MERCHANT_KEY_MERCHANT_RECEIVER, scanResult);
                newPref.setSummary(scanResult);
            }
            else{
                ToastCustom.makeText(this, getString(R.string.unrecognized_xpub), ToastCustom.LENGTH_SHORT, ToastCustom.TYPE_ERROR);
            }

        }
    }

    private void doPopUp(boolean hasReceiver)  {

        newPref.setSummary(PrefsUtil.getInstance(SettingsActivity2.this).getValue(PrefsUtil.MERCHANT_KEY_MERCHANT_RECEIVER, ""));

        if(!hasReceiver)    {

            newPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {

                    final TextView tvReceiverHelp = new EditText(SettingsActivity2.this);
                    tvReceiverHelp.setText(SettingsActivity2.this.getText(R.string.options_add_payment_address_text));

                    new AlertDialog.Builder(SettingsActivity2.this)
                            .setTitle(R.string.options_add_payment_address)
                            .setView(tvReceiverHelp)
                            .setCancelable(false)
                            .setPositiveButton(R.string.paste, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {

                                    final EditText etReceiver = new EditText(SettingsActivity2.this);
                                    etReceiver.setSingleLine(true);
                                    etReceiver.setText(PrefsUtil.getInstance(SettingsActivity2.this).getValue(PrefsUtil.MERCHANT_KEY_MERCHANT_RECEIVER, ""));

                                    new AlertDialog.Builder(SettingsActivity2.this)
                                            .setTitle(R.string.options_add_payment_address)
                                            .setView(etReceiver)
                                            .setCancelable(false)
                                            .setPositiveButton(R.string.prompt_ok, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int whichButton) {

                                                    String receiver = etReceiver.getText().toString().trim();
                                                    if(receiver != null && receiver.length() > 0 && (FormatsUtil.getInstance().isValidBitcoinAddress(receiver) || FormatsUtil.getInstance().isValidXpub(receiver))) {
                                                        PrefsUtil.getInstance(SettingsActivity2.this).setValue(PrefsUtil.MERCHANT_KEY_MERCHANT_RECEIVER, receiver);
                                                        newPref.setSummary(receiver);
                                                    }

                                                }

                                            }).setNegativeButton(R.string.prompt_ko, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {

                                            ;

                                        }
                                    }).show();

                                }

                            }).setNegativeButton(R.string.scan, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {

                            ;

                        }
                    }).show();

                    return true;

                }
            });

        }
        else    {

            final Preference forgetPref = (Preference) findPreference("forget");
            forgetPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {

                    final TextView tvForgetHelp = new EditText(SettingsActivity2.this);
                    tvForgetHelp.setText(SettingsActivity2.this.getText(R.string.options_forget_payment_address_text));

                    new AlertDialog.Builder(SettingsActivity2.this)
                            .setTitle(R.string.options_forget_payment_address)
                            .setView(tvForgetHelp)
                            .setCancelable(false)
                            .setPositiveButton(R.string.prompt_ok, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int whichButton) {

                                    PrefsUtil.getInstance(SettingsActivity2.this).setValue(PrefsUtil.MERCHANT_KEY_MERCHANT_RECEIVER, "");

                                    Intent intent = new Intent(SettingsActivity2.this, SettingsActivity2.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);

                                }

                            }).setNegativeButton(R.string.prompt_ko, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {

                            ;

                        }
                    }).show();

                    return true;
                }
            });

        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if(keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent(SettingsActivity2.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        return false;
    }

}
