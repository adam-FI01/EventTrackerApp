package com.example.adamfineeventtrackerapp.data

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Reads and formats event dates
object EventDates {

    const val INPUT_PATTERN = "MM/dd/yyyy HH:mm"
    private const val READABLE_PATTERN = "EEE, MMM d, yyyy 'at' h:mm a"

    fun parse(date: String, time: String): Long? = try {
        SimpleDateFormat(INPUT_PATTERN, Locale.US).apply { isLenient = false }
            .parse("${date.trim()} ${time.trim()}")?.time
    } catch (_: ParseException) {
        null
    }

    fun readable(millis: Long): String =
        SimpleDateFormat(READABLE_PATTERN, Locale.US).format(Date(millis))
}
