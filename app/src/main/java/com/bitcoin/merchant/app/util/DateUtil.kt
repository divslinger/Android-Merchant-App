package com.bitcoin.merchant.app.util

import java.text.SimpleDateFormat
import java.util.*

class DateUtil private constructor() {
    private val pastYearsFormat = SimpleDateFormat("E dd MMM @ HH:mm")
    private val currentYearFormat = SimpleDateFormat("dd MMM yyyy")
    private val previousDaysFormat = SimpleDateFormat("E dd MMM @ HH:mm")
    private val yesterdayFormat = SimpleDateFormat("HH:mm")
    private val todayFormat = SimpleDateFormat("HH:mm")
    fun format(timeInSec: Long): String {
        if (timeInSec == 0L) {
            return ""
        }
        val hours24 = 60L * 60L * 24
        val now = System.currentTimeMillis() / 1000L
        val cal = Calendar.getInstance()
        cal.time = Date(now * 1000L)
        val nowYear = cal[Calendar.YEAR]
        val nowDay = cal[Calendar.DAY_OF_MONTH]
        cal.time = Date(timeInSec * 1000L)
        val thenYear = cal[Calendar.YEAR]
        val thenDay = cal[Calendar.DAY_OF_MONTH]
        // within 24h
        val ret: String
        ret = if (now - timeInSec < hours24) {
            if (thenDay < nowDay) {
                "Yesterday @ " + yesterdayFormat.format(timeInSec * 1000L)
            } else {
                "Today @ " + todayFormat.format(timeInSec * 1000L)
            }
        } else if (now - timeInSec < hours24 * 2) {
            previousDaysFormat.format(timeInSec * 1000L)
        } else {
            if (thenYear < nowYear) {
                currentYearFormat.format(timeInSec * 1000L)
            } else {
                pastYearsFormat.format(timeInSec * 1000L)
            }
        }
        return ret
    }

    companion object {
        val instance: DateUtil by lazy { DateUtil() }
    }
}