package info.blockchain.util;

public class BitcoinAddressCheck {
	
	private BitcoinAddressCheck() { ; }

	public static boolean isValid(final String btcaddress) {

		boolean ret = false;
		
		if(btcaddress.charAt(0) != '1') {
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
		else if(btcaddress.length() > 34) {
			ret = false;
		}
		else {
			ret = true;
		}

		return ret;
	}

}
