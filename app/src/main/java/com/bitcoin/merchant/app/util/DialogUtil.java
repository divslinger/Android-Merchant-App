package com.bitcoin.merchant.app.util;

import android.app.Activity;
import android.app.AlertDialog;

public class DialogUtil {
    public static void show(Activity activity, String title, String message) {
        show(activity, title, message, null);
    }

    public static void show(Activity activity, String title, String message, final Runnable runner) {
        activity.runOnUiThread(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle(title)
                    .setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton("OK", (dialog, whichButton) -> {
                        dialog.dismiss();
                        if (runner != null) {
                            runner.run();
                        }
                    });
            if (!activity.isFinishing()) {
                builder.create().show();
            }
        });
    }
}
