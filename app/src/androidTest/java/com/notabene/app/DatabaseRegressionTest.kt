package com.notabene.app

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseRegressionTest {
    private lateinit var database: NotaBeneDatabase
    private lateinit var context: Context

    @Before
    fun openDatabase() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, NotaBeneDatabase::class.java).build()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun deleteDoneKeepsOpenTasksAndOtherCollections() = runBlocking {
        val dao = database.collectionDao()
        val selected = dao.insertCollection(Collection(title = "Jobs", kind = "TODO"))
        val other = dao.insertCollection(Collection(title = "Other", kind = "TODO"))
        dao.insertEntry(CollectionEntry(id = 101, collectionId = selected, text = "Done", done = true))
        dao.insertEntry(CollectionEntry(id = 102, collectionId = selected, text = "Open"))
        dao.insertEntry(CollectionEntry(id = 103, collectionId = other, text = "Other done", done = true))
        dao.insertRepeatEvent(RepeatEvent(entryId = 101, occurredAt = 1))

        assertEquals(listOf(101L), dao.deleteCompletedEntries(selected).map { it.id })
        assertEquals(listOf("Open"), dao.observeEntries(selected).first().map { it.text })
        assertEquals(listOf("Other done"), dao.observeEntries(other).first().map { it.text })
        assertTrue(dao.observeRepeatEvents(101).first().isEmpty())
        assertTrue(dao.deleteCompletedEntries(selected).isEmpty())
    }

    @Test
    fun correctingRepeatHistoryRecalculatesLatestWithoutChangingStock() = runBlocking {
        val dao = database.collectionDao()
        val collection = dao.insertCollection(Collection(title = "Routines", kind = "REPEAT"))
        dao.insertEntry(CollectionEntry(id = 201, collectionId = collection, text = "Water fern", quantity = 12))
        val first = dao.recordRepeatEvent(201, 2000)
        val latest = dao.recordRepeatEvent(201, 3000)
        val backdated = dao.recordRepeatEvent(201, 1000)
        assertEquals(3000L, dao.observeEntries(collection).first().single().lastCompletedAt)
        dao.correctRepeatEvent(201, latest, 500)
        assertEquals(2000L, dao.observeEntries(collection).first().single().lastCompletedAt)
        dao.removeRepeatEvent(201, first)
        assertEquals(1000L, dao.observeEntries(collection).first().single().lastCompletedAt)
        dao.removeRepeatEvent(201, backdated)
        dao.removeRepeatEvent(201, latest)
        val entry = dao.observeEntries(collection).first().single()
        assertEquals(null, entry.lastCompletedAt)
        assertEquals(12, entry.quantity)
        assertFalse(entry.done)
    }

    @Test
    fun undoRemovesOnlyItsExactEventAndIsSafeToRepeat() = runBlocking {
        val dao = database.collectionDao()
        val collection = dao.insertCollection(Collection(title = "Routines", kind = "REPEAT"))
        dao.insertEntry(CollectionEntry(id = 202, collectionId = collection, text = "Same time"))
        dao.insertEntry(CollectionEntry(id = 203, collectionId = collection, text = "Other"))
        val first = dao.recordRepeatEvent(202, 1000)
        val second = dao.recordRepeatEvent(202, 1000)
        val other = dao.recordRepeatEvent(203, 2000)
        dao.removeRepeatEvent(202, second)
        dao.removeRepeatEvent(202, second)
        dao.removeRepeatEvent(202, other)
        dao.correctRepeatEvent(202, other, 3000)
        assertEquals(listOf(first), dao.observeRepeatEvents(202).first().map { it.id })
        assertEquals(2000L, dao.observeRepeatEvents(203).first().single().occurredAt)
        dao.deleteEntryAndEvents(202)
        assertEquals(0L, dao.recordRepeatEvent(202, 4000))
        assertTrue(dao.observeRepeatEvents(202).first().isEmpty())
    }

    @Test
    fun historyCorrectionClearsOnlyItsOwnReminderMarkers() {
        val preferences = context.getSharedPreferences("repeat_reminders", Context.MODE_PRIVATE)
        preferences.edit().putBoolean("201:123", true).putBoolean("202:123", true).commit()
        RepeatReminderScheduler.historyChanged(context, 201)
        assertFalse(preferences.contains("201:123"))
        assertTrue(preferences.getBoolean("202:123", false))
        preferences.edit().remove("202:123").commit()
    }

    @Test
    fun repeatedSameDayDosesArePersisted() = runBlocking {
        val dao = database.medicationDao()
        val medicationId = dao.insertMedication(
            Medication(
                name = "Example",
                dosage = "one measure",
                doseTime = "08:00",
                dailyTarget = 2,
                startingDoses = 10,
                reorderAt = 2
            )
        )
        dao.insertDoseLog(DoseLog(medicationId = medicationId, doseDate = "2026-08-29", scheduledFor = 1, takenAt = 2))
        dao.insertDoseLog(DoseLog(medicationId = medicationId, doseDate = "2026-08-29", scheduledFor = 1, takenAt = 3))

        val logs = dao.observeDoseLogs().first()
        assertEquals(2, logs.size)
        assertEquals(8, remainingDoses(10, logs.size))
    }

    @Test
    fun stockCorrectionAndHaltArePersistent() = runBlocking {
        val dao = database.medicationDao()
        val id = dao.insertMedication(
            Medication(name = "Example", dosage = "one", doseTime = "09:00", startingDoses = 7, reorderAt = 2)
        )
        dao.setStartingDoses(id, 14)
        dao.setActive(id, false)

        val saved = dao.observeMedications().first().single()
        assertEquals(14, saved.startingDoses)
        assertTrue(!saved.active)
    }

    @Test
    fun clearAllTablesRemovesEveryInstrumentAndDoseHistory() = runBlocking {
        database.paymentDao().insert(PaymentRecord(merchant = "Shop", amount = "1.00", note = "", capturedFrom = "manual"))
        database.askDao().insert(AskItem(text = "Ask"))
        database.taskDao().insert(TaskItem(text = "Task"))
        database.bodyDao().insert(BodyItem(observation = "Observation"))
        val medicationId = database.medicationDao().insertMedication(
            Medication(name = "Example", dosage = "one", doseTime = "08:00", startingDoses = 2, reorderAt = 1)
        )
        database.medicationDao().insertDoseLog(
            DoseLog(medicationId = medicationId, doseDate = "2026-08-29", scheduledFor = 1, takenAt = 2)
        )

        database.clearAllTables()

        assertTrue(database.paymentDao().observeAll().first().isEmpty())
        assertTrue(database.askDao().observeAll().first().isEmpty())
        assertTrue(database.taskDao().observeAll().first().isEmpty())
        assertTrue(database.bodyDao().observeAll().first().isEmpty())
        assertTrue(database.medicationDao().observeMedications().first().isEmpty())
        assertTrue(database.medicationDao().observeDoseLogs().first().isEmpty())
    }

    @Test
    fun eraseReminderDataClearsReminderMarkers() {
        val preferences = context.getSharedPreferences("meds_reminders", Context.MODE_PRIVATE)
        preferences.edit().putBoolean("7:2026-08-29:due", true).commit()

        MedicineReminderScheduler.eraseReminderData(context)

        assertTrue(preferences.all.isEmpty())
    }

    @Test
    fun reminderTogglePersistsBothStates() {
        MedicineReminderScheduler.setRemindersEnabled(context, false)
        assertFalse(MedicineReminderScheduler.remindersEnabled(context))

        MedicineReminderScheduler.setRemindersEnabled(context, true)
        assertTrue(MedicineReminderScheduler.remindersEnabled(context))
    }
}
