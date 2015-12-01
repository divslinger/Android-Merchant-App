package info.blockchain.merchant.tabsswipe;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.Locale;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Button;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.Typeface;
import android.net.Uri;
//import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.android.Contents;
import com.google.zxing.client.android.encode.QRCodeEncoder;

import com.google.bitcoin.uri.BitcoinURI;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import info.blockchain.api.ReceivePayments;
import info.blockchain.merchant.CurrencyExchange;
import info.blockchain.merchant.db.DBController;
import info.blockchain.merchant.R;
import info.blockchain.util.BitcoinAddressCheck;
import info.blockchain.util.TypefaceUtil;

public class PaymentFragment extends Fragment   {

	private View rootView = null;
	private ImageView imageView = null;
	private ProgressBar progressBar = null;
	private EditText posInput = null;
	private EditText posMessage = null;
	private ImageButton imageConfirm = null;
    private TextView tvCurrency = null;
    private TextView tvCurrencySymbol = null;
    private TextView tvSendingAddress = null;
    private TextWatcher watcher = null;
    private SharedPreferences prefs = null;
    private SharedPreferences.Editor editor = null;
    
    private String strCurrency = null;
    private String strLabel = null;
    private String strMessage = null;
    private String strBTCReceivingAddress = null;
    private String input_address = null;
    private ContentValues dbVals = null;

    private boolean doBTC = false;
    private boolean doContinue = true;

    private Typeface btc_font = null;
    private Typeface default_font = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        editor = prefs.edit();

        rootView = inflater.inflate(R.layout.fragment_payment, container, false);
                
        doBTC = prefs.getBoolean("use_btc", false);

        imageView = (ImageView)rootView.findViewById(R.id.qr);
        imageView.setImageResource(android.R.color.transparent);
        imageView.setVisibility(View.GONE);
        imageView.setOnLongClickListener(new View.OnLongClickListener() {
        	  public boolean onLongClick(View view) {
//      			Toast.makeText(PaymentFragment.this.getActivity(), "Address copied:" + input_address, Toast.LENGTH_LONG).show();
      			
      			android.content.ClipboardManager clipboard = (android.content.ClipboardManager)getActivity().getSystemService(android.content.Context.CLIPBOARD_SERVICE);
      		    android.content.ClipData clip = android.content.ClipData.newPlainText("Send address", input_address);
      		    clipboard.setPrimaryClip(clip);
      			
            	String strFileName = getActivity().getCacheDir() + File.separator + "qr.png";
            	File file = new File(strFileName);
            	file.setReadable(true, false);
      			FileOutputStream fos = null;
      			try {
          			fos = new FileOutputStream(file);
      			}
      			catch(FileNotFoundException fnfe) {
      				;
      			}
      			
      			if(file != null && fos != null) {
          			Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
          	        bitmap.compress(CompressFormat.PNG, 0, fos);
          	        
          			try {
              			fos.close();
          			}
          			catch(IOException ioe) {
          				;
          			}

          	        Intent intent = new Intent(); 
          	        intent.setAction(Intent.ACTION_SEND); 
          	        intent.setType("*/*"); 
          	        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
          	        startActivity(Intent.createChooser(intent, "Send payment code"));
      			}
      	        
        	    return true;
        	  }
        	});
        
        progressBar = (ProgressBar)rootView.findViewById(R.id.progress);
        progressBar.setVisibility(View.GONE);

        tvSendingAddress = (TextView)rootView.findViewById(R.id.sending_address);
        tvCurrencySymbol = (TextView)rootView.findViewById(R.id.currencySymbol);
        default_font = tvCurrencySymbol.getTypeface();
        btc_font = TypefaceUtil.getInstance(getActivity()).getTypeface();
        tvCurrencySymbol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	// toggle currency input mode
                doBTC = doBTC ? false : true;
                editor.putBoolean("use_btc", doBTC);
                editor.commit();
                
            	// swap amounts
                String swap = tvCurrency.getText().subSequence(0, tvCurrency.getText().length() - 4).toString();
                tvCurrency.setText(posInput.getText().toString());
                posInput.setText(swap);

            	// pop-up soft keyboard
                InputMethodManager inputMethodManager = (InputMethodManager)getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                inputMethodManager.showSoftInput(posInput, InputMethodManager.SHOW_IMPLICIT);

            	// select entire EditText
                posInput.setSelection(0, posInput.getText().toString().length());

            	// display correct currency symbol
                setCurrencySymbol();

