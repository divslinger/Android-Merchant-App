package info.blockchain.merchant.tabsswipe;

import java.math.BigInteger;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.Switch;
import android.graphics.Bitmap;

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

public class PaymentFragment extends Fragment implements CompoundButton.OnCheckedChangeListener   {

	private View rootView = null;
	private ImageView imageView = null;
	private EditText posInput = null;
	private EditText posMessage = null;
	private Switch sUseBTC = null;
	private Button bClear = null;
	private Button bNew = null;
    private TextView tvBTC = null;
    private TextView tvCurrencySymbol = null;
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

	private CurrencyExchange ce = CurrencyExchange.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        editor = prefs.edit();

        rootView = inflater.inflate(R.layout.fragment_payment, container, false);

        doBTC = prefs.getBoolean("use_btc", false);

        imageView = (ImageView)rootView.findViewById(R.id.qr);
        imageView.setImageResource(android.R.color.transparent);;

		sUseBTC = (Switch)rootView.findViewById(R.id.use_btc);
        if (sUseBTC != null) {
        	sUseBTC.setOnCheckedChangeListener(this);
        }
        sUseBTC.setChecked(doBTC);

        tvCurrencySymbol = (TextView)rootView.findViewById(R.id.currencySymbol);
        tvBTC = (TextView)rootView.findViewById(R.id.btc_display);

        posInput = (EditText)rootView.findViewById(R.id.posInput);
        posInput.setText("0.00");
        watcher = new POSTextWatcher();
        posInput.addTextChangedListener(watcher);
    	if(!doBTC) {
        	// lock cursor to end of edit field if entering fiat amounts (fixed 2-place decimal)
        	posInput.setSelection(posInput.getText().length());
    	}

        posMessage = (EditText)rootView.findViewById(R.id.note);
		posMessage.addTextChangedListener(new TextWatcher(){
	        public void afterTextChanged(Editable s) { ; }
	        public void beforeTextChanged(CharSequence s, int start, int count, int after)	{ ; }
	        public void onTextChanged(CharSequence s, int start, int before, int count)	{ ; }
	    }); 

