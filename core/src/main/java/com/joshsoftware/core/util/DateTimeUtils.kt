package com.joshsoftware.core.util

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DateTimeUtils {
    fun getLastUpdatedFormatted(timestamp: String): String {
        val inputFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val datetime: LocalDateTime = LocalDateTime.parse(timestamp, inputFormat)
        val today = LocalDateTime.now()
        if(datetime.dayOfMonth == today.dayOfMonth &&
                datetime.year == today.year &&
                datetime.month == today.month) {
            val format: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE d, hh:mm a")
            return datetime.format(format)
        } else {
            val format: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE d, hh:mm a")
            return datetime.format(format)
        }
    }

    fun getCurrentTime(): String {
        val datetime: LocalDateTime = LocalDateTime.now()
        val format: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return datetime.format(format)
    }
}