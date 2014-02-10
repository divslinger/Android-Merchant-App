package info.blockchain.merchant;

import java.util.HashMap;

import org.xml.sax.SAXException;

import android.util.Xml;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.softmachines.pocketchange.bitcoin.markets.*;

public class CurrencyExchange	{

	public static String EUR = "EUR";
	public static String GBP = "GBP";
	public static String USD = "USD";

    private static CurrencyExchange instance = null;
    private static BTCExchange btc = null;
    
    private static String strBitstampPrice = null;
    private static String strUSDGBP = null;
    private static String strUSDEUR = null;

    private CurrencyExchange()	{ ; }

	public static CurrencyExchange getInstance() {
		
		getBitstamp();
		getUSDFX(EUR);
		getUSDFX(GBP);

		if (instance == null) {
	    	instance = new CurrencyExchange();
		}
		
		return instance;
	}
	
    public String getBitstampPrice()	{ return strBitstampPrice; }

    public String getUSDEUR()	{ return strUSDEUR; }
    
    public String getUSDGBP()	{ return strUSDGBP; }

	private static void getBitstamp() {
		btc = BTCExchangeFactory.getInstance().getExchange(BTCExchangeFactory.BITSTAMP, USD);

    	AsyncHttpClient client = new AsyncHttpClient();
        client.get(btc.getUrl(), new AsyncHttpResponseHandler() {

        	@Override
            public void onSuccess(String response) {
        		btc.setData(response);
        		btc.parseData();
        		strBitstampPrice = btc.getPrice();
        		Log.d("Currency Exchange", "Bitstamp:" + strBitstampPrice);
            }

            @Override
            public void onFailure(Throwable arg0) {
        		Log.d("Currency Exchange", "failure:" + arg0.toString());
            }

        });
	}

	private static void getUSDFX(final String currency) {

    	AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://themoneyconverter.com/rss-feed/" + currency + "/rss.xml", new AsyncHttpResponseHandler() {

        	@Override
            public void onSuccess(String response) {

        		USDXML mcx = new USDXML(currency);
            	try {
            		Xml.parse(response, mcx);
            		if(currency.equals(EUR)) {
            			strUSDEUR = mcx.getPrice();
                		Log.d("Currency Exchange", "USDEUR:" + strUSDEUR);
            		}
            		else {
            			strUSDGBP = mcx.getPrice();
                		Log.d("Currency Exchange", "USDGBP:" + strUSDGBP);
            		}
            	} catch (SAXException e) {
            		e.printStackTrace();
            	}
        	}

            @Override
            public void onFailure(Throwable arg0) {
        		Log.d("Currency Exchange", "failure:" + arg0.toString());
            }

        });
	}

}
