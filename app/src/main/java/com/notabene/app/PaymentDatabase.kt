package com.notabene.app

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "payments")
data class PaymentRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val merchant: String,
    val amount: String,
    val note: String,
    val capturedFrom: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<PaymentRecord>>

    @Insert
    suspend fun insert(payment: PaymentRecord)

    @Query("DELETE FROM payments WHERE id = :id")
    suspend fun delete(id: Long)
}

@Entity(tableName = "ask_items")
data class AskItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val done: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface AskDao {
    @Query("SELECT * FROM ask_items ORDER BY done ASC, createdAt DESC")
    fun observeAll(): Flow<List<AskItem>>

    @Insert
    suspend fun insert(item: AskItem)

    @Query("UPDATE ask_items SET done = :done WHERE id = :id")
    suspend fun setDone(id: Long, done: Boolean)
}

@Entity(tableName = "task_items")
data class TaskItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val waitingOn: String = "",
    val done: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface TaskDao {
    @Query("SELECT * FROM task_items ORDER BY done ASC, createdAt DESC")
    fun observeAll(): Flow<List<TaskItem>>

    @Insert
    suspend fun insert(item: TaskItem)

    @Query("UPDATE task_items SET done = :done WHERE id = :id")
    suspend fun setDone(id: Long, done: Boolean)
}

@Entity(tableName = "body_items")
data class BodyItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val observation: String,
    val measurement: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface BodyDao {
    @Query("SELECT * FROM body_items ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<BodyItem>>

    @Insert
    suspend fun insert(item: BodyItem)

    @Query("DELETE FROM body_items WHERE id = :id")
    suspend fun delete(id: Long)
}

@Database(entities = [PaymentRecord::class, AskItem::class, TaskItem::class, BodyItem::class], version = 4, exportSchema = false)
abstract class NotaBeneDatabase : RoomDatabase() {
    abstract fun paymentDao(): PaymentDao
    abstract fun askDao(): AskDao
    abstract fun taskDao(): TaskDao
    abstract fun bodyDao(): BodyDao

    companion object {
        @Volatile private var instance: NotaBeneDatabase? = null

        fun get(context: Context): NotaBeneDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                NotaBeneDatabase::class.java,
                "nota-bene.db"
            ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4).build().also { instance = it }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `ask_items` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `text` TEXT NOT NULL, `done` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL)"
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `task_items` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `text` TEXT NOT NULL, `waitingOn` TEXT NOT NULL, `done` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL)"
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `body_items` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `observation` TEXT NOT NULL, `measurement` TEXT NOT NULL, `createdAt` INTEGER NOT NULL)"
                )
            }
        }
    }
}
