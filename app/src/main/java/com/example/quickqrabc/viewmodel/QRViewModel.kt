package com.example.quickqrabc.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.quickqrabc.data.QRDatabase
import com.example.quickqrabc.data.QRHistoryEntity
import com.example.quickqrabc.repository.QRRepository
import kotlinx.coroutines.launch

class QRViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: QRRepository
    
    init {
        val qrHistoryDao = QRDatabase.getDatabase(application).qrHistoryDao()
        repository = QRRepository(qrHistoryDao)
    }
    
    fun getAllHistory(): LiveData<List<QRHistoryEntity>> = repository.getAllHistory()
    
    fun getScannedHistory(): LiveData<List<QRHistoryEntity>> = repository.getScannedHistory()
    
    fun getGeneratedHistory(): LiveData<List<QRHistoryEntity>> = repository.getGeneratedHistory()
    
    fun insertHistory(type: String, content: String) {
        viewModelScope.launch {
            val history = QRHistoryEntity(
                type = type,
                content = content
            )
            repository.insertHistory(history)
        }
    }
    
    fun deleteHistory(history: QRHistoryEntity) {
        viewModelScope.launch {
            repository.deleteHistory(history)
        }
    }
    
    fun deleteAllHistory() {
        viewModelScope.launch {
            repository.deleteAllHistory()
        }
    }
}
