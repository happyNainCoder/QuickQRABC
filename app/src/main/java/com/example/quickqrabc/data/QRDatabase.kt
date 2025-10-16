package com.example.quickqrabc.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [QRHistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class QRDatabase : RoomDatabase() {
    
    abstract fun qrHistoryDao(): QRHistoryDao
    
    companion object {
        @Volatile
        private var INSTANCE: QRDatabase? = null
        
        fun getDatabase(context: Context): QRDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QRDatabase::class.java,
                    "qr_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
