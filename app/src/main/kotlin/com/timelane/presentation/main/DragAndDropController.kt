package com.timelane.presentation.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import com.timelane.domain.model.Task
import com.timelane.domain.model.Event
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DragAndDropController {
    var undoManager: com.timelane.core.undo.UndoManager? = null
    
    var draggingTask by mutableStateOf<Task?>(null)
        private set
    
    var dragOffset by mutableStateOf(Offset.Zero)
        private set

    var dragTouchOffset by mutableStateOf(Offset.Zero)
        private set

    var draggedTaskId by mutableStateOf<Long?>(null)
        private set

    var dragDistance by mutableStateOf(0f)
        private set

    var isDragging by mutableStateOf(false)
        private set
    
    var clipboardTask by mutableStateOf<Task?>(null)
        private set

    // Clipboard expiration: 30 seconds (30,000 ms)
    private val clipboardExpirationMs = 30_000L
    private var clipboardExpirationJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)
    
    // Track clipboard timestamp for UI feedback if needed
    var clipboardCopiedAt by mutableStateOf<Long?>(null)
        private set

    var dropHandler: ((Task, Offset) -> Unit)? = null
    var reorderHandler: ((Task, Offset) -> Unit)? = null

    fun copyTask(task: Task) {
        // Cancel any existing expiration timer
        clipboardExpirationJob?.cancel()
        
        clipboardTask = task
        clipboardCopiedAt = System.currentTimeMillis()
        
        // Start expiration timer
        clipboardExpirationJob = scope.launch {
            delay(clipboardExpirationMs)
            // Auto-expire clipboard after 1 minute
            clipboardTask = null
            clipboardCopiedAt = null
        }
    }

    fun clearClipboard() {
        clipboardExpirationJob?.cancel()
        clipboardExpirationJob = null
        clipboardTask = null
        clipboardCopiedAt = null
    }

    fun startDragging(task: Task, absoluteTouchPoint: Offset, handleTopLeft: Offset) {
        draggingTask = task
        draggedTaskId = task.id
        dragOffset = absoluteTouchPoint
        dragTouchOffset = absoluteTouchPoint - handleTopLeft
        dragDistance = 0f
        isDragging = true
    }

    fun updateOffset(delta: Offset) {
        dragOffset += delta
        dragDistance += delta.getDistance()
        val task = draggingTask
        if (task != null) {
            reorderHandler?.invoke(task, dragOffset)
        }
    }

    fun stopDragging(finalOffset: Offset) {
        val task = draggingTask
        if (task != null && dragDistance > 10f) {
            dropHandler?.invoke(task, finalOffset)
            reorderHandler?.invoke(task, finalOffset)
        }
        reset()
    }

    fun reset() {
        draggingTask = null
        draggedTaskId = null
        dragOffset = Offset.Zero
        dragTouchOffset = Offset.Zero
        dragDistance = 0f
        isDragging = false
    }
}
