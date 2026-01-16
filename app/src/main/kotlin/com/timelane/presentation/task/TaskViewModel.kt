package com.timelane.presentation.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timelane.domain.model.Task
import com.timelane.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.timelane.core.undo.UndoAction
import com.timelane.core.undo.UndoManager
import com.timelane.core.sound.SoundManager
import com.timelane.core.sound.SoundPreferences
import com.timelane.core.sound.NotificationSound
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val repository: TaskRepository,
    private val undoManager: UndoManager,
    private val soundManager: SoundManager,
    private val soundPreferences: SoundPreferences,
    private val taskPreferences: TaskPreferences
) : ViewModel() {

    val selectedSound = soundPreferences.selectedSound
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NotificationSound.BELL)

    val moveCompletedToBottom = taskPreferences.moveCompletedToBottom
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val fontSize = taskPreferences.fontSize
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 16f)

    val eventFontSize = taskPreferences.eventFontSize
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 16f)

    fun updateSound(sound: NotificationSound) {
        viewModelScope.launch {
            soundPreferences.setSound(sound)
            soundManager.playSound(sound)
        }
    }

    fun setMoveCompletedToBottom(enabled: Boolean) {
        viewModelScope.launch {
            taskPreferences.setMoveCompletedToBottom(enabled)
        }
    }

    fun setFontSize(size: Float) {
        viewModelScope.launch {
            taskPreferences.setFontSize(size)
        }
    }

    fun setEventFontSize(size: Float) {
        viewModelScope.launch {
            taskPreferences.setEventFontSize(size)
        }
    }

    fun playNotificationSound() {
        viewModelScope.launch {
            val sound = selectedSound.value
            soundManager.playSound(sound)
        }
    }

    val tasks: StateFlow<List<Task>> = kotlinx.coroutines.flow.combine(
        repository.getAllTasks(),
        moveCompletedToBottom
    ) { tasks, moveBottom ->
        if (moveBottom) {
            tasks.sortedWith(compareBy<Task> { it.isCompleted }.thenBy { it.position })
        } else {
            tasks.sortedBy { it.position }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addTask(title: String, parentId: Long? = null) {
        viewModelScope.launch {
            val currentTasks = tasks.value
            // If adding subtask, finding correct position is improved by just appending
            val nextPosition = if (parentId == null) {
                (currentTasks.maxOfOrNull { it.position } ?: -1) + 1
            } else {
                 // For subtasks, we might want to put them at the end.
                 // Ideally we query DB index but for now 0 or maxint is fine, 
                 // as we sort by position. Let's use 0 for now or query repo.
                 // Actually simpler: just let repo handle it or use timestamp. 
                 // Position 9999 to append.
                 9999
            }
            repository.insertTask(Task(title = title, position = nextPosition, parentId = parentId))
        }
    }

    fun completeTask(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun reorderTasks(fromIndex: Int, toIndex: Int) {
        if (fromIndex == toIndex) return
        val currentTasks = tasks.value.toMutableList()
        if (fromIndex !in currentTasks.indices || toIndex !in currentTasks.indices) return
        
        val item = currentTasks.removeAt(fromIndex)
        currentTasks.add(toIndex, item)
        
        // Update positions based on the new list order
        val updatedTasks = currentTasks.mapIndexed { index, task ->
            task.copy(position = index)
        }
        
        viewModelScope.launch {
            repository.updateTasks(updatedTasks)
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            val task = tasks.value.find { it.id == taskId }
            if (task != null) {
                repository.deleteTask(taskId)
                undoManager.registerAction(UndoAction.TaskDeleted(task))
            }
        }
    }

    fun undo() {
        undoManager.undo { action ->
            viewModelScope.launch {
                when (action) {
                    is UndoAction.TaskDeleted -> {
                        repository.insertTask(action.task.copy(id = 0))
                    }
                    else -> { /* Handled elsewhere or N/A */ }
                }
            }
        }
    }
}
