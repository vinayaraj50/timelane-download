package com.timelane.domain.model

import java.time.Instant

data class Task(
    val id: Long = 0,
    val title: String,
    val priority: Int = 0,
    val position: Int = 0,
    val isCompleted: Boolean = false,
    val parentId: Long? = null,
    val subtasks: List<Task> = emptyList(),
    val createdAt: Instant = Instant.now()
)
