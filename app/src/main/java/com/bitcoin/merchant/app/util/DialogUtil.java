package com.bitcoin.merchant.app.util;

import android.app.Activity;
import android.app.AlertDialog;

public class DialogUtil {
    public static void show(Activity activity, String title, String message, final Runnable runner) {
        show(activity, title, message, activity.getString(android.R.string.ok), runner);
    }

    public static void show(Activity activity, String title, String message, String positiveText, final Runnable runner) {
        activity.runOnUiThread(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle(title)
                    .setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton(positiveText, (dialog, whichButton) -> {
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
