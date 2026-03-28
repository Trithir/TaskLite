package com.erics.tasklite.ui.expanded

data class ExpandedTasksUiState(
	val completedTasks: List<ExpandedTaskUiModel> = emptyList(),
	val currentTask: ExpandedTaskUiModel? = null,
	val futureTasks: List<ExpandedTaskUiModel> = emptyList(),
	val searchQuery: String = "",
	val currentTaskFlatIndex: Int? = null,
	val editingTaskFlatIndex: Int? = null,
	val isAddTaskFieldVisible: Boolean = false,
	val newTaskText: String = "",
	val editingTask: ExpandedTaskUiModel? = null,
	val editingTaskText: String = "",
	val deleteTargetTask: ExpandedTaskUiModel? = null,
	val pendingCompletionTaskIds: Set<Long> = emptySet(),
	val completionShiftToken: Int = 0,
	val isEditingTask: Boolean = false
) {
	val activeTasks: List<ExpandedTaskUiModel>
		get() = buildList {
			currentTask?.let(::add)
			addAll(futureTasks)
		}
}
