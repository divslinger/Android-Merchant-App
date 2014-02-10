package info.blockchain.merchant;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class USDXML extends DefaultHandler {
    private String strCurrency;
    private String strPrice;
    private String strText;
    private boolean isItem = false;
    private boolean isTitle = false;
    private boolean isUSD = false;
    private StringBuilder builder = null;

    public USDXML(String currency)	{
    	strCurrency = currency;
    	strText = null;
    	strPrice = null;
    	isItem = false;
    	isTitle = false;
    	isUSD = false;
        builder = new StringBuilder(32);
    }

    public String getPrice() {
    	return strPrice;
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
        	isUSD = true;        	
        }
        else	{ ; }    
    }

    public void endElement(String namespaceURI,
                           String localName,
                           String qName) throws SAXException {
        if (localName.equals("item")) {
        	isItem = false;
        	isTitle = false;
        	isUSD = false;
        	builder.setLength(0);
        }
        else if (localName.equals("title") && isItem) {
        	if(strText.compareTo("USD/" + strCurrency) == 0) {
        		isUSD = true;
        	}
        	builder.setLength(0);
        }
        else if (localName.equals("description") && isItem && isTitle && isUSD) {
        	
        	if(strText != null) {
        		int idx1 = strText.indexOf("= ");
            	if(idx1 != -1) {
            		int idx2 = strText.indexOf(" United States Dollar");
                	if(idx2 != -1) {
                		strPrice = strText.substring(idx1 + 2,  idx2).trim();
                	}
            	}
        	}

        	isItem = false;
        	isTitle = false;
        	isUSD = false;
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
