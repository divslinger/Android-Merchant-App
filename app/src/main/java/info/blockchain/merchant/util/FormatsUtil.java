package info.blockchain.merchant.util;

import android.util.Patterns;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.WrongNetworkException;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.uri.BitcoinURI;
import org.bitcoinj.uri.BitcoinURIParseException;

import java.util.regex.Pattern;

//import android.util.Log;

public class FormatsUtil {

    private Pattern emailPattern = Patterns.EMAIL_ADDRESS;
    private Pattern phonePattern = Pattern.compile("(\\+[1-9]{1}[0-9]{1,2}+|00[1-9]{1}[0-9]{1,2}+)[\\(\\)\\.\\-\\s\\d]{6,16}");

    public static final String XPUB = "^xpub[1-9A-Za-z][^OIl]+$";
    public static final String HEX = "^[0-9A-Fa-f]+$";

    private static FormatsUtil instance = null;

    private FormatsUtil() { ; }

    public static FormatsUtil getInstance() {

        if(instance == null) {
            instance = new FormatsUtil();
        }

        return instance;
    }

    public String validateBitcoinAddress(final String address) {

        if(isValidBitcoinAddress(address)) {
            return address;
        }
        else {
            String addr = uri2BitcoinAddress(address);
            if(addr != null) {
                return addr;
            }
            else {
                return null;
            }
        }
    }

    public boolean isBitcoinUri(final String s) {

        boolean ret = false;
        BitcoinURI uri = null;

        try {
            uri = new BitcoinURI(s);
            ret = true;
        }
        catch(BitcoinURIParseException bupe) {
            ret = false;
        }

        return ret;
    }

    public String getBitcoinUri(final String s) {

        String ret = null;
        BitcoinURI uri = null;

        try {
            uri = new BitcoinURI(s);
            ret = uri.toString();
        }
        catch(BitcoinURIParseException bupe) {
            ret = null;
        }

        return ret;
    }

    public String getBitcoinAddress(final String s) {

        String ret = null;
        BitcoinURI uri = null;

        try {
            uri = new BitcoinURI(s);
            ret = uri.getAddress().toString();
        }
        catch(BitcoinURIParseException bupe) {
            ret = null;
        }

        return ret;
    }

    public String getBitcoinAmount(final String s) {

        String ret = null;
        BitcoinURI uri = null;

        try {
            uri = new BitcoinURI(s);
            if(uri.getAmount() != null) {
                ret = uri.getAmount().toString();
            }
            else {
                ret = "0.0000";
            }
        }
        catch(BitcoinURIParseException bupe) {
            ret = null;
        }

        return ret;
    }

    public boolean isValidBitcoinAddress(final String address) {

        boolean ret = false;
        Address addr = null;

        try {
            addr = new Address(MainNetParams.get(), address);
            if(addr != null) {
                ret = true;
            }
        }
        catch(WrongNetworkException wne) {
            ret = false;
        }
        catch(AddressFormatException afe) {
            ret = false;
        }

        return ret;
    }

    public boolean isValidEmailAddress(final String address) {
        if(emailPattern.matcher(address).matches()) {
            return true;
        }
        else {
            return false;
        }
    }

    public boolean isValidMobileNumber(final String mobile) {
        if(phonePattern.matcher(mobile).matches()) {
            return true;
        }
        else {
            return false;
        }
    }

    private String uri2BitcoinAddress(final String address) {

        String ret = null;
        BitcoinURI uri = null;

        try {
            uri = new BitcoinURI(address);
            ret = uri.getAddress().toString();
        }
        catch(BitcoinURIParseException bupe) {
            ret = null;
        }

        return ret;
    }

}
