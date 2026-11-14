package com.example.adamfineeventtrackerapp.data

data class MyEvent(
    val eventId: Int = 0,
    val eventName: String,
    val eventDate: String,
    val eventTime: String,
    val eventLocation: String
)

data class UserAccount(
    val user: String,
    val pass: String
)
