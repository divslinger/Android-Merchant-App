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

	public static String CNY = "CNY";
	public static String EUR = "EUR";
	public static String GBP = "GBP";
	public static String JPY = "JPY";
	public static String USD = "USD";

    private static CurrencyExchange instance = null;
    
    private static ExchangeRates fxRates = null;

    private static Double priceCNY = 0.0;
    private static Double priceEUR = 0.0;
    private static Double priceGBP = 0.0;
    private static Double priceJPY = 0.0;
    private static Double priceUSD = 0.0;

    private static Context context = null;
    
    private CurrencyExchange()	{ ; }

	public static CurrencyExchange getInstance(Context ctx) {
		
		context = ctx;

		fxRates = new ExchangeRates();

		if (instance == null) {

			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			priceCNY = Double.longBitsToDouble(prefs.getLong(CNY, Double.doubleToLongBits(0.0)));
			priceEUR = Double.longBitsToDouble(prefs.getLong(EUR, Double.doubleToLongBits(0.0)));
			priceGBP = Double.longBitsToDouble(prefs.getLong(GBP, Double.doubleToLongBits(0.0)));
			priceJPY = Double.longBitsToDouble(prefs.getLong(JPY, Double.doubleToLongBits(0.0)));
			priceUSD = Double.longBitsToDouble(prefs.getLong(USD, Double.doubleToLongBits(0.0)));

	    	instance = new CurrencyExchange();
		}

		getExchangeRates();

		return instance;
	}
	
    public Double getCurrencyPrice(String currency)	{
    	
    	if(currency.equals("CNY"))	{
    		return priceCNY;
    	}
    	else if(currency.equals("EUR"))	{
    		return priceEUR;
    	}
    	else if(currency.equals("GBP"))	{
    		return priceGBP;
    	}
    	else if(currency.equals("JPY"))	{
    		return priceJPY;
    	}
    	else if(currency.equals("USD"))	{
    		return priceUSD;
    	}
    	else	{
    		return 0.0;
    	}

    }

	private static void getExchangeRates() {

    	AsyncHttpClient client = new AsyncHttpClient();
        client.get(fxRates.getUrl(), new AsyncHttpResponseHandler() {

        	@Override
            public void onSuccess(String response) {
        		fxRates.setData(response);
        		fxRates.parse();
        		
        		priceCNY = fxRates.getLastPrice(CNY);
//        		Log.d("CNY", Double.toString(priceCNY));
        		priceEUR = fxRates.getLastPrice(EUR);
//        		Log.d("EUR", Double.toString(priceEUR));
        		priceGBP = fxRates.getLastPrice(GBP);
//        		Log.d("GBP", Double.toString(priceGBP));
        		priceJPY = fxRates.getLastPrice(JPY);
//        		Log.d("JPY", Double.toString(priceJPY));
        		priceUSD = fxRates.getLastPrice(USD);
//        		Log.d("USD", Double.toString(priceUSD));
        		
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor editor = prefs.edit();

                if(priceCNY > 0.0) {
                    editor.putLong(CNY, Double.doubleToRawLongBits(priceCNY));
                }
                if(priceEUR > 0.0) {
                    editor.putLong(EUR, Double.doubleToRawLongBits(priceEUR));
                }
                if(priceGBP > 0.0) {
                    editor.putLong(GBP, Double.doubleToRawLongBits(priceGBP));
                }
                if(priceJPY > 0.0) {
                    editor.putLong(JPY, Double.doubleToRawLongBits(priceJPY));
                }
                if(priceUSD > 0.0) {
                    editor.putLong(USD, Double.doubleToRawLongBits(priceUSD));
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
