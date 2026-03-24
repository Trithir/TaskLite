package com.erics.tasklite.ui.expanded

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.erics.tasklite.data.TaskEntity
import com.erics.tasklite.data.TaskRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ExpandedTasksViewModel(
	private val repository: TaskRepository
) : ViewModel() {
	private val incompleteTasks = repository.getIncompleteTasks()
		.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
	private val completedTasks = repository.getCompletedTasks()
		.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

	private val isAddTaskFieldVisible = MutableStateFlow(false)
	private val newTaskText = MutableStateFlow("")
	private val editingTaskId = MutableStateFlow<Long?>(null)
	private val editingTaskText = MutableStateFlow("")
	private val deleteTargetTaskId = MutableStateFlow<Long?>(null)
	private val pendingCompletionTaskIds = MutableStateFlow<Set<Long>>(emptySet())
	private val completionShiftToken = MutableStateFlow(0)

	private val taskSnapshot: StateFlow<TaskSnapshot> = combine(
		incompleteTasks,
		completedTasks,
		pendingCompletionTaskIds
	) { incomplete, completed, pendingCompletionIds ->
		val completedTaskModels = completed.map(TaskEntity::toExpandedTaskUiModel)
		val blockedTaskIds = completedTaskModels.mapTo(mutableSetOf()) { it.id }
		blockedTaskIds.addAll(pendingCompletionIds)
		val activeTasks = incomplete
			.map(TaskEntity::toExpandedTaskUiModel)
			.filterNot { it.id in blockedTaskIds }

		TaskSnapshot(
			activeTasks = activeTasks,
			completedTasks = completedTaskModels
		)
	}.stateIn(
		viewModelScope,
		SharingStarted.WhileSubscribed(5_000),
		TaskSnapshot()
	)

	private val baseTransientState: StateFlow<TransientTaskState> = combine(
		isAddTaskFieldVisible,
		newTaskText,
		editingTaskId,
		editingTaskText,
		deleteTargetTaskId
	) { isAddVisible, draftText, editingId, editingDraft, deleteTargetId ->
		TransientTaskState(
			isAddTaskFieldVisible = isAddVisible,
			newTaskText = draftText,
			editingTaskId = editingId,
			editingTaskText = editingDraft,
			deleteTargetTaskId = deleteTargetId
		)
	}.stateIn(
		viewModelScope,
		SharingStarted.WhileSubscribed(5_000),
		TransientTaskState()
	)

	private val transientState: StateFlow<TransientTaskState> = combine(
		baseTransientState,
		pendingCompletionTaskIds,
		completionShiftToken
	) { transient, pendingCompletionIds, shiftToken ->
		transient.copy(
			pendingCompletionTaskIds = pendingCompletionIds,
			completionShiftToken = shiftToken
		)
	}.stateIn(
		viewModelScope,
		SharingStarted.WhileSubscribed(5_000),
		TransientTaskState()
	)

	val uiState: StateFlow<ExpandedTasksUiState> = combine(
		taskSnapshot,
		transientState
	) { tasks, transient ->
		ExpandedTasksUiState(
			completedTasks = tasks.completedTasks,
			currentTask = tasks.activeTasks.firstOrNull(),
			futureTasks = tasks.activeTasks.drop(1),
			currentTaskFlatIndex = tasks.activeTasks.firstOrNull()?.let { tasks.completedTasks.size },
			isAddTaskFieldVisible = transient.isAddTaskFieldVisible,
			newTaskText = transient.newTaskText,
			editingTask = tasks.allTasks.firstOrNull { it.id == transient.editingTaskId },
			editingTaskText = transient.editingTaskText,
			deleteTargetTask = tasks.allTasks.firstOrNull { it.id == transient.deleteTargetTaskId },
			pendingCompletionTaskIds = transient.pendingCompletionTaskIds,
			completionShiftToken = transient.completionShiftToken
		)
	}.stateIn(
		viewModelScope,
		SharingStarted.WhileSubscribed(5_000),
		ExpandedTasksUiState()
	)

	fun showAddTaskField() {
		isAddTaskFieldVisible.value = true
	}

	fun updateNewTaskText(text: String) {
		newTaskText.value = text
	}

	fun cancelNewTask() {
		isAddTaskFieldVisible.value = false
		newTaskText.value = ""
	}

	fun submitNewTask() {
		viewModelScope.launch {
			val text = newTaskText.value.normalizeTaskText() ?: return@launch
			val sortOrder = nextActiveSortOrder()

			repository.insertTask(
				TaskEntity(
					text = text,
					sortOrder = sortOrder
				)
			)
			cancelNewTask()
		}
	}

	fun startEditingTask(taskId: Long) {
		val task = findIncompleteTask(taskId) ?: return

		editingTaskId.value = task.id
		editingTaskText.value = task.text
	}

	fun updateEditingTaskText(text: String) {
		editingTaskText.value = text
	}

	fun cancelEditingTask() {
		editingTaskId.value = null
		editingTaskText.value = ""
	}

	fun saveEditingTask() {
		viewModelScope.launch {
			val task = findTask(editingTaskId.value) ?: return@launch cancelEditingTask()
			val text = editingTaskText.value.normalizeTaskText() ?: return@launch

			repository.updateTask(
				task.toTaskEntity(text = text)
			)
			cancelEditingTask()
		}
	}

	fun requestDeleteTask(taskId: Long) {
		if (findIncompleteTask(taskId) == null) {
			return
		}
		deleteTargetTaskId.value = taskId
	}

	fun cancelDeleteTask() {
		deleteTargetTaskId.value = null
	}

	fun confirmDeleteTask() {
		viewModelScope.launch {
			val task = findTask(deleteTargetTaskId.value) ?: return@launch cancelDeleteTask()

			repository.deleteTask(task.toTaskEntity())
			clearTransientState(task.id)
		}
	}

	fun completeTask(taskId: Long) {
		viewModelScope.launch {
			completeTaskInternal(taskId)
		}
	}

	fun uncheckCompletedTask(taskId: Long) {
		viewModelScope.launch {
			uncheckCompletedTaskInternal(taskId)
		}
	}

	fun toggleTaskCompletion(taskId: Long) {
		viewModelScope.launch {
			when {
				findIncompleteTask(taskId) != null -> completeTaskInternal(taskId)
				findCompletedTask(taskId) != null -> uncheckCompletedTaskInternal(taskId)
			}
		}
	}

	fun moveActiveTask(fromIndex: Int, toIndex: Int) {
		viewModelScope.launch {
			val activeTasks = taskSnapshot.value.activeTasks
			if (fromIndex !in activeTasks.indices || toIndex !in activeTasks.indices) {
				return@launch
			}
			if (fromIndex == toIndex) {
				return@launch
			}

			val reorderedTasks = activeTasks.toMutableList().apply {
				add(toIndex, removeAt(fromIndex))
			}.mapIndexed { index, task ->
				task.toTaskEntity(sortOrder = index.toLong())
			}

			repository.reorderTasks(reorderedTasks)
		}
	}

	private suspend fun completeTaskInternal(taskId: Long) {
		val task = findIncompleteTask(taskId) ?: return
		if (taskId in pendingCompletionTaskIds.value) {
			return
		}

		pendingCompletionTaskIds.update { it + taskId }
		try {
			delay(COMPLETION_SHIFT_DELAY_MS)
			repository.updateTask(
				task.toTaskEntity(
					completedAt = now()
				)
			)
			completionShiftToken.update { it + 1 }
			clearTransientState(task.id)
		} finally {
			pendingCompletionTaskIds.update { it - taskId }
		}
	}

	private suspend fun uncheckCompletedTaskInternal(taskId: Long) {
		val task = findCompletedTask(taskId) ?: return

		repository.insertTask(
			TaskEntity(
				text = task.text,
				sortOrder = nextActiveSortOrder()
			)
		)
		clearTransientState(task.id)
	}

	private fun findTask(taskId: Long?): ExpandedTaskUiModel? {
		if (taskId == null) {
			return null
		}

		return taskSnapshot.value.allTasks.firstOrNull { it.id == taskId }
	}

	private fun findIncompleteTask(taskId: Long): ExpandedTaskUiModel? {
		return taskSnapshot.value.activeTasks.firstOrNull { it.id == taskId }
	}

	private fun findCompletedTask(taskId: Long): ExpandedTaskUiModel? {
		return taskSnapshot.value.completedTasks.firstOrNull { it.id == taskId }
	}

	private fun clearTransientState(taskId: Long) {
		if (editingTaskId.value == taskId) {
			cancelEditingTask()
		}
		if (deleteTargetTaskId.value == taskId) {
			cancelDeleteTask()
		}
	}

	private fun nextActiveSortOrder(): Long {
		val maxSortOrder = incompleteTasks.value.maxOfOrNull { it.sortOrder } ?: -1L
		return maxSortOrder + 1L
	}

	private fun String.normalizeTaskText(): String? {
		val normalizedText = trim()
		return normalizedText.takeIf { it.isNotEmpty() }
	}

	private fun now(): Long = System.currentTimeMillis()

	companion object {
		private const val COMPLETION_SHIFT_DELAY_MS = 300L

		fun factory(
			repository: TaskRepository
		): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
			@Suppress("UNCHECKED_CAST")
			override fun <T : ViewModel> create(modelClass: Class<T>): T {
				if (modelClass.isAssignableFrom(ExpandedTasksViewModel::class.java)) {
					return ExpandedTasksViewModel(repository) as T
				}
				throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
			}
		}
	}
}

private data class TaskSnapshot(
	val activeTasks: List<ExpandedTaskUiModel> = emptyList(),
	val completedTasks: List<ExpandedTaskUiModel> = emptyList()
) {
	val allTasks: List<ExpandedTaskUiModel>
		get() = activeTasks + completedTasks
}

private data class TransientTaskState(
	val isAddTaskFieldVisible: Boolean = false,
	val newTaskText: String = "",
	val editingTaskId: Long? = null,
	val editingTaskText: String = "",
	val deleteTargetTaskId: Long? = null,
	val pendingCompletionTaskIds: Set<Long> = emptySet(),
	val completionShiftToken: Int = 0
)
