package com.example.adamfineeventtrackerapp.data

data class MyEvent(
    val eventId: Int,
    val eventName: String,
    val eventDate: String,
    val eventLocation: String
)

data class UserAccount(
    val user: String,
    val pass: String
)
