package info.blockchain.merchant;

import java.util.HashMap;

import android.content.Context;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
//import android.util.Log;

import info.blockchain.api.ExchangeRates;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

public class CurrencyExchange	{

    private static CurrencyExchange instance = null;
    
    private static ExchangeRates fxRates = null;
    private static HashMap<String,Double> prices = null;
    private static HashMap<String,String> symbols = null;

    private static Context context = null;
    
    private CurrencyExchange()	{ ; }

	public static CurrencyExchange getInstance(Context ctx) {
		
		context = ctx;

		fxRates = new ExchangeRates();

		if (instance == null) {

		    prices = new HashMap<String,Double>();
		    symbols = new HashMap<String,String>();
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			String[] currencies = fxRates.getCurrencies();
	    	for(int i = 0; i < currencies.length; i++)	 {
//	    		Log.d("CurrencyExchange created instance", currencies[i] + "," + Double.longBitsToDouble(prefs.getLong(currencies[i], Double.doubleToLongBits(0.0))));
		    	prices.put(currencies[i], Double.longBitsToDouble(prefs.getLong(currencies[i], Double.doubleToLongBits(0.0))));
		    	symbols.put(currencies[i], prefs.getString(currencies[i] + "-SYM", null));
	    	}

	    	instance = new CurrencyExchange();
		}

		getExchangeRates();

		return instance;
	}
	
    public Double getCurrencyPrice(String currency)	{
    	
    	if(prices.containsKey(currency) && prices.get(currency) != 0.0)	{
    		return prices.get(currency);
    	}
    	else if(OtherCurrencyExchange.getInstance(context).getCurrencyPrices().containsKey(currency) && OtherCurrencyExchange.getInstance(context).getCurrencyPrices().get(currency) != 0.0)	{
    		
    		double usd_curr = OtherCurrencyExchange.getInstance(context).getCurrencyPrices().get(currency);
//    		Log.d("OC rate", "" + usd_curr);
    		double btc_usd = prices.get("USD");
//    		Log.d("USD rate", "" + btc_usd);

    		return 1.0 / ((1.0 / usd_curr) * (1.0 / btc_usd));
    	}
    	else	{
    		return 0.0;
    	}

    }

    public String getCurrencySymbol(String currency)	{
    	
    	if(symbols.containsKey(currency) && symbols.get(currency) != null)	{
    		return symbols.get(currency);
    	}
    	else if(OtherCurrencyExchange.getInstance(context).getCurrencyNames().containsKey(currency))	{
    		return currency.substring(currency.length() - 1, currency.length());
    	}
    	else	{
    		return null;
    	}

    }

	private static void getExchangeRates() {

    	AsyncHttpClient client = new AsyncHttpClient();
        client.get(fxRates.getUrl(), new AsyncHttpResponseHandler() {

        	@Override
            public void onSuccess(String response) {
        		fxRates.setData(response);
        		fxRates.parse();

    			String[] currencies = fxRates.getCurrencies();
    	    	for(int i = 0; i < currencies.length; i++)	 {
    		    	prices.put(currencies[i], fxRates.getLastPrice(currencies[i]));
    		    	symbols.put(currencies[i], fxRates.getSymbol(currencies[i]));
    	    	}

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor editor = prefs.edit();
    	    	for(int i = 0; i < currencies.length; i++)	 {
    		    	if(prices.containsKey(currencies[i]) && prices.get(currencies[i]) != 0.0)	{
                        editor.putLong(currencies[i], Double.doubleToRawLongBits(prices.get(currencies[i])));
                        editor.putString(currencies[i] + "-SYM", symbols.get(currencies[i]));
    		    	}
    	    	}
                editor.commit();
            }

            @Override
            public void onFailure(Throwable arg0) {
//        		Log.d("Currency Exchange", "failure:" + arg0.toString());
            }

        });
	}

}
