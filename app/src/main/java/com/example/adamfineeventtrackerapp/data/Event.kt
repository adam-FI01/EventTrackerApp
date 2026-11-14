package com.example.adamfineeventtrackerapp.data

// One event
data class Event(
    val eventId: Int = 0,
    val eventName: String,
    val eventDate: String,
    val eventTime: String,
    val eventLocation: String
)
