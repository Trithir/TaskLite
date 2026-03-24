package com.erics.tasklite.ui.expanded

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlin.math.roundToInt

data class ExpandedTaskScreenUiState(
	val completedTasks: List<ExpandedTaskRowUiState> = emptyList(),
	val currentTask: ExpandedTaskRowUiState? = null,
	val futureTasks: List<ExpandedTaskRowUiState> = emptyList(),
	val activeTasks: List<ExpandedTaskRowUiState> = emptyList(),
	val activeTaskCount: Int = 0,
	val addTaskText: String = "",
	val addTaskPlaceholder: String = "Add a task",
	val notificationEnabled: Boolean = false,
	val focusAddTaskInput: Boolean = false,
	val showDeleteConfirmation: Boolean = false,
	val deleteConfirmationTask: ExpandedTaskRowUiState? = null
)

data class ExpandedTaskScreenCallbacks(
	val onTaskTextClick: (Long) -> Unit,
	val onTaskTextChange: (Long, String) -> Unit,
	val onTaskEditCommit: (Long) -> Unit,
	val onTaskEditCancel: (Long) -> Unit,
	val onTaskCompleteClick: (Long) -> Unit,
	val onTaskDeleteRequest: (Long) -> Unit,
	val onConfirmDeleteTask: () -> Unit,
	val onDismissDeleteTask: () -> Unit,
	val onTaskReorderRequest: (Int, Int) -> Unit = { _, _ -> },
	val onNotificationToggleRequested: (Boolean) -> Unit = {},
	val onAddTaskTextChange: (String) -> Unit,
	val onAddTaskSubmit: () -> Unit,
	val onAddTaskFocusHandled: () -> Unit = {}
)

data class ExpandedTaskRowUiState(
	val id: Long,
	val text: String,
	val isCompleted: Boolean = false,
	val isPendingCompletion: Boolean = false,
	val isCurrentTask: Boolean = false,
	val isReorderable: Boolean = false,
	val isEditing: Boolean = false,
	val editText: String = text
)

@Composable
fun ExpandedTaskScreen(
	state: ExpandedTaskScreenUiState,
	callbacks: ExpandedTaskScreenCallbacks,
	listState: LazyListState,
	modifier: Modifier = Modifier
) {
	val addTaskFocusRequester = remember { FocusRequester() }
	var bottomControlsHeightPx by remember { mutableIntStateOf(0) }
	val density = LocalDensity.current
	val view = LocalView.current

	LaunchedEffect(state.focusAddTaskInput) {
		if (state.focusAddTaskInput) {
			addTaskFocusRequester.requestFocus()
			callbacks.onAddTaskFocusHandled()
		}
	}

	Surface(
		modifier = modifier.fillMaxSize()
	) {
		val activeTasks = state.activeTasks
		val activeTaskById = remember(activeTasks) { activeTasks.associateBy { it.id } }
		val reorderState = rememberReorderableActiveTaskRowsState(activeTasks)

		Box(
			modifier = Modifier.fillMaxSize()
		) {
			LazyColumn(
				state = listState,
				modifier = Modifier.fillMaxSize(),
				contentPadding = androidx.compose.foundation.layout.PaddingValues(
					start = 20.dp,
					top = 20.dp,
					end = 20.dp,
					bottom = with(density) { bottomControlsHeightPx.toDp() } + 20.dp
				),
				verticalArrangement = Arrangement.spacedBy(12.dp)
			) {
				items(
					items = state.completedTasks,
					key = { task -> "completed-${task.id}" }
				) { task ->
					ExpandedTaskRow(
						task = task,
						callbacks = callbacks
					)
				}

				items(
					items = reorderState.orderedTaskIds,
					key = { taskId -> "active-$taskId" }
				) { taskId ->
					activeTaskById[taskId]?.let { task ->
						ReorderableActiveTaskRow(
							task = task,
							callbacks = callbacks,
							reorderState = reorderState
						)
					}
				}

				if (
					state.completedTasks.isEmpty() &&
					activeTasks.isEmpty()
				) {
					item {
						EmptyStateCard()
					}
				}

				if (state.activeTaskCount >= SOFT_OVERLOAD_WARNING_THRESHOLD) {
					item {
						SoftOverloadWarningCard(
							activeTaskCount = state.activeTaskCount
						)
					}
				}

				item {
					NotificationToggleCard(
						enabled = state.notificationEnabled,
						onCheckedChange = callbacks.onNotificationToggleRequested
					)
				}
			}

			Box(
				modifier = Modifier
					.align(Alignment.BottomCenter)
					.fillMaxWidth()
					.padding(20.dp)
					.onSizeChanged { bottomControlsHeightPx = it.height }
			) {
				ExpandedTaskComposer(
					text = state.addTaskText,
					placeholder = state.addTaskPlaceholder,
					onTextChange = callbacks.onAddTaskTextChange,
					onSubmit = callbacks.onAddTaskSubmit,
					focusRequester = addTaskFocusRequester
				)
			} 
		}
	}

	if (state.showDeleteConfirmation && state.deleteConfirmationTask != null) {
		DeleteConfirmationDialog(
			taskText = state.deleteConfirmationTask.text,
			onConfirm = callbacks.onConfirmDeleteTask,
			onDismiss = callbacks.onDismissDeleteTask
		)
	}
}

