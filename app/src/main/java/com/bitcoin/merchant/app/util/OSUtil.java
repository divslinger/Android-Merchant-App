package com.bitcoin.merchant.app.util;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

public class OSUtil {
    private static OSUtil instance = null;
    private static Context context = null;

    private OSUtil() {
    }

    public static OSUtil getInstance(Context ctx) {
        context = ctx;
        if (instance == null) {
            instance = new OSUtil();
        }
        return instance;
    }

    public static String getSha256(String userEnteredPIN) {
        // TODO fix this with simple transformation
        return userEnteredPIN;
        // return Sha256Hash.wrap(userEnteredPIN.getBytes(Charset.forName("UTF8"))).toString();
    }

    public boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo s : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(s.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public boolean hasPackage(String p) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(p, 0);
            return true;
        } catch (PackageManager.NameNotFoundException nnfe) {
            return false;
        }
    }

    public String getFootprint() {
        String strFootprint = Build.MANUFACTURER + Build.BRAND + Build.MODEL + Build.DEVICE + Build.PRODUCT + Build.SERIAL;
        return getSha256(strFootprint);
    }
}
