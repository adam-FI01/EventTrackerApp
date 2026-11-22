package com.example.adamfineeventtrackerapp.ui.events

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.adamfineeventtrackerapp.data.EventListItem
import com.example.adamfineeventtrackerapp.data.EventRepository
import com.example.adamfineeventtrackerapp.reminders.ReminderScheduler

// Event list logic
class EventListViewModel(
    private val repository: EventRepository,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {

    var searchQuery by mutableStateOf("")
        private set

    // true = upcoming; false = past
    // TLDR: this is the binary switch
    var showUpcoming by mutableStateOf(true)
        private set

    var events by mutableStateOf<List<EventListItem>>(emptyList())
        private set

    // Message to show the user
    var message by mutableStateOf<String?>(null)
        private set

    init {
        repository.loadEvents()
        refreshEvents()
    }

    fun onSearchQueryChange(query: String) {
        searchQuery = query
        refreshEvents()

        // Switch tabs if the other one has matches
        // added because it looked cleaner from the UI perspective
        if (events.isEmpty() && query.isNotBlank()) {
            val otherTab = repository.search(query, !showUpcoming)
            if (otherTab.isNotEmpty()) {
                showUpcoming = !showUpcoming
                events = otherTab
            }
        }
    }

    fun onShowUpcomingChange(upcoming: Boolean) {
        showUpcoming = upcoming
        refreshEvents()
    }

    fun addEvent(name: String, date: String, time: String, location: String) {
        val newId = repository.addEvent(name, date, time, location)
        refreshEvents()
        message = if (newId == null) {
            "Could not save event. Please try again."
        } else {
            scheduleReminder(newId, name, date, time, successText = "Saved! SMS alert scheduled.")
        }
    }

    fun updateEvent(id: Int, name: String, date: String, time: String, location: String) {
        repository.updateEvent(id, name, date, time, location)
        refreshEvents()
        message = scheduleReminder(id, name, date, time, successText = "Updated! SMS alert rescheduled.")
    }

    fun deleteEvent(id: Int) {
        repository.deleteEvent(id)
        reminderScheduler.cancel(id)
        refreshEvents()
    }

    fun onMessageShown() { message = null }

    private fun refreshEvents() {
        events = repository.search(searchQuery, showUpcoming)
    }

    // Sets the reminder and picks a message
    private fun scheduleReminder(id: Int, name: String, date: String, time: String, successText: String): String? =
        when (reminderScheduler.schedule(id, name, date, time)) {
            ReminderScheduler.Result.SCHEDULED -> successText
            ReminderScheduler.Result.INVALID_DATE -> "Format error. Use MM/dd/yyyy and HH:mm"
            ReminderScheduler.Result.NO_PERMISSION -> null
        }

    companion object {
        fun factory(repository: EventRepository, reminderScheduler: ReminderScheduler): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { EventListViewModel(repository, reminderScheduler) }
            }
    }
}