@Composable
private fun rememberReorderableActiveTaskRowsState(tasks: List<ExpandedTaskRowUiState>): ReorderableActiveTaskRowsState {
	val taskIds = tasks.map { it.id }
	val state = remember { ReorderableActiveTaskRowsState(taskIds) }

	LaunchedEffect(taskIds, state.draggingTaskId) {
		if (state.draggingTaskId == null && state.orderedTaskIds != taskIds) {
			state.orderedTaskIds = taskIds
		}
	}

	return state
}

@Stable
private class ReorderableActiveTaskRowsState(initialTaskIds: List<Long>) {
	var orderedTaskIds by mutableStateOf(initialTaskIds)
	var draggingTaskId by mutableStateOf<Long?>(null)
	var dragDistanceY by mutableFloatStateOf(0f)
	var dragStartCenterY by mutableFloatStateOf(0f)
	val rowHeightsPx = mutableStateMapOf<Long, Float>()
}

@Composable
private fun ReorderableActiveTaskRow(
	task: ExpandedTaskRowUiState,
	callbacks: ExpandedTaskScreenCallbacks,
	reorderState: ReorderableActiveTaskRowsState
) {
	val density = LocalDensity.current
	val view = LocalView.current
	val fallbackStepPx = with(density) { 84.dp.toPx() }
	val spacingPx = with(density) { 12.dp.toPx() }

	val dragModifier = Modifier.pointerInput(task.id) {
		detectDragGesturesAfterLongPress(
			onDragStart = {
				reorderState.draggingTaskId = task.id
				reorderState.dragDistanceY = 0f
				reorderState.dragStartCenterY = reorderState.orderedTaskIds.centerYOf(
					taskId = task.id,
					rowHeightsPx = reorderState.rowHeightsPx,
					spacingPx = spacingPx,
					fallbackStepPx = fallbackStepPx
				)
				view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
			},
			onDragEnd = {
				reorderState.draggingTaskId = null
				reorderState.dragDistanceY = 0f
				reorderState.dragStartCenterY = 0f
			},
			onDragCancel = {
				reorderState.draggingTaskId = null
				reorderState.dragDistanceY = 0f
				reorderState.dragStartCenterY = 0f
			}
		) { _, dragAmount ->
			if (reorderState.draggingTaskId != task.id) {
				return@detectDragGesturesAfterLongPress
			}

			reorderState.dragDistanceY += dragAmount.y
			val currentOrder = reorderState.orderedTaskIds.toMutableList()
			val previousIndex = currentOrder.indexOf(task.id)
			if (previousIndex == -1) {
				return@detectDragGesturesAfterLongPress
			}

			val draggedCenterY = reorderState.dragStartCenterY + reorderState.dragDistanceY
			var currentIndex = previousIndex
			var moved = false

			while (currentIndex > 0) {
				val aboveId = currentOrder[currentIndex - 1]
				val aboveCenterY = currentOrder.centerYOf(
					taskId = aboveId,
					rowHeightsPx = reorderState.rowHeightsPx,
					spacingPx = spacingPx,
					fallbackStepPx = fallbackStepPx
				)

				if (draggedCenterY >= aboveCenterY) {
					break
				}

				currentOrder.swap(currentIndex, currentIndex - 1)
				currentIndex -= 1
				moved = true
			}

			while (currentIndex < currentOrder.lastIndex) {
				val belowId = currentOrder[currentIndex + 1]
				val belowCenterY = currentOrder.centerYOf(
					taskId = belowId,
					rowHeightsPx = reorderState.rowHeightsPx,
					spacingPx = spacingPx,
					fallbackStepPx = fallbackStepPx
				)

				if (draggedCenterY <= belowCenterY) {
					break
				}

				currentOrder.swap(currentIndex, currentIndex + 1)
				currentIndex += 1
				moved = true
			}

			if (moved) {
				reorderState.orderedTaskIds = currentOrder
				callbacks.onTaskReorderRequest(previousIndex, currentIndex)
			}
		}
	}

	val dragTranslationY = if (reorderState.draggingTaskId != task.id) {
		0f
	} else {
		val draggedCenterY = reorderState.dragStartCenterY + reorderState.dragDistanceY
		val currentCenterY = reorderState.orderedTaskIds.centerYOf(
			taskId = task.id,
			rowHeightsPx = reorderState.rowHeightsPx,
			spacingPx = spacingPx,
			fallbackStepPx = fallbackStepPx
		)
		draggedCenterY - currentCenterY
	}

	ExpandedTaskRow(
		task = task,
		callbacks = callbacks,
		modifier = Modifier
			.then(dragModifier)
			.onSizeChanged { rowSize ->
				reorderState.rowHeightsPx[task.id] = rowSize.height.toFloat()
			}
			.offset {
				if (reorderState.draggingTaskId == task.id) {
					IntOffset(0, dragTranslationY.roundToInt())
				} else {
					IntOffset.Zero
				}
			}
			.zIndex(if (reorderState.draggingTaskId == task.id) 1f else 0f),
		reorderHandleModifier = Modifier
	)
}

