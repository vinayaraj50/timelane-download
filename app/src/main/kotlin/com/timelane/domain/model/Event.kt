package com.timelane.domain.model

import java.time.LocalDateTime

data class Event(
    val id: Long = 0,
    val title: String,
    val startTime: LocalDateTime,
    val durationMinutes: Int,
    val linkedTaskId: Long? = null,
    val isCompleted: Boolean = false,
    val colorIndex: Int = 0,
    val soundRes: Int? = null
)
