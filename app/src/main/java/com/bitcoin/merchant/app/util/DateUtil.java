package com.bitcoin.merchant.app.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {
    private static DateUtil instance = null;
    private final SimpleDateFormat pastYearsFormat = new SimpleDateFormat("E dd MMM @ HH:mm");
    private final SimpleDateFormat currentYearFormat = new SimpleDateFormat("dd MMM yyyy");
    private final SimpleDateFormat previousDaysFormat = new SimpleDateFormat("E dd MMM @ HH:mm");
    private final SimpleDateFormat yesterdayFormat = new SimpleDateFormat("HH:mm");
    private final SimpleDateFormat todayFormat = new SimpleDateFormat("HH:mm");

    private DateUtil() {
    }

    public static DateUtil getInstance() {
        if (instance == null) {
            instance = new DateUtil();
        }
        return instance;
    }

    public String format(long timeInSec) {
        if (timeInSec == 0) {
            return "";
        }
        long hours24 = 60L * 60L * 24;
        long now = System.currentTimeMillis() / 1000L;
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(now * 1000L));
        int nowYear = cal.get(Calendar.YEAR);
        int nowDay = cal.get(Calendar.DAY_OF_MONTH);
        cal.setTime(new Date(timeInSec * 1000L));
        int thenYear = cal.get(Calendar.YEAR);
        int thenDay = cal.get(Calendar.DAY_OF_MONTH);
        // within 24h
        String ret;
        if (now - timeInSec < hours24) {
            if (thenDay < nowDay) {
                ret = "Yesterday @ " + yesterdayFormat.format(timeInSec * 1000L);
            } else {
                ret = "Today @ " + todayFormat.format(timeInSec * 1000L);
            }
        }
        // within 48h
        else if (now - timeInSec < (hours24 * 2)) {
            ret = previousDaysFormat.format(timeInSec * 1000L);
        } else {
            if (thenYear < nowYear) {
                ret = currentYearFormat.format(timeInSec * 1000L);
            } else {
                ret = pastYearsFormat.format(timeInSec * 1000L);
            }
        }
        return ret;
    }
}
