package com.notabene.app

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationFiveToSixTest {
    private val databaseName = "migration-five-six.db"
    private lateinit var context: Context

    @Before
    fun prepare() {
        context = ApplicationProvider.getApplicationContext()
        context.deleteDatabase(databaseName)
    }

    @After
    fun cleanUp() {
        context.deleteDatabase(databaseName)
    }

    @Test
    fun migrationPreservesHistoryAndAllowsAnotherDoseOnSameDate() {
        createVersionFiveDatabase()

        val database = Room.databaseBuilder(context, NotaBeneDatabase::class.java, databaseName)
            .addMigrations(NotaBeneDatabase.MIGRATION_5_6)
            .build()
        try {
            runBlocking {
                val dao = database.medicationDao()
                val medication = dao.observeMedications().first().single()
                assertEquals(1, medication.dailyTarget)
                assertEquals(1, dao.observeDoseLogs().first().size)

                dao.insertDoseLog(
                    DoseLog(
                        medicationId = medication.id,
                        doseDate = "2026-08-29",
                        scheduledFor = 1_000,
                        takenAt = 3_000
                    )
                )
                assertEquals(2, dao.observeDoseLogs().first().size)
            }
        } finally {
            database.close()
        }
    }

    private fun createVersionFiveDatabase() {
        val configuration = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(databaseName)
            .callback(object : SupportSQLiteOpenHelper.Callback(5) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL("CREATE TABLE IF NOT EXISTS `payments` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `merchant` TEXT NOT NULL, `amount` TEXT NOT NULL, `note` TEXT NOT NULL, `capturedFrom` TEXT NOT NULL, `createdAt` INTEGER NOT NULL)")
                    db.execSQL("CREATE TABLE IF NOT EXISTS `ask_items` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `text` TEXT NOT NULL, `done` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL)")
                    db.execSQL("CREATE TABLE IF NOT EXISTS `task_items` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `text` TEXT NOT NULL, `waitingOn` TEXT NOT NULL, `done` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL)")
                    db.execSQL("CREATE TABLE IF NOT EXISTS `body_items` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `observation` TEXT NOT NULL, `measurement` TEXT NOT NULL, `createdAt` INTEGER NOT NULL)")
                    db.execSQL("CREATE TABLE IF NOT EXISTS `medications` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `dosage` TEXT NOT NULL, `doseTime` TEXT NOT NULL, `startingDoses` INTEGER NOT NULL, `reorderAt` INTEGER NOT NULL, `active` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL)")
                    db.execSQL("CREATE TABLE IF NOT EXISTS `dose_logs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `medicationId` INTEGER NOT NULL, `doseDate` TEXT NOT NULL, `scheduledFor` INTEGER NOT NULL, `takenAt` INTEGER NOT NULL)")
                    db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_dose_logs_medicationId_doseDate` ON `dose_logs` (`medicationId`, `doseDate`)")
                    db.execSQL("INSERT INTO `medications` (`id`,`name`,`dosage`,`doseTime`,`startingDoses`,`reorderAt`,`active`,`createdAt`) VALUES (7,'Example','one','08:00',10,2,1,1)")
                    db.execSQL("INSERT INTO `dose_logs` (`id`,`medicationId`,`doseDate`,`scheduledFor`,`takenAt`) VALUES (11,7,'2026-08-29',1000,2000)")
                }

                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
            })
            .build()
        FrameworkSQLiteOpenHelperFactory().create(configuration).use { helper ->
            helper.writableDatabase
        }
    }
}
