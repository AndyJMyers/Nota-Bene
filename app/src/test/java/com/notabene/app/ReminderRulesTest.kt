package com.notabene.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalTime

class ReminderRulesTest {
    private val scheduled = LocalTime.of(8, 0)

    @Test
    fun `nothing happens before scheduled time`() {
        assertNull(decide(now = LocalTime.of(7, 59)))
    }

    @Test
    fun `due stage begins at scheduled time`() {
        assertEquals(ReminderStage.DUE, decide(now = scheduled))
    }

    @Test
    fun `due stage is not repeated`() {
        assertNull(decide(now = LocalTime.NOON, dueSent = true))
    }

    @Test
    fun `evening stage begins at six`() {
        assertEquals(ReminderStage.EVENING, decide(now = LocalTime.of(18, 0)))
    }

    @Test
    fun `evening reminder can follow an earlier due reminder`() {
        assertEquals(ReminderStage.EVENING, decide(now = LocalTime.of(20, 0), dueSent = true))
    }

    @Test
    fun `evening stage is not repeated`() {
        assertNull(decide(now = LocalTime.of(20, 0), dueSent = true, eveningSent = true))
    }

    @Test
    fun `any recorded dose suppresses both stages`() {
        assertNull(decide(now = LocalTime.NOON, recorded = true))
        assertNull(decide(now = LocalTime.of(20, 0), recorded = true))
    }

    @Test
    fun `evening does not override a later scheduled time`() {
        assertNull(
            reminderStage(
                scheduledTime = LocalTime.of(21, 0),
                currentTime = LocalTime.of(18, 30),
                doseRecordedToday = false,
                dueAlreadyNotified = false,
                eveningAlreadyNotified = false
            )
        )
    }

    private fun decide(
        now: LocalTime,
        recorded: Boolean = false,
        dueSent: Boolean = false,
        eveningSent: Boolean = false
    ) = reminderStage(scheduled, now, recorded, dueSent, eveningSent)
}
