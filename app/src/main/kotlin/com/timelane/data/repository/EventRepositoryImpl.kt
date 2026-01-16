package com.timelane.data.repository

import com.timelane.data.local.EventDao
import com.timelane.data.mapper.toDomain
import com.timelane.data.mapper.toEntity
import com.timelane.domain.model.Event
import com.timelane.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

class EventRepositoryImpl @Inject constructor(
    private val dao: EventDao
) : EventRepository {

    override fun getEventsForDay(date: LocalDate): Flow<List<Event>> {
        // Date pattern for SQL LIKE: "2023-10-27"
        val datePattern = date.toString() 
        return dao.getEventsForDay(datePattern).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getAllEvents(): Flow<List<Event>> {
        return dao.getAllEvents().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertEvent(event: Event): Long {
        return dao.insertEvent(event.toEntity())
    }

    override suspend fun updateEvent(event: Event) {
        dao.updateEvent(event.toEntity())
    }

    override suspend fun deleteEvent(eventId: Long) {
        dao.deleteEvent(eventId)
    }

    override suspend fun getEventById(id: Long): Event? {
        return dao.getEventById(id)?.toDomain()
    }
    
    override suspend fun deleteAllEvents() {
        dao.deleteAllEvents()
    }
    
    override suspend fun deleteOldEvents(beforeTime: LocalDateTime) {
        dao.deleteOldEvents(beforeTime.toString())
    }

    override suspend fun pushEventsDown(fromTime: LocalDateTime, offsetMinutes: Int, excludeId: Long) {
        // Fetch ALL events and filter in memory to avoid SQL string comparison issues
        val allEvents = dao.getAllEventsList()
        
        allEvents.forEach { entity ->
            val eventStart = LocalDateTime.parse(entity.startTime)
            // Filter: Start time is >= trigger time, AND it's not the excluded event
            if (!eventStart.isBefore(fromTime) && entity.id != excludeId) {
                val newStart = eventStart.plusMinutes(offsetMinutes.toLong())
                dao.updateEvent(entity.copy(startTime = newStart.toString()))
            }
        }
    }

    override suspend fun pullEventsUp(fromTime: LocalDateTime, offsetMinutes: Int, excludeId: Long) {
        val allEvents = dao.getAllEventsList()
        
        allEvents.forEach { entity ->
            val eventStart = LocalDateTime.parse(entity.startTime)
            if (!eventStart.isBefore(fromTime) && entity.id != excludeId) {
                val newStart = eventStart.minusMinutes(offsetMinutes.toLong())
                dao.updateEvent(entity.copy(startTime = newStart.toString()))
            }
        }
    }
}

