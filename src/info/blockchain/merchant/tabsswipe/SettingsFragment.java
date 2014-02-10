package info.blockchain.merchant.tabsswipe;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;
import android.widget.CompoundButton;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.AdapterView; 
import android.widget.AdapterView.OnItemSelectedListener; 
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.widget.TextView;

import com.dm.zbar.android.scanner.ZBarConstants;
import com.dm.zbar.android.scanner.ZBarScannerActivity;
import net.sourceforge.zbar.Symbol;

import info.blockchain.merchant.R;

public class SettingsFragment extends Fragment implements CompoundButton.OnCheckedChangeListener  {
	
	private Spinner spCurrencies = null;
	private Switch sPushNotifications = null;
//	private Switch sEmailReceipts = null;
	private String[] currencies = null;
	private AutoCompleteTextView receivingNameView = null;
	private AutoCompleteTextView receivingAddressView = null;
//	private AutoCompleteTextView emailAddressView = null;
	
	private SharedPreferences prefs = null;
    private SharedPreferences.Editor editor = null;
	
	private static int ZBAR_SCANNER_REQUEST = 2026;
 
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        editor = prefs.edit();

        receivingNameView = (AutoCompleteTextView) rootView.findViewById(R.id.receive_coins_name);
		receivingNameView.addTextChangedListener(new TextWatcher(){
	        public void afterTextChanged(Editable s) {
	            editor.putString("receiving_name", s.toString());
	            editor.commit();
	        }
	        public void beforeTextChanged(CharSequence s, int start, int count, int after)	{ ; }
	        public void onTextChanged(CharSequence s, int start, int before, int count)	{ ; }
	    }); 

        receivingAddressView = (AutoCompleteTextView) rootView.findViewById(R.id.receive_coins_receiving_address);
		receivingAddressView.setOnTouchListener(new RightDrawableOnTouchListener(receivingAddressView) {
			@Override
			public boolean onDrawableTouch(final MotionEvent event) {

        		Toast.makeText(getActivity(), "Show QR reader", Toast.LENGTH_SHORT).show();

        		Intent intent = new Intent(getActivity(), ZBarScannerActivity.class);
        		intent.putExtra(ZBarConstants.SCAN_MODES, new int[]{Symbol.QRCODE});
        		startActivityForResult(intent, ZBAR_SCANNER_REQUEST);
        		
				return true;
			}
		});
		receivingAddressView.addTextChangedListener(new TextWatcher(){
	        public void afterTextChanged(Editable s) {
	            editor.putString("receiving_address", s.toString());
	            editor.commit();
	        }
	        public void beforeTextChanged(CharSequence s, int start, int count, int after)	{ ; }
	        public void onTextChanged(CharSequence s, int start, int before, int count)	{ ; }
	    }); 

		/*
        emailAddressView = (AutoCompleteTextView) rootView.findViewById(R.id.receive_coins_email_address);
        emailAddressView.addTextChangedListener(new TextWatcher(){
	        public void afterTextChanged(Editable s) {
	            editor.putString("email_address", s.toString());
	            editor.commit();
	        }
	        public void beforeTextChanged(CharSequence s, int start, int count, int after){}
	        public void onTextChanged(CharSequence s, int start, int before, int count){}
	    });
	    */ 

		sPushNotifications = (Switch)rootView.findViewById(R.id.push_notifications);
        if (sPushNotifications != null) {
        	sPushNotifications.setOnCheckedChangeListener(this);
        }

        /*
        sEmailReceipts = (Switch)rootView.findViewById(R.id.email_receipts);
        if (sEmailReceipts != null) {
        	sEmailReceipts.setOnCheckedChangeListener(this);
        }
        */

        spCurrencies = (Spinner)rootView.findViewById(R.id.receive_coins_default_currency);
        ArrayAdapter<CharSequence> spAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.currencies, android.R.layout.simple_spinner_item);
        	spAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); 
        	spCurrencies.setAdapter(spAdapter);

        	spCurrencies.setOnItemSelectedListener(new OnItemSelectedListener()	{
		    	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3)	{
		        	int index = arg0.getSelectedItemPosition();
		        	currencies = getResources().getStringArray(R.array.currencies);
//		        	Toast.makeText(getActivity(), "Currency selected:" + currencies[index].substring(currencies[index].length() - 3), Toast.LENGTH_SHORT).show();
		            editor.putString("currency", currencies[index].substring(currencies[index].length() - 3));
		            editor.commit();
		        }

		        public void onNothingSelected(AdapterView<?> arg0) { 
		        }
        	});
        
        initValues();

        return rootView;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    	if(buttonView.getId() == R.id.push_notifications) {
//    		Toast.makeText(getActivity(), "Monitored switch 'push' " + (isChecked ? "on" : "off"), Toast.LENGTH_SHORT).show();
            editor.putBoolean("push_notifications", isChecked);
            editor.commit();
    	}
    	/*
    	else if(buttonView.getId() == R.id.email_receipts) {
//    		Toast.makeText(getActivity(), "Monitored switch 'email' " + (isChecked ? "on" : "off"), Toast.LENGTH_SHORT).show();
            editor.putBoolean("email_receipts", isChecked);
            editor.commit();
    	}
    	*/
    	else {
    		;
    	}
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if(isVisibleToUser) {
    		Toast.makeText(getActivity(), "Settings fragment visible", Toast.LENGTH_SHORT).show();
    		if(prefs != null) {
        		initValues();
    		}
        }
        else {
        	;
        }
    }

	private abstract class RightDrawableOnTouchListener implements OnTouchListener {
		Drawable drawable;
//		private int fuzz = 40;
		private int fuzz = 100;

		public RightDrawableOnTouchListener(TextView view) {
			super();
			final Drawable[] drawables = view.getCompoundDrawables();
			if (drawables != null && drawables.length == 4)
				this.drawable = drawables[2];
//    		Toast.makeText(getActivity(), "RightDrawableOnTouchListener created", Toast.LENGTH_SHORT).show();
		}

		@Override
		public boolean onTouch(final View v, final MotionEvent event) {
    		Toast.makeText(getActivity(), "RightDrawableOnTouchListener onTouch fired", Toast.LENGTH_SHORT).show();

			if (event.getAction() == MotionEvent.ACTION_DOWN && drawable != null) {
//				System.out.println("event " + event);

				final int x = (int) event.getX();
				final int y = (int) event.getY();

				final Rect bounds = drawable.getBounds();
//				System.out.println("bounds " + bounds);
	    		Toast.makeText(getActivity(), "bounds " + bounds, Toast.LENGTH_SHORT).show();

	    		Toast.makeText(getActivity(), "x=" + x, Toast.LENGTH_SHORT).show();
	    		Toast.makeText(getActivity(), "" + (v.getRight() - bounds.width() - fuzz), Toast.LENGTH_SHORT).show();
				if (x >= (v.getRight() - bounds.width() - fuzz) && x <= (v.getRight() - v.getPaddingRight() + fuzz)
						&& y >= (v.getPaddingTop() - fuzz) && y <= (v.getHeight() - v.getPaddingBottom()) + fuzz) {
					return onDrawableTouch(event);
				}
			}
			return false;
		}

		public abstract boolean onDrawableTouch(final MotionEvent event);

	}

    private void initValues() {
        receivingNameView.setText(prefs.getString("receiving_name", ""));
        receivingAddressView.setText(prefs.getString("receiving_address", ""));
//        emailAddressView.setText(prefs.getString("email_address", ""));
        sPushNotifications.setChecked(prefs.getBoolean("push_notifications", false));
//        sEmailReceipts.setChecked(prefs.getBoolean("email_receipts", false));
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
