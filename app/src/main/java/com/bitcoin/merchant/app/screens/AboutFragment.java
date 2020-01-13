package com.bitcoin.merchant.app.screens;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bitcoin.merchant.app.BuildConfig;
import com.bitcoin.merchant.app.R;
import com.bitcoin.merchant.app.screens.features.ToolbarAwareFragment;

public class AboutFragment extends ToolbarAwareFragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_about, container, false);
        TextView about = view.findViewById(R.id.about_screen);
        about.setText(BuildConfig.VERSION_NAME + " - 2019");
        view.findViewById(R.id.about_logo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.bitcoin.com/")));
            }
        });
        setToolbarAsBackButton();
        setToolbarTitle(R.string.menu_about);
        return view;
    }

    @Override
    public boolean canFragmentBeDiscardedWhenInBackground() {
        return true;
    }
}
