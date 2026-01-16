package com.timelane.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val priority: Int,
    val position: Int = 0,
    val isCompleted: Boolean = false,
    val parentId: Long? = null,
    val createdAt: Long // Timestamp
)
