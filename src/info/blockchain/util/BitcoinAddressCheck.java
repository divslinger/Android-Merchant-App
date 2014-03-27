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
			System.out.println("BTC address null");
			ret = false;
		}
		else if(btcaddress.length() < 27) {
			System.out.println("BTC address < 27");
			ret = false;
		}
		else if(btcaddress.length() > 34) {
			System.out.println("BTC address  > 34");
			ret = false;
		}
		else if(btcaddress.charAt(0) != '1' && btcaddress.charAt(0) != '3') {
			System.out.println("BTC address invalid start character");
			ret = false;
		}
		else if(btcaddress.contains("0")) {
			System.out.println("BTC address contains 0");
			ret = false;
		}
		else if(btcaddress.contains("O")) {
			System.out.println("BTC address contains O");
			ret = false;
		}
		else if(btcaddress.contains("I")) {
			System.out.println("BTC address contains I");
			ret = false;
		}
		else if(btcaddress.contains("l")) {
			System.out.println("BTC address contains l");
			ret = false;
		}
		else {
			ret = true;
		}

		return ret;
	}

}
