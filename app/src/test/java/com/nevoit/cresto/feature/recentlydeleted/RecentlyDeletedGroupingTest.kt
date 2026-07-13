package com.nevoit.cresto.feature.recentlydeleted

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class RecentlyDeletedGroupingTest {

    @Test
    fun daysRemaining_countsByCalendarDate() {
        val today = LocalDate.of(2026, 7, 10)

        assertEquals(
            30,
            recentlyDeletedDaysRemaining(
                deletedAt = LocalDateTime.of(2026, 7, 10, 23, 59),
                today = today
            )
        )
        assertEquals(
            29,
            recentlyDeletedDaysRemaining(
                deletedAt = LocalDateTime.of(2026, 7, 9, 0, 1),
                today = today
            )
        )
    }

    @Test
    fun daysRemaining_staysWithinRetentionRange() {
        val today = LocalDate.of(2026, 7, 10)

        assertEquals(
            0,
            recentlyDeletedDaysRemaining(
                deletedAt = LocalDateTime.of(2026, 6, 10, 12, 0),
                today = today
            )
        )
        assertEquals(
            0,
            recentlyDeletedDaysRemaining(
                deletedAt = LocalDateTime.of(2026, 6, 9, 12, 0),
                today = today
            )
        )
        assertEquals(
            30,
            recentlyDeletedDaysRemaining(
                deletedAt = LocalDateTime.of(2026, 7, 11, 12, 0),
                today = today
            )
        )
    }
}
