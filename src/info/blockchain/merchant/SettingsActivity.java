package info.blockchain.merchant;

import java.nio.charset.Charset;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.EditText;
import android.widget.AdapterView; 
import android.widget.AdapterView.OnItemSelectedListener; 
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Toast;
import android.util.Log;

import com.dm.zbar.android.scanner.ZBarConstants;
import com.dm.zbar.android.scanner.ZBarScannerActivity;
import com.google.common.hash.Hashing;

import net.sourceforge.zbar.Symbol;

import info.blockchain.util.BitcoinAddressCheck;

public class SettingsActivity extends Activity	{

	private Spinner spCurrencies = null;
	private Switch sPushNotifications = null;
	private String[] currencies = null;
//	private AutoCompleteTextView receivingNameView = null;
//	private AutoCompleteTextView receivingAddressView = null;
	private EditText receivingAddressView = null;
	private EditText receivingNameView = null;
	private ImageButton imageScan = null;
	private Button bOK = null;
	private Button bCancel = null;
	
	private SharedPreferences prefs = null;
    private SharedPreferences.Editor editor = null;
	
	private static int ZBAR_SCANNER_REQUEST = 2026;
 
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_settings);
	    
	    setTitle(R.string.action_settings_title);
 
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        editor = prefs.edit();

        receivingAddressView = (EditText)findViewById(R.id.receive_coins_receiving_address);
		
        imageScan = (ImageButton)findViewById(R.id.scan);
        imageScan.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
        		Toast.makeText(SettingsActivity.this, "Show QR reader", Toast.LENGTH_SHORT).show();
        		Intent intent = new Intent(SettingsActivity.this, ZBarScannerActivity.class);
        		intent.putExtra(ZBarConstants.SCAN_MODES, new int[]{ Symbol.QRCODE } );
        		startActivityForResult(intent, ZBAR_SCANNER_REQUEST);
            }
        });

        receivingNameView = (EditText)findViewById(R.id.receive_coins_name);

		sPushNotifications = (Switch)findViewById(R.id.push_notifications);

        spCurrencies = (Spinner)findViewById(R.id.receive_coins_default_currency);
        ArrayAdapter<CharSequence> spAdapter = ArrayAdapter.createFromResource(this, R.array.currencies, android.R.layout.simple_spinner_item);
        	spAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); 
        	spCurrencies.setAdapter(spAdapter);

        	spCurrencies.setOnItemSelectedListener(new OnItemSelectedListener()	{
		    	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3)	{ ; }
		        public void onNothingSelected(AdapterView<?> arg0) { ; }
        	});

        bOK = (Button)findViewById(R.id.confirm);
        bOK.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
            	
            	String strReceivingAddress = receivingAddressView.getEditableText().toString();
            	String strReceivingName = receivingNameView.getEditableText().toString();
            	boolean push_notifications = sPushNotifications.isChecked();
            	int currency = spCurrencies.getSelectedItemPosition();
	        	currencies = getResources().getStringArray(R.array.currencies);
	        	
	        	if(BitcoinAddressCheck.isValid(BitcoinAddressCheck.clean(strReceivingAddress))) {
		            editor.putString("receiving_address", strReceivingAddress);
		            editor.putString("receiving_name", strReceivingName);
		            editor.putBoolean("push_notifications", push_notifications);
		            editor.putString("currency", currencies[currency].substring(currencies[currency].length() - 3));
		            editor.commit();
		            
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
			// Scan result is available by making a call to data.getStringExtra(ZBarConstants.SCAN_RESULT)
			// Type of the scan result is available by making a call to data.getStringExtra(ZBarConstants.SCAN_RESULT_TYPE)
			Toast.makeText(this, "Scan Result = " + data.getStringExtra(ZBarConstants.SCAN_RESULT), Toast.LENGTH_SHORT).show();
//			Toast.makeText(this, "Scan Result Type = " + data.getIntExtra(ZBarConstants.SCAN_RESULT_TYPE, 0), Toast.LENGTH_SHORT).show();
			// The value of type indicates one of the symbols listed in Advanced Options below.
			
			String strResult = BitcoinAddressCheck.clean(data.getStringExtra(ZBarConstants.SCAN_RESULT));
			if(BitcoinAddressCheck.isValid(strResult)) {
	            receivingAddressView.setText(strResult);
			}
			else {
				Toast.makeText(this, R.string.invalid_btc_address, Toast.LENGTH_LONG).show();
			}

        } else if(resultCode == Activity.RESULT_CANCELED && requestCode == ZBAR_SCANNER_REQUEST) {
            Toast.makeText(this, "Camera unavailable", Toast.LENGTH_SHORT).show();
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
        receivingNameView.setText(prefs.getString("receiving_name", ""));
        receivingAddressView.setText(prefs.getString("receiving_address", ""));
        sPushNotifications.setChecked(prefs.getBoolean("push_notifications", false));
    	currencies = getResources().getStringArray(R.array.currencies);
    	String strCurrency = prefs.getString("currency", "USD");
    	for(int i = 0; i < currencies.length; i++) {
    		if(currencies[i].endsWith(strCurrency)) {
    	        spCurrencies.setSelection(i);
    	        break;
    		}
    	}
    }

}
