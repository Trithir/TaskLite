package io.tasklite.notification

import android.content.Context
import io.tasklite.data.TaskDatabaseProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

data class TaskLiteNotificationState(
	val currentTaskText: String? = null
)

class TaskLiteNotificationRepository(
	private val context: Context
) {
	companion object {
		fun from(context: Context): TaskLiteNotificationRepository {
			return TaskLiteNotificationRepository(context.applicationContext)
		}
	}

	fun observeState(): Flow<TaskLiteNotificationState> {
		val taskDao = TaskDatabaseProvider.getDatabase(context).taskDao()
		return taskDao.getCurrentTask()
			.map { task -> TaskLiteNotificationState(currentTaskText = task?.text) }
			.distinctUntilChanged()
	}

	suspend fun loadState(): TaskLiteNotificationState {
		return TaskLiteNotificationState(
			currentTaskText = TaskDatabaseProvider.getDatabase(context).taskDao().getCurrentTaskOnce()?.text
		)
	}
}
