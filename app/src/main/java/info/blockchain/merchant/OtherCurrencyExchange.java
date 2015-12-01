package info.blockchain.merchant;

import java.util.ArrayList;
import java.util.HashMap;

import org.xml.sax.SAXException;

import android.content.Context;
import android.util.Xml;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

public class OtherCurrencyExchange	{

    private static OtherCurrencyExchange instance = null;
    
	private static String[] currencies = null;
    private static HashMap<String,Double> prices = null;
    private static HashMap<String,String> names = null;

    private static Context context = null;
    
    private OtherCurrencyExchange()	{ ; }

	public static OtherCurrencyExchange getInstance(Context ctx) {
		
		context = ctx;

		if (instance == null) {
		    prices = new HashMap<String,Double>();
		    names = new HashMap<String,String>();
	    	instance = new OtherCurrencyExchange();
		}

		getExchangeRates();

		return instance;
	}
	
    public Double getCurrencyPrice(String currency)	{
    	
    	if(prices.containsKey(currency) && prices.get(currency) != 0.0)	{
    		return prices.get(currency);
    	}
    	else	{
    		return 0.0;
    	}

    }

    public String getCurrencyName(String currency)	{
    	
    	if(names.containsKey(currency) && names.get(currency) != null)	{
    		return names.get(currency);
    	}
    	else	{
    		return null;
    	}

    }

    public HashMap<String,String> getCurrencyNames()	{
    	return names;
    }

    public HashMap<String,Double> getCurrencyPrices()	{
    	return prices;
    }

	private static void getExchangeRates() {

    	AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://themoneyconverter.com/rss-feed/USD/rss.xml", new AsyncHttpResponseHandler() {

        	@Override
            public void onSuccess(String response) {
        		
	        	currencies = context.getResources().getStringArray(R.array.currencies);
	        	ArrayList<String> currencyCodes = new ArrayList<String>();
	        	for(int i = 0; i < (currencies.length - 1); i++) {
	        		currencyCodes.add(currencies[i].substring(currencies[i].length() - 3));
	        	}
	        	
        		TheMoneyConverterXML mcx = new TheMoneyConverterXML(currencyCodes);
            	try {
            		Xml.parse(response, mcx);
            		if(mcx.getExchangeRates() != null && mcx.getCurrencyNames() != null) {
                		prices = mcx.getExchangeRates();
                		names = mcx.getCurrencyNames();
            		}
            	} catch (SAXException se) {
            		se.printStackTrace();
            	}

            }

            @Override
            public void onFailure(Throwable arg0) {
//        		Log.d("Currency Exchange", "failure:" + arg0.toString());
            }

        });
	}

}
