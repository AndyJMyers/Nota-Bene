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

@Database(entities = [PaymentRecord::class], version = 1, exportSchema = false)
abstract class NotaBeneDatabase : RoomDatabase() {
    abstract fun paymentDao(): PaymentDao

    companion object {
        @Volatile private var instance: NotaBeneDatabase? = null

        fun get(context: Context): NotaBeneDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                NotaBeneDatabase::class.java,
                "nota-bene.db"
            ).build().also { instance = it }
        }
    }
}
