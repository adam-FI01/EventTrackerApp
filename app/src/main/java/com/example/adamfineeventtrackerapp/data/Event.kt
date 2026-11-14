package com.example.adamfineeventtrackerapp.data

/**
 * A single event stored in the events table.
 */
data class Event(
    val eventId: Int = 0,
    val eventName: String,
    val eventDate: String,
    val eventTime: String,
    val eventLocation: String
)
