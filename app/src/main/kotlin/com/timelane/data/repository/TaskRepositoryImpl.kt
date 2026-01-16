package com.timelane.data.repository

import com.timelane.data.local.TaskDao
import com.timelane.data.mapper.toDomain
import com.timelane.data.mapper.toEntity
import com.timelane.domain.model.Task
import com.timelane.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val dao: TaskDao
) : TaskRepository {

    override fun getAllTasks(): Flow<List<Task>> {
        return dao.getAllTasks().map { entities ->
            val allTasks = entities.map { it.toDomain() }
            val childrenMap = allTasks.filter { it.parentId != null }.groupBy { it.parentId!! }
            
            fun buildTree(task: Task): Task {
                val children = childrenMap[task.id]
                    ?.map { buildTree(it) }
                    ?.sortedBy { it.position } 
                    ?: emptyList()
                return task.copy(subtasks = children)
            }
            
            allTasks.filter { it.parentId == null }
                .map { buildTree(it) }
                .sortedBy { it.position }
        }
    }

    override suspend fun insertTask(task: Task): Long {
        return dao.insertTask(task.toEntity())
    }

    override suspend fun updateTask(task: Task) {
        dao.updateTask(task.toEntity())
    }

    override suspend fun updateTasks(tasks: List<Task>) {
        dao.updateTasks(tasks.map { it.toEntity() })
    }

    override suspend fun deleteTask(taskId: Long) {
        dao.deleteTask(taskId)
    }

    override suspend fun reorderTask(taskId: Long, newPriority: Int) {
        dao.updateTaskPriority(taskId, newPriority)
    }
}
