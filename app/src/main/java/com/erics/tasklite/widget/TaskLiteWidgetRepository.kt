package com.erics.tasklite.widget

import android.content.Context
import com.erics.tasklite.data.TaskDatabaseProvider
import com.erics.tasklite.data.TaskDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

data class TaskLiteWidgetState(
	val currentTaskId: Long? = null,
	val currentTaskText: String? = null,
	val isCompleting: Boolean = false
) {
	val hasCurrentTask: Boolean
		get() = currentTaskText != null
}

class TaskLiteWidgetRepository(
	private val context: Context,
	private val taskDao: TaskDao
) {
	companion object {
		fun from(context: Context): TaskLiteWidgetRepository {
			return TaskLiteWidgetRepository(
				context = context.applicationContext,
				taskDao = TaskDatabaseProvider.getDatabase(context).taskDao()
			)
		}
	}

	suspend fun loadState(): TaskLiteWidgetState {
		val nowMillis = System.currentTimeMillis()
		val pendingCompletion = TaskLiteWidgetCompletionStore
			.observePendingCompletion(context)
			.map { pending ->
				if (pending.isActive(nowMillis)) pending else PendingWidgetCompletionState()
			}

		return pendingCompletion
			.map { pending ->
				if (pending.taskText != null) {
					TaskLiteWidgetState(
						currentTaskId = pending.taskId,
						currentTaskText = pending.taskText,
						isCompleting = true
					)
				} else {
					val currentTask = taskDao.getCurrentTaskOnce()
					TaskLiteWidgetState(
						currentTaskId = currentTask?.id,
						currentTaskText = currentTask?.text
					)
				}
			}
			.distinctUntilChanged()
			.first()
	}

	fun observeState(): Flow<TaskLiteWidgetState> {
		return combine(
			taskDao.getCurrentTask(),
			TaskLiteWidgetCompletionStore.observePendingCompletion(context)
		) { currentTask, pendingCompletion ->
			if (pendingCompletion.isActive(nowMillis = System.currentTimeMillis())) {
				TaskLiteWidgetState(
					currentTaskId = pendingCompletion.taskId,
					currentTaskText = pendingCompletion.taskText,
					isCompleting = true
				)
			} else {
				TaskLiteWidgetState(
					currentTaskId = currentTask?.id,
					currentTaskText = currentTask?.text
				)
			}
		}
			.distinctUntilChanged()
	}

	suspend fun getCurrentTask(): com.erics.tasklite.data.TaskEntity? {
		return taskDao.getCurrentTaskOnce()
	}

	suspend fun completeCurrentTask(taskId: Long): Boolean {
		val currentTask = taskDao.getCurrentTaskOnce() ?: return false
		if (currentTask.id != taskId) {
			return false
		}

		taskDao.updateTask(
			currentTask.copy(
				completedAt = System.currentTimeMillis()
			)
		)
		return true
	}
}
