package com.timelane.core.undo

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.timelane.domain.model.Event
import com.timelane.domain.model.Task

sealed class UndoAction {
    data class EventAdded(val event: Event) : UndoAction()
    data class EventUpdated(val oldEvent: Event, val newEvent: Event) : UndoAction()
    data class EventDeleted(val event: Event) : UndoAction()
    data class TaskDeleted(val task: Task) : UndoAction()
    data class EventMoved(val oldEvent: Event, val newEvent: Event) : UndoAction()
    data class EventResized(val oldEvent: Event, val newEvent: Event) : UndoAction()
}

class UndoManager {
    var lastAction: UndoAction? = null
        private set
        
    var showActionPopup by mutableStateOf(false)

    fun registerAction(action: UndoAction) {
        lastAction = action
        showActionPopup = true
    }

    fun undo(onUndo: (UndoAction) -> Unit) {
        lastAction?.let {
            onUndo(it)
            lastAction = null
            showActionPopup = false
        }
    }
    
    fun dismissActionPopup() {
        showActionPopup = false
    }
}
