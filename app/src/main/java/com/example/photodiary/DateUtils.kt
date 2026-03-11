package com.example.photodiary

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val displayDateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

fun Long.toDayStartMillis(): Long {
    return Calendar.getInstance().apply {
        timeInMillis = this@toDayStartMillis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

fun Long.toDisplayDate(): String = displayDateFormatter.format(Date(this))
