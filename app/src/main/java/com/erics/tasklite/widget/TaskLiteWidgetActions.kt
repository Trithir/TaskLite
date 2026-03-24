package com.erics.tasklite.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback

class OpenTaskLiteActivityAction : ActionCallback {
	override suspend fun onAction(
		context: Context,
		glanceId: GlanceId,
		parameters: ActionParameters
	) {
		context.startActivity(createOpenTaskLiteIntent(context))
	}
}

class OpenAddTaskLiteActivityAction : ActionCallback {
	override suspend fun onAction(
		context: Context,
		glanceId: GlanceId,
		parameters: ActionParameters
	) {
		context.startActivity(createAddTaskLiteIntent(context))
	}
}

class CompleteCurrentTaskAction : ActionCallback {
	override suspend fun onAction(
		context: Context,
		glanceId: GlanceId,
		parameters: ActionParameters
	) {
		val repository = TaskLiteWidgetRepository.from(context)
		if (repository.completeCurrentTask()) {
			TaskLiteWidgetSync.refresh(context)
		}
	}
}

private fun Context.startActivity(intent: android.content.Intent) {
	intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
	startActivity(intent)
}
