package com.example.adamfineeventtrackerapp.ui.events

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.adamfineeventtrackerapp.data.DatabaseHelper
import com.example.adamfineeventtrackerapp.data.Event
import com.example.adamfineeventtrackerapp.reminders.ReminderScheduler

// Event list logic
class EventListViewModel(
    private val db: DatabaseHelper,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {

    var events by mutableStateOf(db.getAllEvents())
        private set

    // Message to show the user
    var message by mutableStateOf<String?>(null)
        private set

    fun addEvent(name: String, date: String, time: String, location: String) {
        val newId = db.addEvent(name, date, time, location).toInt()
        refreshEvents()
        message = scheduleReminder(newId, name, date, time, successText = "Saved! SMS alert scheduled.")
    }

    fun updateEvent(id: Int, name: String, date: String, time: String, location: String) {
        db.updateEvent(id, name, date, time, location)
        refreshEvents()
        message = scheduleReminder(id, name, date, time, successText = "Updated! SMS alert rescheduled.")
    }

    fun deleteEvent(id: Int) {
        db.deleteEvent(id)
        reminderScheduler.cancel(id)
        refreshEvents()
    }

    fun onMessageShown() { message = null }

    private fun refreshEvents() {
        events = db.getAllEvents()
    }

    // Sets the reminder and picks a message
    private fun scheduleReminder(id: Int, name: String, date: String, time: String, successText: String): String? =
        when (reminderScheduler.schedule(id, name, date, time)) {
            ReminderScheduler.Result.SCHEDULED -> successText
            ReminderScheduler.Result.INVALID_DATE -> "Format error. Use MM/dd/yyyy and HH:mm"
            ReminderScheduler.Result.NO_PERMISSION -> null
        }

    companion object {
        fun factory(db: DatabaseHelper, reminderScheduler: ReminderScheduler): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { EventListViewModel(db, reminderScheduler) }
            }
    }
}
