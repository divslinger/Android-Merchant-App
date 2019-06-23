package com.bitcoin.merchant.app.screens;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class TabsPagerAdapter extends FragmentPagerAdapter {
    private String[] tabs = null;
    private PaymentInputFragment paymentInputFragment;
    private TransactionsHistoryFragment transactionsHistoryFragment;

    public TabsPagerAdapter(FragmentManager fm, String[] tabs) {
        super(fm);
        this.tabs = tabs;
    }

    @Override
    public int getCount() {
        return tabs.length;
    }

    public CharSequence getPageTitle(int position) {
        return tabs[position];
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                if (paymentInputFragment == null) {
                    paymentInputFragment = new PaymentInputFragment();
                }
                return paymentInputFragment;
            case 1:
                if (transactionsHistoryFragment == null) {
                    transactionsHistoryFragment = new TransactionsHistoryFragment();
                }
                return transactionsHistoryFragment;
            default:
                return null;
        }
    }
}