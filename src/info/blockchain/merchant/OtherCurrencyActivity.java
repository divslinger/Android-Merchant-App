package info.blockchain.merchant;

import java.util.ArrayList;  
import java.util.HashMap;  
import java.util.SortedSet;  
import java.util.TreeSet;  

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
//import android.util.Log;

public class OtherCurrencyActivity extends Activity	{

	private Spinner spCurrencies = null;
	private String[] currencies = null;
	private ProgressBar progressBar = null;
	private Button bOK = null;
	private Button bCancel = null;
	private String strOtherCurrency = null;
	
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_other_currency);
	    
	    setTitle(R.string.action_settings_title);

        Bundle extras = getIntent().getExtras();
        if(extras != null)	{
        	strOtherCurrency = extras.getString("ocurrency");
        }
        if(strOtherCurrency == null)	{
        	strOtherCurrency = "";
        }
        
        Log.d("OtherCurrencyActivity", "" + strOtherCurrency);

        progressBar = (ProgressBar)findViewById(R.id.progress);
        progressBar.setVisibility(View.GONE);

        spCurrencies = (Spinner)findViewById(R.id.receive_coins_other_currency);
        spCurrencies.setVisibility(View.GONE);
    	currencies = getResources().getStringArray(R.array.currencies);
    	final ArrayList<String> currSymbols = new ArrayList<String>();
    	for(int i = 0; i < (currencies.length - 1); i++) {
    		currSymbols.add(currencies[i].substring(currencies[i].length() - 3));
    	}

        progressBar.setVisibility(View.VISIBLE);
        spCurrencies.setVisibility(View.GONE);
        
        if(OtherCurrencyExchange.getInstance(OtherCurrencyActivity.this).getCurrencyNames() != null && OtherCurrencyExchange.getInstance(OtherCurrencyActivity.this).getCurrencyPrices() != null) {
        	HashMap<String,String> otherCurrencyNames = OtherCurrencyExchange.getInstance(OtherCurrencyActivity.this).getCurrencyNames();
        	ArrayList<String> otherCurrencies = new ArrayList<String>();
        	
        	SortedSet<String> keys = new TreeSet<String>(otherCurrencyNames.keySet());
        	int sel = -1;
        	int i = 0;
        	for (String key : keys) { 
        	    otherCurrencies.add(otherCurrencyNames.get(key) + " - " + key);
        	    if(key.equals(strOtherCurrency)) {
        	    	sel = i;
        	    }
        	    ++i;
        	}
        	
            ArrayAdapter spArrayAdapter = new ArrayAdapter(OtherCurrencyActivity.this, android.R.layout.simple_spinner_dropdown_item, otherCurrencies);
            spCurrencies.setAdapter(spArrayAdapter);
	        spCurrencies.setSelection(sel == -1 ? 0 : sel);
	        spCurrencies.setVisibility(View.VISIBLE);
	        progressBar.setVisibility(View.GONE);
        }

        bOK = (Button)findViewById(R.id.confirm);
        bOK.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
            	String sel = (String)spCurrencies.getSelectedItem();
            	if(sel != null) {
                	setResult(RESULT_OK,(new Intent()).setAction(sel.substring(sel.length() - 3)));
            	}
            	else {
                	setResult(RESULT_OK);
            	}
            	finish();
            }
        });

        bCancel = (Button)findViewById(R.id.cancel);
        bCancel.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
            	finish();
            }
        });

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
