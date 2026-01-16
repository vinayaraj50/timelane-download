package com.timelane.domain.repository

import com.timelane.domain.model.Event
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime

interface EventRepository {
    fun getEventsForDay(date: LocalDate): Flow<List<Event>>
    fun getAllEvents(): Flow<List<Event>>
    suspend fun insertEvent(event: Event): Long
    suspend fun updateEvent(event: Event)
    suspend fun deleteEvent(eventId: Long)
    suspend fun getEventById(id: Long): Event?
    
    // Batch delete operations
    suspend fun deleteAllEvents()
    suspend fun deleteOldEvents(beforeTime: LocalDateTime)
    
    // Batch operations for "Push/Pull" logic
    suspend fun pushEventsDown(fromTime: LocalDateTime, offsetMinutes: Int, excludeId: Long = -1L)
    suspend fun pullEventsUp(fromTime: LocalDateTime, offsetMinutes: Int, excludeId: Long = -1L)
}

