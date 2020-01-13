package com.bitcoin.merchant.app.screens.features;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;

import com.bitcoin.merchant.app.MainActivity;
import com.bitcoin.merchant.app.R;
import com.bitcoin.merchant.app.application.CashRegisterApplication;
import com.bitcoin.merchant.app.util.AppUtil;

public abstract class ToolbarAwareFragment extends Fragment {
    public MainActivity activity;
    private Toolbar toolbar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        activity = (MainActivity) getActivity();
        toolbar = activity.getToolbar();
        if (AppUtil.isEmulator()) {
            printFragmentStack();
        }
        return null;
    }

    private void printFragmentStack() {
        Fragment navHostFragment = activity.getSupportFragmentManager().getPrimaryNavigationFragment();
        int stackCount = 0;
        if (navHostFragment != null) {
            FragmentManager fragmentManager = navHostFragment.getChildFragmentManager();
            stackCount = fragmentManager.getBackStackEntryCount();
        }
        Log.i("Nav", "Current=" + getClass().getSimpleName() + ", Stack=" + stackCount);
    }

    public void setToolbarVisible(boolean enabled) {
        toolbar.setVisibility(enabled ? View.VISIBLE : View.GONE);
    }

    public void setToolbarAsMenuButton() {
        setToolbarVisible(true);
        toolbar.setTitleTextColor(Color.BLACK);
        toolbar.setNavigationIcon(R.drawable.ic_menu_black_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.openMenuDrawer();
            }
        });
    }

    public void setToolbarAsBackButton() {
        setToolbarVisible(true);
        toolbar.setTitleTextColor(Color.BLACK);
        toolbar.setNavigationIcon(R.drawable.ic_back_black_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.onBackPressed();
            }
        });
    }

    public void clearToolbarTitle() {
        setToolbarVisible(true);
        toolbar.setTitle("");
    }

    public void setToolbarTitle(int titleResourceId) {
        setToolbarVisible(true);
        toolbar.setTitle(titleResourceId);
    }

    public CashRegisterApplication getApp() {
        return activity.getApp();
    }

    protected NavController getNav() {
        return MainActivity.getNav(activity);
    }

    /**
     * @return true if already managed
     */
    public boolean isBackAllowed() {
        if (AppUtil.isEmulator()) {
            printFragmentStack();
        }
        return true;
    }

    public boolean canFragmentBeDiscardedWhenInBackground() {
        return false;
    }
}
