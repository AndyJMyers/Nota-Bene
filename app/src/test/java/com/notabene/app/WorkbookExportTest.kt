package com.notabene.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipInputStream

class WorkbookExportTest {
    @Test
    fun `workbook contains exactly the five gospel sheets`() {
        val entries = workbookEntries(WorkbookSnapshot())
        val workbook = entries.getValue("xl/workbook.xml")
        assertTrue(workbook.contains("name=\"SPEND\""))
        assertTrue(workbook.contains("name=\"MEDS\""))
        assertTrue(workbook.contains("name=\"SOMA\""))
        assertTrue(workbook.contains("name=\"TASK\""))
        assertTrue(workbook.contains("name=\"ASK\""))
        assertEquals(5, entries.keys.count { it.startsWith("xl/worksheets/sheet") })
    }

    @Test
    fun `MEDS exports repeated same-day doses and usual count`() {
        val medicine = Medication(
            id = 7,
            name = "Example",
            dosage = "one measure",
            doseTime = "08:00",
            dailyTarget = 2,
            startingDoses = 12,
            reorderAt = 3
        )
        val snapshot = WorkbookSnapshot(
            medicines = listOf(medicine),
            doses = listOf(
                DoseLog(id = 1, medicationId = 7, doseDate = "2026-08-29", scheduledFor = 1_000, takenAt = 2_000),
                DoseLog(id = 2, medicationId = 7, doseDate = "2026-08-29", scheduledFor = 1_000, takenAt = 3_000)
            )
        )
        val medsSheet = workbookEntries(snapshot).getValue("xl/worksheets/sheet5.xml")
        assertEquals(2, "Dose taken".toRegex().findAll(medsSheet).count())
        assertTrue(medsSheet.contains("<v>2</v>"))
        assertTrue(medsSheet.contains("Example"))
    }

    @Test
    fun `all record types and completion states are retained`() {
        val snapshot = WorkbookSnapshot(
            payments = listOf(PaymentRecord(merchant = "Shop", amount = "4.20", note = "note", capturedFrom = "manual")),
            asks = listOf(AskItem(text = "Question", done = true)),
            tasks = listOf(TaskItem(text = "Task", waitingOn = "Parcel", done = true)),
            body = listOf(BodyItem(observation = "Fine", measurement = "120/80"))
        )
        val entries = workbookEntries(snapshot)
        assertTrue(entries.getValue("xl/worksheets/sheet1.xml").contains("Shop"))
        assertTrue(entries.getValue("xl/worksheets/sheet2.xml").contains("Question"))
        assertTrue(entries.getValue("xl/worksheets/sheet2.xml").contains("Yes"))
        assertTrue(entries.getValue("xl/worksheets/sheet3.xml").contains("Parcel"))
        assertTrue(entries.getValue("xl/worksheets/sheet4.xml").contains("120/80"))
    }

    @Test
    fun `spreadsheet XML escapes punctuation and strips illegal controls`() {
        val snapshot = WorkbookSnapshot(
            payments = listOf(
                PaymentRecord(merchant = "A&B <Market>", amount = "1.00", note = "safe\u0001text", capturedFrom = "manual")
            )
        )
        val sheet = workbookEntries(snapshot).getValue("xl/worksheets/sheet1.xml")
        assertTrue(sheet.contains("A&amp;B &lt;Market&gt;"))
        assertTrue(sheet.contains("safetext"))
        assertFalse(sheet.contains('\u0001'))
    }

    private fun workbookEntries(snapshot: WorkbookSnapshot): Map<String, String> {
        val output = ByteArrayOutputStream()
        writeWorkbook(snapshot, output)
        val entries = linkedMapOf<String, String>()
        ZipInputStream(ByteArrayInputStream(output.toByteArray())).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                entries[entry.name] = zip.readBytes().toString(Charsets.UTF_8)
            }
        }
        return entries
    }
}
