package com.example.quickqrabc.repository

import androidx.lifecycle.LiveData
import com.example.quickqrabc.data.QRHistoryDao
import com.example.quickqrabc.data.QRHistoryEntity

class QRRepository(private val qrHistoryDao: QRHistoryDao) {
    
    fun getAllHistory(): LiveData<List<QRHistoryEntity>> = qrHistoryDao.getAllHistory()
    
    fun getScannedHistory(): LiveData<List<QRHistoryEntity>> = qrHistoryDao.getHistoryByType("scan")
    
    fun getGeneratedHistory(): LiveData<List<QRHistoryEntity>> = qrHistoryDao.getHistoryByType("generate")
    
    suspend fun insertHistory(history: QRHistoryEntity) {
        qrHistoryDao.insertHistory(history)
    }
    
    suspend fun deleteHistory(history: QRHistoryEntity) {
        qrHistoryDao.deleteHistory(history)
    }
    
    suspend fun deleteAllHistory() {
        qrHistoryDao.deleteAllHistory()
    }
    
    suspend fun getHistoryCount(): Int = qrHistoryDao.getHistoryCount()
}
