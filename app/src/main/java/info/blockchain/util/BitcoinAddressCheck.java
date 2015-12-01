package info.blockchain.util;

import info.blockchain.wallet.util.FormatsUtil;

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
		return FormatsUtil.getInstance().isValidBitcoinAddress(btcaddress);
	}

}
