package com.notabene.app

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
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
}
