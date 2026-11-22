package com.example.adamfineeventtrackerapp.data

// An event with a readable date
data class EventListItem(
    val event: Event,
    val readableDate: String,
    val startsAt: Long? // null if the date is bad
) {
    fun isUpcoming(now: Long): Boolean = startsAt == null || startsAt >= now
}

// Sorts events and maps each word to event ids
class EventSearchIndex(events: List<Event>) {

    private val items: List<EventListItem>
    private val wordIndex = HashMap<String, MutableSet<Int>>()

    init {
        val allItems = events.map { toListItem(it) }

        // Soonest first, bad dates at the bottom
        val goodDates = allItems.filter { it.startsAt != null }.sortedBy { it.startsAt }
        val badDates = allItems.filter { it.startsAt == null }
        items = goodDates + badDates

        for (item in items) {
            val text = "${item.event.eventName} ${item.event.eventLocation} ${item.readableDate}"
            for (word in splitWords(text)) {
                wordIndex.getOrPut(word) { mutableSetOf() }.add(item.event.eventId)
            }
        }
    }

    fun search(query: String, upcoming: Boolean, now: Long): List<EventListItem> {
        val words = splitWords(query)
        val results = mutableListOf<EventListItem>()

        for (item in items) {
            if (item.isUpcoming(now) != upcoming) continue
            if (matchesAllWords(item.event.eventId, words)) {
                results.add(item)
            }
        }

        // Newest past event first
        return if (upcoming) results else results.reversed()
    }

    private fun matchesAllWords(eventId: Int, words: List<String>): Boolean {
        for (word in words) {
            val ids = wordIndex[word]
            if (ids == null || eventId !in ids) return false
        }
        return true
    }

    private fun toListItem(event: Event): EventListItem {
        val startsAt = EventDates.parse(event.eventDate, event.eventTime)
        val readable = startsAt?.let { EventDates.readable(it) } ?: "${event.eventDate} ${event.eventTime}"
        return EventListItem(event, readable, startsAt)
    }

    companion object {
        fun splitWords(text: String): List<String> =
            text.lowercase().split(Regex("[^a-z0-9]+")).filter { it.isNotBlank() }
    }
}
