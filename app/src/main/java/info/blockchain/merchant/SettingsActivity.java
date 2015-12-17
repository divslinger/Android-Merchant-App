package info.blockchain.merchant;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.dm.zbar.android.scanner.ZBarConstants;
import com.dm.zbar.android.scanner.ZBarScannerActivity;

import net.sourceforge.zbar.Symbol;

import info.blockchain.merchant.util.PrefsUtil;
import info.blockchain.merchant.util.ToastCustom;
import info.blockchain.wallet.util.FormatsUtil;

//import android.util.Log;

public class SettingsActivity extends AppCompatActivity {

	private Spinner spCurrencies = null;
	private CheckBox sPushNotifications = null;
	private String[] currencies = null;
	private EditText merchantReceiverView = null;
	private EditText merchantNameView = null;

	private TextView tvOK = null;
	private TextView tvCancel = null;
    private ImageView ivQr = null;
    private ArrayAdapter<CharSequence> spAdapter = null;

	private static int ZBAR_SCANNER_REQUEST = 2026;

    private static int PIN_ACTIVITY 		= 2;
    private static int RESET_PIN_ACTIVITY 	= 3;
	
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	    setContentView(R.layout.activity_settings);

        initToolbar();
        initValues();

        }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    private void initToolbar(){
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle(getResources().getString(R.string.action_settings_title));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if(resultCode == Activity.RESULT_OK && requestCode == ZBAR_SCANNER_REQUEST)	{

            String scanResult = data.getStringExtra(ZBarConstants.SCAN_RESULT);
            if(scanResult.startsWith("bitcoin:"))    {
                scanResult = scanResult.substring(8);
            }
            if(FormatsUtil.getInstance().isValidXpub(scanResult) || FormatsUtil.getInstance().isValidBitcoinAddress(scanResult)){
                merchantReceiverView.setText(scanResult);
            }
            else{
                ToastCustom.makeText(this, getString(R.string.unrecognized_xpub), ToastCustom.LENGTH_SHORT, ToastCustom.TYPE_ERROR);
            }
        }
	}

    private void initValues() {

        spCurrencies = (Spinner)findViewById(R.id.receive_coins_default_currency);
        merchantReceiverView = (EditText)findViewById(R.id.et_merchant_receiver);
        ivQr = (ImageView)findViewById(R.id.iv_QR);
        merchantNameView = (EditText)findViewById(R.id.et_merchant_name);
        tvOK = (TextView)findViewById(R.id.confirm);
        tvCancel = (TextView)findViewById(R.id.cancel);

        ivQr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, ZBarScannerActivity.class);
                intent.putExtra(ZBarConstants.SCAN_MODES, new int[]{Symbol.QRCODE});
                startActivityForResult(intent, ZBAR_SCANNER_REQUEST);
            }
        });

        sPushNotifications = (CheckBox)findViewById(R.id.push_notifications);
        sPushNotifications.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PrefsUtil.getInstance(SettingsActivity.this).setValue(PrefsUtil.MERCHANT_KEY_PUSH_NOTIFS, isChecked);
            }
        });

//        AccountAdapter dataAdapter = new AccountAdapter(getActivity(), R.layout.spinner_item, _accounts);
        spAdapter = ArrayAdapter.createFromResource(this, R.array.currencies, R.layout.spinner_item);
        spAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCurrencies.setAdapter(spAdapter);

        tvOK.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {

                String strMerchantReceiver = merchantReceiverView.getEditableText().toString().trim();
                String strMerchantName = merchantNameView.getEditableText().toString().trim();
                boolean push_notifications = sPushNotifications.isChecked();
                int currency = spCurrencies.getSelectedItemPosition();
                currencies = getResources().getStringArray(R.array.currencies);

                if(FormatsUtil.getInstance().isValidBitcoinAddress(strMerchantReceiver) || FormatsUtil.getInstance().isValidXpub(strMerchantReceiver)) {

                    PrefsUtil.getInstance(SettingsActivity.this).setValue(PrefsUtil.MERCHANT_KEY_MERCHANT_RECEIVER, strMerchantReceiver);
                    PrefsUtil.getInstance(SettingsActivity.this).setValue(PrefsUtil.MERCHANT_KEY_MERCHANT_NAME, strMerchantName);
                    PrefsUtil.getInstance(SettingsActivity.this).setValue(PrefsUtil.MERCHANT_KEY_PUSH_NOTIFS, push_notifications);

                    PrefsUtil.getInstance(SettingsActivity.this).setValue(PrefsUtil.MERCHANT_KEY_CURRENCY, currencies[currency].substring(currencies[currency].length() - 3));

                    finish();
                } else {
                    Toast.makeText(SettingsActivity.this, R.string.unrecognized_xpub, Toast.LENGTH_LONG).show();
                }

            }
        });

        tvCancel.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        merchantNameView.setText(PrefsUtil.getInstance(SettingsActivity.this).getValue(PrefsUtil.MERCHANT_KEY_MERCHANT_NAME, ""));
        merchantReceiverView.setText(PrefsUtil.getInstance(SettingsActivity.this).getValue(PrefsUtil.MERCHANT_KEY_MERCHANT_RECEIVER, ""));
        sPushNotifications.setChecked(PrefsUtil.getInstance(SettingsActivity.this).getValue(PrefsUtil.MERCHANT_KEY_PUSH_NOTIFS, false));

    	currencies = getResources().getStringArray(R.array.currencies);
    	String strCurrency = PrefsUtil.getInstance(SettingsActivity.this).getValue(PrefsUtil.MERCHANT_KEY_CURRENCY, "USD");

    	int sel = -1;
    	for(int i = 0; i < currencies.length; i++) {
    		if(currencies[i].endsWith(strCurrency)) {
    	        spCurrencies.setSelection(i);
    	        sel = i;
    	        break;
    		}
    	}
    	if(sel == -1) {
	        spCurrencies.setSelection(currencies.length - 1);
    	}

    }

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
	    Rect dialogBounds = new Rect();
	    getWindow().getDecorView().getHitRect(dialogBounds);

	    if(!dialogBounds.contains((int) event.getX(), (int) event.getY()) && event.getAction() == MotionEvent.ACTION_DOWN) {
	    	return false;
	    }
	    else {
		    return super.dispatchTouchEvent(event);
	    }
	}

    public void changePinClicked(View view) {
        Intent intent = new Intent(this, PinActivity.class);
        intent.putExtra("create", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivityForResult(intent, PIN_ACTIVITY);
    }
}
