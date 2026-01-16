package com.timelane.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM events ORDER BY startTime ASC")
    fun getAllEvents(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE startTime LIKE :datePattern || '%' ORDER BY startTime ASC")
    fun getEventsForDay(datePattern: String): Flow<List<EventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity): Long

    @Update
    suspend fun updateEvent(event: EventEntity)
    
    @Query("DELETE FROM events WHERE id = :eventId")
    suspend fun deleteEvent(eventId: Long)
    
    @Query("DELETE FROM events")
    suspend fun deleteAllEvents()
    
    @Query("DELETE FROM events WHERE startTime < :isoTimestamp")
    suspend fun deleteOldEvents(isoTimestamp: String)

    @Query("SELECT * FROM events WHERE id = :id")
    suspend fun getEventById(id: Long): EventEntity?
    
    // For push/pull logic, we might need manual handling in Repository or complex queries
    // Simple fetch-modify-save logic is safer for MVP than complex SQL update for time shifting
    @Query("SELECT * FROM events WHERE startTime >= :isoTimestamp AND id != :excludeId")
    suspend fun getFutureEvents(isoTimestamp: String, excludeId: Long): List<EventEntity>

    // Fallback for manual filtering
    @Query("SELECT * FROM events")
    suspend fun getAllEventsList(): List<EventEntity>
}

