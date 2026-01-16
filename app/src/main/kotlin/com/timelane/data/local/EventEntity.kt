package com.timelane.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val startTime: String, // Stored as ISO string
    val durationMinutes: Int,
    val linkedTaskId: Long? = null,
    val isCompleted: Boolean = false,
    val soundRes: Int? = null
)
