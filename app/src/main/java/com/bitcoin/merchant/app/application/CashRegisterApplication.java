package com.bitcoin.merchant.app.application;

import android.app.Application;

import com.bitcoin.merchant.app.database.DBControllerV3;
import com.bitcoin.merchant.app.util.AppUtil;
import com.bitcoin.merchant.app.util.WalletUtil;

public class CashRegisterApplication extends Application {
    private PaymentProcessor paymentProcessor;
    private WalletUtil walletUtil;
    private DBControllerV3 db;

    @Override
    public void onCreate() {
        super.onCreate();
        // to avoid dead-lock risk due to multi-threads access, always create it on launch
        db = new DBControllerV3(this);
    }

    public PaymentProcessor getPaymentProcessor() {
        if (paymentProcessor == null) {
            paymentProcessor = new PaymentProcessor(this);
        }
        return paymentProcessor;
    }

    /**
     * For performance reasons, we cache the wallet (reported in May 2019 on Lenovo Tab E8)
     */
    public WalletUtil getWallet() throws Exception {
        String xPub = AppUtil.getReceivingAddress(this);
        if (walletUtil == null || !walletUtil.isSameXPub(xPub)) {
            walletUtil = new WalletUtil(xPub, this);
        }
        return walletUtil;
    }

    public DBControllerV3 getDb() {
        return db;
    }
}
