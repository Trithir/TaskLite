package com.erics.tasklite.ui.expanded

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import kotlin.math.roundToInt

data class ExpandedTaskScreenUiState(
	val completedTasks: List<ExpandedTaskRowUiState> = emptyList(),
	val currentTask: ExpandedTaskRowUiState? = null,
	val futureTasks: List<ExpandedTaskRowUiState> = emptyList(),
	val activeTasks: List<ExpandedTaskRowUiState> = emptyList(),
	val activeTaskCount: Int = 0,
	val searchQuery: String = "",
	val addTaskText: String = "",
	val editingTaskIndex: Int? = null,
	val isEditingTask: Boolean = false,
	val addTaskPlaceholder: String = "Add a task",
	val notificationEnabled: Boolean = false,
	val focusAddTaskInput: Boolean = false,
	val showAddTaskComposer: Boolean = true,
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
	val onSearchQueryChange: (String) -> Unit = {},
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

object ExpandedTaskScreenTestTags {
	const val SEARCH_INPUT = "search_input"
	const val ADD_TASK_INPUT = "add_task_input"
	const val ADD_TASK_BUTTON = "add_task_button"
	const val EMPTY_STATE_CARD = "empty_state_card"
}

@Composable
fun ExpandedTaskScreen(
	state: ExpandedTaskScreenUiState,
	callbacks: ExpandedTaskScreenCallbacks,
	listState: LazyListState,
	modifier: Modifier = Modifier
) {
	val addTaskFocusRequester = remember { FocusRequester() }
	var composerHeightPx by remember { mutableIntStateOf(0) }
	val density = LocalDensity.current
	val topInsetPx = WindowInsets.statusBars.getTop(density)
	val bottomInsetPx = WindowInsets.navigationBars.getBottom(density)
	val focusManager = LocalFocusManager.current
	val backgroundInteractionSource = remember { MutableInteractionSource() }
	val reservedBottomOverlayHeightPx = if (state.showAddTaskComposer || state.isEditingTask) {
		composerHeightPx
	} else {
		0
	}

	LaunchedEffect(state.focusAddTaskInput) {
		if (state.focusAddTaskInput) {
			addTaskFocusRequester.requestFocus()
			callbacks.onAddTaskFocusHandled()
		}
	}

	LaunchedEffect(state.editingTaskIndex, reservedBottomOverlayHeightPx) {
		val editingTaskIndex = state.editingTaskIndex ?: return@LaunchedEffect
		if (reservedBottomOverlayHeightPx == 0) {
			return@LaunchedEffect
		}

		listState.awaitItemsAtLeast(editingTaskIndex)
		listState.scrollEditingTaskAboveBottomOverlay(
			editingTaskIndex = editingTaskIndex,
			bottomOverlayHeightPx = reservedBottomOverlayHeightPx.toFloat(),
			extraGapPx = with(density) { 28.dp.toPx() }
		)
	}

	Surface(
		modifier = modifier.fillMaxSize()
	) {
		val activeTasks = state.activeTasks
		val activeTaskById = remember(activeTasks) { activeTasks.associateBy { it.id } }
		val reorderState = rememberReorderableActiveTaskRowsState(activeTasks)

		Box(
			modifier = Modifier
				.fillMaxSize()
				.clickable(
					interactionSource = backgroundInteractionSource,
					indication = null
				) {
					focusManager.clearFocus()
				}
		) {
			LazyColumn(
				state = listState,
				modifier = Modifier
					.fillMaxSize()
					.imePadding(),
				contentPadding = androidx.compose.foundation.layout.PaddingValues(
					start = 20.dp,
					top = with(density) { topInsetPx.toDp() } + 12.dp,
					end = 20.dp,
					bottom = with(density) {
						reservedBottomOverlayHeightPx.toDp() + bottomInsetPx.toDp()
					} + 20.dp
				),
				verticalArrangement = Arrangement.spacedBy(12.dp)
			) {
				item(key = "search-composer") {
					SearchTaskComposer(
						text = state.searchQuery,
						onTextChange = callbacks.onSearchQueryChange
					)
				}

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
							reorderState = reorderState,
							listState = listState,
							bottomOverlayHeightPx = reservedBottomOverlayHeightPx.toFloat(),
							reorderEnabled = state.searchQuery.isBlank()
						)
					}
				}

				if (state.completedTasks.isEmpty() && activeTasks.isEmpty()) {
					item {
						if (state.searchQuery.isBlank()) {
							EmptyStateCard()
						} else {
							SearchEmptyStateCard()
						}
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

			if (state.showAddTaskComposer) {
				Box(
					modifier = Modifier
						.align(Alignment.BottomCenter)
						.fillMaxWidth()
						.padding(20.dp)
						.navigationBarsPadding()
						.imePadding()
				) {
					Box(
						modifier = Modifier.onSizeChanged { composerHeightPx = it.height }
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
	var dragStartIndex by mutableIntStateOf(-1)
	var dragDistanceY by mutableFloatStateOf(0f)
	var dragStartCenterY by mutableFloatStateOf(0f)
	var autoScrollInFlight by mutableStateOf(false)
	val rowHeightsPx = mutableStateMapOf<Long, Float>()
}

@Composable
private fun ReorderableActiveTaskRow(
	task: ExpandedTaskRowUiState,
	callbacks: ExpandedTaskScreenCallbacks,
	reorderState: ReorderableActiveTaskRowsState,
	listState: LazyListState,
	bottomOverlayHeightPx: Float,
	reorderEnabled: Boolean
) {
	val density = LocalDensity.current
	val coroutineScope = rememberCoroutineScope()
	val fallbackStepPx = with(density) { 84.dp.toPx() }
	val spacingPx = with(density) { 12.dp.toPx() }
	val edgeAutoScrollThresholdPx = with(density) { 76.dp.toPx() }
	val edgeAutoScrollStepPx = with(density) { 10.dp.toPx() }

	val dragHandleModifier = if (reorderEnabled) {
		Modifier.pointerInput(task.id) {
		detectDragGestures(
			onDragStart = {
				reorderState.draggingTaskId = task.id
				reorderState.dragStartIndex = reorderState.orderedTaskIds.indexOf(task.id)
				reorderState.dragDistanceY = 0f
				reorderState.dragStartCenterY = reorderState.orderedTaskIds.centerYOf(
					taskId = task.id,
					rowHeightsPx = reorderState.rowHeightsPx,
					spacingPx = spacingPx,
					fallbackStepPx = fallbackStepPx
				)
			},
			onDragEnd = {
				commitReorderIfNeeded(
					taskId = task.id,
					reorderState = reorderState,
					callbacks = callbacks
				)
				reorderState.draggingTaskId = null
				reorderState.dragStartIndex = -1
				reorderState.dragDistanceY = 0f
				reorderState.dragStartCenterY = 0f
				reorderState.autoScrollInFlight = false
			},
			onDragCancel = {
				reorderState.orderedTaskIds = reorderState.orderedTaskIds.toList()
				reorderState.draggingTaskId = null
				reorderState.dragStartIndex = -1
				reorderState.dragDistanceY = 0f
				reorderState.dragStartCenterY = 0f
				reorderState.autoScrollInFlight = false
			}
		) { change, dragAmount ->
			if (reorderState.draggingTaskId != task.id) {
				return@detectDragGestures
			}
			change.consume()

			reorderState.dragDistanceY += dragAmount.y
			val currentOrder = reorderState.orderedTaskIds.toMutableList()
			val previousIndex = currentOrder.indexOf(task.id)
			if (previousIndex == -1) {
				return@detectDragGestures
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
			}

			val currentCenterY = currentOrder.centerYOf(
				taskId = task.id,
				rowHeightsPx = reorderState.rowHeightsPx,
				spacingPx = spacingPx,
				fallbackStepPx = fallbackStepPx
			)
			val dragTranslationY = draggedCenterY - currentCenterY
			val autoScrollDelta = listState.edgeAutoScrollDeltaFor(
				taskId = task.id,
				dragTranslationY = dragTranslationY,
				thresholdPx = edgeAutoScrollThresholdPx,
				stepPx = edgeAutoScrollStepPx,
				bottomInsetPx = bottomOverlayHeightPx
			)

			if (autoScrollDelta != 0f && !reorderState.autoScrollInFlight) {
				reorderState.autoScrollInFlight = true
				coroutineScope.launch {
					val consumedScroll = listState.scrollBy(autoScrollDelta)
					reorderState.dragDistanceY += consumedScroll
					reorderState.autoScrollInFlight = false
				}
			}
		}
		}
	} else {
		Modifier
	}

	val dragTranslationY = if (!reorderEnabled || reorderState.draggingTaskId != task.id) {
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
		showReorderHandle = reorderEnabled,
		modifier = Modifier
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
		reorderHandleModifier = dragHandleModifier
	)
}

@Composable
private fun ExpandedTaskRow(
	task: ExpandedTaskRowUiState,
	callbacks: ExpandedTaskScreenCallbacks,
	showReorderHandle: Boolean = true,
	reorderHandleModifier: Modifier = Modifier,
	modifier: Modifier = Modifier
) {
	val isVisuallyCompleted = task.isCompleted || task.isPendingCompletion

	val cardColors = if (isVisuallyCompleted) {
		CardDefaults.cardColors(
			containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)
		)
	} else if (task.isCurrentTask) {
		CardDefaults.cardColors(
			containerColor = androidx.compose.material3.MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.78f)
		)
	} else {
		CardDefaults.cardColors(
			containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface
		)
	}

	Card(
		modifier = modifier.fillMaxWidth(),
		shape = RoundedCornerShape(24.dp),
		colors = cardColors,
		border = if (task.isCurrentTask) {
			BorderStroke(1.dp, androidx.compose.material3.MaterialTheme.colorScheme.secondary.copy(alpha = 0.45f))
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
				showReorderHandle = showReorderHandle,
				reorderHandleModifier = reorderHandleModifier
			)
		}
	}
}

@Composable
private fun DisplayTaskRow(
	task: ExpandedTaskRowUiState,
	callbacks: ExpandedTaskScreenCallbacks,
	showReorderHandle: Boolean = true,
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

		if (task.isReorderable && showReorderHandle) {
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
	val focusRequester = remember { FocusRequester() }
	val keyboardController = LocalSoftwareKeyboardController.current

	LaunchedEffect(task.id) {
		focusRequester.requestFocus()
		keyboardController?.show()
	}

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.padding(12.dp),
		verticalArrangement = Arrangement.spacedBy(12.dp)
	) {
		OutlinedTextField(
			value = task.editText,
			onValueChange = { callbacks.onTaskTextChange(task.id, it) },
			modifier = Modifier
				.fillMaxWidth()
				.focusRequester(focusRequester),
			shape = RoundedCornerShape(20.dp),
			textStyle = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
			placeholder = {
				Text(text = "Edit task")
			},
			keyboardOptions = KeyboardOptions(
				capitalization = KeyboardCapitalization.Sentences
			),
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
				Color.Transparent
			},
			border = BorderStroke(
				1.5.dp,
				if (completed) {
					androidx.compose.material3.MaterialTheme.colorScheme.primary
				} else {
					androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant
				}
			)
		) {
			Box(
				modifier = Modifier.size(34.dp),
				contentAlignment = Alignment.Center
			) {
				Text(
					text = "\u2713",
					style = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(
						fontWeight = FontWeight.SemiBold
					),
					color = if (completed) {
						androidx.compose.material3.MaterialTheme.colorScheme.onPrimary
					} else {
						androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
					}
				)
			}
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
				.focusRequester(focusRequester)
				.testTag(ExpandedTaskScreenTestTags.ADD_TASK_INPUT),
				shape = RoundedCornerShape(22.dp),
				placeholder = {
					Text(text = placeholder)
				},
				keyboardOptions = KeyboardOptions(
					capitalization = KeyboardCapitalization.Sentences,
					imeAction = ImeAction.Done
				),
				keyboardActions = KeyboardActions(
					onDone = { onSubmit() }
				),
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
				modifier = Modifier.testTag(ExpandedTaskScreenTestTags.ADD_TASK_BUTTON),
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
private fun SearchTaskComposer(
	text: String,
	onTextChange: (String) -> Unit,
	modifier: Modifier = Modifier
) {
	Surface(
		modifier = modifier.fillMaxWidth(),
		shape = RoundedCornerShape(28.dp),
		color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
	) {
		OutlinedTextField(
			value = text,
			onValueChange = onTextChange,
			modifier = Modifier
				.fillMaxWidth()
				.testTag(ExpandedTaskScreenTestTags.SEARCH_INPUT),
			shape = RoundedCornerShape(28.dp),
			placeholder = {
				Text(text = "Search")
			},
			trailingIcon = {
				if (text.isNotEmpty()) {
					IconButton(onClick = { onTextChange("") }) {
						Text(
							text = "x",
							style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
							color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
						)
					}
				}
			},
			singleLine = true,
			colors = TextFieldDefaults.colors(
				focusedContainerColor = Color.Transparent,
				unfocusedContainerColor = Color.Transparent,
				disabledContainerColor = Color.Transparent
			)
		)
	}
}

@Composable
private fun SearchEmptyStateCard() {
	Card(
		modifier = Modifier.fillMaxWidth(),
		shape = RoundedCornerShape(22.dp),
		colors = CardDefaults.cardColors(
			containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
		)
	) {
		Text(
			text = "No matching tasks.",
			modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
			style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
			color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
		)
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
		modifier = Modifier
			.fillMaxWidth()
			.testTag(ExpandedTaskScreenTestTags.EMPTY_STATE_CARD),
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

private fun LazyListState.edgeAutoScrollDeltaFor(
	taskId: Long,
	dragTranslationY: Float,
	thresholdPx: Float,
	stepPx: Float,
	bottomInsetPx: Float
): Float {
	val rowInfo = layoutInfo.visibleItemsInfo.firstOrNull { it.key == "active-$taskId" } ?: return 0f
	val draggedCenterY = rowInfo.offset + (rowInfo.size / 2f) + dragTranslationY
	val viewportTop = layoutInfo.viewportStartOffset.toFloat() + thresholdPx
	val viewportBottom = layoutInfo.viewportEndOffset.toFloat() - thresholdPx - bottomInsetPx

	return when {
		draggedCenterY < viewportTop -> -stepPx
		draggedCenterY > viewportBottom -> stepPx
		else -> 0f
	}
}

private suspend fun LazyListState.awaitItemsAtLeast(targetIndex: Int) {
	val requiredItemCount = (targetIndex + 1).coerceAtLeast(1)

	snapshotFlow { layoutInfo.totalItemsCount }
		.first { count -> count >= requiredItemCount }
}

private suspend fun LazyListState.scrollEditingTaskAboveBottomOverlay(
	editingTaskIndex: Int,
	bottomOverlayHeightPx: Float,
	extraGapPx: Float
) {
	if (layoutInfo.visibleItemsInfo.none { it.index == editingTaskIndex }) {
		animateScrollToItem(index = editingTaskIndex.coerceAtLeast(0))
	}

	val rowInfo = layoutInfo.visibleItemsInfo.firstOrNull { it.index == editingTaskIndex } ?: return
	val targetBottom = layoutInfo.viewportEndOffset - bottomOverlayHeightPx - extraGapPx
	val scrollDelta = (rowInfo.offset + rowInfo.size) - targetBottom

	if (kotlin.math.abs(scrollDelta) > 1f) {
		animateScrollBy(scrollDelta)
	}
}

private fun commitReorderIfNeeded(
	taskId: Long,
	reorderState: ReorderableActiveTaskRowsState,
	callbacks: ExpandedTaskScreenCallbacks
) {
	val fromIndex = reorderState.dragStartIndex
	val toIndex = reorderState.orderedTaskIds.indexOf(taskId)
	if (fromIndex == -1 || toIndex == -1 || fromIndex == toIndex) {
		return
	}

	callbacks.onTaskReorderRequest(fromIndex, toIndex)
}
