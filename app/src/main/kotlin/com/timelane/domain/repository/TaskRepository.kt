package com.timelane.domain.repository

import com.timelane.domain.model.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun getAllTasks(): Flow<List<Task>>
    suspend fun insertTask(task: Task): Long
    suspend fun updateTask(task: Task)
    suspend fun updateTasks(tasks: List<Task>)
    suspend fun deleteTask(taskId: Long)
    suspend fun reorderTask(taskId: Long, newPriority: Int)
}
