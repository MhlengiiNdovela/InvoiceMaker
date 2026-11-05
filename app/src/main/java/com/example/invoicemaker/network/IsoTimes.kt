package com.example.invoicemaker.network

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Converts between the app's epoch millis and the UTC timestamps Supabase returns.
 */
object IsoTimes {
    private const val PATTERN = "yyyy-MM-dd'T'HH:mm:ss"

    fun toIso(millis: Long): String {
        val format = SimpleDateFormat(PATTERN, Locale.US)
        format.timeZone = TimeZone.getTimeZone("UTC")
        return format.format(Date(millis)) + "Z"
    }

    fun toMillis(value: String): Long {
        val clock = value.substringBefore(".").take(PATTERN.length)
        val format = SimpleDateFormat(PATTERN, Locale.US)
        format.timeZone = TimeZone.getTimeZone("UTC")
        return format.parse(clock)?.time ?: System.currentTimeMillis()
    }
}
