package com.erics.tasklite.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.action.ActionParameters.Key
import androidx.glance.appwidget.action.ActionCallback
import com.erics.tasklite.ui.performWidgetCompleteTaskVibration
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex

private const val WIDGET_COMPLETION_VISUAL_PAUSE_MILLIS = 1_000L
private val widgetCompletionActionMutex = Mutex()
val WidgetTaskIdParamKey: Key<Long> = ActionParameters.Key("widget_task_id")

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
		if (!widgetCompletionActionMutex.tryLock()) {
			return
		}

		try {
			val repository = TaskLiteWidgetRepository.from(context)
			val expectedTaskId = parameters[WidgetTaskIdParamKey] ?: return
			val currentTask = repository.getCurrentTask() ?: return
			if (currentTask.id != expectedTaskId) {
				return
			}

			TaskLiteWidgetCompletionStore.setPendingCompletion(
				context = context,
				taskId = currentTask.id,
				taskText = currentTask.text,
				visualUntilMillis = System.currentTimeMillis() + WIDGET_COMPLETION_VISUAL_PAUSE_MILLIS
			)
			TaskLiteWidgetSync.refresh(context)
			performWidgetCompleteTaskVibration(context = context)

			try {
				delay(WIDGET_COMPLETION_VISUAL_PAUSE_MILLIS)
				repository.completeCurrentTask(taskId = expectedTaskId)
			} finally {
				TaskLiteWidgetCompletionStore.clearPendingCompletion(context)
				TaskLiteWidgetSync.refresh(context)
			}
		} finally {
			widgetCompletionActionMutex.unlock()
		}
	}
}

private fun Context.startActivity(intent: android.content.Intent) {
	intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
	startActivity(intent)
}
