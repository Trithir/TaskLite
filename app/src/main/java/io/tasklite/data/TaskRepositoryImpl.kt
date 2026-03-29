package io.tasklite.data

import android.content.Context
import io.tasklite.widget.TaskLiteWidgetSync
import kotlinx.coroutines.flow.Flow

class TaskRepositoryImpl(
	private val taskDao: TaskDao,
	private val appContext: Context
) : TaskRepository {
	override fun getCurrentTask(): Flow<TaskEntity?> = taskDao.getCurrentTask()

	override fun getIncompleteTasks(): Flow<List<TaskEntity>> = taskDao.getIncompleteTasksOrdered()

	override fun getCompletedTasks(): Flow<List<TaskEntity>> = taskDao.getCompletedTasksOrdered()

	override suspend fun insertTask(task: TaskEntity): Long {
		val rowId = taskDao.insertTask(task)
		refreshWidget()
		return rowId
	}

	override suspend fun updateTask(task: TaskEntity) {
		taskDao.updateTask(task)
		refreshWidget()
	}

	override suspend fun reorderTasks(tasks: List<TaskEntity>) {
		taskDao.updateTasks(tasks)
		refreshWidget()
	}

	override suspend fun deleteTask(task: TaskEntity) {
		taskDao.deleteTask(task)
		refreshWidget()
	}

	private suspend fun refreshWidget() {
		TaskLiteWidgetSync.refresh(appContext)
	}
}
