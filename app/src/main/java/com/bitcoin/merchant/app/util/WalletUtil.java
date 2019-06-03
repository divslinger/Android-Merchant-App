package com.bitcoin.merchant.app.util;

import android.content.Context;
import android.util.Log;

import com.bitcoin.merchant.app.database.DBControllerV3;

import org.bitcoinj.core.Base58;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.crypto.DeterministicHierarchy;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.wallet.KeyChain;
import org.bitcoinj.wallet.KeyChainGroup;
import org.bitcoinj.wallet.Wallet;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class WalletUtil {
    public static final String TAG = "WalletUtil";
    private final String xPub;
    private final Context context;
    private final File file;
    private final Wallet wallet;
    // For performance reasons, we cache all used addresses (reported in May 2019 on Lenovo Tab E8)
    private final AddressBank addressBank;

    private class AddressBank {
        final Set<String> usedAddresses;

        public AddressBank() {
            Set<String> addresses = new HashSet<>();
            try {
                addresses = new DBControllerV3(context).getAllAddresses();
                Log.d(TAG, "loaded " + addresses.size() + " addresses from TX history: " + addresses);
            } catch (Exception e) {
                Log.e(TAG, "Unable to load addresses from TX history");
            }
            usedAddresses = Collections.synchronizedSet(addresses);
        }

        public boolean isUsed(String address) {
            return usedAddresses.contains(address);
        }

        public void addUsedAddress(String address) {
            usedAddresses.add(address);
        }
    }

    public boolean isSameXPub(String xPub) {
        byte[] b1 = Base58.decodeChecked(this.xPub);
        byte[] b2 = Base58.decodeChecked(xPub);
        return Arrays.equals(b1, b2);
    }

    public WalletUtil(String xPub, Context context) throws Exception {
        this.xPub = xPub;
        this.context = context;
        byte[] xPubBytes = Base58.decodeChecked(xPub);
        file = getWalletFile(xPubBytes, context);
        wallet = createOrLoadWallet(xPubBytes, file);
        addressBank = new AddressBank();
    }

    public void addUsedAddress(String address) {
        addressBank.addUsedAddress(address.trim());
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
            KeyChainGroup keyChainGroup = new KeyChainGroup(netParams, watchKey);
            keyChainGroup.setLookaheadSize(1);
            keyChainGroup.setLookaheadThreshold(0);
            wallet = new Wallet(netParams, keyChainGroup);
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
        String address = wallet.freshAddress(KeyChain.KeyPurpose.RECEIVE_FUNDS).toBase58();
        while (addressBank.isUsed(address)) {
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
