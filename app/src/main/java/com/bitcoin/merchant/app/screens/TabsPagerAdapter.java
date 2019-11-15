package com.bitcoin.merchant.app.screens;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class TabsPagerAdapter extends FragmentPagerAdapter {
    public static final int TAB_INPUT_AMOUNT = 0;
    public static final int TAB_TX_HISTORY = 1;

    private String[] tabs = null;
    private PaymentInputFragment paymentInputFragment;
    private TransactionsHistoryFragment transactionsHistoryFragment;

    public TabsPagerAdapter(FragmentManager fm, String[] tabs) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
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