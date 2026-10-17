package com.notabene.app

import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CalendarRulesTest {
    private fun next(start: String, repeat: CalendarRepeat, now: String) =
        nextCalendarOccurrence(LocalDateTime.parse(start), repeat, LocalDateTime.parse(now))?.toString()

    @Test fun oneOffIsShownUntilItsTimeAndThenBecomesPast() {
        assertEquals("2026-10-17T12:00", next("2026-10-17T12:00", CalendarRepeat.NONE, "2026-10-17T12:00"))
        assertNull(next("2026-10-17T12:00", CalendarRepeat.NONE, "2026-10-17T12:01"))
    }
    @Test fun futureAnchorDoesNotMove() {
        assertEquals("2026-11-01T08:00", next("2026-11-01T08:00", CalendarRepeat.DAILY, "2026-10-17T12:00"))
    }
    @Test fun dailyKeepsWallClockTime() {
        assertEquals("2026-10-25T08:00", next("2026-10-01T08:00", CalendarRepeat.DAILY, "2026-10-24T09:00"))
    }
    @Test fun weeklyKeepsWeekdayAndTime() {
        assertEquals("2026-10-24T12:00", next("2026-10-17T12:00", CalendarRepeat.WEEKLY, "2026-10-17T12:01"))
    }
    @Test fun monthlyClampsWithoutDrifting() {
        assertEquals("2026-02-28T09:00", next("2026-01-31T09:00", CalendarRepeat.MONTHLY, "2026-02-01T00:00"))
        assertEquals("2026-03-31T09:00", next("2026-01-31T09:00", CalendarRepeat.MONTHLY, "2026-03-01T00:00"))
    }
    @Test fun leapDayReturnsInLeapYear() {
        assertEquals("2025-02-28T09:00", next("2024-02-29T09:00", CalendarRepeat.YEARLY, "2025-01-01T00:00"))
        assertEquals("2028-02-29T09:00", next("2024-02-29T09:00", CalendarRepeat.YEARLY, "2028-01-01T00:00"))
    }
    @Test fun oldAnchorsJumpDirectlyToUpcomingOccurrence() {
        assertEquals("2026-10-18T08:00", next("1926-01-01T08:00", CalendarRepeat.DAILY, "2026-10-17T12:00"))
    }
}