@Composable
private fun ExpandedTaskRow(
	task: ExpandedTaskRowUiState,
	callbacks: ExpandedTaskScreenCallbacks,
	reorderHandleModifier: Modifier = Modifier,
	modifier: Modifier = Modifier
) {
	val isVisuallyCompleted = task.isCompleted || task.isPendingCompletion

	val cardColors = if (isVisuallyCompleted) {
		CardDefaults.cardColors(
			containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface.copy(alpha = 0.55f)
		)
	} else if (task.isCurrentTask) {
		CardDefaults.cardColors(
			containerColor = androidx.compose.material3.MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.72f)
		)
	} else {
		CardDefaults.cardColors()
	}

	Card(
		modifier = modifier.fillMaxWidth(),
		shape = RoundedCornerShape(24.dp),
		colors = cardColors,
		border = if (task.isCurrentTask) {
			BorderStroke(1.dp, androidx.compose.material3.MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f))
		} else null
	) {
		if (task.isEditing) {
			EditingTaskRow(
				task = task,
				callbacks = callbacks
			)
		} else {
			DisplayTaskRow(
				task = task,
				callbacks = callbacks,
				reorderHandleModifier = reorderHandleModifier
			)
		}
	}
}

@Composable
private fun DisplayTaskRow(
	task: ExpandedTaskRowUiState,
	callbacks: ExpandedTaskScreenCallbacks,
	reorderHandleModifier: Modifier = Modifier
) {
	val isVisuallyCompleted = task.isCompleted || task.isPendingCompletion

	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = 16.dp, vertical = 14.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		TaskBubble(
			completed = isVisuallyCompleted,
			onClick = { callbacks.onTaskCompleteClick(task.id) }
		)

		Spacer(modifier = Modifier.width(12.dp))

		Text(
			text = task.text,
			modifier = Modifier
				.weight(1f)
				.then(
					if (task.isCompleted) {
						Modifier
					} else {
						Modifier.clickable { callbacks.onTaskTextClick(task.id) }
					}
				),
			style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
			color = if (isVisuallyCompleted) {
				androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
			} else {
				androidx.compose.material3.MaterialTheme.colorScheme.onSurface
			},
			textDecoration = if (isVisuallyCompleted) TextDecoration.LineThrough else null
		)

		if (task.isReorderable) {
			Spacer(modifier = Modifier.width(8.dp))
			ReorderHandle(
				modifier = reorderHandleModifier
			)
		}
	}
}

