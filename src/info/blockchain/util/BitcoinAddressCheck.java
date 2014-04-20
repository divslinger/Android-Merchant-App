package info.blockchain.util;

public class BitcoinAddressCheck {
	
	private BitcoinAddressCheck() { ; }

	public static String clean(final String btcaddress) {

		String ret = null;

		if(btcaddress.startsWith("bitcoin://")) {
			ret = btcaddress.substring(10);
			int idx = ret.indexOf("?");
			if(idx != -1) {
				ret = ret.substring(0, idx);
			}
		}
		else if(btcaddress.startsWith("bitcoin:")) {
			ret = btcaddress.substring(8);
			int idx = ret.indexOf("?");
			if(idx != -1) {
				ret = ret.substring(0, idx);
			}
		}
		else {
			ret = btcaddress;
		}
		
		return ret;
	}

	public static boolean isValid(final String btcaddress) {

		boolean ret = false;
		
		if(btcaddress == null) {
			ret = false;
		}
		else if(btcaddress.length() < 27) {
			ret = false;
		}
		else if(btcaddress.length() > 34) {
			ret = false;
		}
		else if(btcaddress.charAt(0) != '1' && btcaddress.charAt(0) != '3') {
			ret = false;
		}
		else if(btcaddress.contains("0")) {
			ret = false;
		}
		else if(btcaddress.contains("O")) {
			ret = false;
		}
		else if(btcaddress.contains("I")) {
			ret = false;
		}
		else if(btcaddress.contains("l")) {
			ret = false;
		}
		else {
			ret = true;
		}

		return ret;
	}

}
