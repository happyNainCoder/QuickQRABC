package com.example.quickqrabc.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "qr_history")
data class QRHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String, // "scan" or "generate"
    val content: String,
    val createdAt: Long = System.currentTimeMillis()
)