        bNew = (Button)rootView.findViewById(R.id.new_payment);
        bNew.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
            	if(bNew.getText().equals("Start Payment")) {
                	getNewAddress();
            	}
            	else if(bNew.getText().equals("Confirm Payment")) {
            		// get timestamp (Unix time)
            		dbVals.put("ts", System.currentTimeMillis() / 1000);
            		// write to DB
            		DBController pdb = new DBController(getActivity());
            		pdb.insertPayment(dbVals.getAsLong("ts"), dbVals.getAsString("iad"), dbVals.getAsLong("amt"), dbVals.getAsInteger("cfm"), dbVals.getAsString("msg"));
            		pdb.close();
            		//
//            		pdb.updateConfirmed("1TestAddressABABAB");
//            		pdb.deleteConfirmed();
            		//

                	bNew.setText(R.string.payment_new);
                    initValues();
            	}
            	else {
            		;
            	}
            }
        });

        bClear = (Button)rootView.findViewById(R.id.clear);
        bClear.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                initValues();
            }
        });

        initValues();

        return rootView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if(isVisibleToUser) {

        	ce = CurrencyExchange.getInstance();

        	initValues();

        	if(posInput != null && posMessage != null) {
            	posInput.setText("0.00");
            	posMessage.setText("");
            	posInput.setSelection(posInput.getText().length());
        	}

        }
        else {
        	;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    	if(buttonView.getId() == R.id.use_btc) {
            editor.putBoolean("use_btc", isChecked);
            editor.commit();
            doBTC = isChecked;
            initValues();
    	}
    	else {
    		;
    	}
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

    	double amount = 0.0;
    	
    	String strUSDEUR = ce.getUSDEUR();
    	String strUSDGBP = ce.getUSDGBP();
    	String strBitstamp = ce.getBitstampPrice();

    	if(doBTC) {
        	amount = Double.valueOf(posInput.getText().toString());
    	}
    	else {
    		//
    		// temporary kludge until we get https://blockchain.info/ticker up-to-date at which time we'll use 'info.blockchain.api.ExchangeRates'
    		//
    		if(strCurrency.equals("EUR")) {
    			// use Bitstamp USD converted to EUR
    	    	Toast.makeText(getActivity(), "Bitstamp=" + strBitstamp + "," + "USD/EUR=" + strUSDEUR, Toast.LENGTH_LONG).show();
            	amount = Double.valueOf(posInput.getText().toString()) / (Double.valueOf(strBitstamp) / Double.valueOf(strUSDEUR));
    		}
    		else if(strCurrency.equals("GBP")) {
    			// use Bitstamp USD converted to GBP
    	    	Toast.makeText(getActivity(), "Bitstamp=" + strBitstamp + "," + "USD/GBP=" + strUSDGBP, Toast.LENGTH_LONG).show();
            	amount = Double.valueOf(posInput.getText().toString()) / (Double.valueOf(strBitstamp) / Double.valueOf(strUSDGBP));
    		}
    		else {
    			// USD: use Bitstamp
    	    	Toast.makeText(getActivity(), "Bitstamp=" + strBitstamp, Toast.LENGTH_LONG).show();
            	amount = Double.valueOf(posInput.getText().toString()) / Double.valueOf(strBitstamp);
    		}
    	}
    	double value = Math.round(amount * 100000000.0);
    	long longValue = (Double.valueOf(value)).longValue();
        EditText posNote = (EditText)rootView.findViewById(R.id.note);
        strMessage = posNote.getText().toString();
        if(!doBTC) {
        	tvBTC.setVisibility(View.VISIBLE);
        	tvBTC.setText(BitcoinURI.bitcoinValueToPlainString(BigInteger.valueOf(longValue)) + " BTC");
        }
        
        dbVals = new ContentValues();
        dbVals.put("iad", input_address);
        dbVals.put("amt", longValue);
        dbVals.put("cfm", -1);
        dbVals.put("msg", strMessage);
        return BitcoinURI.convertToBitcoinURI(input_address, BigInteger.valueOf(longValue), strLabel, strMessage);
    }

    /** POSTextWatcher: handles input digit by digit and processed DEL (backspace) key
	 * 
	 */
    private class POSTextWatcher implements TextWatcher {

        public void afterTextChanged(Editable arg0) {
        	if(!doBTC) {
            	// lock cursor to end of edit field if entering fiat amounts (fixed 2-place decimal)
            	posInput.setSelection(posInput.getText().length());
        	}
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) { ; }

        public void onTextChanged(CharSequence s, int start, int before, int count) {

        	posInput.removeTextChangedListener(this);

        	if(doBTC) {
            	Log.d("sequence", s.toString());
            	Log.d("start", "" + start);
            	Log.d("count", "" + count);

            	String in = s.toString();
            	if(in.length() == 0) {
                	posInput.setText("0.00");
            	}
        	}
        	else {
            	Log.d("sequence", s.toString());
            	Log.d("start", "" + start);
            	Log.d("count", "" + count);

            	String in = s.toString().replaceAll("\\.", "");
            	// handle DEL key
            	if(start == 3 && count == 0) {
        			in = "0" + in.substring(0, 2);
            	}
            	// handle insert
            	else {
            		while(in.length() > 3 && in.charAt(0) == '0') {
            			in = in.substring(1);
            		}
            	}
            	posInput.setText(in.substring(0, in.toString().length() - 2) + "." + in.substring(in.length() - 2));
        	}

        	posInput.addTextChangedListener(this);
        }

    }

    private void initValues() {

        if(prefs != null) {
        	strLabel = prefs.getString("receiving_name", "");
        	strBTCReceivingAddress = prefs.getString("receiving_address", "");
            strCurrency = prefs.getString("currency", "USD");
        }

        if(tvBTC != null) {
        	tvBTC.setVisibility(View.INVISIBLE);
        }

        if(tvCurrencySymbol != null) {
            if(doBTC) {
            	tvCurrencySymbol.setText("฿");
            }
            else if(strCurrency.equals("EUR")) {
            	tvCurrencySymbol.setText("€");
            }
            else if(strCurrency.equals("GBP")) {
            	tvCurrencySymbol.setText("£");
            }
            else {
            	tvCurrencySymbol.setText("$");
            }
        }

        if(posInput != null) {
        	posInput.setText("0.00");
        }

        if(posMessage != null) {
        	posMessage.setText("");
        }
        
        if(bNew != null) {
            bNew.setText(R.string.payment_new);
        }

        if(imageView != null) {
            imageView.setImageResource(android.R.color.transparent);;
        }

        if(tvBTC != null) {
        	tvBTC.setVisibility(View.INVISIBLE);
        }

    }

    private void getNewAddress() {

    	final ReceivePayments receive_payments = new ReceivePayments(strBTCReceivingAddress);
    	
    	AsyncHttpClient client = new AsyncHttpClient();
        client.get(receive_payments.getUrl(), new AsyncHttpResponseHandler() {

        	@Override
            public void onSuccess(String response) {

                receive_payments.setData(response);
                receive_payments.parse();
                input_address = receive_payments.getInputAddress();

                imageView.setImageBitmap(generateQRCode(generateURI()));
                
            	bNew.setText(R.string.payment_ready);

            	Toast.makeText(getActivity(),
            			"BTC=" + strBTCReceivingAddress + "," +
                    	"Input=" + input_address + "," +
            	    	"Currency=" + strCurrency + "," +
                    	"Use BTC=" + doBTC,
            	Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onFailure(Throwable arg0) {
//        		Log.d(TAG, "failure:" + arg0.toString());
            }

        });

    }
    
}