@Composable
private fun EditingTaskRow(
	task: ExpandedTaskRowUiState,
	callbacks: ExpandedTaskScreenCallbacks
) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.padding(12.dp),
		verticalArrangement = Arrangement.spacedBy(12.dp)
	) {
		OutlinedTextField(
			value = task.editText,
			onValueChange = { callbacks.onTaskTextChange(task.id, it) },
			modifier = Modifier.fillMaxWidth(),
			shape = RoundedCornerShape(20.dp),
			textStyle = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
			placeholder = {
				Text(text = "Edit task")
			},
			singleLine = false,
			minLines = 1,
			maxLines = 4,
			colors = TextFieldDefaults.colors(
				focusedContainerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface,
				unfocusedContainerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface,
				disabledContainerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface
			)
		)

		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.End,
			verticalAlignment = Alignment.CenterVertically
		) {
			TextButton(onClick = { callbacks.onTaskDeleteRequest(task.id) }) {
				Text(text = "Delete")
			}

			Spacer(modifier = Modifier.width(8.dp))

			TextButton(onClick = { callbacks.onTaskEditCancel(task.id) }) {
				Text(text = "Cancel")
			}

			Spacer(modifier = Modifier.width(8.dp))

			Button(
				onClick = { callbacks.onTaskEditCommit(task.id) },
				shape = RoundedCornerShape(18.dp)
			) {
				Text(text = "Done")
			}
		}
	}
}

@Composable
private fun TaskBubble(
	completed: Boolean,
	onClick: () -> Unit
) {
	Box(
		modifier = Modifier
			.size(34.dp)
			.clip(CircleShape)
			.clickable(onClick = onClick),
		contentAlignment = Alignment.Center
	) {
		Surface(
			shape = CircleShape,
			color = if (completed) {
				androidx.compose.material3.MaterialTheme.colorScheme.primary
			} else {
				androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant
			},
			border = BorderStroke(
				1.dp,
				androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant
			)
		) {
			Box(
				modifier = Modifier.size(34.dp)
			)
		}
	}
}

@Composable
private fun ExpandedTaskComposer(
	text: String,
	placeholder: String,
	onTextChange: (String) -> Unit,
	onSubmit: () -> Unit,
	focusRequester: FocusRequester,
	modifier: Modifier = Modifier
) {
	Surface(
		modifier = modifier,
		shape = RoundedCornerShape(28.dp),
		shadowElevation = 8.dp,
		color = androidx.compose.material3.MaterialTheme.colorScheme.surface
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(12.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			OutlinedTextField(
				value = text,
				onValueChange = onTextChange,
				modifier = Modifier
					.weight(1f)
					.focusRequester(focusRequester),
				shape = RoundedCornerShape(22.dp),
				placeholder = {
					Text(text = placeholder)
				},
				singleLine = true,
				colors = TextFieldDefaults.colors(
					focusedContainerColor = androidx.compose.material3.MaterialTheme.colorScheme.background,
					unfocusedContainerColor = androidx.compose.material3.MaterialTheme.colorScheme.background,
					disabledContainerColor = androidx.compose.material3.MaterialTheme.colorScheme.background
				)
			)

			Spacer(modifier = Modifier.width(10.dp))

			AssistChip(
				onClick = onSubmit,
				label = { Text(text = "Add") },
				shape = CircleShape,
				colors = AssistChipDefaults.assistChipColors(
					containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer
				)
			)
		}
	}
}

@Composable
private fun SoftOverloadWarningCard(
	activeTaskCount: Int
) {
	if (activeTaskCount < SOFT_OVERLOAD_WARNING_THRESHOLD) {
		return
	}

	val prompt = rememberSaveable(
		inputs = arrayOf<Any>("soft_warning_prompt", activeTaskCount),
		saver = SoftWarningPromptSaver
	) {
		randomSoftWarningPrompt()
	}

	Card(
		modifier = Modifier.fillMaxWidth(),
		shape = RoundedCornerShape(22.dp),
		colors = CardDefaults.cardColors(
			containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
		),
		border = BorderStroke(
			1.dp,
			androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
		)
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 16.dp, vertical = 12.dp),
			verticalArrangement = Arrangement.spacedBy(4.dp)
		) {
			Text(
				text = prompt.title,
				style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
			)
			Text(
				text = "${prompt.message} $activeTaskCount active tasks is a lot to carry.",
				style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
				color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
			)
		}
	}
}

