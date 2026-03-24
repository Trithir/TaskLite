package com.erics.tasklite.data

import kotlinx.coroutines.flow.Flow

interface TaskRepository {
	fun getCurrentTask(): Flow<TaskEntity?>
	fun getIncompleteTasks(): Flow<List<TaskEntity>>
	fun getCompletedTasks(): Flow<List<TaskEntity>>
	suspend fun insertTask(task: TaskEntity): Long
	suspend fun updateTask(task: TaskEntity)
	suspend fun reorderTasks(tasks: List<TaskEntity>)
	suspend fun deleteTask(task: TaskEntity)
}
