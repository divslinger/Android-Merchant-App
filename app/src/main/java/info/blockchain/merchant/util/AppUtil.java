package info.blockchain.merchant.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import com.bitcoin.merchant.app.currency.CurrencyDetector;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import info.blockchain.wallet.util.FormatsUtil;

public class AppUtil {
    public static final Gson GSON = new Gson();
    public static final String PACKAGE_BITCOIN_DOT_COM_WALLET = "com.bitcoin.mwallet";
    public static final String DEFAULT_CURRENCY_FIAT = "USD";
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

    public static String getCurrency(Context context) {
        String currency = PrefsUtil.getInstance(context).getValue(PrefsUtil.MERCHANT_KEY_CURRENCY, "");
        if (currency.length() == 0) {
            // auto-detect currency
            currency = CurrencyDetector.findCurrencyFromLocale(context);
            if (currency.length() == 0) {
                currency = DEFAULT_CURRENCY_FIAT;
            }
            // save to avoid further auto-detection
            PrefsUtil.getInstance(context).setValue(PrefsUtil.MERCHANT_KEY_CURRENCY, currency);
        }
        return currency;
    }

    public static String getCountry(Context context) {
        return PrefsUtil.getInstance(context).getValue(PrefsUtil.MERCHANT_KEY_COUNTRY, null);
    }

    public static String getLocale(Context context) {
        return PrefsUtil.getInstance(context).getValue(PrefsUtil.MERCHANT_KEY_LOCALE, null);
    }

    public static <T> T readFromJsonFile(Context ctx, String fileName, Class<T> classOfT) {
        return GSON.fromJson(readFromfile(fileName, ctx), classOfT);
    }

    public static String readFromfile(String fileName, Context context) {
        StringBuilder b = new StringBuilder();
        BufferedReader input = null;
        try {
            input = new BufferedReader(new InputStreamReader(context.getResources().getAssets().open(fileName)));
            String line = "";
            while ((line = input.readLine()) != null) {
                b.append(line);
            }
        } catch (Exception e) {
            e.getMessage();
        } finally {
            try {
                if (input != null)
                    input.close();
            } catch (Exception e2) {
                e2.getMessage();
            }
        }
        return b.toString();
    }

    public boolean isV2API() {
        String strReceiver = PrefsUtil.getInstance(context).getValue(PrefsUtil.MERCHANT_KEY_MERCHANT_RECEIVER, "");
        return !FormatsUtil.getInstance().isValidBitcoinAddress(strReceiver);
    }

    public boolean hasValidReceiver() {
        String receiver = PrefsUtil.getInstance(context).getValue(PrefsUtil.MERCHANT_KEY_MERCHANT_RECEIVER, "");
        return FormatsUtil.getInstance().isValidBitcoinAddress(receiver) || FormatsUtil.getInstance().isValidXpub(receiver);
    }
}
