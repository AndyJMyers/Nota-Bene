package com.notabene.app

import java.io.ByteArrayOutputStream
import org.junit.Assert.*
import org.junit.Test

class WorkbookImportTest {
    private fun roundTrip(snapshot: WorkbookSnapshot): WorkbookSnapshot {
        val bytes = ByteArrayOutputStream()
        writeWorkbook(snapshot, bytes)
        return readWorkbook(bytes.toByteArray().inputStream())
    }

    @Test fun `all five record types and repeated doses survive export and import`() {
        val med = Medication(id = 42, name = "Example", dosage = "5 mg", doseTime = "08:00",
            dailyTarget = 2, startingDoses = 30, reorderAt = 7, active = false)
        val source = WorkbookSnapshot(
            payments = listOf(PaymentRecord(merchant = "A & B", amount = "12.00", note = "<receipt>", capturedFrom = "Manual")),
            asks = listOf(AskItem(text = "Why?", done = true)),
            tasks = listOf(TaskItem(text = "Research", waitingOn = "Library", done = true)),
            body = listOf(BodyItem(observation = "Fine", measurement = "120/80")),
            medicines = listOf(med),
            doses = listOf(DoseLog(medicationId = 42, doseDate = "2026-09-04", scheduledFor = 1788508800000),
                DoseLog(medicationId = 42, doseDate = "2026-09-04", scheduledFor = 1788508800000))
        )
        val result = roundTrip(source)
        assertEquals("A & B", result.payments.single().merchant)
        assertEquals("<receipt>", result.payments.single().note)
        assertTrue(result.asks.single().done)
        assertEquals("Library", result.tasks.single().waitingOn)
        assertEquals("120/80", result.body.single().measurement)
        assertEquals(30, result.medicines.single().startingDoses)
        assertFalse(result.medicines.single().active)
        assertEquals(2, result.doses.size)
        assertTrue(result.doses.all { it.medicationId == result.medicines.single().id })
    }

    @Test(expected = IllegalArgumentException::class)
    fun `ambiguous medicine names reject the whole workbook`() {
        val med = Medication(id = 1, name = "Same", dosage = "5 mg", doseTime = "08:00", startingDoses = 20, reorderAt = 5)
        roundTrip(WorkbookSnapshot(medicines = listOf(med, med.copy(id = 2)),
            doses = listOf(DoseLog(medicationId = 1, doseDate = "2026-09-04", scheduledFor = 1788508800000))))
    }

    @Test fun `invalid archive is rejected`() {
        assertThrows(Exception::class.java) { readWorkbook("not a workbook".byteInputStream()) }
    }

    @Test fun `empty workbook is valid`() {
        assertEquals(0, roundTrip(WorkbookSnapshot()).medicines.size)
    }
}
