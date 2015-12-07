package info.blockchain.merchant;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.Switch;
import android.widget.EditText;
import android.widget.AdapterView; 
import android.widget.AdapterView.OnItemSelectedListener; 
import android.widget.ArrayAdapter;
import android.widget.Toast;
//import android.util.Log;

import com.dm.zbar.android.scanner.ZBarConstants;
import com.dm.zbar.android.scanner.ZBarScannerActivity;

import net.sourceforge.zbar.Symbol;

import java.util.Set;

import info.blockchain.merchant.util.BitcoinAddressCheck;
import info.blockchain.merchant.util.PrefsUtil;

public class SettingsActivity extends Activity	{

	private SelectedSpinner spCurrencies = null;
	private Switch sPushNotifications = null;
	private String[] currencies = null;
	private EditText receivingAddressView = null;
	private EditText receivingNameView = null;
	private Button bOK = null;
	private Button bCancel = null;
    private ArrayAdapter<CharSequence> spAdapter = null;

	private static int ZBAR_SCANNER_REQUEST = 2026;
	
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_settings);
	    
	    setTitle(R.string.action_settings_title);
	    
        receivingAddressView = (EditText)findViewById(R.id.receive_coins_receiving_address);
        receivingAddressView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                final int DRAWABLE_RIGHT = 2;

                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(event.getRawX() >= (receivingAddressView.getRight() - receivingAddressView.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
//                		Toast.makeText(SettingsActivity.this, "Show QR reader", Toast.LENGTH_SHORT).show();
                		Intent intent = new Intent(SettingsActivity.this, ZBarScannerActivity.class);
                		intent.putExtra(ZBarConstants.SCAN_MODES, new int[]{ Symbol.QRCODE } );
                		startActivityForResult(intent, ZBAR_SCANNER_REQUEST);
                    }
                }

                return false;
            }
        });
		
        receivingNameView = (EditText)findViewById(R.id.receive_coins_name);

		sPushNotifications = (Switch)findViewById(R.id.push_notifications);

        spCurrencies = (SelectedSpinner)findViewById(R.id.receive_coins_default_currency);
        spAdapter = ArrayAdapter.createFromResource(this, R.array.currencies, android.R.layout.simple_spinner_item);
    	spAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); 
    	spCurrencies.setAdapter(spAdapter);

        bOK = (Button)findViewById(R.id.confirm);
        bOK.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
            	
            	String strReceivingAddress = receivingAddressView.getEditableText().toString();
            	String strReceivingName = receivingNameView.getEditableText().toString();
            	boolean push_notifications = sPushNotifications.isChecked();
            	int currency = spCurrencies.getSelectedItemPosition();
	        	currencies = getResources().getStringArray(R.array.currencies);

	        	if(BitcoinAddressCheck.isValid(BitcoinAddressCheck.clean(strReceivingAddress))) {

					PrefsUtil.getInstance(SettingsActivity.this).setValue(PrefsUtil.MERCHANT_KEY_RECEIVING_ADDRESS, strReceivingAddress);
					PrefsUtil.getInstance(SettingsActivity.this).setValue(PrefsUtil.MERCHANT_KEY_RECEIVING_NAME, strReceivingName);
					PrefsUtil.getInstance(SettingsActivity.this).setValue(PrefsUtil.MERCHANT_KEY_PUSH_NOTIFS, push_notifications);

					PrefsUtil.getInstance(SettingsActivity.this).setValue(PrefsUtil.MERCHANT_KEY_CURRENCY, currencies[currency].substring(currencies[currency].length() - 3));

	            	finish();
	        	}
	        	else {
					Toast.makeText(SettingsActivity.this, R.string.invalid_btc_address, Toast.LENGTH_LONG).show();
	        	}

            }
        });

        bCancel = (Button)findViewById(R.id.cancel);
        bCancel.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
            	finish();
            }
        });

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
        else {
        	;
        }

	}

	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) { 
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        else	{
        	;
        }

        return false;
    }

    private void initValues() {
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
