package info.blockchain.merchant;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.graphics.Rect;
//import android.util.Log;

import java.nio.charset.Charset;

import com.google.common.hash.Hashing;

public class PinActivity extends Activity	{

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
//                        Toast.makeText(PinActivity.this, hashed, Toast.LENGTH_LONG).show();
        	            editor.putString("pin", hashed);
        	            editor.commit();
        	            
                    	setResult(RESULT_OK);
        	            finish();
                	}
                	else {
    					Toast.makeText(PinActivity.this, R.string.pin_code_create_error, Toast.LENGTH_LONG).show();
                	}
            	}
            	else {
                	String pin1 = pinView1.getEditableText().toString();
            		String hashed = Hashing.sha256().hashString(pin1, Charset.forName("UTF8")).toString();
                	String stored = prefs.getString("pin", "");
                	if(stored.equals(hashed)) {
                    	setResult(RESULT_OK);
                    	finish();
                	}
                	else {
    					Toast.makeText(PinActivity.this, R.string.pin_code_enter_error, Toast.LENGTH_LONG).show();
                	}
            	}
            }
        });

        bCancel = (Button)findViewById(R.id.cancel);
        if(doCreate)	{
        	bCancel.setVisibility(View.GONE);
        }
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
