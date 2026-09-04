package com.notabene.app

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@RunWith(AndroidJUnit4::class)
class WorkbookImportDeviceTest {
    @Test fun exportedRecordsImportOnAndroid() = runBlocking {
        val medicine = Medication(id = 9, name = "Example", dosage = "5 mg", doseTime = "08:00", startingDoses = 20, reorderAt = 7)
        val snapshot = WorkbookSnapshot(
            payments = listOf(PaymentRecord(merchant = "Books & things", amount = "12", note = "", capturedFrom = "Manual")),
            asks = listOf(AskItem(text = "Why?", done = true)),
            tasks = listOf(TaskItem(text = "Read", waitingOn = "Book")),
            body = listOf(BodyItem(observation = "Fine", measurement = "120/80")),
            medicines = listOf(medicine),
            doses = List(2) { DoseLog(medicationId = 9, doseDate = "2026-09-04", scheduledFor = 1788508800000) }
        )
        val bytes = ByteArrayOutputStream()
        writeWorkbook(snapshot, bytes)
        val parsed = readWorkbook(bytes.toByteArray().inputStream())
        val db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), NotaBeneDatabase::class.java).build()
        try {
            db.askDao().insert(AskItem(text = "Existing record"))
            importWorkbook(db, parsed)
            assertEquals(2, db.askDao().observeAll().first().size)
            assertEquals("Books & things", db.paymentDao().observeAll().first().single().merchant)
            assertEquals("Book", db.taskDao().observeAll().first().single().waitingOn)
            assertEquals("120/80", db.bodyDao().observeAll().first().single().measurement)
            val med = db.medicationDao().observeMedications().first().single()
            val doses = db.medicationDao().observeDoseLogs().first()
            assertEquals(20, med.startingDoses)
            assertEquals(2, doses.size)
            assertTrue(doses.all { it.medicationId == med.id })
            // An invalid link must roll back the preceding inserts as well.
            try {
                importWorkbook(db, parsed.copy(doses = listOf(parsed.doses.first().copy(medicationId = -1))))
                fail("Expected missing medicine reference to fail")
            } catch (_: NoSuchElementException) { }
            assertEquals(2, db.askDao().observeAll().first().size)
            assertEquals(1, db.medicationDao().observeMedications().first().size)
        } finally { db.close() }
    }

    @Test fun xmlDeclarationsAreRejectedBeforeParsing() {
        for (xml in listOf("<!DOCTYPE workbook [<!ENTITY x SYSTEM 'file:///private'>]><workbook>&x;</workbook>", "<workbook/>\u0000")) {
            val bytes = ByteArrayOutputStream()
            ZipOutputStream(bytes).use {
                it.putNextEntry(ZipEntry("xl/workbook.xml"))
                it.write(xml.toByteArray())
                it.closeEntry()
            }
            try { readWorkbook(bytes.toByteArray().inputStream()); fail("Unsafe XML accepted") }
            catch (_: IllegalArgumentException) { }
        }
    }
}
