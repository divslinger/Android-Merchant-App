package info.blockchain.merchant;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
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

import info.blockchain.merchant.util.BitcoinAddressCheck;
import info.blockchain.merchant.util.PrefsUtil;

//import android.util.Log;

public class SettingsActivity extends Activity	{

	private Spinner spCurrencies = null;
	private CheckBox sPushNotifications = null;
	private String[] currencies = null;
	private EditText receivingAddressView = null;
	private EditText receivingNameView = null;

	private TextView tvOK = null;
	private TextView tvCancel = null;
    private ImageView ivQr = null;
    private ArrayAdapter<CharSequence> spAdapter = null;

	private static int ZBAR_SCANNER_REQUEST = 2026;
	
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

	    setContentView(R.layout.activity_settings);
	    
        initValues();
    }

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if(resultCode == Activity.RESULT_OK && requestCode == ZBAR_SCANNER_REQUEST)	{

			String strResult = BitcoinAddressCheck.clean(data.getStringExtra(ZBarConstants.SCAN_RESULT));
//        	Log.d("Scan result", strResult);
			if(BitcoinAddressCheck.isValid(BitcoinAddressCheck.clean(strResult))) {
	            receivingAddressView.setText(strResult);
			}
			else {
				Toast.makeText(this, R.string.invalid_btc_address, Toast.LENGTH_LONG).show();
			}

        }
	}

    private void initValues() {

        spCurrencies = (Spinner)findViewById(R.id.receive_coins_default_currency);
        receivingAddressView = (EditText)findViewById(R.id.receive_coins_receiving_address);
        ivQr = (ImageView)findViewById(R.id.iv_QR);
        receivingNameView = (EditText)findViewById(R.id.receive_coins_name);
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

                String strReceivingAddress = receivingAddressView.getEditableText().toString();
                String strReceivingName = receivingNameView.getEditableText().toString();
                boolean push_notifications = sPushNotifications.isChecked();
                int currency = spCurrencies.getSelectedItemPosition();
                currencies = getResources().getStringArray(R.array.currencies);

                if (BitcoinAddressCheck.isValid(BitcoinAddressCheck.clean(strReceivingAddress))) {

                    PrefsUtil.getInstance(SettingsActivity.this).setValue(PrefsUtil.MERCHANT_KEY_RECEIVING_ADDRESS, strReceivingAddress);
                    PrefsUtil.getInstance(SettingsActivity.this).setValue(PrefsUtil.MERCHANT_KEY_RECEIVING_NAME, strReceivingName);
                    PrefsUtil.getInstance(SettingsActivity.this).setValue(PrefsUtil.MERCHANT_KEY_PUSH_NOTIFS, push_notifications);

                    PrefsUtil.getInstance(SettingsActivity.this).setValue(PrefsUtil.MERCHANT_KEY_CURRENCY, currencies[currency].substring(currencies[currency].length() - 3));

                    finish();
                } else {
                    Toast.makeText(SettingsActivity.this, R.string.invalid_btc_address, Toast.LENGTH_LONG).show();
                }

            }
        });

        tvCancel.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        receivingNameView.setText(PrefsUtil.getInstance(SettingsActivity.this).getValue(PrefsUtil.MERCHANT_KEY_RECEIVING_NAME, ""));
        receivingAddressView.setText(PrefsUtil.getInstance(SettingsActivity.this).getValue(PrefsUtil.MERCHANT_KEY_RECEIVING_ADDRESS, ""));
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

}
