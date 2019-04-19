package info.blockchain.merchant.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import info.blockchain.wallet.util.FormatsUtil;

public class AppUtil {
    public static final String PACKAGE_BITCOIN_DOT_COM_WALLET = "com.bitcoin.mwallet";
    private static final boolean MISSING_WALLET_SIMULATED = false;
    private static Context context = null;
    private static AppUtil instance = null;

    private AppUtil() {
    }

    public static AppUtil getInstance(Context ctx) {
        context = ctx;
        if (instance == null) {
            instance = new AppUtil();
        }
        return instance;
    }

    public static boolean isWalletInstalled(Activity activity) {
        PackageManager pm = activity.getPackageManager();
        try {
            pm.getPackageInfo(PACKAGE_BITCOIN_DOT_COM_WALLET, 0);
            if (MISSING_WALLET_SIMULATED) {
                return false;
            }
            return true;
        } catch (PackageManager.NameNotFoundException nnfe) {
            return false;
        }
    }

    public static boolean isValidAddress(String address) {
        return (address != null && address.length() > 0)
                && (FormatsUtil.getInstance().isValidXpub(address)
                || FormatsUtil.getInstance().isValidBitcoinAddress(address));
    }

    public boolean isV2API() {
        String strReceiver = PrefsUtil.getInstance(context).getValue(PrefsUtil.MERCHANT_KEY_MERCHANT_RECEIVER, "");
        if (FormatsUtil.getInstance().isValidBitcoinAddress(strReceiver)) {
            return false;
        } else {
            return true;
        }
    }

    public boolean hasValidReceiver() {
        String receiver = PrefsUtil.getInstance(context).getValue(PrefsUtil.MERCHANT_KEY_MERCHANT_RECEIVER, "");
        if (FormatsUtil.getInstance().isValidBitcoinAddress(receiver) || FormatsUtil.getInstance().isValidXpub(receiver)) {
            return true;
        } else {
            return false;
        }
    }
}