@Composable
private fun NotificationToggleCard(
	enabled: Boolean,
	onCheckedChange: (Boolean) -> Unit
) {
	Card(
		modifier = Modifier.fillMaxWidth(),
		shape = RoundedCornerShape(22.dp),
		colors = CardDefaults.cardColors(
			containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)
		),
		border = BorderStroke(
			1.dp,
			androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
		)
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 16.dp, vertical = 12.dp),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically
		) {
			Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
				Text(
					text = "Ongoing notification",
					style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
				)
				Text(
					text = if (enabled) "On" else "Off",
					style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
					color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
				)
			}

			Switch(
				checked = enabled,
				onCheckedChange = onCheckedChange
			)
		}
	}
}

@Composable
private fun DeleteConfirmationDialog(
	taskText: String,
	onConfirm: () -> Unit,
	onDismiss: () -> Unit
) {
	AlertDialog(
		onDismissRequest = onDismiss,
		title = {
			Text(text = "Delete task?")
		},
		text = {
			Text(text = "This removes \"$taskText\" from the list.")
		},
		confirmButton = {
			TextButton(onClick = onConfirm) {
				Text(text = "Delete")
			}
		},
		dismissButton = {
			TextButton(onClick = onDismiss) {
				Text(text = "Keep it")
			}
		}
	)
}

@Composable
private fun ReorderHandle(
	modifier: Modifier = Modifier
) {
	Box(
		modifier = modifier.size(24.dp),
		contentAlignment = Alignment.Center
	) {
		Text(
			text = "|||",
			color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
		)
	}
}

@Composable
private fun EmptyStateCard() {
	val prompt = rememberSaveable(
		inputs = arrayOf<Any>("empty_state_prompt"),
		saver = EmptyStatePromptSaver
	) {
		randomEmptyStatePrompt()
	}

	Card(
		modifier = Modifier.fillMaxWidth(),
		shape = RoundedCornerShape(24.dp),
		colors = CardDefaults.cardColors(
			containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
		),
		border = BorderStroke(
			1.dp,
			androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
		)
	) {
		Column(
			modifier = Modifier.padding(20.dp),
			verticalArrangement = Arrangement.spacedBy(8.dp)
		) {
			Text(
				text = prompt.title,
				style = androidx.compose.material3.MaterialTheme.typography.titleMedium
			)
			Text(
				text = prompt.message,
				color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
			)
		}
	}
}

private val EmptyStatePromptSaver = androidx.compose.runtime.saveable.listSaver<EmptyStatePrompt, String>(
	save = { listOf(it.title, it.message) },
	restore = { restored -> EmptyStatePrompt(title = restored[0], message = restored[1]) }
)

private val SoftWarningPromptSaver = androidx.compose.runtime.saveable.listSaver<SoftWarningPrompt, String>(
	save = { listOf(it.title, it.message) },
	restore = { restored -> SoftWarningPrompt(title = restored[0], message = restored[1]) }
)

private const val SOFT_OVERLOAD_WARNING_THRESHOLD = 50

private fun <T> MutableList<T>.swap(firstIndex: Int, secondIndex: Int) {
	val firstValue = this[firstIndex]
	this[firstIndex] = this[secondIndex]
	this[secondIndex] = firstValue
}

private fun List<Long>.centerYOf(
	taskId: Long,
	rowHeightsPx: Map<Long, Float>,
	spacingPx: Float,
	fallbackStepPx: Float
): Float {
	var topY = 0f
	for (currentTaskId in this) {
		val heightPx = rowHeightsPx[currentTaskId] ?: fallbackStepPx
		if (currentTaskId == taskId) {
			return topY + (heightPx / 2f)
		}
		topY += heightPx + spacingPx
	}
	return fallbackStepPx / 2f
}
