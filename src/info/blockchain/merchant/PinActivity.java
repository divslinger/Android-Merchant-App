package info.blockchain.merchant;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;
import android.text.InputType;
import android.text.method.NumberKeyListener;
import android.util.Log;

import java.nio.charset.Charset;

import com.google.common.hash.Hashing;

public class PinActivity extends Activity	{

//	private AutoCompleteTextView pinView1 = null;
//	private AutoCompleteTextView pinView2 = null;
	private EditText pinView1 = null;
	private EditText pinView2 = null;
	private Button bOK = null;
	private Button bCancel = null;
	
	private boolean doCreate = false;
	
	private SharedPreferences prefs = null;
    private SharedPreferences.Editor editor = null;
	
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_pin);
	    
	    setTitle(R.string.action_pincode);

        Bundle extras = getIntent().getExtras();
        if(extras != null)	{
            if(extras.getBoolean("create"))	{
            	doCreate = true;
            }
        }

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        editor = prefs.edit();

        pinView1 = (EditText)findViewById(R.id.pin1);
        pinView2 = (EditText)findViewById(R.id.pin2);
        
        if(!doCreate)	{
        	pinView2.setVisibility(View.GONE);
        	pinView1.setHint(R.string.pin_code_prompt3);
        }

        bOK = (Button)findViewById(R.id.confirm);
        bOK.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
            	
            	if(doCreate) {
                	String pin1 = pinView1.getEditableText().toString();
                	String pin2 = pinView2.getEditableText().toString();
                	
                	if(pin1 != null && pin2 != null && pin1.length() == 4 && pin2.length() == 4 && pin1.equals(pin2)) {
                		
                		String hashed = Hashing.sha256().hashString(pin1, Charset.forName("UTF8")).toString();
                        Toast.makeText(PinActivity.this, hashed, Toast.LENGTH_LONG).show();
        	            editor.putString("pin", hashed);
        	            editor.commit();
        	            
                    	setResult(RESULT_OK);
        	            finish();
                	}
                	else {
                		// error message here
                	}
            	}
            	else {
                	String pin1 = pinView1.getEditableText().toString();
                	Log.d("PinActivity", "pin1 == " + pin1);
            		String hashed = Hashing.sha256().hashString(pin1, Charset.forName("UTF8")).toString();
                	Log.d("PinActivity", "hashed == " + hashed);
                	String stored = prefs.getString("pin", "");
                	Log.d("PinActivity", "stored == " + stored);
                	if(stored.equals(hashed)) {
                    	setResult(RESULT_OK);
                    	finish();
                	}
                	else {
                		// ko
                	}
            	}
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
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		


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

}
