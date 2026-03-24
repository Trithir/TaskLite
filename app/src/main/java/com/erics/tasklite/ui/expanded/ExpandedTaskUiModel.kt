package com.erics.tasklite.ui.expanded

import com.erics.tasklite.data.TaskEntity

data class ExpandedTaskUiModel(
	val id: Long,
	val text: String,
	val sortOrder: Long,
	val completedAt: Long?
) {
	val isCompleted: Boolean
		get() = completedAt != null
}

fun TaskEntity.toExpandedTaskUiModel(): ExpandedTaskUiModel = ExpandedTaskUiModel(
	id = id,
	text = text,
	sortOrder = sortOrder,
	completedAt = completedAt
)

fun ExpandedTaskUiModel.toTaskEntity(
	text: String = this.text,
	sortOrder: Long = this.sortOrder,
	completedAt: Long? = this.completedAt
): TaskEntity = TaskEntity(
	id = id,
	text = text,
	sortOrder = sortOrder,
	completedAt = completedAt
)
