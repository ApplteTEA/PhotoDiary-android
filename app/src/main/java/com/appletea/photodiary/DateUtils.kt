package com.appletea.photodiary

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import org.json.JSONArray

private val displayDateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
private val koreanDisplayDateFormatter = SimpleDateFormat("yyyy년 M월 d일", Locale.getDefault())

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

fun Long.toKoreanDisplayDate(): String = koreanDisplayDateFormatter.format(Date(this))

fun String?.toImagePathList(): List<String> {
    if (this.isNullOrBlank()) return emptyList()

    return runCatching {
        val json = JSONArray(this)
        List(json.length()) { index -> json.optString(index) }
            .filter { it.isNotBlank() }
    }.getOrElse {
        listOf(this)
    }
}

fun List<String>.toImagePathPayload(): String? {
    if (isEmpty()) return null
    val json = JSONArray()
    forEach { path ->
        if (path.isNotBlank()) {
            json.put(path)
        }
    }
    return if (json.length() == 0) null else json.toString()
}
