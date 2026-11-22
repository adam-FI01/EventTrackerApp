package com.example.adamfineeventtrackerapp.data

// Saves and searches events
class EventRepository(
    private val db: DatabaseHelper,
    private val now: () -> Long = System::currentTimeMillis
) {

    private var index = EventSearchIndex(emptyList())

    fun loadEvents() {
        index = EventSearchIndex(db.getAllEvents())
    }

    fun search(query: String, upcoming: Boolean): List<EventListItem> =
        index.search(query, upcoming, now())

    // null if it didn't save
    fun addEvent(name: String, date: String, time: String, location: String): Int? {
        val newId = db.addEvent(name, date, time, location)
        loadEvents()
        return if (newId == -1L) null else newId.toInt()
    }

    fun updateEvent(id: Int, name: String, date: String, time: String, location: String) {
        db.updateEvent(id, name, date, time, location)
        loadEvents()
    }

    fun deleteEvent(id: Int) {
        db.deleteEvent(id)
        loadEvents()
    }
}
