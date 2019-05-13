package com.bitcoin.merchant.app.util;

import android.content.Context;
import android.util.Log;

import org.bitcoinj.core.Base58;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.crypto.DeterministicHierarchy;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.wallet.KeyChain;
import org.bitcoinj.wallet.Wallet;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import com.bitcoin.merchant.app.database.DBControllerV3;

public class WalletUtil {
    public static final String TAG = "WalletUtil";
    private final String xPub;
    private final Context context;
    private final File file;
    private final Wallet wallet;

    public WalletUtil(String xPub, Context context) {
        this.xPub = xPub;
        this.context = context;
        byte[] xPubBytes = Base58.decodeChecked(xPub);
        file = getWalletFile(xPubBytes, context);
        wallet = createOrLoadWallet(xPubBytes, file);
    }

    private File getWalletFile(byte[] xPubBytes, Context context) {
        String sha256 = Sha256Hash.of(xPubBytes).toString();
        String name = "xpub-" + sha256 + ".wallet";
        return new File(context.getFilesDir(), name);
    }

    private static Wallet createOrLoadWallet(byte[] xPubBytes, File file) {
        MainNetParams netParams = MainNetParams.get();
        org.bitcoinj.core.Context.propagate(org.bitcoinj.core.Context.getOrCreate(netParams));
        DeterministicKey watchKey = DeterministicKey.deserialize(netParams, xPubBytes, null);
        watchKey.setCreationTimeSeconds(DeterministicHierarchy.BIP32_STANDARDISATION_TIME_SECS);
        Wallet wallet = null;
        try {
            if (file.isFile()) {
                wallet = Wallet.loadFromFile(file);
                Log.d(TAG, "loaded wallet " + file.getName());
            }
        } catch (Exception e) {
            Log.e(TAG, "Unable to load wallet " + file.getName());
        }
        if (wallet == null) {
            wallet = Wallet.fromWatchingKey(netParams, watchKey);
        }
        return wallet;
    }

    private void saveWallet(Wallet wallet, File file) {
        try {
            wallet.saveToFile(file);
            Log.d(TAG, "saved wallet " + file.getName());
        } catch (Exception e) {
            Log.e(TAG, "Unable to save wallet " + file.getName());
        }
    }

    public String generateAddressFromXPub() {
        Set<String> addresses = new HashSet<>();
        try {
            addresses = new DBControllerV3(context).getAllAddresses();
            Log.d(TAG, "loaded " + addresses.size() + " addresses from TX history: " + addresses);
        } catch (Exception e) {
            Log.e(TAG, "Unable to load addresses from TX history");
        }
        String address = wallet.freshAddress(KeyChain.KeyPurpose.RECEIVE_FUNDS).toBase58();
        while (addresses.contains(address)) {
            saveWallet(wallet, file);
            Log.d(TAG, "BCH-address skipped from xPub: " + address);
            address = wallet.freshAddress(KeyChain.KeyPurpose.RECEIVE_FUNDS).toBase58();
        }
        return address;
    }

    @Override
    public String toString() {
        return "WalletUtil{" +
                "xPub='" + xPub + '\'' +
                ", file=" + file +
                '}';
    }
}