                //
                // in case someone toggle 15x fast ;)
                //
                System.gc();
            }
        });

        tvCurrency = (TextView)rootView.findViewById(R.id.curr_display);

        posInput = (EditText)rootView.findViewById(R.id.posInput);
        posInput.setText("0");
        watcher = new POSTextWatcher();
        posInput.addTextChangedListener(watcher);
    	if(!doBTC) {
        	posInput.setSelection(posInput.getText().length());
    	}
        posInput.setOnEditorActionListener(new OnEditorActionListener() {
		    @Override
		    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		        if (actionId == EditorInfo.IME_ACTION_DONE) {
                	if(BitcoinAddressCheck.isValid(strBTCReceivingAddress)) {
                        makeNewPayment();
                        doContinue = false;
                    	imageConfirm.setImageResource(R.drawable.clear_button);
                    	imageConfirm.setBackgroundResource(R.drawable.balance_bg);
                    	tvCurrencySymbol.setClickable(false);
                	}
		        }
		        return false;
		    }
		});

        posMessage = (EditText)rootView.findViewById(R.id.note);
		posMessage.addTextChangedListener(new TextWatcher()	{
	        public void afterTextChanged(Editable s) { ; }
	        public void beforeTextChanged(CharSequence s, int start, int count, int after)	{ ; }
	        public void onTextChanged(CharSequence s, int start, int before, int count)	{ ; }
	    }); 

        imageConfirm = (ImageButton)rootView.findViewById(R.id.confirm);
        imageConfirm.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
            	
            	if(doContinue) {
                    if(strCurrency != null && CurrencyExchange.getInstance(getActivity()).getCurrencyPrice(strCurrency) == 0.0) {
            			;
                    }
                    else if(BitcoinAddressCheck.isValid(strBTCReceivingAddress)) {
                        makeNewPayment();
                        doContinue = false;
                    	imageConfirm.setImageResource(R.drawable.clear_button);
                    	imageConfirm.setBackgroundResource(R.drawable.balance_bg);
                    	tvCurrencySymbol.setClickable(false);
                	}
                    else {
                    	;
                    }
            	}
            	else {
                	initValues();
            	}

            }
        });

        initValues();

        return rootView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if(isVisibleToUser) {

        	initValues();

        }
        else {
        	;
        }
    }

    @Override
    public void onResume() {
    	super.onResume();

    	initValues();

    }
    
    private Bitmap generateQRCode(String uri) {

        Bitmap bitmap = null;
        int qrCodeDimension = 380;

        QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(uri, null, Contents.Type.TEXT, BarcodeFormat.QR_CODE.toString(), qrCodeDimension);

    	try {
            bitmap = qrCodeEncoder.encodeAsBitmap();
        } catch (WriterException e) {
            e.printStackTrace();
        }
    	
    	return bitmap;
    }

    private String generateURI() {

    	long longValue = setAmount();
        
        EditText posNote = (EditText)rootView.findViewById(R.id.note);
        strMessage = posNote.getText().toString();
        
        dbVals = new ContentValues();
        dbVals.put("amt", longValue);
        dbVals.put("iad", input_address);
        dbVals.put("cfm", -1);
        dbVals.put("msg", strMessage);
        return BitcoinURI.convertToBitcoinURI(input_address, BigInteger.valueOf(longValue), strLabel, strMessage);
    }

    /** POSTextWatcher: handles input digit by digit and processed DEL (backspace) key
	 * 
	 */
    private class POSTextWatcher implements TextWatcher {

        public void afterTextChanged(Editable arg0) {
        	
            if(posInput.getText().toString() != null && !posInput.getText().toString().equals("0") && strCurrency != null && CurrencyExchange.getInstance(getActivity()).getCurrencyPrice(strCurrency) == 0.0) {
    			Toast.makeText(PaymentFragment.this.getActivity(), R.string.no_exchange_rate, Toast.LENGTH_LONG).show();
            }

        	if(setAmount() > 0L) {
        		imageConfirm.setBackgroundResource(R.drawable.continue_bg);
        		imageConfirm.setClickable(true);
        	}
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) { ; }

        public void onTextChanged(CharSequence s, int start, int before, int count) { ; }

    }
    
    private void initValues() {
    	
    	CurrencyExchange.getInstance(getActivity());

        if(prefs != null) {
        	strLabel = prefs.getString("receiving_name", "");
        	strBTCReceivingAddress = prefs.getString("receiving_address", "");
            strCurrency = prefs.getString("currency", "USD");
            if(strCurrency.equals("ZZZ")) {
                strCurrency = prefs.getString("ocurrency", "USD");
            }
        }

        if(tvCurrency != null) {
        	tvCurrency.setText("0 " + ((!doBTC) ? " BTC" : (" " + strCurrency)));
        }
        
        setCurrencySymbol();
        
        if(tvCurrencySymbol != null) {
        	tvCurrencySymbol.setClickable(true);
        }

        if(tvSendingAddress != null) {
            tvSendingAddress.setText("");
            tvSendingAddress.setVisibility(View.INVISIBLE);
        }

        if(posMessage != null) {
        	posMessage.setText("");
        }

        if(imageView != null) {
            imageView.setVisibility(View.GONE);
        }

        if(progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

        if(imageConfirm != null) {
            doContinue = true;
        	imageConfirm.setImageResource(R.drawable.continue_button);
    		imageConfirm.setBackgroundResource(R.drawable.balance_bg);
    		imageConfirm.setClickable(false);
        }
        
        if(posInput != null) {
        	posInput.setText("0");
        	posInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        	posInput.requestFocus();
            posInput.setSelection(0, posInput.getText().toString().length());
            InputMethodManager inputMethodManager = (InputMethodManager)  getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.showSoftInput(posInput, InputMethodManager.SHOW_IMPLICIT);
        }

    }

    private void makeNewPayment() {

        imageView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        tvSendingAddress.setText(R.string.generating_qr);

    	final ReceivePayments receive_payments = new ReceivePayments(strBTCReceivingAddress);
    	
//    	Log.d("makeNewPayments", receive_payments.getUrl());
    	
    	AsyncHttpClient client = new AsyncHttpClient();
        client.get(receive_payments.getUrl(), new AsyncHttpResponseHandler() {

        	@Override
            public void onSuccess(String response) {

//                Log.d("Receive payments API", response);

                receive_payments.setData(response);
                receive_payments.parse();
                input_address = receive_payments.getInputAddress();
                
//                Log.d("Receive payments API", input_address);

        		Bitmap bm = generateQRCode(generateURI());
                progressBar.setVisibility(View.GONE);
                imageView.setVisibility(View.VISIBLE);
                imageView.setImageBitmap(bm);
                tvSendingAddress.setText(input_address);

            	posInput.setInputType(0);

        		// get timestamp (Unix time)
        		dbVals.put("ts", System.currentTimeMillis() / 1000);
        		// get fiat
        		String fiat_amount = null;
        		if(doBTC) {
        			String fiat = (String)tvCurrency.getText();
        			int idx = fiat.indexOf(" ");
        			if(idx != -1) {
        				fiat_amount = getFiatCurrencySymbol() + fiat.substring(0, idx); 
        			}
        			else {
        				fiat_amount = getFiatCurrencySymbol() + "0"; 
        			}
        		}
        		else {
        			fiat_amount = getFiatCurrencySymbol() + posInput.getEditableText().toString();
        		}
        		dbVals.put("famt", fiat_amount);

        		// write to DB
        		DBController pdb = new DBController(getActivity());
        		pdb.insertPayment(dbVals.getAsLong("ts"), dbVals.getAsString("iad"), dbVals.getAsLong("amt"), dbVals.getAsString("famt"), dbVals.getAsInteger("cfm"), dbVals.getAsString("msg"));
        		pdb.close();
            }

            @Override
            public void onFailure(Throwable arg0) {
				Toast.makeText(PaymentFragment.this.getActivity(), arg0.toString(), Toast.LENGTH_LONG).show();
            }

        });

    }

    private long setAmount() {
    	long longValue = 0L;
    	double amount = 0.0;

        if(strCurrency != null && CurrencyExchange.getInstance(getActivity()).getCurrencyPrice(strCurrency) == 0.0) {
        	return longValue;
        }

    	if(doBTC) {
    		try {
            	amount = Double.valueOf(posInput.getText().toString().length() == 0 ? "0" : posInput.getText().toString());
    		}
    		catch(NumberFormatException e) {
    			amount = 0.0;
    		}
    	}
    	else {
    		amount = xlatFiat2BTC(posInput.getText().toString().length() == 0 ? "0" : posInput.getText().toString());
    	}

    	double value = Math.round(amount * 100000000.0);
    	longValue = (Double.valueOf(value)).longValue();
        if(!doBTC) {
        	tvCurrency.setText(BitcoinURI.bitcoinValueToPlainString(BigInteger.valueOf(longValue)) + " BTC");
        }
        else {
        	DecimalFormat df2 = new DecimalFormat("######0.00");
        	String amt = df2.format(xlatBTC2Fiat(posInput.getText().toString()));
        	if(amt.equals("0.00")) {
        		amt = "0";
        	}
        	tvCurrency.setText(amt + " " + strCurrency);
        }
        
        return longValue;
    }

    private void setCurrencySymbol() {
    	
//    	Log.d("setCurrencySymbol()", "strCurrency ==" + strCurrency);
//    	Log.d("CurrencyExchange get symbol", CurrencyExchange.getInstance(getActivity()).getCurrencySymbol(strCurrency) == null ? "null" : CurrencyExchange.getInstance(getActivity()).getCurrencySymbol(strCurrency));

        if(tvCurrencySymbol != null) {
            if(doBTC) {
            	// use 'Ƀ' as soon as it is available in an Android font
            	tvCurrencySymbol.setTypeface(btc_font);
            	tvCurrencySymbol.setText(R.string.bitcoin_currency_symbol);
            }
            else if(CurrencyExchange.getInstance(getActivity()).getCurrencySymbol(strCurrency) != null) {
            	tvCurrencySymbol.setTypeface(default_font);
            	tvCurrencySymbol.setText(CurrencyExchange.getInstance(getActivity()).getCurrencySymbol(strCurrency).substring(0, 1));
            }
            else {
            	tvCurrencySymbol.setTypeface(default_font);
            	tvCurrencySymbol.setText("$");
            }
        }
    }

    private String getCurrencySymbol() {
        if(doBTC) {
        	return getActivity().getResources().getString(R.string.bitcoin_currency_symbol);
        }
        else if(CurrencyExchange.getInstance(getActivity()).getCurrencySymbol(strCurrency) != null) {
        	return CurrencyExchange.getInstance(getActivity()).getCurrencySymbol(strCurrency).substring(0, 1);
        }
        else {
        	return "$";
        }
    }

    private String getFiatCurrencySymbol() {
        if(CurrencyExchange.getInstance(getActivity()).getCurrencySymbol(strCurrency) != null) {
        	return CurrencyExchange.getInstance(getActivity()).getCurrencySymbol(strCurrency).substring(0, 1);
        }
        else if(CurrencyExchange.getInstance(getActivity()).getCurrencySymbol(strCurrency) != null) {
        	return CurrencyExchange.getInstance(getActivity()).getCurrencySymbol(strCurrency).substring(0, 1);
        }
        else {
        	return "$";
        }
    }

    private double xlatBTC2Fiat(String strAmount) {

    	Locale locale = new Locale("en", "US");
        Locale.setDefault(locale);

    	if(strAmount.length() < 1) {
    		strAmount = "0";
    	}
    	if(strAmount.equals(".")) {
    		strAmount = "0";
    	}

    	double amount = 0;

		if(CurrencyExchange.getInstance(getActivity()).getCurrencyPrice(strCurrency) != 0.0) {
        	amount = Double.valueOf(strAmount) * CurrencyExchange.getInstance(getActivity()).getCurrencyPrice(strCurrency);
		}
		else {
        	amount = Double.valueOf(strAmount) * CurrencyExchange.getInstance(getActivity()).getCurrencyPrice("USD");
		}
		
		return amount;
    }
    
    private double xlatFiat2BTC(String strAmount) {

    	Locale locale = new Locale("en", "US");
        Locale.setDefault(locale);

    	if(strAmount.length() < 1) {
    		strAmount = "0";
    	}
    	if(strAmount.equals(".")) {
    		strAmount = "0";
    	}

    	double amount = 0;

		if(CurrencyExchange.getInstance(getActivity()).getCurrencyPrice(strCurrency) != 0.0) {
        	amount = Double.valueOf(strAmount) / CurrencyExchange.getInstance(getActivity()).getCurrencyPrice(strCurrency);
		}
		else {
        	amount = Double.valueOf(strAmount) / CurrencyExchange.getInstance(getActivity()).getCurrencyPrice("USD");
		}
		
		return amount;
    }

    private void confirmPurchase() {

    	final ReceivePayments receive_payments = new ReceivePayments(strBTCReceivingAddress);
    	
    	AsyncHttpClient client = new AsyncHttpClient();
        client.get(receive_payments.getUrl(), new AsyncHttpResponseHandler() {

        	@Override
            public void onSuccess(String response) {

                receive_payments.setData(response);
                receive_payments.parse();
                input_address = receive_payments.getInputAddress();

                imageView.setImageBitmap(generateQRCode(generateURI()));
            }

            @Override
            public void onFailure(Throwable arg0) {
				Toast.makeText(PaymentFragment.this.getActivity(), arg0.toString(), Toast.LENGTH_LONG).show();
            }

        });

    }

}
