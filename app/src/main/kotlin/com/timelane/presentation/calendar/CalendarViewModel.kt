package com.timelane.presentation.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timelane.core.undo.UndoAction
import com.timelane.core.undo.UndoManager
import com.timelane.domain.model.Event
import com.timelane.domain.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

import com.timelane.presentation.task.TaskPreferences
import com.timelane.core.sound.SoundManager
import com.timelane.core.sound.NotificationSound
import com.timelane.core.sound.SoundPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val repository: EventRepository,
    private val undoManager: UndoManager,
    private val taskPreferences: TaskPreferences,
    private val soundManager: SoundManager,
    private val soundPreferences: SoundPreferences
) : ViewModel() {

    val eventFontSize = taskPreferences.eventFontSize
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 16f)

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events.asStateFlow()

    private val _currentDate = MutableStateFlow(LocalDate.now())
    val currentDate: StateFlow<LocalDate> = _currentDate.asStateFlow()

    val undoManagerInstance = undoManager

    private var lastCheckedMinute: Int = -1

    fun checkEventNotification(time: LocalDateTime) {
        val currentMinute = time.hour * 60 + time.minute
        if (currentMinute == lastCheckedMinute) return
        lastCheckedMinute = currentMinute

        viewModelScope.launch {
            val globalSound = soundPreferences.selectedSound.first() // Default sound
            
            val dayEvents = _events.value.filter { it.startTime.toLocalDate() == time.toLocalDate() }
            
            for (event in dayEvents) {
                val startTime = event.startTime
                val endTime = startTime.plusMinutes(event.durationMinutes.toLong())

                val isStartsNow = startTime.hour == time.hour && startTime.minute == time.minute
                val isEndsNow = endTime.hour == time.hour && endTime.minute == time.minute

                if (isStartsNow || isEndsNow) {
                    if (event.soundRes != null) {
                        soundManager.playSound(event.soundRes)
                    } else if (globalSound != NotificationSound.SILENT) {
                        soundManager.playSound(globalSound)
                    }
                    return@launch 
                }
            }
        }
    }

    init {
        loadEvents()
    }

    private fun loadEvents() {
        viewModelScope.launch {
            repository.getAllEvents().collect {
                _events.value = it
            }
        }
    }

    fun addEvent(title: String, durationMinutes: Int, time: LocalDateTime, soundRes: Int? = null) {
        viewModelScope.launch {
            val newEvent = Event(
                title = title,
                startTime = time,
                durationMinutes = durationMinutes,
                colorIndex = (0..7).random(),
                soundRes = soundRes
            )
            val id = repository.insertEvent(newEvent)
            val eventWithId = newEvent.copy(id = id)
            undoManager.registerAction(UndoAction.EventAdded(eventWithId))
        }
    }

    fun moveEvent(event: Event) {
        viewModelScope.launch {
            val oldEvent = repository.getEventById(event.id)
            repository.updateEvent(event)
            if (oldEvent != null) {
                undoManager.registerAction(UndoAction.EventMoved(oldEvent, event))
            }
        }
    }
    
    fun addEventPushingOthers(title: String, durationMinutes: Int, time: LocalDateTime, soundRes: Int? = null) {
        viewModelScope.launch {
            // Push future events down
            repository.pushEventsDown(time, durationMinutes)
            // Insert new event
            val newEvent = Event(
                title = title,
                startTime = time,
                durationMinutes = durationMinutes,
                colorIndex = (0..7).random(),
                soundRes = soundRes
            )
            val id = repository.insertEvent(newEvent)
            val eventWithId = newEvent.copy(id = id)
            undoManager.registerAction(UndoAction.EventAdded(eventWithId))
        }
    }
    
    fun updateEvent(event: Event) {
        viewModelScope.launch {
            val oldEvent = repository.getEventById(event.id)
            repository.updateEvent(event)
            if (oldEvent != null) {
                undoManager.registerAction(UndoAction.EventUpdated(oldEvent, event))
            }
        }
    }

    fun updateEventSilently(event: Event) {
        viewModelScope.launch {
            repository.updateEvent(event)
        }
    }
    
    fun deleteEvent(eventId: Long) {
        viewModelScope.launch {
            val event = repository.getEventById(eventId)
            if (event != null) {
                repository.deleteEvent(eventId)
                undoManager.registerAction(UndoAction.EventDeleted(event))
            }
        }
    }

    fun restoreEvent(event: Event) {
        viewModelScope.launch {
            repository.insertEvent(event.copy(id = 0))
        }
    }
    
    fun deleteEventPullingOthers(event: Event) {
        viewModelScope.launch {
             // Delete target
            repository.deleteEvent(event.id)
            // Pull future events up
            repository.pullEventsUp(event.startTime, event.durationMinutes)
            undoManager.registerAction(UndoAction.EventDeleted(event))
        }
    }

    fun undo() {
        undoManager.undo { action ->
            viewModelScope.launch {
                when (action) {
                    is UndoAction.EventAdded -> {
                        val currentEvents = repository.getAllEvents().first()
                        val toDelete = currentEvents.find { it.title == action.event.title && it.startTime == action.event.startTime }
                        if (toDelete != null) {
                            repository.deleteEvent(toDelete.id)
                        }
                    }
                    is UndoAction.EventDeleted -> {
                        repository.insertEvent(action.event.copy(id = 0))
                    }
                    is UndoAction.EventUpdated -> {
                        repository.updateEvent(action.oldEvent)
                    }
                    is UndoAction.EventMoved -> {
                        repository.updateEvent(action.oldEvent)
                    }
                    is UndoAction.EventResized -> {
                        repository.updateEvent(action.oldEvent)
                    }
                    is UndoAction.TaskDeleted -> {
                        // Handled in TaskViewModel but we could handle it here if shared?
                        // It's better to keep it clean.
                    }
                }
            }
        }
    }

    fun applyFlow() {
        val action = undoManager.lastAction ?: return
        viewModelScope.launch {
            when (action) {
                is UndoAction.EventAdded -> {
                    repository.pushEventsDown(action.event.startTime, action.event.durationMinutes, excludeId = action.event.id)
                }
                is UndoAction.EventDeleted -> {
                    repository.pullEventsUp(action.event.startTime, action.event.durationMinutes)
                }
                is UndoAction.EventMoved -> {
                    if (action.newEvent.startTime.isBefore(action.oldEvent.startTime)) {
                        // PULL logic: moved earlier, fill the gap at OLD position
                        repository.pullEventsUp(action.oldEvent.startTime, action.oldEvent.durationMinutes, excludeId = action.newEvent.id)
                    } else {
                        // PUSH logic: moved later, push events at NEW position
                        repository.pushEventsDown(action.newEvent.startTime, action.newEvent.durationMinutes, excludeId = action.newEvent.id)
                    }
                }
                is UndoAction.EventResized -> {
                    val delta = action.newEvent.durationMinutes - action.oldEvent.durationMinutes
                    if (delta > 0) {
                        // Extend: Push events below the OLD end time
                        repository.pushEventsDown(
                            action.oldEvent.startTime.plusMinutes(action.oldEvent.durationMinutes.toLong()),
                            delta,
                            excludeId = action.newEvent.id
                        )
                    } else if (delta < 0) {
                        // Shrink: Pull events up into the GAP
                        repository.pullEventsUp(
                            action.oldEvent.startTime.plusMinutes(action.oldEvent.durationMinutes.toLong()),
                            -delta,
                            excludeId = action.newEvent.id
                        )
                    }
                }
                else -> {}
            }
            undoManager.dismissActionPopup()
        }
    }
    
    fun deleteAllEvents() {
        viewModelScope.launch {
            repository.deleteAllEvents()
        }
    }
    
    fun deleteOldEvents() {
        viewModelScope.launch {
            // Delete events that ended before now
            val now = java.time.LocalDateTime.now()
            repository.deleteOldEvents(now)
        }
    }

    suspend fun hasFutureEvents(time: LocalDateTime): Boolean {
        // We need to check if ANY event starts after 'time'
        // Since listing all events might be heavy, we should ideally add a repository method
        // But for now, let's filter the current list since we have it in memory
        val currentEvents = _events.value
        return currentEvents.any { it.startTime.isAfter(time) }
    }
}

