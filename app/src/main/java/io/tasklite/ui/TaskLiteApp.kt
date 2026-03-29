package io.tasklite.ui

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.tasklite.data.TaskRepository
import io.tasklite.ui.expanded.ExpandedTaskRowUiState
import io.tasklite.ui.expanded.ExpandedTaskScreen
import io.tasklite.ui.expanded.ExpandedTaskScreenCallbacks
import io.tasklite.ui.expanded.ExpandedTaskScreenUiState
import io.tasklite.ui.expanded.ExpandedTasksViewModel
import io.tasklite.widget.TaskLiteLaunchMode
import io.tasklite.widget.TaskLiteLaunchStateStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private const val EXPANDED_TASKS_ROUTE = "expanded_tasks"

@Composable
fun TaskLiteApp(
	repository: TaskRepository,
	notificationEnabled: Boolean = false,
	onNotificationToggleRequested: (Boolean) -> Unit = {}
) {
	val navController = rememberNavController()

	Surface(
		modifier = Modifier.fillMaxSize(),
		color = MaterialTheme.colorScheme.background
	) {
		NavHost(
			navController = navController,
			startDestination = EXPANDED_TASKS_ROUTE
		) {
			composable(EXPANDED_TASKS_ROUTE) {
				ExpandedTasksRoute(
					repository = repository,
					notificationEnabled = notificationEnabled,
					onNotificationToggleRequested = onNotificationToggleRequested
				)
			}
		}
	}
}

@Composable
private fun ExpandedTasksRoute(
	repository: TaskRepository,
	notificationEnabled: Boolean,
	onNotificationToggleRequested: (Boolean) -> Unit
) {
	val viewModel: ExpandedTasksViewModel = viewModel(
		factory = ExpandedTasksViewModel.factory(repository = repository)
	)
	val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
	val launchMode = TaskLiteLaunchStateStore.launchMode.collectAsState().value
	val launchNonce = TaskLiteLaunchStateStore.launchNonce.collectAsState().value
	val listState = rememberLazyListState()
	var appliedLaunchNonce by remember { mutableStateOf(-1) }
	val view = LocalView.current

	SideEffect {
		view.isHapticFeedbackEnabled = true
	}

	LaunchedEffect(launchNonce, uiState.currentTaskFlatIndex, uiState.completedTasks.size) {
		val currentTaskIndex = uiState.currentTaskFlatIndex ?: return@LaunchedEffect
		if (appliedLaunchNonce == launchNonce) {
			return@LaunchedEffect
		}

		delay(100)
		listState.awaitItemsAtLeast(currentTaskIndex)
		listState.scrollCurrentTaskNearThirdRow(
			currentTaskIndex = currentTaskIndex,
			animated = false
		)
		appliedLaunchNonce = launchNonce
	}

	LaunchedEffect(uiState.completionShiftToken, uiState.currentTaskFlatIndex, uiState.completedTasks.size) {
		val currentTaskIndex = uiState.currentTaskFlatIndex ?: return@LaunchedEffect
		if (uiState.completionShiftToken == 0) {
			return@LaunchedEffect
		}

		delay(100)
		listState.awaitItemsAtLeast(currentTaskIndex)
		listState.scrollCurrentTaskNearThirdRow(
			currentTaskIndex = currentTaskIndex,
			animated = true
		)
	}

	ExpandedTaskScreen(
		state = uiState.toScreenState(
			launchMode = launchMode,
			notificationEnabled = notificationEnabled
		),
		callbacks = ExpandedTaskScreenCallbacks(
			onTaskTextClick = viewModel::startEditingTask,
			onTaskTextChange = { _, text -> viewModel.updateEditingTaskText(text) },
			onTaskEditCommit = {
				val editingTask = uiState.editingTask
				val editingText = uiState.editingTaskText.trim()
				val shouldVibrateOnEditCommit =
					editingTask != null &&
					editingText.isNotEmpty() &&
					editingTask.text != editingText
				viewModel.saveEditingTask()
				if (shouldVibrateOnEditCommit) {
					performEditTaskVibration(
						context = view.context,
						fallbackView = view
					)
				}
			},
			onTaskEditCancel = { viewModel.cancelEditingTask() },
			onTaskCompleteClick = { taskId ->
				if (uiState.activeTasks.any { it.id == taskId }) {
					performCompleteTaskVibration(
						context = view.context,
						fallbackView = view
					)
				}
				viewModel.toggleTaskCompletion(taskId)
			},
			onTaskDeleteRequest = viewModel::requestDeleteTask,
			onConfirmDeleteTask = viewModel::confirmDeleteTask,
			onDismissDeleteTask = viewModel::cancelDeleteTask,
			onTaskReorderRequest = { fromIndex, toIndex ->
				if (fromIndex in uiState.activeTasks.indices && toIndex in uiState.activeTasks.indices && fromIndex != toIndex) {
					viewModel.moveActiveTask(fromIndex = fromIndex, toIndex = toIndex)
					performMoveTaskVibration(
						context = view.context,
						fallbackView = view
					)
				}
			},
			onNotificationToggleRequested = onNotificationToggleRequested,
			onSearchQueryChange = viewModel::updateSearchQuery,
			onAddTaskTextChange = viewModel::updateNewTaskText,
			onAddTaskSubmit = {
				if (uiState.newTaskText.isNotBlank()) {
					performAddTaskVibration(
						context = view.context,
						fallbackView = view
					)
				}
				viewModel.submitNewTask()
			},
			onAddTaskFocusHandled = TaskLiteLaunchStateStore::consumeAddLaunch
		),
		listState = listState,
		modifier = Modifier.fillMaxSize()
	)
}

