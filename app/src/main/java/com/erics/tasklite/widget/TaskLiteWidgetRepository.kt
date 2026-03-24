package com.erics.tasklite.widget

import android.content.Context
import com.erics.tasklite.data.TaskDatabaseProvider
import com.erics.tasklite.data.TaskDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

data class TaskLiteWidgetState(
	val currentTaskText: String? = null
) {
	val hasCurrentTask: Boolean
		get() = currentTaskText != null
}

class TaskLiteWidgetRepository(
	private val taskDao: TaskDao
) {
	companion object {
		fun from(context: Context): TaskLiteWidgetRepository {
			return TaskLiteWidgetRepository(TaskDatabaseProvider.getDatabase(context).taskDao())
		}
	}

	suspend fun loadState(): TaskLiteWidgetState {
		return TaskLiteWidgetState(
			currentTaskText = taskDao.getCurrentTaskOnce()?.text
		)
	}

	fun observeState(): Flow<TaskLiteWidgetState> {
		return taskDao.getCurrentTask()
			.map { task -> TaskLiteWidgetState(currentTaskText = task?.text) }
			.distinctUntilChanged()
	}

	suspend fun completeCurrentTask(): Boolean {
		val currentTask = taskDao.getCurrentTaskOnce() ?: return false

		taskDao.updateTask(
			currentTask.copy(
				completedAt = System.currentTimeMillis()
			)
		)
		return true
	}
}
