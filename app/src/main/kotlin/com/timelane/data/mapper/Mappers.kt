package com.timelane.data.mapper

import com.timelane.data.local.EventEntity
import com.timelane.data.local.TaskEntity
import com.timelane.domain.model.Event
import com.timelane.domain.model.Task
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

fun EventEntity.toDomain(): Event {
    return Event(
        id = id,
        title = title,
        startTime = LocalDateTime.parse(startTime),
        durationMinutes = durationMinutes,
        linkedTaskId = linkedTaskId,
        isCompleted = isCompleted,
        soundRes = soundRes
    )
}

fun Event.toEntity(): EventEntity {
    return EventEntity(
        id = if (id == 0L) 0 else id, // Ensure 0 for auto-gen on insert
        title = title,
        startTime = startTime.toString(),
        durationMinutes = durationMinutes,
        linkedTaskId = linkedTaskId,
        isCompleted = isCompleted,
        soundRes = soundRes
    )
}

fun TaskEntity.toDomain(): Task {
    return Task(
        id = id,
        title = title,
        priority = priority,
        position = position,
        isCompleted = isCompleted,
        parentId = parentId,
        createdAt = Instant.ofEpochMilli(createdAt)
    )
}

fun Task.toEntity(): TaskEntity {
    return TaskEntity(
        id = if (id == 0L) 0 else id,
        title = title,
        priority = priority,
        position = position,
        isCompleted = isCompleted,
        parentId = parentId,
        createdAt = createdAt.toEpochMilli()
    )
}
