package com.notabene.app

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

internal enum class CalendarRepeat(val label: String) {
    NONE("Never"), DAILY("Daily"), WEEKLY("Weekly"), MONTHLY("Monthly"), YEARLY("Yearly")
}

/** Compute from the original anchor, so 31 January → February end → 31 March. */
internal fun nextCalendarOccurrence(start: LocalDateTime, repeat: CalendarRepeat, now: LocalDateTime): LocalDateTime? {
    if (!start.isBefore(now)) return start
    if (repeat == CalendarRepeat.NONE) return null
    val steps = when (repeat) {
        CalendarRepeat.DAILY -> ChronoUnit.DAYS.between(start.toLocalDate(), now.toLocalDate())
        CalendarRepeat.WEEKLY -> ChronoUnit.DAYS.between(start.toLocalDate(), now.toLocalDate()) / 7
        CalendarRepeat.MONTHLY -> ChronoUnit.MONTHS.between(start.toLocalDate().withDayOfMonth(1), now.toLocalDate().withDayOfMonth(1))
        CalendarRepeat.YEARLY -> (now.year - start.year).toLong()
        CalendarRepeat.NONE -> 0
    }.coerceAtLeast(0)
    fun occurrence(n: Long) = when (repeat) {
        CalendarRepeat.DAILY -> start.plusDays(n)
        CalendarRepeat.WEEKLY -> start.plusWeeks(n)
        CalendarRepeat.MONTHLY -> start.plusMonths(n)
        CalendarRepeat.YEARLY -> start.plusYears(n)
        CalendarRepeat.NONE -> start
    }
    val candidate = occurrence(steps)
    return if (candidate.isBefore(now)) occurrence(steps + 1) else candidate
}
