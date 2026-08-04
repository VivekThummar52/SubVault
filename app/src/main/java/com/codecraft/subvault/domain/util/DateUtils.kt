package com.codecraft.subvault.domain.util

import java.util.*

object DateUtils {
    /**
     * Calculates the next renewal date based on start date and cycle.
     */
    fun calculateNextRenewal(renewalDate: Long, cycle: String): Long {
        if (cycle.lowercase() == "lifetime") return Long.MAX_VALUE

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = renewalDate
        
        val now = Calendar.getInstance()
        
        // If start date is in the future, that's the next renewal
        if (calendar.after(now)) return renewalDate
        
        while (calendar.before(now) || isSameDay(calendar, now)) {
            when (cycle.lowercase()) {
                "weekly" -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
                "monthly" -> calendar.add(Calendar.MONTH, 1)
                "yearly" -> calendar.add(Calendar.YEAR, 1)
                else -> calendar.add(Calendar.MONTH, 1)
            }
        }
        return calendar.timeInMillis
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    /**
     * Calculates the very first renewal date that occurs after the start date.
     */
    fun getFirstRenewalDate(renewalDate: Long, cycle: String): Long {
        if (cycle.lowercase() == "lifetime") return Long.MAX_VALUE
        
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = renewalDate
        
        when (cycle.lowercase()) {
            "weekly" -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
            "monthly" -> calendar.add(Calendar.MONTH, 1)
            "yearly" -> calendar.add(Calendar.YEAR, 1)
            else -> calendar.add(Calendar.MONTH, 1)
        }
        
        return calendar.timeInMillis
    }
}
