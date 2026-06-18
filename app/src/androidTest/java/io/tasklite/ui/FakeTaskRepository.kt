package io.tasklite.ui

import io.tasklite.data.TaskEntity
import io.tasklite.data.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeTaskRepository(
	initialTasks: List<TaskEntity> = emptyList()
) : TaskRepository {
	private val tasksFlow = MutableStateFlow(initialTasks.sortedWith(taskComparator))
	private var nextId = (initialTasks.maxOfOrNull(TaskEntity::id) ?: 0L) + 1L

	override fun getCurrentTask(): Flow<TaskEntity?> = tasksFlow.map { tasks ->
		tasks
			.filter { it.completedAt == null }
			.minByOrNull(TaskEntity::sortOrder)
	}

	override fun getIncompleteTasks(): Flow<List<TaskEntity>> = tasksFlow.map { tasks ->
		tasks
			.filter { it.completedAt == null }
			.sortedBy(TaskEntity::sortOrder)
	}

	override fun getCompletedTasks(): Flow<List<TaskEntity>> = tasksFlow.map { tasks ->
		tasks
			.filter { it.completedAt != null }
			.sortedByDescending(TaskEntity::completedAt)
	}

	override suspend fun insertActiveTaskAtTop(text: String): Long {
		val activeTasks = tasksFlow.value
			.filter { it.completedAt == null }
			.sortedBy(TaskEntity::sortOrder)
		val insertedTask = TaskEntity(
			id = nextId++,
			text = text,
			sortOrder = 0L
		)
		val shiftedTasks = activeTasks.mapIndexed { index, task ->
			task.copy(sortOrder = index.toLong() + 1L)
		}
		val shiftedTaskIds = shiftedTasks.mapTo(mutableSetOf()) { it.id }
		tasksFlow.value = (
			tasksFlow.value.filterNot { it.id in shiftedTaskIds } +
				shiftedTasks +
				insertedTask
			).sortedWith(taskComparator)
		return insertedTask.id
	}

	override suspend fun updateTask(task: TaskEntity) {
		tasksFlow.value = tasksFlow.value
			.map { existing -> if (existing.id == task.id) task else existing }
			.sortedWith(taskComparator)
	}

	override suspend fun reorderTasks(tasks: List<TaskEntity>) {
		val replacements = tasks.associateBy(TaskEntity::id)
		tasksFlow.value = tasksFlow.value
			.map { existing -> replacements[existing.id] ?: existing }
			.sortedWith(taskComparator)
	}

	override suspend fun deleteTask(task: TaskEntity) {
		tasksFlow.value = tasksFlow.value.filterNot { it.id == task.id }
	}

	private companion object {
		private val taskComparator = compareBy<TaskEntity> { it.completedAt != null }
			.thenBy { it.sortOrder }
			.thenByDescending { it.completedAt ?: Long.MIN_VALUE }
			.thenBy { it.id }
	}
}
