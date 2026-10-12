package com.notabene.app

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Test

class RepeatHistoryTimeTest {
    private val zone = ZoneId.of("Europe/London")
    private val now = Instant.parse("2026-12-01T12:00:00Z").toEpochMilli()

    @Test fun usesLocalTimeForPastEvents() {
        assertEquals(Instant.parse("2026-07-01T07:15:00Z").toEpochMilli(),
            repeatHistoryTime(LocalDateTime.parse("2026-07-01T08:15:00"), zone, null, now))
    }

    @Test(expected = IllegalArgumentException::class) fun rejectsFutureEvents() {
        repeatHistoryTime(LocalDateTime.parse("2026-12-02T08:15:00"), zone, null, now)
    }

    @Test(expected = IllegalArgumentException::class) fun rejectsSpringClockGap() {
        repeatHistoryTime(LocalDateTime.parse("2026-03-29T01:30:00"), zone, null, now)
    }

    @Test fun preservesOffsetInRepeatedAutumnHour() {
        val time = LocalDateTime.parse("2026-10-25T01:30:00")
        assertEquals(Instant.parse("2026-10-25T01:30:00Z").toEpochMilli(), repeatHistoryTime(time, zone, ZoneOffset.UTC, now))
        assertEquals(Instant.parse("2026-10-25T00:30:00Z").toEpochMilli(), repeatHistoryTime(time, zone, ZoneOffset.ofHours(1), now))
    }
}
