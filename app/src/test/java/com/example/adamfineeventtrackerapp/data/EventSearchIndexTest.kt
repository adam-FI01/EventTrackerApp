package com.example.adamfineeventtrackerapp.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EventSearchIndexTest {

    // Dec 1, 2026 at noon
    private val now = EventDates.parse("12/01/2026", "12:00")!!

    private val events = listOf(
        Event(1, "Team Meeting", "12/05/2026", "09:00", "Office"),
        Event(2, "Dentist", "12/02/2026", "14:30", "Main Street"),
        Event(3, "Birthday Party", "11/20/2026", "18:00", "Office"),
        Event(4, "Old Meeting", "11/25/2026", "10:00", ""),
        Event(5, "Broken Date", "not a date", "??", "")
    )

    private val index = EventSearchIndex(events)

    private fun ids(items: List<EventListItem>) = items.map { it.event.eventId }

    @Test
    fun emptySearch_showsUpcomingSoonestFirst() {
        assertEquals(listOf(2, 1, 5), ids(index.search("", upcoming = true, now = now)))
    }

    @Test
    fun emptySearch_showsPastMostRecentFirst() {
        assertEquals(listOf(4, 3), ids(index.search("", upcoming = false, now = now)))
    }

    @Test
    fun search_ignoresCase() {
        assertEquals(listOf(1), ids(index.search("MEETING", upcoming = true, now = now)))
    }

    @Test
    fun search_needsEveryWord() {
        assertEquals(listOf(1), ids(index.search("team office", upcoming = true, now = now)))
        assertTrue(index.search("dentist office", upcoming = true, now = now).isEmpty())
    }

    @Test
    fun search_matchesLocation() {
        assertEquals(listOf(3), ids(index.search("office", upcoming = false, now = now)))
    }

    @Test
    fun search_matchesReadableDate() {
        assertEquals(listOf(2, 1), ids(index.search("dec", upcoming = true, now = now)))
        assertEquals(listOf(2), ids(index.search("wed", upcoming = true, now = now)))
    }

    @Test
    fun search_onlyMatchesWholeWords() {
        assertTrue(index.search("meet", upcoming = true, now = now).isEmpty())
    }

    @Test
    fun search_noMatchReturnsEmpty() {
        assertTrue(index.search("zebra", upcoming = true, now = now).isEmpty())
    }

    @Test
    fun parse_rejectsImpossibleDates() {
        assertNull(EventDates.parse("13/45/2026", "10:00"))
        assertNull(EventDates.parse("12/01/2026", "25:00"))
    }

    @Test
    fun readable_formatsDate() {
        val millis = EventDates.parse("12/02/2026", "14:30")!!
        assertEquals("Wed, Dec 2, 2026 at 2:30 PM", EventDates.readable(millis))
    }
}
