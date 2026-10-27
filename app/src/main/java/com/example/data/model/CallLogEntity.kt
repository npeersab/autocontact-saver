package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class CallActionStatus {
    SAVED,              // Newly saved into phone contacts
    ALREADY_EXISTS,     // Existed in phonebook, untouched
    PRIVATE_NUMBER,     // Private / Unknown number
    MANUAL_SAVED,       // Saved manually via UI
    ERROR               // Failed to save due to permission or OS error
}

@Entity(tableName = "call_logs")
data class CallLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val phoneNumber: String,
    val contactName: String,
    val status: String,
    val timestamp: Long = System.currentTimeMillis(),
    val details: String = "",
    val isSimulated: Boolean = false
)
