package com.notabene.app

import android.content.Context
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationNineToTenTest {
    private val databaseName = "migration-nine-ten.db"
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
    fun migrationAddsAnEmptyAttachmentWithoutChangingExistingRecords() {
        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(databaseName)
                .callback(object : SupportSQLiteOpenHelper.Callback(9) {
                    override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                        db.execSQL("CREATE TABLE collection_entries (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, collectionId INTEGER NOT NULL, text TEXT NOT NULL, detail TEXT NOT NULL, intervalDays INTEGER NOT NULL, quantity INTEGER, restockAt INTEGER, notifyWhenDue INTEGER NOT NULL, notifyAt TEXT NOT NULL, done INTEGER NOT NULL, lastCompletedAt INTEGER, createdAt INTEGER NOT NULL)")
                        db.execSQL("INSERT INTO collection_entries (collectionId, text, detail, intervalDays, notifyWhenDue, notifyAt, done, createdAt) VALUES (7, 'Existing record', '', 0, 0, '09:00', 0, 1)")
                    }

                    override fun onUpgrade(db: androidx.sqlite.db.SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
                })
                .build()
        )
        helper.writableDatabase.use { database ->
            NotaBeneDatabase.MIGRATION_9_10.migrate(database)
            database.query("SELECT text, attachmentPath FROM collection_entries").use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals("Existing record", cursor.getString(0))
                assertEquals("", cursor.getString(1))
            }
        }
        helper.close()
    }
}
