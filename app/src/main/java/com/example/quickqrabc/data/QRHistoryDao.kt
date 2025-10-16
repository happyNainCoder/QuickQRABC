package com.example.quickqrabc.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface QRHistoryDao {
    
    @Query("SELECT * FROM qr_history ORDER BY createdAt DESC")
    fun getAllHistory(): LiveData<List<QRHistoryEntity>>
    
    @Query("SELECT * FROM qr_history WHERE type = :type ORDER BY createdAt DESC")
    fun getHistoryByType(type: String): LiveData<List<QRHistoryEntity>>
    
    @Insert
    suspend fun insertHistory(history: QRHistoryEntity)
    
    @Delete
    suspend fun deleteHistory(history: QRHistoryEntity)
    
    @Query("DELETE FROM qr_history")
    suspend fun deleteAllHistory()
    
    @Query("SELECT COUNT(*) FROM qr_history")
    suspend fun getHistoryCount(): Int
}
