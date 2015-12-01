package info.blockchain.merchant;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

//import android.util.Log;

public class TheMoneyConverterXML extends DefaultHandler {
    private String strPrice;
    private String strSymbol;
    private String strText;
    private boolean isItem = false;
    private boolean isTitle = false;
    private boolean isRate = false;
    private StringBuilder builder = null;
    private static HashMap<String,Double> prices = null;
    private static HashMap<String,String> names = null;
    private List<String> currencies = null;

    public TheMoneyConverterXML(ArrayList<String> currencies)	{
    	strText = null;
    	strPrice = null;
    	strSymbol = null;
    	isItem = false;
    	isTitle = false;
        builder = new StringBuilder(32);
	    prices = new HashMap<String,Double>();
	    names = new HashMap<String,String>();
	    this.currencies = currencies;
    }

    public HashMap<String,Double> getExchangeRates() {
    	return prices;
    }

    public HashMap<String,String> getCurrencyNames() {
    	return names;
    }

    public void startDocument() throws SAXException { ; }

    public void endDocument() throws SAXException { ; }

    public void startElement(String namespaceURI,
                             String localName,
                             String qName,
                             Attributes atts) throws SAXException {
        if (localName.equals("item")) {
        	isItem = true;        	
        }
        else if (localName.equals("title") && isItem) {
        	isTitle = true;        	
        }
        else if (localName.equals("description") && isItem && isTitle) {
        	isRate = true;        	
        }
        else	{ ; }    
    }

    public void endElement(String namespaceURI,
                           String localName,
                           String qName) throws SAXException {
        if (localName.equals("item")) {
        	isItem = false;
        	isTitle = false;
        	builder.setLength(0);
        }
        else if (localName.equals("title") && isItem) {
        	int idx = strText.indexOf("/USD");
        	if(idx != -1) {
        		isRate = true;
        		strSymbol = strText.substring(0, idx).trim();
        	}
        	builder.setLength(0);
        }
        else if (localName.equals("description") && isItem && isTitle && isRate) {

        	if(!currencies.contains(strSymbol)) {
        		int idx = strText.indexOf("1 United States Dollar = ");
            	if(strText != null && idx != -1) {
            		String tmp = strText.substring(idx + 25);
            		int idx1 = tmp.indexOf(" ");
                	if(idx1 != -1) {
                    	Locale locale = new Locale("en", "US");
                        Locale.setDefault(locale);
                        strPrice = tmp.substring(0, idx1).replaceAll(",", "");
                		prices.put(strSymbol, Double.valueOf(strPrice));
                        String strName = tmp.substring(idx1 + 1).trim();
                		names.put(strSymbol, strName);
//                		Log.d("XML", strSymbol + "," + strName + "=" + strPrice);
                	}
            	}
        	}

        	isItem = false;
        	isTitle = false;
        	isRate = false;
        	strPrice = strSymbol = null;
        	builder.setLength(0);

        }
        else	{ builder.setLength(0); }    
    }

    public void characters(char[] chars, int i, int i1) throws SAXException {
//        StringBuilder builder = new StringBuilder(32);
	    builder.append(chars, i, i1);
       	strText = builder.toString();
    }
}
