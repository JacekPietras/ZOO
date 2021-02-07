package com.jacekpietras.logger

import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

@Suppress("unused")
class ThreadSafeDateFormat(
    private val pattern: String,
    timeZone: TimeZone? = TimeZone.getTimeZone("GMT")
) : ThreadLocal<DateFormat>() {

    var timeZone: TimeZone? = timeZone
        set(value) {
            field = value
            set(initialValue())
        }

    override fun initialValue(): DateFormat {
        val dateFormat = SimpleDateFormat(pattern, Locale.US)
        if (timeZone != null) {
            dateFormat.timeZone = timeZone!!
        }
        return dateFormat
    }

    fun format(timestamp: Long): String {
        return format(Date(timestamp))
    }

    fun format(timestamp: Date): String {
        val dateFormat = get() ?: initialValue()
        return dateFormat.format(timestamp)
    }

    fun parse(date: String?): Date? {
        if (date.isNullOrEmpty()) {
            return null
        }
        val dateFormat = get() ?: initialValue()
        return try {
            dateFormat.parse(date)
        } catch (e: ParseException) {
            null
        }
    }
}