private fun io.tasklite.ui.expanded.ExpandedTasksUiState.toScreenState(
	launchMode: TaskLiteLaunchMode,
	notificationEnabled: Boolean
): ExpandedTaskScreenUiState {
	return ExpandedTaskScreenUiState(
		completedTasks = completedTasks.asReversed().map { task ->
			task.toRowUiState(
				isEditing = editingTask?.id == task.id,
				editText = editingTaskText
			)
		},
		currentTask = currentTask?.toRowUiState(
			isCurrentTask = true,
			isEditing = editingTask?.id == currentTask.id,
			editText = editingTaskText,
			isPendingCompletion = currentTask.id in pendingCompletionTaskIds
		),
		futureTasks = futureTasks.map { task ->
			task.toRowUiState(
				isEditing = editingTask?.id == task.id,
				editText = editingTaskText,
				isPendingCompletion = task.id in pendingCompletionTaskIds
			)
		},
		activeTasks = buildList {
			currentTask?.let { current ->
				add(
					current.toRowUiState(
						isCurrentTask = true,
						isReorderable = true,
						isEditing = editingTask?.id == current.id,
						editText = editingTaskText,
						isPendingCompletion = current.id in pendingCompletionTaskIds
					)
				)
			}
			futureTasks.forEach { task ->
				add(
					task.toRowUiState(
						isReorderable = true,
						isEditing = editingTask?.id == task.id,
						editText = editingTaskText,
						isPendingCompletion = task.id in pendingCompletionTaskIds
					)
				)
			}
		},
		activeTaskCount = activeTasks.size,
		searchQuery = searchQuery,
		addTaskText = newTaskText,
		editingTaskIndex = editingTaskFlatIndex,
		isEditingTask = isEditingTask,
		notificationEnabled = notificationEnabled,
		focusAddTaskInput = launchMode == TaskLiteLaunchMode.ADD,
		showAddTaskComposer = editingTask == null,
		showDeleteConfirmation = deleteTargetTask != null,
		deleteConfirmationTask = deleteTargetTask?.toRowUiState(
			isEditing = editingTask?.id == deleteTargetTask.id,
			editText = editingTaskText
		)
	)
}

private fun io.tasklite.ui.expanded.ExpandedTaskUiModel.toRowUiState(
	isCurrentTask: Boolean = false,
	isReorderable: Boolean = false,
	isEditing: Boolean = false,
	editText: String = text,
	isPendingCompletion: Boolean = false
): ExpandedTaskRowUiState {
	return ExpandedTaskRowUiState(
		id = id,
		text = text,
		isCompleted = isCompleted,
		isPendingCompletion = isPendingCompletion,
		isCurrentTask = isCurrentTask,
		isReorderable = isReorderable,
		isEditing = isEditing,
		editText = if (isEditing) editText else text
	)
}

private suspend fun androidx.compose.foundation.lazy.LazyListState.awaitItemsAtLeast(targetIndex: Int) {
	val requiredItemCount = (targetIndex + 1).coerceAtLeast(1)

	snapshotFlow { layoutInfo.totalItemsCount }
		.map { count -> count >= requiredItemCount }
		.first { it }
}

private suspend fun androidx.compose.foundation.lazy.LazyListState.scrollCurrentTaskNearThirdRow(
	currentTaskIndex: Int,
	animated: Boolean
) {
	val targetIndex = (currentTaskIndex - 2).coerceAtLeast(0)
	if (animated) {
		animateScrollToItem(index = targetIndex)
	} else {
		scrollToItem(index = targetIndex)
	}
}
